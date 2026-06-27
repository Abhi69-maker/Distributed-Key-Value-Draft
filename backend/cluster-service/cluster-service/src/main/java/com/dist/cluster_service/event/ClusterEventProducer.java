package com.dist.cluster_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterEventProducer {

    private final KafkaTemplate<String, ClusterEvent> kafkaTemplate;

    @Value("${app.kafka.topics.cluster-events:cluster-events}")
    private String topic;

    public void publish(ClusterEvent event) {
        log.info("Publishing cluster event: nodeId={} status={}", event.nodeId(), event.status());
        kafkaTemplate.send(topic, event.nodeId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish cluster event for nodeId={}: {}", event.nodeId(), ex.getMessage());
                    }
                });
    }
}