package com.dist.replication_service.controller;

import com.dist.replication_service.entity.ReplicationLog;
import com.dist.replication_service.repo.ReplicatedKvRepository;
import com.dist.replication_service.repo.ReplicationLogRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReplicationController {

    private final ReplicatedKvRepository replicaRepository;
    private final ReplicationLogRepository logRepository;

    @GetMapping("/replicas/{key}")
    public ResponseEntity<?> getReplica(@PathVariable String key) {
        return replicaRepository.findByKey(key)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/replication/logs")
    public ResponseEntity<Page<ReplicationLog>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(logRepository.findAllByOrderByProcessedAtDesc(PageRequest.of(page, size)));
    }

    @GetMapping("/replication/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
                "success", logRepository.countByStatus(ReplicationLog.ReplicationStatus.SUCCESS),
                "failed", logRepository.countByStatus(ReplicationLog.ReplicationStatus.FAILED),
                "skippedDuplicate", logRepository.countByStatus(ReplicationLog.ReplicationStatus.SKIPPED_DUPLICATE)
        ));
    }
}
