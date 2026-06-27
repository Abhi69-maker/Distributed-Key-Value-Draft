package com.dist.cluster_service.controller;

import com.dist.cluster_service.service.ClusterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterService clusterService;

    @GetMapping("/cluster/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(clusterService.getClusterStatus());
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<String>> getActiveNodes() {
        return ResponseEntity.ok(clusterService.getActiveNodes());
    }
}