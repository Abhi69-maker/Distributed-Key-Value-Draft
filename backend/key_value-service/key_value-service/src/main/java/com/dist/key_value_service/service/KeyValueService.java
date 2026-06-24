package com.dist.key_value_service.service;


import com.dist.key_value_service.entity.KeyValue;
import com.dist.key_value_service.repository.KeyValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class KeyValueService {
    private final KeyValueRepository keyValueRepository;




    public List<KeyValue> getAllUsers() {

        log.info("Getting All the users");

        return keyValueRepository.findAll();

    }

    public KeyValue getUserByKey(String key) {

        log.info("Getting the user by key {}", key);

        KeyValue keyValue = keyValueRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        return KeyValue.builder()
                .key(keyValue.getKey())
                .value(keyValue.getValue())
                .build();
    }

    public KeyValue deleteByKey(String id) {

        KeyValue keyValue = keyValueRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User Not Found"));

        keyValueRepository.delete(keyValue);

        return KeyValue.builder()
                .key(keyValue.getKey())
                .value(keyValue.getValue())
                .build();
    }

    public KeyValue createUser(KeyValue keyValue) {

        log.info("Creating a new user {}", keyValue);

        return keyValueRepository.save(keyValue);

    }
}
