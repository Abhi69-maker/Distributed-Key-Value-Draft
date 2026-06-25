package com.dist.replication_service.service;

import com.dist.replication_service.entity.ReplicatedKvEntry;
import com.dist.replication_service.event.KvEvent;
import com.dist.replication_service.repo.ReplicatedKvRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReplicationService {

    private final ReplicatedKvRepository repository;

    @Transactional
    public void applyEvent(KvEvent event) {
        switch (event.operation()) {
            case PUT, UPDATE -> applyUpsert(event);
            case DELETE -> applyDelete(event);
        }
    }

    private void applyUpsert(KvEvent event) {
        Optional<ReplicatedKvEntry> existing = repository.findByKey(event.key());

        if (existing.isPresent() && existing.get().getVersion() >= event.version()) {
            log.warn("Skipping stale event for key='{}' — replica version={} >= event version={}",
                    event.key(), existing.get().getVersion(), event.version());
            return;
        }

        ReplicatedKvEntry entry = existing.orElseGet(() -> ReplicatedKvEntry.builder().key(event.key()).build());
        entry.setValue(event.value());
        entry.setVersion(event.version());
        entry.setSourceNodeId(event.nodeId());

        repository.save(entry);
    }

    private void applyDelete(KvEvent event) {
        if (repository.existsByKey(event.key())) {
            repository.deleteByKey(event.key());
        }
    }
}