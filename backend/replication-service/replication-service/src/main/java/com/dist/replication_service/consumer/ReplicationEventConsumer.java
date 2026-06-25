package com.dist.replication_service.consumer;

import com.dist.replication_service.event.KvEvent;
import com.dist.replication_service.service.ReplicationLogService;
import com.dist.replication_service.service.ReplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReplicationEventConsumer {

    private final ReplicationService replicationService;
    private final ReplicationLogService replicationLogService;

    @KafkaListener(
            topics = "${app.kafka.topics.kv-events:kv-events}",
            groupId = "replication-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(KvEvent event, Acknowledgment acknowledgment) {
        log.info("Received event eventId={} operation={} key='{}' fromNode={}",
                event.eventId(), event.operation(), event.key(), event.nodeId());

        try {
            if (replicationLogService.isAlreadyProcessed(event.eventId())) {
                log.warn("DUPLICATE event detected — eventId={} key='{}' already processed. Skipping apply.",
                        event.eventId(), event.key());
                replicationLogService.logSkippedDuplicate(event);
                acknowledgment.acknowledge();
                return;
            }

            replicationService.applyEvent(event);
            replicationLogService.logSuccess(event);

            log.info("Successfully replicated eventId={} key='{}' operation={}",
                    event.eventId(), event.key(), event.operation());

            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("FAILED to replicate eventId={} key='{}': {}", event.eventId(), event.key(), ex.getMessage(), ex);
            replicationLogService.logFailure(event, ex.getMessage());
            // Not acking → Kafka redelivers. Idempotency check above makes retry safe.
        }
    }
}
