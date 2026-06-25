package com.dist.key_value_service.repository;


import com.dist.key_value_service.dto.KVResponse;
import com.dist.key_value_service.entity.KV;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
public interface KVRepository extends JpaRepository<KV, UUID> {

    Optional<KV> findByKey(String key);

    boolean existsByKey(String key);

    void deleteByKey(String key);
}
