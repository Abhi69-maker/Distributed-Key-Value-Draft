package com.dist.key_value_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "key_value", indexes = @Index(name = "idx_kv_key", columnList = "kv_key", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class KV {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "kv_key", unique = true, nullable = false, length = 255)
    private String key;

    @Column(name = "kv_value", columnDefinition = "TEXT", nullable = false)
    private String value;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 1L;

    @Column(name = "node_id", length = 50)
    private String nodeId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}