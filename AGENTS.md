# Kestra X Plugin

## What

- Provides plugin components under `io.kestra.plugin.x`.
- Includes classes such as `XTemplate`, `XExecution`.

## Why

- This plugin integrates Kestra with X.
- It provides tasks that post messages to X (formerly Twitter).

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
