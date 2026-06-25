package com.dist.replication_service.service;

import com.dist.replication_service.entity.ReplicationLog;
import com.dist.replication_service.event.KvEvent;
import com.dist.replication_service.repo.ReplicationLogRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplicationLogService {
    private final ReplicationLogRepository repository;

    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Transactional
    public void logSuccess(KvEvent event) {
        save(event, ReplicationLog.ReplicationStatus.SUCCESS, null);
    }

    @Transactional
    public void logFailure(KvEvent event, String errorMessage) {
        save(event, ReplicationLog.ReplicationStatus.FAILED, errorMessage);
    }

    @Transactional
    public void logSkippedDuplicate(KvEvent event) {
        log.info("Duplicate event eventId={} key='{}' — original SUCCESS record preserved", event.eventId(), event.key());
    }

    private void save(KvEvent event, ReplicationLog.ReplicationStatus status, String errorMessage) {
        try {
            ReplicationLog logEntry = ReplicationLog.builder()
                    .eventId(event.eventId())
                    .key(event.key())
                    .operation(event.operation().name())
                    .sourceNode(event.nodeId())
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();
            repository.save(logEntry);
        } catch (DataIntegrityViolationException e) {
            log.warn("Race condition caught by unique constraint — eventId={} already logged", event.eventId());
        }
    }
}
