package io.kestra.plugin.x;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.repositories.LocalFlowRepositoryLoader;
import io.kestra.core.runners.TestRunner;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@KestraTest
public class XExecutionTest extends AbstractXTest {

    @Inject
    protected TestRunner runner;

    @Inject
    protected LocalFlowRepositoryLoader repositoryLoader;

    @BeforeAll
    protected void init() throws IOException, URISyntaxException {
        repositoryLoader
            .load(Objects.requireNonNull(XExecutionTest.class.getClassLoader().getResource("flows")));
        this.runner.run();
    }

    @AfterAll
    protected void destroy() throws Exception {
        this.runner.close();
    }

    @Test
    void flow() throws Exception {
        var execution = runAndCaptureExecution(
            "main-flow-that-fails",
            "x"
        );

        String receivedData = waitForWebhookData(
            () -> FakeWebhookController.data != null && FakeWebhookController.data.contains(execution.getId())
                ? FakeWebhookController.data
                : null,
            5000
        );

        assertThat(receivedData, containsString(execution.getId()));
        assertThat(receivedData, containsString("https://mysuperhost.com/kestra/ui"));
        assertThat(receivedData, containsString("Failed"));
        assertThat(receivedData, containsString("\"text\""));
        assertThat(receivedData, containsString("Environment: DEV"));
        assertThat(receivedData, containsString("Cloud: GCP"));
        assertThat(receivedData, containsString("myCustomMessage"));
    }

    @Test
    void flow_successfulFlowShowLastTaskId() throws Exception {
        var execution = runAndCaptureExecution(
            "main-flow-that-succeeds",
            "x-successful"
        );

        String receivedData = waitForWebhookData(
            () -> FakeWebhookController.data != null && FakeWebhookController.data.contains(execution.getId())
                ? FakeWebhookController.data
                : null,
            5000
        );

        assertThat(receivedData, containsString(execution.getId()));
        assertThat(receivedData, containsString("https://mysuperhost.com/kestra/ui"));
        assertThat(receivedData, not(containsString("Failed")));
        assertThat(receivedData, containsString("SUCCESS"));
        assertThat(receivedData, containsString("Environment: DEV"));
        assertThat(receivedData, containsString("Status: SUCCESS"));
        assertThat(receivedData, containsString("\"text\""));
    }
}
