package com.dist.cluster_service.config;

import com.dist.cluster_service.config.ClusterRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClusterCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Store heartbeat with TTL.
     */
    public void saveHeartbeat(String nodeId) {

        redisTemplate.opsForValue().set(
                ClusterRedisKeys.heartbeatKey(nodeId),
                "UP",
                Duration.ofSeconds(30)
        );

        redisTemplate.opsForSet().add(
                ClusterRedisKeys.NODE_REGISTRY,
                nodeId
        );
    }

    /**
     * Check whether node heartbeat exists.
     */
    public boolean isNodeAlive(String nodeId) {

        return redisTemplate.hasKey(
                ClusterRedisKeys.heartbeatKey(nodeId)
        );
    }

    /**
     * Get all registered nodes.
     */
    public Set<String> getRegisteredNodes() {

        return redisTemplate
                .opsForSet()
                .members(ClusterRedisKeys.NODE_REGISTRY);
    }

    /**
     * Remove a node from registry.
     */
    public void removeNode(String nodeId) {

        redisTemplate.delete(
                ClusterRedisKeys.heartbeatKey(nodeId)
        );

        redisTemplate.opsForSet().remove(
                ClusterRedisKeys.NODE_REGISTRY,
                nodeId
        );
    }
}