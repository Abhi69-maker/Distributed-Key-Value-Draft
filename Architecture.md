# Distributed Key-Value Store

A Distributed Key-Value Store built using Spring Boot Microservices, Redis, PostgreSQL, Docker, and React. The project is designed to demonstrate the core concepts of Distributed Systems including Data Replication, Cluster Management, Fault Tolerance, Scalability, High Availability, and System Monitoring.

---

# Table of Contents

- Overview
- Problem Statement
- Project Goals
- System Architecture
- Services Overview
- Request Flow
- Technologies Used
- Design Principles
- Distributed System Concepts
- Features
- Challenges Solved
- Future Enhancements
- Setup Instructions
- Team Responsibilities

---

# Overview

A Key-Value Store is a database that stores data in the form:

```text
key -> value
```

Example:

```text
name -> Abhilash
city -> Hyderabad
age  -> 22
```

Unlike traditional relational databases that store data in tables and relationships, a Key-Value Store focuses on extremely fast data access.

This project implements a distributed version of a Key-Value Store similar to:

- Redis
- Amazon DynamoDB
- Apache Cassandra
- etcd

The system is built using a microservices architecture where each service has a dedicated responsibility.

---

# Problem Statement

Traditional applications often face challenges such as:

- Single Point of Failure
- Limited Scalability
- Data Loss During Server Failure
- Difficult Monitoring
- Tight Coupling Between Components
- Poor Fault Recovery

This project aims to solve these issues by introducing:

- Replication
- Cluster Management
- Service Isolation
- Monitoring
- High Availability

---

# Project Goals

The primary objectives of this project are:

- Build a Distributed Key-Value Store
- Support CRUD Operations
- Implement Data Replication
- Achieve Fault Tolerance
- Enable Horizontal Scalability
- Provide Monitoring and Metrics
- Simulate Real Distributed Database Systems
- Learn Microservices Architecture

---

# System Architecture

```text
                           Client
                              |
                              v
                     API Gateway Service
                              |
         ------------------------------------------------
         |                     |                       |
         v                     v                       v
 Key Value Service     Cluster Service       Metrics Service
         |
         |
         v
 Replication Service
         |
         |
   -------------------
   |        |        |
   v        v        v
 Node-1   Node-2   Node-3
```

---

# Services Overview

## 1. API Gateway Service

### Purpose

Acts as the single entry point for all client requests.

### Responsibilities

- Request Routing
- Authentication
- Authorization
- Logging
- Rate Limiting
- Load Balancing
- Centralized Access Point

### Example

```text
Client
  |
  v
API Gateway
  |
  v
Key Value Service
```

Without an API Gateway, clients would need to communicate directly with each microservice.

---

## 2. Key-Value Service

### Purpose

Core service responsible for storing and retrieving data.

### Responsibilities

- Create Key-Value Pair
- Read Value by Key
- Update Existing Value
- Delete Key-Value Pair
- Redis Integration
- PostgreSQL Persistence

### Supported Operations

#### PUT

```text
name -> Abhilash
```

#### GET

```text
GET(name)
```

Result:

```text
Abhilash
```

#### UPDATE

```text
name -> Rahul
```

#### DELETE

```text
DELETE(name)
```

### Redis Usage

Stores frequently accessed data in memory.

Benefits:

- Extremely Fast Reads
- Low Latency
- High Throughput

### PostgreSQL Usage

Provides durable storage.

Benefits:

- Persistent Data
- Recovery Mechanism
- Backup Storage

---

## 3. Replication Service

### Purpose

Ensures that data is copied across multiple nodes.

### Why Replication?

Without replication:

```text
Node-1
name -> Abhilash
```

If Node-1 crashes:

```text
Data Lost
```

With replication:

```text
Node-1
name -> Abhilash

Node-2
name -> Abhilash

Node-3
name -> Abhilash
```

Data remains available even if a node fails.

### Responsibilities

- Data Replication
- Replica Synchronization
- Recovery Synchronization
- Consistency Maintenance
- Replica Health Tracking

---

## 4. Cluster Service

### Purpose

Manages all nodes participating in the distributed system.

### Responsibilities

#### Node Registration

Registers newly started nodes.

#### Node Discovery

Provides available node information.

#### Health Checks

Verifies node availability.

#### Leader Election

Elects a leader node responsible for write operations.

#### Rebalancing

Distributes data evenly when nodes are added or removed.

### Example

```text
Node-1
Node-2
Node-3
```

Cluster Service continuously tracks their status.

---

## 5. Metrics Service

### Purpose

Monitors the health and performance of the entire system.

### Responsibilities

- Request Monitoring
- Latency Tracking
- Error Monitoring
- Replication Metrics
- CPU Usage
- Memory Usage
- JVM Metrics
- Node Availability

### Example Metrics

```text
GET Requests     : 5000
PUT Requests     : 1200
Average Latency  : 8ms
Replication Rate : 99.8%
```

---

## 6. Frontend Dashboard

### Purpose

Provides a visual interface for managing and monitoring the system.

### Features

- View Stored Keys
- View Cluster Status
- Monitor Metrics
- Track Replication Status
- Display Node Health

---

# Request Flow

Example:

```json
{
  "key": "name",
  "value": "Abhilash"
}
```

Flow:

```text
1. Client sends request

2. API Gateway receives request

3. Gateway forwards request

4. Key Value Service stores data in Redis

5. Data is persisted in PostgreSQL

6. Replication Service copies data to replicas

7. Metrics Service records operation

8. Response returned to client
```

---

# Technologies Used

## Backend

- Java
- Spring Boot
- Spring Cloud Gateway
- Spring Data Redis
- Spring Data JPA
- PostgreSQL

## Distributed Systems

- Redis
- Replication Mechanisms
- Leader Election
- Node Discovery

## Monitoring

- Spring Boot Actuator
- Micrometer
- Prometheus
- Grafana

## DevOps

- Docker
- Docker Compose

## Frontend

- React
- Tailwind CSS
- Axios

---

# Design Principles

The project follows the following design principles:

## Microservices Architecture

Each service performs one dedicated responsibility.

Benefits:

- Independent Deployment
- Easier Maintenance
- Better Scalability

---

## Single Responsibility Principle

Every service focuses on one business function.

Examples:

```text
API Gateway      -> Routing
Key Value        -> Storage
Replication      -> Data Copying
Metrics          -> Monitoring
Cluster          -> Node Management
```

---

## Fault Tolerance

System remains operational even when individual nodes fail.

---

## High Availability

Data remains accessible due to replication.

---

## Scalability

New nodes can be added without downtime.

---

# Distributed System Concepts Implemented

## Replication

Copies data across nodes.

---

## Leader Election

Selects one node as the write leader.

---

## Health Monitoring

Tracks active and inactive nodes.

---

## Service Discovery

Locates available nodes.

---

## Load Distribution

Spreads workload across multiple nodes.

---

## Data Recovery

Restores data after failures.

---

# Features

- CRUD Operations
- Redis Caching
- PostgreSQL Persistence
- Data Replication
- Cluster Management
- Health Checks
- Monitoring Dashboard
- High Availability
- Fault Tolerance
- Dockerized Deployment
- Distributed Architecture

---

# Challenges Solved

## Single Point of Failure

Solved using replication.

---

## Data Loss

Solved through PostgreSQL persistence and replica synchronization.

---

## Server Failure

Replica nodes continue serving requests.

---

## Scalability Issues

Additional nodes can be added dynamically.

---

## Monitoring Complexity

Centralized metrics service simplifies observability.

---

## Tight Coupling

Microservices architecture provides loose coupling.

---

# Future Enhancements

- Consistent Hashing
- Distributed Transactions
- Multi-Leader Replication
- Event-Driven Replication using Kafka
- Raft Consensus Algorithm
- Automatic Failover
- Distributed Locking
- Sharding
- Multi-Region Deployment
- Kubernetes Deployment

---

# Setup Instructions

## Clone Repository

```bash
git clone <repository-url>
```

## Start Services

```bash
docker compose up -d
```

## Verify Containers

```bash
docker ps
```

## Access Services

```text
API Gateway        : localhost:8080
Key Value Service  : localhost:8081
Cluster Service    : localhost:8082
Metrics Service    : localhost:8083
Replication Service: localhost:8084
Frontend           : localhost:3000
```

---

# Team Responsibilities

## Backend Lead

Responsibilities:

- Spring Boot Development
- Redis Integration
- PostgreSQL Integration
- CRUD APIs
- Replication Logic
- Docker Setup
- Distributed System Implementation

## Frontend Lead

Responsibilities:

- React Dashboard
- API Integration
- Metrics Visualization
- Cluster Monitoring UI
- Replication Status View

---

# Educational Value

This project demonstrates practical implementation of:

- Distributed Systems
- Microservices
- Redis
- PostgreSQL
- Replication
- Fault Tolerance
- Scalability
- Monitoring
- Docker
- Cloud-Native Architecture

It serves as an excellent learning project for understanding how modern distributed databases and high-availability systems are designed and implemented.
