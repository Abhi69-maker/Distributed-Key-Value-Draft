package com.dist.metrics_service.controller;

import com.dist.metrics_service.service.MetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics Summary", description = "Unified observability layer. Aggregates KV operation counts, live cluster health, and live replication stats into a single service.")
public class MetricsController {

    private final MetricService metricService;

    @Operation(
            summary = "Get unified metrics summary",
            description = """
                    Returns a single aggregated dashboard response combining all three
                    metric sources in the system.
                    
                    **What is included:**
                    
                    **`requests`** — KV operation counts from local PostgreSQL (`metrics_db`).
                    Recorded from Kafka events. Shows how many CREATE, UPDATE, DELETE operations
                    have been performed across all KV nodes since the service started.
                    
                    **`cluster`** — Live cluster health fetched via HTTP from the Cluster Service.
                    Shows how many nodes are currently UP and DOWN, plus per-node status detail.
                    
                    **`replication`** — Live replication stats fetched via HTTP from the Replication Service.
                    Shows how many Kafka events were successfully replicated, failed, or skipped as duplicates.
                    
                    **Failure behavior:**
                    If the Cluster Service or Replication Service is unreachable (timeout = 3s),
                    the corresponding section returns an `error` field and all counts default to `0`.
                    The endpoint itself always returns `200` — partial data is still returned.
                    
                    **Ideal for:**
                    - Frontend monitoring dashboards
                    - Health check aggregators
                    - Single-call system status overview
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Unified metrics summary returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "All services healthy",
                                            summary = "Normal system state with all services reachable",
                                            value = """
                                            {
                                              "requests": {
                                                "CREATE": 42,
                                                "UPDATE": 18,
                                                "DELETE": 5
                                              },
                                              "cluster": {
                                                "nodesUp": 3,
                                                "nodesDown": 0,
                                                "nodeDetails": {
                                                  "node-1": "UP",
                                                  "node-2": "UP",
                                                  "node-3": "UP"
                                                }
                                              },
                                              "replication": {
                                                "success": 65,
                                                "failed": 0,
                                                "skippedDuplicate": 3
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "One downstream service unreachable",
                                            summary = "Cluster Service is down — partial data returned",
                                            value = """
                                            {
                                              "requests": {
                                                "CREATE": 42,
                                                "UPDATE": 18,
                                                "DELETE": 5
                                              },
                                              "cluster": {
                                                "nodesUp": 0,
                                                "nodesDown": 0,
                                                "error": "Cluster service unreachable"
                                              },
                                              "replication": {
                                                "success": 65,
                                                "failed": 0,
                                                "skippedDuplicate": 3
                                              }
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(metricService.getSummary());
    }

    @Tag(name = "Request Metrics")
    @Operation(
            summary = "Get KV operation counts by type",
            description = """
                    Returns the total count of each KV operation type recorded
                    since the Metrics Service started consuming Kafka events.
                    
                    **Data source:** Local PostgreSQL `request_stat` table.
                    Rows are inserted when Kafka events are consumed from:
                    - `kv-create-events` → counted as CREATE
                    - `kv-update-events` → counted as UPDATE
                    - `kv-delete-events` → counted as DELETE
                    
                    **Note:** These counts reflect events **consumed from Kafka**,
                    not requests made directly to the KV Service.
                    There may be up to ~1 second of lag between a write and it
                    appearing in these counts.
                    
                    **Use this to understand:**
                    - Overall write volume across the cluster.
                    - Ratio of creates vs updates vs deletes.
                    - Whether write traffic is growing or stable.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Request counts by operation type.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Active system",
                                            value = """
                                            {
                                              "CREATE": 42,
                                              "UPDATE": 18,
                                              "DELETE": 5
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Fresh start — no events yet",
                                            value = """
                                            {
                                              "CREATE": 0,
                                              "UPDATE": 0,
                                              "DELETE": 0
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/requests")
    public ResponseEntity<Map<String, Long>> getRequestBreakdown() {
        return ResponseEntity.ok(metricService.getRequestBreakdown());
    }

    @Tag(name = "Request Metrics")
    @Operation(
            summary = "Get KV operation counts grouped by node and operation",
            description = """
                    Returns request counts broken down by both **node ID** and **operation type**.
                    
                    **Data source:** Local PostgreSQL `request_stat` table,
                    grouped using a native SQL aggregation query.
                    
                    **Each row in the response contains:**
                    - Node ID (e.g. `node-1`, `node-2`, `node-3`)
                    - Operation type (CREATE, UPDATE, DELETE)
                    - Count of that operation on that node
                    
                    **Use this to understand:**
                    - Which nodes are handling more write traffic.
                    - Whether load is evenly distributed across nodes.
                    - If one node is receiving disproportionately more deletes or updates.
                    
                    **Response format:**
                    Returns a list of raw `Object[]` arrays:
                    - Index `0` → nodeId (String)
                    - Index `1` → operation (String)
                    - Index `2` → count (Long)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Request counts grouped by node and operation.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Balanced load across nodes",
                                            value = """
                                            [
                                              ["node-1", "CREATE", 15],
                                              ["node-1", "UPDATE", 6],
                                              ["node-1", "DELETE", 2],
                                              ["node-2", "CREATE", 14],
                                              ["node-2", "UPDATE", 7],
                                              ["node-2", "DELETE", 2],
                                              ["node-3", "CREATE", 13],
                                              ["node-3", "UPDATE", 5],
                                              ["node-3", "DELETE", 1]
                                            ]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Unbalanced — node-1 handling most traffic",
                                            value = """
                                            [
                                              ["node-1", "CREATE", 40],
                                              ["node-1", "UPDATE", 15],
                                              ["node-2", "CREATE", 2],
                                              ["node-3", "CREATE", 0]
                                            ]
                                            """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/requests/by-node")
    public ResponseEntity<List<Object[]>> getRequestsByNode() {
        return ResponseEntity.ok(metricService.getRequestsByNode());
    }

    @Tag(name = "Cluster Metrics")
    @Operation(
            summary = "Get live cluster node health",
            description = """
                    Returns the current health status of all registered cluster nodes
                    by making a **live HTTP call** to the Cluster Service.
                    
                    **Data source:** HTTP GET to `http://localhost:8083/api/v1/cluster/status`
                    
                    **How node status is determined:**
                    The Cluster Service checks Redis heartbeat keys (`heartbeat:{nodeId}`)
                    for each registered node. If the key exists → `UP`. If expired → `DOWN`.
                    
                    **Response includes:**
                    - `nodesUp` — count of nodes currently alive.
                    - `nodesDown` — count of nodes currently unreachable.
                    - `nodeDetails` — per-node status map.
                    
                    **Timeout:** 3 seconds. If the Cluster Service does not respond
                    within 3 seconds, an `error` field is returned and all counts default to `0`.
                    
                    **This endpoint reflects the state at the moment of the call.**
                    There is no caching — every request triggers a fresh HTTP call
                    to the Cluster Service.
                    
                    **Use this to:**
                    - Detect node failures in real time.
                    - Verify that a restarted node has re-registered successfully.
                    - Monitor cluster capacity (how many nodes are accepting traffic).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Live cluster health returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "All nodes UP",
                                            value = """
                                            {
                                              "nodesUp": 3,
                                              "nodesDown": 0,
                                              "nodeDetails": {
                                                "node-1": "UP",
                                                "node-2": "UP",
                                                "node-3": "UP"
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "One node DOWN",
                                            value = """
                                            {
                                              "nodesUp": 2,
                                              "nodesDown": 1,
                                              "nodeDetails": {
                                                "node-1": "UP",
                                                "node-2": "UP",
                                                "node-3": "DOWN"
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Cluster Service unreachable",
                                            value = """
                                            {
                                              "nodesUp": 0,
                                              "nodesDown": 0,
                                              "error": "Cluster service unreachable"
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/cluster")
    public ResponseEntity<Map<String, Object>> getClusterStats() {
        return ResponseEntity.ok(metricService.getClusterStats());
    }

    @Tag(name = "Replication Metrics")
    @Operation(
            summary = "Get live replication event statistics",
            description = """
                    Returns the current replication processing statistics by making
                    a **live HTTP call** to the Replication Service.
                    
                    **Data source:** HTTP GET to `http://localhost:8082/api/v1/replication/stats`
                    
                    **What each field means:**
                    
                    | Field | Meaning |
                    |-------|---------|
                    | `success` | Kafka events successfully consumed and applied to the replica DB |
                    | `failed` | Events that threw an exception during processing — Kafka will redeliver |
                    | `skippedDuplicate` | Events whose `eventId` already existed in `replication_log` — safely ignored |
                    
                    **Healthy system shows:**
                    - `success` > 0 (events are flowing through the pipeline)
                    - `failed` = 0 (no processing errors)
                    - `skippedDuplicate` ≥ 0 (normal — expected after Kafka consumer restarts)
                    
                    **If `failed` > 0:**
                    Check the Replication Service logs for exception details.
                    Common causes: PostgreSQL connection issue, malformed Kafka payload,
                    or database constraint violation.
                    
                    **Timeout:** 3 seconds. If the Replication Service does not respond,
                    an `error` field is returned and all counts default to `0`.
                    
                    **Use this to:**
                    - Confirm that writes from the KV Service are being replicated.
                    - Detect replication failures early.
                    - Verify idempotency is working (skippedDuplicate count).
                    - Check replication pipeline health after a service restart.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Live replication statistics returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Healthy replication",
                                            value = """
                                            {
                                              "success": 65,
                                              "failed": 0,
                                              "skippedDuplicate": 3
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Replication failures detected",
                                            value = """
                                            {
                                              "success": 60,
                                              "failed": 5,
                                              "skippedDuplicate": 3
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Replication Service unreachable",
                                            value = """
                                            {
                                              "replicationCompleted": 0,
                                              "replicationFailed": 0,
                                              "error": "Replication service unreachable"
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/replication")
    public ResponseEntity<Map<String, Object>> getReplicationStats() {
        return ResponseEntity.ok(metricService.getReplicationStats());
    }
}