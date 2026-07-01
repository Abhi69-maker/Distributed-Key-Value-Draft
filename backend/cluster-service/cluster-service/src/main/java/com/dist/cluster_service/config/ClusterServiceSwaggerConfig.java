package com.dist.cluster_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ClusterServiceSwaggerConfig {

    @Bean
    public OpenAPI clusterServiceOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("Distributed KV Store - Cluster Service API")
                        .version("1.0.0")
                        .description("""
# 📘 Cluster Service API

The **Cluster Service** is the **nerve center** of the Distributed Key-Value Store.

It tracks node membership, monitors node health via Redis heartbeats, detects
failures automatically, and publishes **Kafka events** whenever a node's status changes.

---

# 🎯 Purpose

This service is responsible for:

- Maintaining a registry of all nodes in the cluster.
- Monitoring node liveness using Redis heartbeat keys.
- Automatically detecting node failures and recoveries.
- Publishing Kafka events to `node-status-events` on every status change.
- Providing APIs for load balancing and service discovery.

---

# 🏗 Architecture

```text
        KV Service Nodes
    node-1 | node-2 | node-3
             │
    Heartbeat keys written
    to Redis every N seconds
             │
             ▼
           Redis
     (node-registry Set)
     (heartbeat:{nodeId} keys)
             │
             ▼
      Cluster Service
    (polls every 5 seconds)
             │
      ┌──────┴──────┐
      ▼             ▼
  Status APIs    Kafka Event
  /cluster/status  node-status-events
  /nodes
```

---

# 🔄 Health Check Workflow

The Cluster Service runs a scheduled job every **5 seconds**:

1. Fetch all registered node IDs from the Redis Set `node-registry`.
2. For each node, check if its heartbeat key `heartbeat:{nodeId}` exists in Redis.
3. Compare current status to the last known status.
4. If status has **changed** (UP → DOWN or DOWN → UP):
   - Log the transition.
   - Publish a Kafka event to `node-status-events`.
   - Update the in-memory status map.

---

# ⚙ Node Status Values

| Status | Meaning |
|--------|---------|
| `UP` | Heartbeat key exists in Redis — node is alive |
| `DOWN` | Heartbeat key expired or missing — node has crashed or restarted |

---

# 📡 Kafka Events Published

**Topic:** `node-status-events`

**Published when:**
- A node transitions from `UP` → `DOWN` (node failure detected).
- A node transitions from `DOWN` → `UP` (node recovery detected).

**Event payload example:**
```json
{
  "nodeId": "node-3",
  "status": "DOWN",
  "timestamp": "2026-07-01T10:05:00"
}
```

**Consumers of this event:**
- Replication Service — stops sending replicas to downed nodes.
- API Gateway — removes downed nodes from load balancing pool.

---

# 🔑 Redis Keys Used

| Key | Type | Purpose |
|-----|------|---------|
| `node-registry` | Set | Contains all registered node IDs |
| `heartbeat:{nodeId}` | String with TTL | Liveness signal per node |

**Heartbeat TTL:** Each KV node refreshes its heartbeat key every few seconds.
If a node crashes, the key expires and the Cluster Service marks it `DOWN`.

---

# 🗄 Node Registration

Nodes register themselves by:

1. Adding their `nodeId` to the Redis Set `node-registry` on startup.
2. Starting a background task to refresh `heartbeat:{nodeId}` every N seconds.

The Cluster Service does **not** manage registration directly —
it only reads from Redis and reports what it finds.

---

# ⚙ Supported Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/cluster/status` | Full cluster health map (all nodes) |
| GET | `/api/v1/nodes` | Active nodes only (UP status) |

---

# 💡 Use Cases for `/nodes` Endpoint

| Consumer | How it uses active nodes |
|----------|--------------------------|
| API Gateway | Routes requests only to alive nodes |
| Replication Service | Sends replication only to healthy nodes |
| Monitoring Dashboard | Displays cluster health in real time |
| Load Balancer | Selects target node for write operations |

---

# ⚠ Failure Scenarios

| Scenario | Cluster Service Behavior |
|----------|--------------------------|
| Node crashes | Heartbeat expires → status → `DOWN` → Kafka event published |
| Node restarts | Heartbeat refreshed → status → `UP` → Kafka event published |
| Redis unavailable | Cluster Service cannot determine status — all nodes appear `DOWN` |
| Kafka unavailable | Status change detected but event publish fails — logged as error |

---

# ℹ Service Information

**Application:** Cluster Service

**Version:** 1.0.0

**Redis Keys:** `node-registry`, `heartbeat:{nodeId}`

**Kafka Topic Published:** `node-status-events`

**Scheduled Poll Interval:** Every 5 seconds

**Default Port:** `8083`

---

> **Note**
>
> The Cluster Service is **read-only** with respect to node data.
> It observes Redis heartbeat keys written by KV nodes themselves.
> It does **not** start, stop, or restart nodes.
> It only detects and reports their status.
""")
                        .contact(new Contact()
                                .name("Distributed KV Store")
                                .url("https://github.com/kuderella-abhilash/Distributed-Key-Value-Draft")))

                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Cluster Service"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway")
                ))

                .tags(List.of(
                        new Tag()
                                .name("Cluster Management")
                                .description("Node registration, discovery, and health monitoring. Uses Redis heartbeat keys to track node liveness and publishes Kafka events on status changes.")
                ));
    }
}