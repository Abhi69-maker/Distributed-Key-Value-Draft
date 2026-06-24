package com.dist.metrics_service.controller;

import com.dist.metrics_service.service.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricService metricService;

    @GetMapping
    public ResponseEntity<Map<String,Object>> getSummary() {
        return ResponseEntity.ok(
                metricService.getSummary()
        );
    }

    @GetMapping("/requests")
    public ResponseEntity<Map<String,Long>> getRequestBreakdown() {
        return ResponseEntity.ok(
                metricService.getRequestBreakdown()
        );
    }

    @GetMapping("/requests/by-node")
    public ResponseEntity<List<Map<String,Object>>> getRequestsByNode() {

        List<Object[]> rows =
                metricService.getRequestsByNode();

        List<Map<String,Object>> result =
                new ArrayList<>();

        for(Object[] row : rows) {

            result.add(
                    Map.of(
                            "nodeId", row[0],
                            "operation", row[1],
                            "count", row[2]
                    )
            );
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/replication")
    public ResponseEntity<Map<String,Long>> getReplicationStats() {
        return ResponseEntity.ok(
                metricService.getReplicationStats()
        );
    }

    @GetMapping("/cluster")
    public ResponseEntity<Map<String,Long>> getClusterStats() {
        return ResponseEntity.ok(
                metricService.getClusterStats()
        );
    }
}