package com.dist.key_value_service.controller;

import com.dist.key_value_service.dto.KVRequest;
import com.dist.key_value_service.dto.KVResponse;
import com.dist.key_value_service.service.KVService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Key-Value Store", description = "CRUD operations for the distributed key-value store. Writes persist to PostgreSQL, cache to Redis, and publish Kafka replication events.")
public class KVController {

    private final KVService kvService;

    @Operation(
            summary = "Create a new key-value pair",
            description = """
                    Creates a new key in the distributed key-value store.
                    
                    **Flow:**
                    1. Validates request body.
                    2. Checks if key already exists in PostgreSQL.
                    3. Persists the key-value pair to PostgreSQL.
                    4. Caches the entry in Redis (Write-Through).
                    5. Publishes a Kafka CREATE event to `kv-create-events` topic for replication.
                    
                    **Note:** Keys must be unique. Duplicate key returns HTTP 409.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Key created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success",
                                    value = """
                                    {
                                      "key": "user:1",
                                      "value": "Ajay Kumar",
                                      "version": 1,
                                      "nodeId": "node-1",
                                      "createdAt": "2026-07-01T10:00:00",
                                      "updatedAt": "2026-07-01T10:00:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body — key or value is missing."),
            @ApiResponse(responseCode = "409", description = "Key already exists in the store.")
    })
    @PostMapping("/create")
    public ResponseEntity<KVResponse> createKeyValue(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Key-value pair to store",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "key": "user:1",
                                      "value": "Ajay Kumar"
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody KVRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(kvService.createKeyValue(request));
    }

    @Operation(
            summary = "Update an existing key",
            description = """
                    Updates the value of an existing key in the store.
                    
                    **Flow:**
                    1. Looks up the key in PostgreSQL.
                    2. Updates the value and increments the version number.
                    3. Evicts the stale Redis cache entry.
                    4. Publishes a Kafka UPDATE event to `kv-update-events` topic for replication.
                    
                    **Note:** Version is auto-incremented on every update for optimistic concurrency tracking.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Key updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "key": "user:1",
                                      "value": "Lohith Kumar",
                                      "version": 2,
                                      "nodeId": "node-1",
                                      "createdAt": "2026-07-01T10:00:00",
                                      "updatedAt": "2026-07-01T10:05:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Key not found in the store."),
            @ApiResponse(responseCode = "400", description = "Invalid request body.")
    })
    @PutMapping("/{key}")
    public ResponseEntity<KVResponse> updateKeyValue(
            @Parameter(description = "Existing key to update", example = "user:1")
            @PathVariable String key,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New value for the key",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "key": "user:1",
                                      "value": "Lohith Kumar"
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody KVRequest kvRequest) {
        return ResponseEntity.ok(kvService.updateKeyValue(key, kvRequest));
    }

    @Operation(
            summary = "Retrieve value by key",
            description = """
                    Retrieves the value associated with a key.
                    
                    **Cache-Aside Flow:**
                    1. Checks Redis cache for the key.
                    2. **Cache HIT** → Returns immediately from Redis (low latency).
                    3. **Cache MISS** → Queries PostgreSQL, caches the result in Redis, then returns.
                    
                    **Note:** This ensures low-latency reads for frequently accessed keys.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Key found and value returned.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "key": "user:1",
                                      "value": "Ajay Kumar",
                                      "version": 1,
                                      "nodeId": "node-1",
                                      "createdAt": "2026-07-01T10:00:00",
                                      "updatedAt": "2026-07-01T10:00:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Key not found in the store.")
    })
    @GetMapping("/{key}")
    public ResponseEntity<KVResponse> getKeyValue(
            @Parameter(description = "Key to retrieve", example = "user:1")
            @PathVariable String key) {
        return ResponseEntity.ok(kvService.getKeyValue(key));
    }

    @Operation(
            summary = "Delete a key",
            description = """
                    Permanently deletes a key from the distributed store.
                    
                    **Flow:**
                    1. Looks up the key in PostgreSQL.
                    2. Publishes a Kafka DELETE event to `kv-delete-events` topic.
                    3. Evicts the Redis cache entry.
                    4. Deletes the key from PostgreSQL.
                    
                    **Note:** Replication nodes consume the DELETE event and remove the key from their local stores.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Key deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Key not found in the store.")
    })
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<String> deleteByKey(
            @Parameter(description = "Key to delete", example = "user:1")
            @PathVariable String key) {
        kvService.deleteByKey(key);
        return ResponseEntity.ok("Key Deleted");
    }

    @Operation(
            summary = "Get all key-value pairs (paginated)",
            description = """
                    Returns all stored key-value pairs with pagination support.
                    
                    **Parameters:**
                    - `page` : Zero-based page index (default: 0).
                    - `pageSize` : Number of records per page (default: 10).
                    
                    **Note:** Data is fetched directly from PostgreSQL without cache.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated list returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    [
                                      {
                                        "key": "user:1",
                                        "value": "Ajay Kumar",
                                        "version": 1,
                                        "nodeId": "node-1",
                                        "createdAt": "2026-07-01T10:00:00",
                                        "updatedAt": "2026-07-01T10:00:00"
                                      },
                                      {
                                        "key": "user:2",
                                        "value": "Lohith",
                                        "version": 1,
                                        "nodeId": "node-1",
                                        "createdAt": "2026-07-01T10:01:00",
                                        "updatedAt": "2026-07-01T10:01:00"
                                      }
                                    ]
                                    """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<KVResponse>> getAllKeyValue(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(kvService.getAllKeyValues(PageRequest.of(page, pageSize)));
    }

    @Operation(
            summary = "Check if a key exists",
            description = """
                    Returns whether a specific key exists in PostgreSQL.
                    
                    **Use cases:**
                    - Validate before a CREATE to avoid 409 conflicts.
                    - Validate before an UPDATE to avoid 404 errors.
                    - Quick existence check without fetching the full value.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Existence check completed.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Key exists", value = """
                                    {
                                      "key": "user:1",
                                      "exists": true
                                    }
                                    """),
                                    @ExampleObject(name = "Key missing", value = """
                                    {
                                      "key": "user:99",
                                      "exists": false
                                    }
                                    """)
                            }
                    )
            )
    })
    @GetMapping("/exists/{key}")
    public ResponseEntity<Map<String, Object>> checkExistsKey(
            @Parameter(description = "Key to check", example = "user:1")
            @PathVariable String key) {
        return ResponseEntity.ok(Map.of("key", key, "exists", kvService.checkExistsKey(key)));
    }
}