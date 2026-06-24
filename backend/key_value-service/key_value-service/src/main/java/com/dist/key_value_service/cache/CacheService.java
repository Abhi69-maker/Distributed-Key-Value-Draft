package com.dist.key_value_service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String,Object> redisTemplate;

    public void put(String key,Object value){

        redisTemplate.opsForValue()
                .set(key,value);
    }

    public Optional<Object> get(String key){

        Object value =
                redisTemplate.opsForValue()
                        .get(key);

        return Optional.ofNullable(value);
    }

    public void evict(String key){

        redisTemplate.delete(key);
    }
}