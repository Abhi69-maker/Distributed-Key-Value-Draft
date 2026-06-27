package com.dist.cluster_service.scheduler;

import com.dist.cluster_service.config.ClusterCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeartbeatScheduler {

    private final ClusterCacheService clusterCacheService;

    @Value("${app.node-id:node-1}")
    private String nodeId;

    @Scheduled(fixedRate = 10000)
    public void sendHeartbeat() {

        try {

            clusterCacheService.saveHeartbeat(nodeId);

            log.info("Heartbeat sent successfully for node: {}", nodeId);

        } catch (Exception e) {

            log.error("Failed to send heartbeat for node {} : {}",
                    nodeId,
                    e.getMessage());
        }
    }
}