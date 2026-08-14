package org.acme;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.ResourceTemplate;
import io.quarkiverse.mcp.server.TextResourceContents;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;

public class InfraRemediationServer {

    private static final Set<String> BLOCKED_SERVICES = Set.of("database", "payment-gateway");
    private static final int MAX_REPLICAS = 5;
    private final AtomicInteger ticketCounter = new AtomicInteger(9938);

    // ── Resources ──

    @Resource(uri = "config://policies/safety-limits", mimeType = "application/json")
    String safetyLimits() {
        return "{\"max_db_replicas\": 5, \"blocked_services\": [\"database\", \"payment-gateway\"]}";
    }

    @ResourceTemplate(uriTemplate = "logs://{server_id}/syslog")
    TextResourceContents syslog(String server_id) {
        return TextResourceContents.create(
                "logs://" + server_id + "/syslog",
                "ERROR: nginx process consuming 99% memory on " + server_id + "\n"
                        + "WARN: disk usage at 92% on /var/log\n"
                        + "ERROR: OOM killer invoked for pid 4521\n"
                        + "INFO: systemd restarting failed units");
    }

    // ── Tools ──

    @Tool(description = "Restart a service on a specific server")
    String restartService(
            @ToolArg(description = "The server identifier") String serverId,
            @ToolArg(description = "The service to restart") String service) {
        if (BLOCKED_SERVICES.contains(service)) {
            throw new ToolCallException(
                    "POLICY BLOCK: Service '" + service + "' is in the blocked list and cannot be restarted"
                            + " (see config://policies/safety-limits).");
        }
        return "SUCCESS: Service '" + service + "' restarted on server '" + serverId + "'.";
    }

    @Tool(description = "Scale a deployment to a given number of replicas")
    String scaleDeployment(
            @ToolArg(description = "The deployment target") String target,
            @ToolArg(description = "Desired number of replicas (1-5)") int replicas) {
        if (replicas < 1) {
            throw new ToolCallException(
                    "INVALID: Replica count must be at least 1, got " + replicas + ".");
        }
        if (replicas > MAX_REPLICAS) {
            throw new ToolCallException(
                    "POLICY BLOCK: Cannot scale '" + target + "' to " + replicas
                            + " replicas. Maximum allowed is " + MAX_REPLICAS
                            + " (see config://policies/safety-limits).");
        }
        return "SUCCESS: Deployment '" + target + "' scaled to " + replicas + " replica(s).";
    }

    @Tool(description = "Escalate an incident by creating a support ticket")
    String escalateTicket(
            @ToolArg(description = "Reason for escalation") String reason,
            @ToolArg(description = "Urgency level: low, medium, or high") Urgency urgency) {
        int id = ticketCounter.incrementAndGet();
        return "Ticket INC-" + id + " created with urgency=" + urgency + ": " + reason;
    }
}
