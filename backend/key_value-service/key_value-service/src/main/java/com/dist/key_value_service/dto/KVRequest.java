package com.dist.key_value_service.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KVRequest {
    @NotBlank
    private String key;
    @NotBlank
    private String value;

}
