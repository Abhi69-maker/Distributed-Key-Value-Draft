package com.dist.key_value_service.repository;


import com.dist.key_value_service.dto.KVResponse;
import com.dist.key_value_service.entity.KV;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KVRepository extends JpaRepository<KV,String> {

    void deleteByKey(String key);
    boolean existsByKey(String key);


    Optional<KV> findByKey(String key);
}
