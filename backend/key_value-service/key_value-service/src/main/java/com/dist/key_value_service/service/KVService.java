package com.dist.key_value_service.service;

import com.dist.key_value_service.cache.CacheService;
import com.dist.key_value_service.dto.KVRequest;
import com.dist.key_value_service.dto.KVResponse;
import com.dist.key_value_service.entity.KV;
import com.dist.key_value_service.event.KVEvent;
import com.dist.key_value_service.event.KVEventProducer;
import com.dist.key_value_service.repository.KVRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KVService {
    private final KVEventProducer eventProducer;
    private final KVRepository kvRepository;
    private final CacheService cacheService;

    @Value("${app.node-id:node-1}")
    private String nodeId;

    public KVResponse createKeyValue(KVRequest request) {
        log.info("[{}] Creating key '{}'", nodeId, request.getKey());

        if (kvRepository.existsByKey(request.getKey())) {
            throw new RuntimeException("Key already exists : " + request.getKey());
        }

        KV kv = KV.builder()
                .key(request.getKey())
                .value(request.getValue())
                .nodeId(nodeId)
                .build();

        KV saved = kvRepository.save(kv);
        KVResponse response = mapToResponse(saved);

        cacheService.put(
                saved.getKey(),
                response
        );

//        eventProducer.publish(
//                KVEvent.builder()
//                        .operation("CREATE")
//                        .key(saved.getKey())
//                        .value(saved.getValue())
//                        .version(saved.getVersion())
//                        .nodeId(saved.getNodeId())
//                        .build()
//        );
        eventProducer.publish(
                KVEvent.builder()
                        .eventId(UUID.randomUUID())   // ← added this
                        .operation("CREATE")
                        .key(saved.getKey())
                        .value(saved.getValue())
                        .version(saved.getVersion())
                        .nodeId(saved.getNodeId())
                        .build()
        );

        return response;
    }

    public KVResponse getKeyValue(String key) {
        log.info("Retrieving key {}", key);

        Optional<KVResponse> cached = cacheService.get(key);
        if (cached.isPresent()) {
            log.info("CACHE HIT");
            return cached.get();
        }

        log.info("CACHE MISS");

        KV kv = kvRepository.findByKey(key)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Key Not Found"));

        KVResponse response =
                mapToResponse(kv);
        cacheService.put(key,response);

        return response;
    }

    public KVResponse updateKeyValue(
            String key,
            KVRequest request) {
        log.info("[{}] UPDATE key '{}'", nodeId, key);

        KV kv = kvRepository.findByKey(key)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Key Not Found : " + key
                        ));

        kv.setValue(request.getValue());
        kv.setVersion(
                kv.getVersion() + 1
        );

        KV updated = kvRepository.save(kv);
        KVResponse response = mapToResponse(updated);

        cacheService.evict(key);            // ← evict stale cache, not put
//        cacheService.put(
//                updated.getKey(),
//                response
//        );

        eventProducer.publish(
                KVEvent.builder()
                        .eventId(UUID.randomUUID())   // ← added
                        .operation("UPDATE")
                        .key(updated.getKey())
                        .value(updated.getValue())
                        .version(updated.getVersion())   // ← added
                        .nodeId(nodeId)                  // ← added
                        .build()
        );
        return response;
    }

    @Transactional
    public void deleteByKey(String key) {
        log.info("Deleting key {}", key);

        KV kv = kvRepository.findByKey(key)
                        .orElseThrow(() -> new RuntimeException("Key Not Found"));

        eventProducer.publish(
                KVEvent.builder()
                        .eventId(UUID.randomUUID())   // ← added
                        .operation("DELETE")
                        .key(key)
                        .value(null)
                        .version(kv.getVersion())     // ← added
                        .nodeId(nodeId)               // ← added
                        .build()
        );
        cacheService.evict(key);
        kvRepository.deleteByKey(key);
    }

    public List<KVResponse> getAllKeyValues(
            Pageable pageable) {
        log.info("Getting all keys");

        return kvRepository.findAll(pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public boolean checkExistsKey(String key) {
        return kvRepository.existsByKey(key);
    }

    private KVResponse mapToResponse(KV kv) {
        return KVResponse.builder()
                .key(kv.getKey())
                .value(kv.getValue())
                .version(kv.getVersion())
                .nodeId(kv.getNodeId())
                .createdAt(kv.getCreatedAt())
                .updatedAt(kv.getUpdatedAt())
                .build();
    }
}