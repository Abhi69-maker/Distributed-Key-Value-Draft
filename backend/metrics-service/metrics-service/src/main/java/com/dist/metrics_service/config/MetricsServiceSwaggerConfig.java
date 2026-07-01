package com.dist.metrics_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MetricsServiceSwaggerConfig {

    @Bean
    public OpenAPI metricsServiceOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("Distributed KV Store - Metrics Service API")
                        .version("1.0.0")
                        .description("""
# 📘 Metrics Service API

The **Metrics Service** is the **observability layer** of the Distributed Key-Value Store.

It aggregates operational data from all services — KV nodes, the Cluster Service,
and the Replication Service — into a single unified view.

It does **not** collect metrics by scraping (like Prometheus).
Instead it consumes **Kafka events** published by other services and also
makes **live HTTP calls** to the Cluster Service and Replication Service
to fetch current state.

---

# 🎯 Purpose

This service is responsible for:

- Recording every KV operation (CREATE, UPDATE, DELETE) via Kafka.
- Recording every node status change (UP/DOWN) via Kafka.
- Aggregating request counts per operation type.
- Aggregating request counts per node per operation.
- Fetching live cluster health from the Cluster Service.
- Fetching live replication stats from the Replication Service.
- Exposing a unified summary dashboard endpoint.

---

# 🏗 Architecture

```text
   KV Service Nodes          Cluster Service
   (publishes Kafka)         (REST /cluster/status)
         │                          │
         ▼                          │
   kv-create-events                 │
   kv-update-events                 │
   kv-delete-events                 │
         │                          │
         ▼                          ▼
   Metrics Service ◄──── HTTP ─────┘
         │
         │  Also consumes:
         ▼
   node-status-events (Kafka)
         │
         ▼
   Replication Service
   (REST /replication/stats)
         │
         ▼
   Metrics Service ◄──── HTTP ────┘
         │
         ▼
   PostgreSQL (metrics_db)
   ┌──────────────┐
   │ request_stat │  ← one row per KV event
   │ metric       │  ← one row per cluster event
   └──────────────┘
         │
         ▼
   REST API exposed
   /api/v1/metrics/**
```

---

# 🔄 Data Collection Workflow

### KV Event Recording

Every time a client writes to the KV Service:

1. KV Service publishes a Kafka event to `kv-create-events`, `kv-update-events`, or `kv-delete-events`.
2. Metrics Service consumes the event.
3. A `RequestStat` row is saved: operation, nodeId, key, timestamp.
4. These rows power `/metrics/requests` and `/metrics/requests/by-node`.

### Cluster Event Recording

Every time a node changes status:

1. Cluster Service detects the change via Redis heartbeat.
2. Cluster Service publishes a Kafka event to `node-status-events`.
3. Metrics Service consumes the event.
4. A `Metric` row is saved: `node.status.changed`, status value, nodeId, timestamp.

### Live Stats Fetching

`/metrics/cluster` and `/metrics/replication` make **live HTTP calls**:

- `GET http://localhost:8083/api/v1/cluster/status` → Cluster Service
- `GET http://localhost:8082/api/v1/replication/stats` → Replication Service

If either service is unreachable, the response includes an `error` field
and defaults all counts to `0`.

---

# ⚙ Supported Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/metrics` | Unified summary — requests + cluster + replication |
| GET | `/api/v1/metrics/requests` | KV operation counts by type |
| GET | `/api/v1/metrics/requests/by-node` | KV operation counts grouped by node and type |
| GET | `/api/v1/metrics/cluster` | Live cluster node health from Cluster Service |
| GET | `/api/v1/metrics/replication` | Live replication stats from Replication Service |

---

# 🗄 Database Schema

### request_stat table

Records every KV operation event consumed from Kafka.

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Primary key |
| `operation` | VARCHAR | CREATE, UPDATE, or DELETE |
| `node_id` | VARCHAR | KV node that performed the operation |
| `key` | VARCHAR | The key that was operated on |
| `recorded_at` | TIMESTAMP | When the event was recorded |

### metric table

Records every node status change event consumed from Kafka.

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Primary key |
| `metric_name` | VARCHAR | e.g. `node.status.changed` |
| `metric_value` | VARCHAR | `UP` or `DOWN` |
| `node_id` | VARCHAR | The node that changed status |
| `recorded_at` | TIMESTAMP | When the event was recorded |

---

# 📡 Kafka Topics Consumed

| Topic | Publisher | Data Recorded |
|-------|-----------|---------------|
| `kv-create-events` | KV Service | RequestStat (operation=CREATE) |
| `kv-update-events` | KV Service | RequestStat (operation=UPDATE) |
| `kv-delete-events` | KV Service | RequestStat (operation=DELETE) |
| `node-status-events` | Cluster Service | Metric (node.status.changed) |

---

# 🌐 Downstream HTTP Calls

| Endpoint Called | Service | Used By |
|----------------|---------|---------|
| `/api/v1/cluster/status` | Cluster Service `:8083` | `/metrics/cluster`, `/metrics` |
| `/api/v1/replication/stats` | Replication Service `:8082` | `/metrics/replication`, `/metrics` |

**Timeout:** 3 seconds per call.

**Failure behavior:** Returns `error` field with message, all counts default to `0`.
The Metrics Service itself remains healthy even if downstream services are down.

---

# 💡 Using the Summary Endpoint

`GET /api/v1/metrics` is the **single dashboard call**.

It combines:
- Request breakdown (from local DB)
- Cluster health (live HTTP to Cluster Service)
- Replication stats (live HTTP to Replication Service)

**Ideal for:**
- Frontend dashboards
- Health check aggregators
- Monitoring scripts

---

# ⚠ Error Handling

| Scenario | Behavior |
|----------|----------|
| Cluster Service unreachable | `cluster.error` field returned, counts = 0 |
| Replication Service unreachable | `replication.error` field returned, counts = 0 |
| Kafka consumer lag | Request counts may temporarily lag behind actual writes |
| Database unavailable | All endpoints return 500 |

---

# ℹ Service Information

**Application:** Metrics Service

**Version:** 1.0.0

**Database:** `metrics_db` (PostgreSQL)

**Kafka Topics Consumed:** `kv-create-events`, `kv-update-events`, `kv-delete-events`, `node-status-events`

**Downstream Services:** Cluster Service (`:8083`), Replication Service (`:8082`)

**Default Port:** `8084`

---

> **Note**
>
> Request counts shown in `/metrics/requests` reflect events **consumed from Kafka**,
> not requests made directly to the KV Service.
> There may be a small delay (typically under 1 second) between a write
> and it appearing in the metrics counts.
""")
                        .contact(new Contact()
                                .name("Distributed KV Store")
                                .url("https://github.com/kuderella-abhilash/Distributed-Key-Value-Draft")))

                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("Metrics Service"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway")
                ))

                .tags(List.of(
                        new Tag()
                                .name("Metrics Summary")
                                .description("Unified dashboard endpoint combining request stats, cluster health, and replication stats into a single response."),
                        new Tag()
                                .name("Request Metrics")
                                .description("KV operation counts recorded from Kafka events. Shows CREATE, UPDATE, DELETE breakdown per operation type and per node."),
                        new Tag()
                                .name("Cluster Metrics")
                                .description("Live cluster health fetched from the Cluster Service. Shows which nodes are UP or DOWN at the time of the call."),
                        new Tag()
                                .name("Replication Metrics")
                                .description("Live replication statistics fetched from the Replication Service. Shows success, failed, and skipped duplicate event counts.")
                ));
    }
}