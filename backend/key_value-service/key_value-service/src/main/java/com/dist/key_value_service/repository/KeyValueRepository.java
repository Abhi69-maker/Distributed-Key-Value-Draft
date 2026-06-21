package com.dist.key_value_service.repository;


import com.dist.key_value_service.entity.KeyValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyValueRepository extends JpaRepository<KeyValue,String> {



}
