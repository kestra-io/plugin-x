# Kestra X Plugin

## What

- Provides plugin components under `io.kestra.plugin.x`.
- Includes classes such as `XTemplate`, `XExecution`.

## Why

- What user problem does this solve? Teams need to post messages to X (formerly Twitter) from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps X steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on X.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `x`

Infrastructure dependencies (Docker Compose services):

- `app`

### Key Plugin Classes

- `io.kestra.plugin.x.XExecution`

### Project Structure

```
plugin-x/
├── src/main/java/io/kestra/plugin/x/
├── src/test/java/io/kestra/plugin/x/
├── build.gradle
└── README.md
```

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
