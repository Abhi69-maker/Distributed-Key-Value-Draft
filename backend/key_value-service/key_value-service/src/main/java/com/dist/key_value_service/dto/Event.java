package com.dist.key_value_service.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Event {
    public String Operation;

    public String Key;

    public String Value;

}
