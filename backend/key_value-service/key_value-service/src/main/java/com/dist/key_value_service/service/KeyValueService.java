package com.dist.key_value_service.service;

import com.dist.key_value_service.dto.KeyValueDTO;
import com.dist.key_value_service.repositary.KeyValueRepositary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyValueService {
    KeyValueRepositary keyValueRepositary;



    public List<KeyValueDTO> getAllUsers() {

        log.info("Getting All the users");

        return keyValueRepositary.findAll();
    }

    public KeyValueDTO getUserByKey(String key) {

        log.info("Getting the user by key {}", key);
        return keyValueRepositary.findById(key)
                .orElseThrow(() -> new RuntimeException("User Not found"));

    }

    public KeyValueDTO createuser(KeyValueDTO keyValueDTO) {

    }

    public KeyValueDTO deleteByUser(KeyValueDTO keyValueDTO) {

    }
}
