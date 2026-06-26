package com.dist.key_value_service.controller;


import com.dist.key_value_service.dto.KVRequest;
import com.dist.key_value_service.dto.KVResponse;
import com.dist.key_value_service.service.KVService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Slf4j
@RestController

@RequiredArgsConstructor
@RequestMapping("/api/v1/keys")
public class KVController {
    private final KVService kvService;

    @PostMapping("/create")
    public ResponseEntity<KVResponse> createKeyValue(
            @Valid @RequestBody KVRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(kvService.createKeyValue(request));
    }

    @PutMapping("/{key}")
    public ResponseEntity<KVResponse> updateKeyValue(
            @PathVariable String key,@Valid @RequestBody KVRequest kvRequest) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(kvService.updateKeyValue(key, kvRequest));
    }

    @GetMapping("/{key}")
    public ResponseEntity<KVResponse> getKeyValue(@PathVariable String key) {
        return ResponseEntity.ok(
                kvService.getKeyValue(key)
        );
    }

    @DeleteMapping("/delete/{key}")
        public ResponseEntity<String> deleteByKey(@PathVariable String key){
        kvService.deleteByKey(key);
        return ResponseEntity.ok().body("Key Deleted");
    }

    @GetMapping
    public ResponseEntity<List<KVResponse>> getAllKeyValue(
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int pageSize){

        return ResponseEntity.ok(
                kvService.getAllKeyValues(
                        PageRequest.of(page,pageSize)
                )
        );
    }

    @GetMapping("/exists/{key}")
    public ResponseEntity<Map<String,Object>> checkExistsKey(
            @PathVariable String key){

        return ResponseEntity.ok(
                Map.of(
                        "key",key,
                        "exists",kvService.checkExistsKey(key)
                )
        );
    }
}
