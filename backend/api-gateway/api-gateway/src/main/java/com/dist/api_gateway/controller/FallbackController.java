package com.dist.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * This fallback endpoint is triggered when all KV service nodes become
     * unavailable or the gateway cannot successfully route a request to any node.
     * Instead of returning a generic error, it provides a structured response
     * containing the error reason, user-friendly message, and timestamp.
     * This improves fault tolerance, helps clients understand the failure,
     * and demonstrates the circuit breaker/fallback mechanism of the system.
     */
    @GetMapping("/kv-service")
    public ResponseEntity<Map<String, Object>> kvServiceFallback() {

        Map<String, Object> response = new HashMap<>();

        response.put("error", "Service Unavailable");
        response.put("message", "All KV nodes are currently unreachable.");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * This fallback endpoint is used when the Cluster Service cannot be reached
     * due to node failures, network issues, or service downtime. It returns a
     * meaningful response indicating that cluster health information is currently
     * unavailable instead of exposing internal gateway errors. This ensures better
     * user experience and supports resilience in the distributed architecture.
     */
    @GetMapping("/cluster-service")
    public ResponseEntity<Map<String,Object>>clusterFallback(){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "Service Unavailable",
                "message", "Cluster Service is currently unreachable. Node status cannot be determined.",
                "timestamp", LocalDateTime.now()
        ));
    }
}
