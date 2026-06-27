package com.dist.replication_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.dist.replication_service.entity.ReplicationLog;
import com.dist.replication_service.repo.ReplicatedKvRepository;
import com.dist.replication_service.repo.ReplicationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReplicationController {

    private final ReplicatedKvRepository replicaRepository;
    private final ReplicationLogRepository logRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // GET REPLICA
    // ─────────────────────────────────────────────────────────────────────────

    @Tag(name = "Replica Data")
    @Operation(
            summary = "Get replicated value by key",
            description = """
                Retrieves the replicated copy of a key from `replication_db`.
                
                **Primary use: verify replication worked.**
                After a PUT/UPDATE on the KV Service (port 8081), call this endpoint
                within ~1 second to confirm the Kafka event was consumed and the
                value is now mirrored here.
                
                **This is NOT the same database as the KV Service.**
                The KV Service writes to `kv_db`. This service reads from `replication_db`.
                They are completely separate PostgreSQL databases. Same data, different DB.
                
                **Fields specific to replication:**
                - `sourceNodeId` — which KV node originally wrote this key
                - `version` — matches the version in kv_db at time of last replication event
                - `replicatedAt` — when this service last applied an event for this key
                
                **If this returns 404:**
                Either the key was never created, or replication hasn't completed yet
                (check replication service logs for errors), or the key was deleted
                and the DELETE event was already applied here too.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Replicated entry found.",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                            {
                              "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                              "key": "user:1",
                              "value": "Ajay Kumar",
                              "version": 1,
                              "sourceNodeId": "node-1",
                              "replicatedAt": "2026-06-26T12:00:01"
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Key has not been replicated yet, or does not exist, or was deleted."
            )
    })
    @GetMapping("/replicas/{key}")
    public ResponseEntity<?> getReplica(
            @Parameter(
                    description = "The key to look up in the replica database",
                    example = "user:1",
                    required = true
            )
            @PathVariable String key) {
        return replicaRepository.findByKey(key)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REPLICATION STATS
    // ─────────────────────────────────────────────────────────────────────────

    @Tag(name = "Replication Audit")
    @Operation(
            summary = "Get replication event statistics",
            description = """
                Returns a summary count of all Kafka events processed by this service,
                grouped by outcome.
                
                **Status meanings:**
                
                | Status | Meaning |
                |--------|---------|
                | `success` | Event was received, processed, and applied to replication_db |
                | `failed` | Event processing threw an exception. Kafka will redeliver. |
                | `skippedDuplicate` | EventId already existed in replication_log — safely skipped |
                
                **Healthy system shows:**
                - `success` > 0 (events are flowing and being applied)
                - `failed` = 0 (no processing errors)
                - `skippedDuplicate` ≥ 0 (normal — happens on Kafka redelivery after restart)
                
                **If `failed` > 0:**
                Check replication service logs for the specific error. Common causes:
                - PostgreSQL connection issue
                - Malformed Kafka event (wrong JSON schema)
                - Database constraint violation
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event processing statistics.",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Healthy system",
                                            summary = "All events processed successfully",
                                            value = """
                                {
                                  "success": 15,
                                  "failed": 0,
                                  "skippedDuplicate": 2
                                }
                                """
                                    ),
                                    @ExampleObject(
                                            name = "Fresh start",
                                            summary = "No events processed yet",
                                            value = """
                                {
                                  "success": 0,
                                  "failed": 0,
                                  "skippedDuplicate": 0
                                }
                                """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/replication/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
                "success", logRepository.countByStatus(ReplicationLog.ReplicationStatus.SUCCESS),
                "failed", logRepository.countByStatus(ReplicationLog.ReplicationStatus.FAILED),
                "skippedDuplicate", logRepository.countByStatus(ReplicationLog.ReplicationStatus.SKIPPED_DUPLICATE)
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REPLICATION LOGS
    // ─────────────────────────────────────────────────────────────────────────

    @Tag(name = "Replication Audit")
    @Operation(
            summary = "Get replication audit log (paginated)",
            description = """
                Returns the full audit trail of every Kafka event this service has processed,
                ordered by most recent first.
                
                **Each log entry contains:**
                - `eventId` — the UUID from the original Kafka event (unique per write operation)
                - `kv_key` — which key this event was about
                - `operation` — PUT, UPDATE, or DELETE
                - `sourceNode` — which KV node published the event
                - `status` — SUCCESS, FAILED, or SKIPPED_DUPLICATE
                - `errorMessage` — populated only when status=FAILED
                - `processedAt` — when this service processed the event
                
                **Use this log to:**
                - Debug why a key isn't showing up in replica (look for FAILED entries)
                - Confirm the full lifecycle of a key (PUT → UPDATE → DELETE all appear)
                - Verify idempotency (SKIPPED_DUPLICATE entries show safe handling of redelivery)
                - Audit who wrote what and from which node
                
                **The UNIQUE constraint on `event_id`** in this table is the database-level
                guarantee that even if the application-level idempotency check has a race
                condition, duplicate events cannot corrupt the log.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated audit log entries, newest first.",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                            {
                              "content": [
                                {
                                  "id": "abc123",
                                  "eventId": "550e8400-e29b-41d4-a716-446655440001",
                                  "key": "user:1",
                                  "operation": "UPDATE",
                                  "sourceNode": "node-2",
                                  "status": "SUCCESS",
                                  "errorMessage": null,
                                  "processedAt": "2026-06-26T12:10:00"
                                },
                                {
                                  "id": "abc122",
                                  "eventId": "550e8400-e29b-41d4-a716-446655440000",
                                  "key": "user:1",
                                  "operation": "PUT",
                                  "sourceNode": "node-1",
                                  "status": "SUCCESS",
                                  "errorMessage": null,
                                  "processedAt": "2026-06-26T12:00:01"
                                }
                              ],
                              "totalElements": 2,
                              "totalPages": 1,
                              "number": 0,
                              "size": 20
                            }
                            """
                            )
                    )
            )
    })
    @GetMapping("/replication/logs")
    public ResponseEntity<Page<ReplicationLog>> getLogs(
            @Parameter(description = "Page number, zero-based", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of log entries per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                logRepository.findAllByOrderByProcessedAtDesc(PageRequest.of(page, size))
        );
    }
}