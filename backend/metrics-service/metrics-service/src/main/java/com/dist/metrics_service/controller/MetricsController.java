package com.dist.metrics_service.controller;


import com.dist.metrics_service.repository.MetricRepo;
import com.dist.metrics_service.repository.RequestStatRepo;
import com.dist.metrics_service.service.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final RequestStatRepo requestStatRepository;
git branch    private final MetricRepo metricRepository;
    private final MetricService metricService;

    @Value("${app.cluster-service.url:http://localhost:8083}")
    private String clusterServiceUrl;

    @Value("${app.replication-service.url:http://localhost:8082}")
    private String replicationServiceUrl;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("requests", getRequestBreakdown().getBody());
        summary.put("activeNodes", getActiveNodeCount());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/requests")
    public ResponseEntity<Map<String, Long>> getRequestBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (String op : List.of("PUT", "GET", "UPDATE", "DELETE")) {
            breakdown.put(op, requestStatRepository.countByOperation(op));
        }
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/requests/by-node")
    public ResponseEntity<List<Map<String, Object>>> getRequestsByNode() {
        List<Object[]> rows = requestStatRepository.countGroupedByNodeAndOperation();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(Map.of("nodeId", row[0], "operation", row[1], "count", row[2]));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/replication")
    public ResponseEntity<Object> getReplicationStats() {
        return ResponseEntity.ok(metricService.getReplicationStats());
    }

    @GetMapping("/cluster")
    public ResponseEntity<Long> getDownTransitionCount() {
        return ResponseEntity.ok(metricRepository.countByMetricNameAndMetricValue("node.status.changed", "DOWN"));
    }

    public int getActiveNodeCount() {
        return clusterService.getAllNodes().size();
    }

}
