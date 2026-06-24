package com.dist.metrics_service.consumer;

import com.dist.metrics_service.event.KvEvent;
import com.dist.metrics_service.service.MetricService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KvEventMetricsConsumer {

    private final MetricService metricService;
    private final MeterRegistry meterRegistry;

    @KafkaListener(
            topics = "${app.kafka.topics.kv-events:kv-events}",
            groupId = "metrics-group",
            containerFactory = "kvEventListenerContainerFactory"
    )
    public void consume(KvEvent event,
                        Acknowledgment acknowledgment) {

        try {

            metricService.recordKvEvent(event);

            Counter.builder("kv.requests.total")
                    .tags(
                            Tags.of(
                                    "operation",
                                    event.operation().name(),
                                    "node",
                                    event.nodeId()
                            )
                    )
                    .register(meterRegistry)
                    .increment();

            acknowledgment.acknowledge();

        } catch (Exception e) {

            log.error(
                    "Failed to record metric for eventId={}",
                    event.eventId(),
                    e
            );
        }
    }
}