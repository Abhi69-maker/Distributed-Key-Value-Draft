package com.dist.key_value_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeartbeatScheduler {

    private final RedisTemplate<String, String> stringRedisTemplate;

    @Value("${app.node-id:node-1}")
    private String nodeId;

    private static final String HEARTBEAT_PREFIX = "cluster:heartbeat:";
    private static final String NODE_REGISTRY = "cluster:nodes:registry";

    @Scheduled(fixedRate = 10_000)
    public void sendHeartbeat() {
        try {
            String key = HEARTBEAT_PREFIX + nodeId;
            stringRedisTemplate.opsForValue().set(key, "UP", Duration.ofSeconds(30));
            stringRedisTemplate.opsForSet().add(NODE_REGISTRY, nodeId);
            log.debug("Heartbeat sent for nodeId={}", nodeId);
        } catch (Exception e) {
            log.warn("Heartbeat failed for nodeId={}: {}", nodeId, e.getMessage());
        }
    }

    @Scheduled(fixedRate = 10000)
    public void monitorCluster() {

        Set<String> nodes = stringRedisTemplate.opsForSet().members(NODE_REGISTRY);

        if (nodes == null) return;

        for (String node : nodes) {

            String key = HEARTBEAT_PREFIX + node;

            Boolean alive = stringRedisTemplate.hasKey(key);

            log.info("{} -> {}", node, Boolean.TRUE.equals(alive) ? "UP" : "DOWN");
        }
    }
}