package com.dist.key_value_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KVResponse {

    private String key;
    private String value;
    private Long version;
    private String nodeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
