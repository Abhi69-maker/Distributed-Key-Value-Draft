package com.dist.metrics_service.service;

import com.dist.metrics_service.entity.Metric;
import com.dist.metrics_service.entity.RequestStat;
import com.dist.metrics_service.event.KvEvent;
import com.dist.metrics_service.repository.MetricRepo;
import com.dist.metrics_service.repository.RequestStatRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final RequestStatRepo requestStatRepository;
    private final MetricRepo metricRepository;

    public Map<String, Object> getSummary() {

        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("requests", getRequestBreakdown());
        summary.put("cluster", getClusterStats());
        summary.put("replication", getReplicationStats());

        return summary;
    }

    public Map<String, Long> getRequestBreakdown() {

        Map<String, Long> breakdown = new LinkedHashMap<>();

        for (String op : List.of("PUT", "GET", "UPDATE", "DELETE")) {
            breakdown.put(
                    op,
                    requestStatRepository.countByOperation(op)
            );
        }

        return breakdown;
    }

    public void recordKvEvent(KvEvent event) {

        RequestStat stat =
                RequestStat.builder()
                        .operation(event.operation().name())
                        .nodeId(event.nodeId())
                        .key(event.key())
                        .build();

        requestStatRepository.save(stat);
    }

//    public void recordClusterEvent(ClusterEvent event) {
//
//        Metric metric =
//                Metric.builder()
//                        .metricName("node.status.changed")
//                        .metricValue(event.status().name())
//                        .nodeId(event.nodeId())
//                        .build();
//
//        metricRepository.save(metric);
//    }

    public Map<String, Long> getClusterStats() {

        Map<String, Long> stats = new LinkedHashMap<>();

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

    public Map<String, Long> getReplicationStats() {

        Map<String, Long> stats = new LinkedHashMap<>();

        stats.put(
                "replicationCompleted",
                metricRepository.countByMetricName(
                        "replication.completed"
                )
        );

        stats.put(
                "replicationFailed",
                metricRepository.countByMetricName(
                        "replication.failed"
                )
        );

        return stats;
    }

    public List<Object[]> getRequestsByNode() {
        return requestStatRepository.countGroupedByNodeAndOperation();
    }
}