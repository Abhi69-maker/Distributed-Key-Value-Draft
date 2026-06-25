package com.dist.replication_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "replicated_key_value", indexes = @Index(name = "idx_replica_key", columnList = "kv_key", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReplicatedKvEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "kv_key", unique = true, nullable = false, length = 255)
    private String key;

    @Column(name = "kv_value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "source_node_id", length = 50)
    private String sourceNodeId;

    @UpdateTimestamp
    @Column(name = "replicated_at")
    private LocalDateTime replicatedAt;
}
