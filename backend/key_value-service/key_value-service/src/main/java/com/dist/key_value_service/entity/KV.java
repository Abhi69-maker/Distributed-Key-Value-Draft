package com.dist.key_value_service.entity;


import jakarta.persistence.*;
import lombok.*;
import org.w3c.dom.Text;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "key_value",
        indexes = {
                @Index(
                        name = "idx_key_name",
                        columnList = "key_name",
                        unique = true
                )
        }
)
public class KV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "key_name",
            nullable = false,
            unique = true,
            length = 255
    )
    private String key;


    @Column(
            name = "key_data",
            nullable = false,
            columnDefinition = "Text"
    )
    private String value;

    @Builder.Default
    private Long version = 1L;

    @Column(
            name = "node_id",
            length = 50
    )
    private String nodeId;


    @Column(
            name = "created_at",
            updatable = false

    )
    private LocalDateTime createdAt;


    @Column(
            name = "updated_at"
    )
    private LocalDateTime updatedAt;




}
