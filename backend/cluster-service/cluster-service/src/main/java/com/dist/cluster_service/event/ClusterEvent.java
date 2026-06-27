package com.dist.cluster_service.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClusterEvent(
        UUID eventId,
        String nodeId,
        NodeStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp
) {
    public static ClusterEvent of(String nodeId, NodeStatus status) {
        return new ClusterEvent(UUID.randomUUID(), nodeId, status, LocalDateTime.now());
    }
    public enum NodeStatus { UP, DOWN }
}