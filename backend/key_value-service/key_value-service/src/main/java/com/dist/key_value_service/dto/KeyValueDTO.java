package com.dist.key_value_service.dto;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

public class KeyValueDTO {
    @Id
    private String key;
    private String value;



}
