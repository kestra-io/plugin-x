# How to use the X plugin

Post to X (Twitter) and send execution summaries from Kestra flows.

## Authentication

Two auth modes are supported. Set `bearerToken` for app-only authentication (read operations and posting as the app). For user-context posting, use OAuth 1.0a: set `consumerKey`, `consumerSecret`, `accessToken`, and `accessSecret`. Store all credentials in [secrets](https://kestra.io/docs/concepts/secret).

## Tasks

`XExecution` posts a structured execution summary including status, duration, and an execution link, and is designed for use with a [Flow trigger](https://kestra.io/docs/workflow-components/triggers) in a dedicated monitoring namespace that watches other namespaces for failures. Set `textBody` to customize the post content; posts are capped at 280 characters.
