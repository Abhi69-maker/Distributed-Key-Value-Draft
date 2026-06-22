package com.dist.metrics_service.service;

import com.dist.metrics_service.repository.MetricRepo;
import com.dist.metrics_service.repository.RequestStatRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricService {

    private final RequestStatRepo requestStatRepository;
    private final MetricRepo metricRepository;

    public Map<String, Object> getSummary() {
        Map<String, Object> response = new HashMap<>();
        response.put("requestStats", getRequestBreakdown());
        response.put("clusterStats", getClusterStats());
        response.put("replicationStats", getReplicationStats());
        return response;
    }

    public Map<String, Long> getRequestBreakdown() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("PUT",
                requestStatRepository.countByOperation("PUT"));

        stats.put("GET",
                requestStatRepository.countByOperation("GET"));

        stats.put("UPDATE",
                requestStatRepository.countByOperation("UPDATE"));

        stats.put("DELETE",
                requestStatRepository.countByOperation("DELETE"));
        return stats;
    }

    public Map<String, Long> getReplicationStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put(
                "replicatedEvents",
                metricRepository.countByMetricName("replication.completed")
        );
        stats.put(
                "failedReplications",
                metricRepository.countByMetricName("replication.failed")
        );
        return stats;
    }

    public Map<String, Long> getClusterStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put(
                "nodesUp",
                metricRepository.countByMetricNameAndMetricValue(
                        "node.status.changed",
                        "UP"
                )
        );
        stats.put(
                "nodesDown",
                metricRepository.countByMetricNameAndMetricValue(
                        "node.status.changed",
                        "DOWN"
                )
        );
        return stats;
    }
}