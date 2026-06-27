package com.dist.replication_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI replicationServiceOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("Distributed KV Store - Replication Service API")
                        .version("1.0.0")
                        .description("""
# 📘 Replication Service API

The **Replication Service** maintains a **read-only replica** of the Distributed Key-Value Store.

It continuously consumes events from **Apache Kafka**, applies them to the replica database, and exposes replicated data through REST APIs.

---

# 🎯 Purpose

This service is responsible for:

- Maintaining a synchronized read replica.
- Reducing read traffic on the primary KV Service.
- Providing eventual consistency.
- Keeping an audit history of processed events.
- Preventing duplicate event processing.

---

# 🏗 Architecture

```text
          Client
             │
             ▼
     KV Service (Primary)
             │
   PUT / UPDATE / DELETE
             │
             ▼
     Kafka Topic: kv-events
             │
      Consumer Group
    replication-group
             │
             ▼
    Replication Service
       │
       ├── Duplicate Check
       ├── Apply Changes
       └── Write Audit Log
             │
             ▼
      replication_db
```

---

# 🔄 Replication Workflow

Every write operation performed by the KV Service generates a **KvEvent**.

The Replication Service processes each event in the following order:

1. Consume event from Kafka.
2. Check whether the **eventId** already exists.
3. Ignore duplicate events.
4. Apply the database operation.
5. Store processing information in **replication_log**.

---

# ⚙ Supported Operations

- **PUT** → Create a new replicated key-value pair.
- **UPDATE** → Update an existing replicated value.
- **DELETE** → Remove the replicated record.

---

# 🔒 Idempotency Guarantee

Every Kafka event contains a globally unique **eventId**.

Before processing an event:

- Search for the event in **replication_log**.
- If found, skip processing.
- Otherwise apply the change.
- Store the event in **replication_log**.

A **UNIQUE constraint** on `event_id` provides database-level protection against duplicate processing.

---

# 🗄 Database Tables

### replicated_key_value

Stores the replicated copy of the KV data.

### replication_log

Stores audit information for every processed event.

Each log includes:

- Event ID
- Operation
- Processing Status
- Timestamp
- Error Message (if any)

---

# 📡 Kafka Configuration

**Topic**

`kv-events`

**Consumer Group**

`replication-group`

**Delivery Guarantee**

At-Least-Once Delivery

**Duplicate Protection**

Idempotent Processing

---

# ⏱ Consistency Model

This service follows **Eventual Consistency**.

Typical replication delay:

- **100–500 ms**

During this period the replica may temporarily lag behind the primary database.

---

# 📂 REST API Modules

## Replica Data

Endpoints for querying replicated key-value records.

## Replication Audit

Endpoints for:

- Replication history
- Event logs
- Processing statistics
- Duplicate detection

---

# ⚠ Error Handling

The service automatically handles:

- Duplicate Kafka messages
- Consumer restarts
- Database transaction rollback
- Retry after failures
- Concurrent event processing

---

# ℹ Service Information

**Application**

Replication Service

**Version**

1.0.0

**Database**

replication_db

**Kafka Topic**

kv-events

**Port**

8082

**Replica Type**

Read Replica

---

> **Note**
>
> Replication is asynchronous. Newly written data may take up to **500 ms**
> before becoming visible in the replica.
""")

                        .contact(new Contact()
                                .name("Distributed KV ")
                                .url("https://github.com/kuderella-abhilash/Distributed-Key-Value-Draft")))

                .servers(List.of(

                        new Server()
                                .url("http://localhost:8082")
                                .description("Replication Service"),

                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway")

                ))

                .tags(List.of(

                        new Tag()
                                .name("Replica Data")
                                .description("Endpoints for querying replicated key-value records from the replica database."),

                        new Tag()
                                .name("Replication Audit")
                                .description("Endpoints for viewing replication logs, processed events, replication status, and audit information.")

                ));

    }

}