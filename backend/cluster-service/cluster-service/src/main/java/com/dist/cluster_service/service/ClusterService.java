package com.dist.cluster_service.service;

import com.dist.cluster_service.config.ClusterRedisKeys;
import com.dist.cluster_service.event.ClusterEvent;
import com.dist.cluster_service.event.ClusterEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterService {

    private final RedisTemplate<String, String> stringRedisTemplate;
    private final ClusterEventProducer eventProducer;

    private final Map<String, ClusterEvent.NodeStatus> lastKnownStatus = new ConcurrentHashMap<>();

    public Map<String, String> getClusterStatus() {
        Set<String> allNodes = stringRedisTemplate.opsForSet().members(ClusterRedisKeys.NODE_REGISTRY);
        Map<String, String> status = new HashMap<>();
        if (allNodes == null) return status;

        for (String nodeId : allNodes) {
            boolean alive = Boolean.TRUE.equals(stringRedisTemplate.hasKey(ClusterRedisKeys.heartbeatKey(nodeId)));
            status.put(nodeId, alive ? "UP" : "DOWN");
        }
        return status;
    }

    public java.util.List<String> getActiveNodes() {
        return getClusterStatus().entrySet().stream()
                .filter(e -> "UP".equals(e.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

    @Scheduled(fixedRate = 5_000)
    public void detectStatusChanges() {
        Map<String, String> currentStatus = getClusterStatus();
        currentStatus.forEach((nodeId, statusStr) -> {
            ClusterEvent.NodeStatus current = ClusterEvent.NodeStatus.valueOf(statusStr);
            ClusterEvent.NodeStatus previous = lastKnownStatus.get(nodeId);

            if (previous != current) {
                log.info("Node status CHANGED: nodeId={} {} → {}", nodeId, previous, current);
                eventProducer.publish(ClusterEvent.of(nodeId, current));
                lastKnownStatus.put(nodeId, current);
            }
        });
    }
}