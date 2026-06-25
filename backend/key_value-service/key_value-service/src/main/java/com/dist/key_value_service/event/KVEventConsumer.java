package com.dist.key_value_service.event;

import com.dist.key_value_service.event.KVEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KVEventConsumer {

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "kv-group"
    )
    public void consume(KVEvent event) {

        log.info("Received Event");

        log.info("Operation : {}", event.getOperation());
        log.info("Key       : {}", event.getKey());
        log.info("Value     : {}", event.getValue());
        log.info("Version   : {}", event.getVersion());
        log.info("NodeId    : {}", event.getNodeId());

    }
}