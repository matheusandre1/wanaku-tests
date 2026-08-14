# Infrastructure Auto-Remediation MCP Server

A mock MCP (Model Context Protocol) server for testing LLM intent-extraction and decision-making. Built with Quarkus and the `quarkus-mcp-server-http` extension.

## What it exposes

### Resources

| URI | Description |
|---|---|
| `config://policies/safety-limits` | Static JSON with `max_db_replicas` and `blocked_services` |
| `logs://{server_id}/syslog` | Dynamic template returning mock syslog entries for a given server |

### Tools

| Tool | Parameters | Behavior |
|---|---|---|
| `restartService` | `serverId`, `service` | Returns a simulated restart confirmation |
| `scaleDeployment` | `target`, `replicas` | Returns success if replicas <= 5; returns a policy-block error otherwise |
| `escalateTicket` | `reason`, `urgency` (low/medium/high) | Returns a mock ticket ID |

## Build

```shell
./mvnw clean install -B
```

Requires Java 25+.

## Run via HTTP transport

```shell
java -jar target/quarkus-app/quarkus-run.jar
```

The server starts on `http://localhost:8080` and exposes the MCP endpoint at `/mcp`.

### Claude Desktop configuration

Add to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "infra-remediation": {
      "type": "streamable-http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

### Claude Code configuration

Add to `.claude/settings.json`:

```json
{
  "mcpServers": {
    "infra-remediation": {
      "type": "streamable-http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```
