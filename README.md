# 🚀 Distributed Key Value Store

<p align="left">

<img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=flat&logo=openjdk&logoColor=white" alt="Java">

<img src="https://img.shields.io/badge/Spring_Boot-6DB33F.svg?style=flat&logo=springboot&logoColor=white" alt="Spring Boot">

<img src="https://img.shields.io/badge/Apache_Kafka-231F20.svg?style=flat&logo=apachekafka&logoColor=white" alt="Kafka">

<img src="https://img.shields.io/badge/Redis-DC382D.svg?style=flat&logo=redis&logoColor=white" alt="Redis">

<img src="https://img.shields.io/badge/PostgreSQL-4169E1.svg?style=flat&logo=postgresql&logoColor=white" alt="PostgreSQL">

<img src="https://img.shields.io/badge/Docker-2496ED.svg?style=flat&logo=Docker&logoColor=white" alt="Docker">

<img src="https://img.shields.io/badge/React-61DAFB.svg?style=flat&logo=react&logoColor=black" alt="React">

<img src="https://img.shields.io/badge/Postman-FF6C37.svg?style=flat&logo=postman&logoColor=white" alt="Postman">

<img src="https://img.shields.io/badge/JUnit-25A162.svg?style=flat&logo=junit5&logoColor=white" alt="JUnit">

<img src="https://img.shields.io/badge/Mockito-78A641.svg?style=flat&logoColor=white" alt="Mockito">

</p>



---

# 🌟 Project Overview

Distributed Key Value Store is a portfolio-focused distributed systems project inspired by modern distributed databases and coordination systems such as:

* Redis
* DynamoDB
* Cassandra
* etcd
* Consul

This project demonstrates real-world backend engineering concepts including:

✅ Distributed Systems

✅ Event Driven Architecture

✅ Data Replication

✅ Fault Tolerance

✅ Caching

✅ Kafka Messaging

✅ Redis Integration

✅ Monitoring & Observability

✅ Dockerized Deployment

✅ Spring Boot Microservices

---

# 🎯 Project Goals

The purpose of this project is educational.

This system is designed to help developers understand:

* How distributed systems work
* How data replication is implemented
* How caching improves performance
* How event-driven architectures operate
* How monitoring and observability are implemented
* How microservices communicate

---

# 🏗️ High Level Architecture

```
                ┌──────────────────┐
                │ React Dashboard  │
                └─────────┬────────┘
                          │
                          ▼
                ┌──────────────────┐
                │   API Gateway    │
                └─────────┬────────┘
                          │
         ┌────────────────┼────────────────┐
         ▼                ▼                ▼
  ┌───────────┐    ┌───────────┐    ┌───────────┐
  │ Node 1    │    │ Node 2    │    │ Node 3    │
  │ :8081     │    │ :8082     │    │ :8083     │
  └─────┬─────┘    └─────┬─────┘    └─────┬─────┘
        │                │                │
        └────────┬───────┴────────┬───────┘
                 ▼                ▼
            ┌──────────┐    ┌──────────┐
            │  Kafka   │    │  Redis   │
            └────┬─────┘    └────┬─────┘
                 ▼                ▼
              PostgreSQL Database
```

---

# 🔥 Core Features

### Key Operations

* Store Key Value Pair
* Retrieve Key Value Pair
* Update Key Value Pair
* Delete Key Value Pair

### Distributed Features

* Multi Node Cluster
* Data Replication
* Event Driven Synchronization
* Node Health Monitoring
* Distributed Caching

### Monitoring Features

* Prometheus Metrics
* Grafana Dashboards
* Cluster Health Status
* Replication Metrics

---

# 🧩 System Components

## API Gateway

Responsibilities:

* Request Routing
* Load Balancing
* Authentication
* Centralized Entry Point

Technology:

* Spring Cloud Gateway

---

## Node Service

Each node is an independent Spring Boot application.

Example:

Node 1 → Port 8081

Node 2 → Port 8082

Node 3 → Port 8083

Responsibilities:

* Data Storage
* Kafka Event Publishing
* Kafka Event Consumption
* Redis Integration
* Replication

---

## PostgreSQL

Stores:

* Key Value Data
* Audit Logs
* Metrics
* Node Status

---

## Redis

Used for:

* Read Through Cache
* Write Through Cache
* Fast Data Retrieval
* TTL Management

---

## Kafka

Used for:

* Replication Events
* Event Streaming
* Node Synchronization

---

## Monitoring Stack

Prometheus

Grafana

Spring Boot Actuator

---

# 🔄 Replication Workflow

Step 1

Client sends request

PUT /store

Step 2

Node 1 stores data in PostgreSQL

Step 3

Node 1 updates Redis cache

Step 4

Node 1 publishes Kafka event

Topic:

kv-replication-events

Step 5

Node 2 consumes event

Step 6

Node 3 consumes event

Step 7

All nodes become synchronized

---

# ⚡ Cache Flow

Request

GET /store/user1

Redis Hit

↓

Return Immediately

Redis Miss

↓

Read PostgreSQL

↓

Update Redis

↓

Return Response

---

# 📡 Kafka Topics

| Topic              | Purpose            |
| ------------------ | ------------------ |
| kv-create-events   | Create operations  |
| kv-update-events   | Update operations  |
| kv-delete-events   | Delete operations  |
| node-status-events | Cluster monitoring |
| audit-events       | Logging            |

---

# 🗄️ Database Tables

### KEY_VALUE

Stores actual data.

### NODE_STATUS

Stores cluster node information.

### AUDIT_LOG

Stores operation history.

### METRICS

Stores monitoring information.

---

# 📂 Project Structure

distributed-key-value-store/

├── gateway-service

├── node-service

├── frontend-dashboard

├── docker

├── monitoring

├── docs

├── scripts

└── README.md

---

# 🖥️ Frontend Dashboard

Pages:

### Dashboard

Cluster Overview

### Key Browser

Search Keys

### Metrics

Performance Monitoring

### Kafka Events

Replication Visualization

### Cluster Status

Node Health

---

# 🐳 Docker Deployment

Containers

* gateway
* node1
* node2
* node3
* postgres
* redis
* zookeeper
* kafka
* prometheus
* grafana
* frontend

---

# 📈 Monitoring Metrics

Tracked Metrics:

* Request Count
* Replication Count
* Cache Hit Ratio
* Cache Miss Ratio
* Kafka Event Count
* Node Health
* Storage Utilization

---

# 🧪 Testing Strategy

### Unit Testing

JUnit

Mockito

### Integration Testing

Spring Boot Test

Testcontainers

### API Testing

Postman

### Load Testing

JMeter

### Failure Testing

Node Shutdown Simulation

Kafka Failure Simulation

Redis Failure Simulation

---

# 🛣️ Development Roadmap

### Phase 1

Single Node + PostgreSQL

### Phase 2

Redis Integration

### Phase 3

Kafka Integration

### Phase 4

Replication

### Phase 5

Multi Node Cluster

### Phase 6

API Gateway

### Phase 7

Monitoring

### Phase 8

React Dashboard

### Phase 9

Dockerization

### Phase 10

Production Simulation

---

# 🎓 Learning Outcomes

After completing this project you will understand:

* Distributed Systems
* Microservices
* Event Driven Architecture
* Kafka
* Redis
* Spring Boot
* Docker
* Monitoring
* Fault Tolerance
* System Design

---

# 💼 Resume Description

Designed and developed a Distributed Key Value Store inspired by Redis, DynamoDB, Cassandra, etcd, and Consul using Java 21, Spring Boot 3, Kafka, Redis, PostgreSQL, Docker, Prometheus, Grafana, and React. Implemented distributed replication, event-driven synchronization, caching strategies, fault tolerance, monitoring, and multi-node cluster communication.

---

# 🚀 Future Enhancements

* Consistent Hashing
* Leader Election
* Raft Consensus Algorithm
* gRPC Communication
* Kubernetes Deployment
* Distributed Transactions
* Snapshotting
* WAL (Write Ahead Logging)
* Multi Region Replication
* Cloud Deployment

---

# ⭐ Star This Repository

If you found this project useful, consider giving it a star and following the development journey.

Happy Coding 🚀
