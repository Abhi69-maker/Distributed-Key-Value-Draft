package com.dist.key_value_service.repositary;

import com.dist.key_value_service.dto.KeyValueDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyValueRepositary extends JpaRepository<KeyValueDTO,String> {



}
