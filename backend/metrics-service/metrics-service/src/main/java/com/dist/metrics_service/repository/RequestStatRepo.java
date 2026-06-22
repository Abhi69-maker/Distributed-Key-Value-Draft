package com.dist.metrics_service.repository;

import com.dist.metrics_service.entity.RequestStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestStatRepo extends JpaRepository<RequestStat, UUID> {
    long countByOperation(String operation);
    long countByNodeId(String nodeId);

    @Query("SELECT r.nodeId, r.operation, COUNT(r) FROM RequestStat r GROUP BY r.nodeId, r.operation")
    List<Object[]> countGroupedByNodeAndOperation();
}
