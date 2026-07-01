package com.dist.key_value_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class KVServiceSwaggerConfig {

    @Bean
    public OpenAPI kvServiceOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("Distributed KV Store - Key Value Service API")
                        .version("1.0.0")
                        .description("""
# 📘 Key Value Service API

The **Key Value Service** is the **primary write node** of the Distributed Key-Value Store.

It handles all CRUD operations, maintains data in **PostgreSQL**, caches reads in **Redis**,
and publishes **Kafka events** so the Replication Service can synchronize replica nodes.

---

# 🎯 Purpose

This service is responsible for:

- Accepting all client write and read operations.
- Persisting data durably in PostgreSQL.
- Caching frequently accessed keys in Redis for low-latency reads.
- Publishing Kafka events on every mutation for event-driven replication.
- Enforcing key uniqueness and optimistic version tracking.

---

# 🏗 Architecture

```text
          Client
             │
             ▼
     API Gateway (port 8080)
             │
             ▼
     KV Service (Primary)
       │           │
       ▼           ▼
  PostgreSQL     Redis
   (kv_db)     (Cache)
       │
       ▼
   Kafka Topics
       │
       ├── kv-create-events
       ├── kv-update-events
       └── kv-delete-events
             │
             ▼
    Replication Service
    (replica nodes sync)
```

---

# 🔄 Write Flow

Every write operation follows this exact sequence:

1. Validate the incoming request body.
2. Check business rules (key uniqueness for CREATE).
3. Persist the change to **PostgreSQL** (`kv_db`).
4. Update or evict the **Redis** cache entry.
5. Publish a **Kafka event** with a unique `eventId` for replication.

---

# 📖 Read Flow (Cache-Aside Pattern)

Every GET request follows this sequence:

1. Check **Redis** for the key.
2. **Cache HIT** → Return immediately (sub-millisecond latency).
3. **Cache MISS** → Query **PostgreSQL**, store result in Redis, return response.

---

# ⚙ Supported Operations

| Operation | Method | Endpoint | Description |
|-----------|--------|----------|-------------|
| Create | POST | `/api/v1/keys/create` | Store a new key-value pair |
| Read | GET | `/api/v1/keys/{key}` | Retrieve value by key |
| Update | PUT | `/api/v1/keys/{key}` | Update value, increment version |
| Delete | DELETE | `/api/v1/keys/delete/{key}` | Remove key from store |
| List All | GET | `/api/v1/keys` | Paginated list of all keys |
| Exists | GET | `/api/v1/keys/exists/{key}` | Check if a key exists |

---

# 🔒 Optimistic Versioning

Every key-value record includes a `version` field.

- Starts at `1` on creation.
- Auto-incremented on every UPDATE.
- Included in Kafka events so replica nodes can track mutation order.
- Useful for detecting out-of-order event processing in replicas.

---

# 📡 Kafka Topics Published

| Topic | Trigger |
|-------|---------|
| `kv-create-events` | POST /create |
| `kv-update-events` | PUT /{key} |
| `kv-delete-events` | DELETE /delete/{key} |

Every event contains:

- `eventId` — globally unique UUID for idempotent replication.
- `operation` — CREATE, UPDATE, or DELETE.
- `key` — the affected key.
- `value` — the new value (null for DELETE).
- `version` — the version number after the operation.
- `nodeId` — which node processed the write.

---

# 🗄 Database Schema

### key_value table (`kv_db`)

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Primary key |
| `key` | VARCHAR | Unique key identifier |
| `value` | TEXT | Stored value |
| `version` | BIGINT | Optimistic version counter |
| `node_id` | VARCHAR | Node that wrote this record |
| `created_at` | TIMESTAMP | Record creation time |
| `updated_at` | TIMESTAMP | Last modification time |

---

# ⚡ Redis Cache Behavior

| Operation | Cache Action |
|-----------|-------------|
| CREATE | Write-Through — entry added to Redis |
| GET (hit) | Return from Redis directly |
| GET (miss) | Query PostgreSQL, populate Redis |
| UPDATE | Evict stale entry from Redis |
| DELETE | Evict entry from Redis |

**Cache TTL:** Configured per environment. Default: 10 minutes.

---

# 🔁 Fail-Open Architecture

If Redis becomes unavailable:

- Reads fall back to PostgreSQL automatically.
- Writes continue to PostgreSQL without caching.
- The system remains operational — Redis failure does not cause downtime.

---

# ⚠ Error Handling

| HTTP Status | Cause |
|-------------|-------|
| `400` | Invalid request body — missing key or value |
| `404` | Key not found in PostgreSQL |
| `409` | Key already exists (duplicate CREATE) |
| `500` | Unexpected server error |

---

# ℹ Service Information

**Application:** Key Value Service

**Version:** 1.0.0

**Database:** `kv_db` (PostgreSQL)

**Cache:** Redis

**Kafka Topics:** `kv-create-events`, `kv-update-events`, `kv-delete-events`

**Default Port:** `8081` (Node 1), `8085` (Node 2), `8086` (Node 3)

**Node IDs:** `node-1`, `node-2`, `node-3`

---

> **Note**
>
> This is the **primary write service**. All writes should go here.
> The Replication Service at port `8082` provides read replicas
> that lag by approximately **100–500 ms**.
""")
                        .contact(new Contact()
                                .name("Distributed KV Store")
                                .url("https://github.com/kuderella-abhilash/Distributed-Key-Value-Draft")))

                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("KV Service - Node 1"),
                        new Server()
                                .url("http://localhost:8085")
                                .description("KV Service - Node 2"),
                        new Server()
                                .url("http://localhost:8086")
                                .description("KV Service - Node 3"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway")
                ))

                .tags(List.of(
                        new Tag()
                                .name("Key-Value Store")
                                .description("Core CRUD endpoints. All writes persist to PostgreSQL, cache to Redis, and publish Kafka replication events.")
                ));
    }
}