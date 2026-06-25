package com.dist.replication_service.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

public record KvEvent(
        UUID eventId,
        EventOperation operation,
        String key,
        String value,
        Long version,
        String nodeId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp
) {
    public enum EventOperation { PUT, UPDATE, DELETE }
}
