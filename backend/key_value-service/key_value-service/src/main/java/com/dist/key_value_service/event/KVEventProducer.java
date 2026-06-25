package com.dist.key_value_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KVEventProducer {

    private final KafkaTemplate<String, KVEvent> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    public void publish(KVEvent event) {

        log.info("Publishing Event -> Operation: {}, Key: {}",
                event.getOperation(),
                event.getKey());

        kafkaTemplate.send(
                topic,
                event.getKey(),
                event
        );

        log.info("Event Published Successfully");
    }
}