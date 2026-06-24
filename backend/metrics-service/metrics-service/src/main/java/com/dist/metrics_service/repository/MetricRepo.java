package com.dist.metrics_service.repository;

import com.dist.metrics_service.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Repository
public interface MetricRepo extends JpaRepository<Metric, UUID> {
    long countByMetricNameAndMetricValue(
            String metricName,
            String metricValue
    );

    long countByMetricName(String metricName);
}
