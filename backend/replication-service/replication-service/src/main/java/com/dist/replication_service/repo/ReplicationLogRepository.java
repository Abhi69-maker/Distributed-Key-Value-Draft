package com.dist.replication_service.repo;

import com.dist.replication_service.entity.ReplicationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReplicationLogRepository extends JpaRepository<ReplicationLog, UUID> {

    boolean existsByEventId(UUID eventId);

    Page<ReplicationLog> findAllByOrderByProcessedAtDesc(Pageable pageable);

    long countByStatus(ReplicationLog.ReplicationStatus status);
}