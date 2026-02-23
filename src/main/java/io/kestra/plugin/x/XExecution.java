package io.kestra.plugin.x;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.plugins.notifications.ExecutionInterface;
import io.kestra.core.plugins.notifications.ExecutionService;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Post execution summary to X",
    description = "Renders the bundled x.peb template with execution details (id, namespace, link, status, timings) plus custom fields/message, then sends it to X via bearer token or OAuth 1.0a with a 280-character cap."
)
@Plugin(
    examples = {
        @Example(
            title = "Send an X notification on a failed flow execution using Bearer Token.",
            full = true,
            code = """
                id: failure_alert_x
                namespace: company.team

                tasks:
                  - id: send_x_alert
                    type: io.kestra.plugin.x.XExecution
                    bearerToken: "{{ secret('X_BEARER_TOKEN') }}"
                    executionId: "{{ trigger.executionId }}"
                    customMessage: "Production workflow failed - immediate attention required!"  # Keep total text <= 280 chars
                    customFields:
                      Environment: "Production"
                      Team: "DevOps"
                      Priority: "High"

                triggers:
                  - id: failed_prod_workflows
                    type: io.kestra.plugin.core.trigger.Flow
                    conditions:
                      - type: io.kestra.plugin.core.condition.ExecutionStatus
                        in:
                          - FAILED
                          - WARNING
                      - type: io.kestra.plugin.core.condition.ExecutionNamespace
                        namespace: prod
                        prefix: true
                """
        ),
        @Example(
            title = "Send an X notification using OAuth 1.0a credentials.",
            full = true,
            code = """
                id: success_alert_x
                namespace: company.team

                tasks:
                  - id: send_x_success
                    type: io.kestra.plugin.x.XExecution
                    consumerKey: "{{ secret('X_CONSUMER_KEY') }}"
                    consumerSecret: "{{ secret('X_CONSUMER_SECRET') }}"
                    accessToken: "{{ secret('X_ACCESS_TOKEN') }}"
                    accessSecret: "{{ secret('X_ACCESS_SECRET') }}"
                    executionId: "{{ trigger.executionId }}"
                    customMessage: "Deployment completed successfully!"  # Keep total text <= 280 chars
                    options:
                      readTimeout: PT5S
                      headers:
                        X-Datacenter: "eu-west-1"

                triggers:
                  - id: successful_deployments
                    type: io.kestra.plugin.core.trigger.Flow
                    conditions:
                      - type: io.kestra.plugin.core.condition.ExecutionStatus
                        in:
                          - SUCCESS
                """
        )
    },
    aliases = "io.kestra.plugin.notifications.x.XExecution"
)
public class XExecution extends XTemplate implements ExecutionInterface {

    @Schema(title = "Execution ID", description = "Execution to include in the template; defaults to the current execution and should be set to {{ trigger.executionId }} when called from a Flow trigger")
    @Builder.Default
    private final Property<String> executionId = Property.ofExpression("{{ execution.id }}");

    @Schema(title = "Custom fields", description = "Key-value pairs appended to the rendered template output")
    private Property<Map<String, Object>> customFields;

    @Schema(title = "Custom message", description = "Optional extra text appended to the template output")
    private Property<String> customMessage;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        this.templateUri = Property.ofValue("x.peb");
        this.templateRenderMap = Property.ofValue(ExecutionService.executionMap(runContext, this));
        return super.run(runContext);
    }
}
