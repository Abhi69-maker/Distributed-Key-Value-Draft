package com.dist.key_value_service.cache;

import com.dist.key_value_service.dto.KVResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class CacheService {

    private final RedisTemplate<String, KVResponse> redisTemplate;

    public void put(String key, KVResponse value) {

        redisTemplate.opsForValue().set(key, value);
    }

    public Optional<KVResponse> get(String key) {

        return Optional.ofNullable(
                redisTemplate.opsForValue().get(key)
        );
    }

    public void evict(String key) {

        redisTemplate.delete(key);
    }
}