package com.dist.replication_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "replication_log", indexes = @Index(name = "idx_replication_event_id", columnList = "event_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", unique = true, nullable = false)
    private UUID eventId;

    @Column(name = "kv_key", nullable = false, length = 255)
    private String key;

    @Column(name = "operation", nullable = false, length = 20)
    private String operation;

    @Column(name = "source_node", length = 50)
    private String sourceNode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReplicationStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private LocalDateTime processedAt;

    public enum ReplicationStatus { SUCCESS, FAILED, SKIPPED_DUPLICATE }
}
