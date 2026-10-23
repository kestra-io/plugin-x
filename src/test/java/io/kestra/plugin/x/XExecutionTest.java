package io.kestra.plugin.x;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.flows.Flow;
import io.kestra.core.models.flows.GenericFlow;
import io.kestra.core.models.flows.State;
import io.kestra.core.models.validations.ModelValidator;
import io.kestra.core.repositories.FlowRepositoryInterface;
import io.kestra.core.repositories.LocalFlowRepositoryLoader;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.runners.TestRunner;
import io.kestra.core.serializers.YamlParser;
import io.kestra.core.utils.IdUtils;

import jakarta.inject.Inject;

import static io.kestra.core.tenant.TenantService.MAIN_TENANT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@KestraTest
public class XExecutionTest extends AbstractXTest {

    @Inject
    protected TestRunner runner;

    @Inject
    protected LocalFlowRepositoryLoader repositoryLoader;

    @Inject
    protected FlowRepositoryInterface flowRepository;

    @Inject
    protected ModelValidator modelValidator;

    @Inject
    protected RunContextFactory runContextFactory;

    @BeforeEach
    protected void init() throws IOException, URISyntaxException {
        repositoryLoader
            .load(Objects.requireNonNull(XExecutionTest.class.getClassLoader().getResource("flows")));
        this.runner.run();
    }

    @Test
    void flow() throws Exception {
        var execution = runAndCaptureExecution(
            "main-flow-that-fails",
            "x"
        );

        String receivedData = waitForWebhookData(() -> FakeWebhookController.data, 5000);

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

        String receivedData = waitForWebhookData(() -> FakeWebhookController.data, 5000);

        assertThat(receivedData, containsString(execution.getId()));
        assertThat(receivedData, containsString("https://mysuperhost.com/kestra/ui"));
        assertThat(receivedData, not(containsString("Failed")));
        assertThat(receivedData, containsString("SUCCESS"));
        assertThat(receivedData, containsString("Environment: DEV"));
        assertThat(receivedData, containsString("Status: SUCCESS"));
        assertThat(receivedData, containsString("\"text\""));
    }

    @SuppressWarnings("deprecation")
    @Test
    void flowSettingDeprecatedOptionsStillValidatesAndRuns() throws Exception {
        var message = "deprecated-options-" + IdUtils.create();
        var source = """
            id: x_deprecated_options
            namespace: io.kestra.tests
            tasks:
              - id: x
                type: io.kestra.plugin.x.XExecution
                url: "%s/webhook-unit-test"
                bearerToken: "test_bearer_token"
                customMessage: "%s"
                options:
                  readTimeout: PT30S
                  connectionPoolIdleTimeout: PT10S
                  maxContentLength: 1024
            """.formatted(embeddedServer.getURI(), message);
        var flow = YamlParser.parse(source, Flow.class);

        assertThat(modelValidator.isValid(flow).isEmpty(), is(true));

        var runContext = runContextFactory.of(Map.of());
        var options = ((XExecution) flow.getTasks().getFirst()).getOptions();
        assertThat(runContext.render(options.getReadTimeout()).as(Duration.class).orElseThrow(), is(Duration.ofSeconds(30)));
        assertThat(runContext.render(options.getConnectionPoolIdleTimeout()).as(Duration.class).orElseThrow(), is(Duration.ofSeconds(10)));
        assertThat(runContext.render(options.getMaxContentLength()).as(Integer.class).orElseThrow(), is(1024));

        flowRepository.create(GenericFlow.fromYaml(MAIN_TENANT, source));
        var execution = runnerUtils.runOne(MAIN_TENANT, "io.kestra.tests", "x_deprecated_options");

        assertThat(execution.getState().getCurrent(), is(State.Type.SUCCESS));
        assertThat(waitForWebhookData(() -> FakeWebhookController.data, 5000), containsString(message));
    }
}
