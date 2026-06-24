package com.dist.metrics_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "request_stats")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RequestStat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "operation", nullable = false, length = 20)
    private String operation;

    @Column(name = "node_id", length = 50)
    private String nodeId;

    @Column(name = "kv_key", length = 255)
    private String key;

    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

}
