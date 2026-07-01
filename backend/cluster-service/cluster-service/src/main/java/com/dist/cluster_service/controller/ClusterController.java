package com.dist.cluster_service.controller;

import com.dist.cluster_service.service.ClusterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Cluster Management", description = "Node registration, discovery, and health monitoring. Uses Redis heartbeat keys to track node liveness and publishes Kafka events on status changes.")
public class ClusterController {

    private final ClusterService clusterService;

    @Operation(
            summary = "Get cluster health status",
            description = """
                    Returns the current health status of every registered node in the cluster.
                    
                    **How it works:**
                    1. Fetches all registered node IDs from a Redis Set (`node-registry`).
                    2. For each node, checks if its heartbeat key exists in Redis.
                    3. Returns `UP` if the heartbeat key is alive, `DOWN` if it has expired or is missing.
                    
                    **Heartbeat TTL:** Each node refreshes its heartbeat key every few seconds.
                    If a node crashes, its heartbeat expires and its status becomes `DOWN`.
                    
                    **Status values:**
                    - `UP`   → Node is alive and healthy.
                    - `DOWN` → Node heartbeat has expired or node has crashed.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cluster status retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "All nodes UP",
                                            value = """
                                            {
                                              "node-1": "UP",
                                              "node-2": "UP",
                                              "node-3": "UP"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "One node DOWN",
                                            value = """
                                            {
                                              "node-1": "UP",
                                              "node-2": "UP",
                                              "node-3": "DOWN"
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/cluster/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(clusterService.getClusterStatus());
    }

    @Operation(
            summary = "Get active cluster nodes",
            description = """
                    Returns only the nodes that are currently alive and healthy.
                    
                    **How it works:**
                    1. Calls the cluster status check internally.
                    2. Filters out nodes with status `DOWN`.
                    3. Returns only node IDs with status `UP`.
                    
                    **Use cases:**
                    - Load balancing — route traffic only to alive nodes.
                    - Service discovery — find available nodes dynamically.
                    - Replication targeting — replicate only to healthy nodes.
                    
                    **Note:** A scheduled job runs every 5 seconds to detect status changes
                    and publish Kafka events to `node-status-events` topic.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Active nodes returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "All nodes active",
                                            value = """
                                            ["node-1", "node-2", "node-3"]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "One node down",
                                            value = """
                                            ["node-1", "node-2"]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "All nodes down",
                                            value = """
                                            []
                                            """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/nodes")
    public ResponseEntity<List<String>> getActiveNodes() {
        return ResponseEntity.ok(clusterService.getActiveNodes());
    }
}