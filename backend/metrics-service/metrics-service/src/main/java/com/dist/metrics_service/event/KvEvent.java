package com.dist.metrics_service.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record KvEvent(

        UUID eventId,

        Operation operation,

        String key,

        String value,

        String nodeId,

        LocalDateTime timestamp

) {

    public enum Operation {
        PUT,
        GET,
        UPDATE,
        DELETE
    }
}