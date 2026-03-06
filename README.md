
# Microservices in Kafka Event-Driven Architecture

This project is a simple example that uses a Spring Boot microservices architecture that uses Apache Kafka for event-driven communication between services. The architecture includes multiple microservices that communicate asynchronously by publishing and consuming events via Kafka topics.
![Logo](microservice_architecture.jpeg)

---

## 1️⃣ Order Service (Microservice 1)

- **Responsibility:** Publishes `OrderEvent` to Kafka topic.
- **Role:** Acts as the **Producer** in the Kafka ecosystem.

---

## 2️⃣ Stock Service (Microservice 2)

- **Responsibility:** Listens to the Kafka topic.
- **Action:** Consumes `OrderEvent` 
- **Role:** Acts as a **Consumer**.

---

## 3️⃣ Email Service (Microservice 3)

- **Responsibility:** Also listens to the same Kafka topic.
- **Action:** Consumes `OrderEvent` to:
  - Send a confirmation email to the customer.
- **Role:** Acts as a **Consumer**.

---

Each of these services runs independently and communicates via events using **Kafka Topics**, allowing for a loosely coupled and scalable architecture.

## Getting Started

### Prerequisites
- **Java 17+**
- **Maven** (or use the included `mvnw` wrapper)
- **Apache Kafka 4.0+** running on `localhost:9092` (KRaft mode — no ZooKeeper needed)

> **Note on `spring-kafka` version:** This project uses Spring Boot `3.5.3`, which bundles `spring-kafka 3.x`. This works with Kafka 4.0 for all basic producer/consumer operations. For full Kafka 4.0 alignment (including KRaft-aware embedded testing), `spring-kafka 4.x` (available via Spring Boot `4.x`) is the officially recommended version.

---

### Step 1 — Start Kafka

> **Kafka 4.0+ uses KRaft mode** — ZooKeeper is no longer required or supported.

**Option A: Homebrew (Mac)**
```bash
# Generate a cluster ID (only needed once)
KAFKA_CLUSTER_ID="$(kafka-storage.sh random-uuid)"

# Format the storage directory
kafka-storage.sh format -t $KAFKA_CLUSTER_ID -c /usr/local/etc/kafka/kraft/server.properties

# Start Kafka (KRaft mode, no ZooKeeper needed)
kafka-server-start /usr/local/etc/kafka/kraft/server.properties
```

**Option B: Docker**
```bash
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  apache/kafka:4.0.0
```

---

### Step 2 — Build the Shared Module

The services depend on `base-domains`, so install it first:
```bash
cd base-domains
./mvnw clean install -DskipTests
```

---

### Step 3 — Run Each Service

Open **three separate terminals**:

```bash
# Terminal 1 — Order Service (port 8080)
cd order-service && ./mvnw spring-boot:run

# Terminal 2 — Stock Service (port 8081)
cd stock-service && ./mvnw spring-boot:run

# Terminal 3 — Email Service (port 8082)
cd email-service && ./mvnw spring-boot:run
```

---

## Testing the Flow

### ✅ Happy Path — Place a Valid Order

```bash
curl -s -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"name": "Book order", "qty": 50, "price": 18000}' | jq
```

Expected response:
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": "Order ID: <uuid>",
  "timestamp": "..."
}
```

Check the terminal logs for:  
`stock-service` – should log `Order event received`  
`email-service` – should log `Order event received`

---

### ❌ Input Validation — Send an Invalid Payload

```bash
curl -s -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"name": "", "qty": -1, "price": -100}' | jq
```

Expected `400 Bad Request`:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "name": "Order name must not be blank",
    "qty": "Quantity must be at least 1",
    "price": "Price must be a positive value"
  }
}
```

---

### 🔁 Idempotency — Duplicate Event Protection

Send the same `orderId` twice — the second should be silently ignored by both consumers:

```bash
curl -s -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId": "test-123", "name": "Duplicate Test", "qty": 1, "price": 100}'

# Send again immediately
curl -s -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId": "test-123", "name": "Duplicate Test", "qty": 1, "price": 100}'
```

On the second event, both consumer logs should show:
```
WARN [stock-service] Duplicate event ignored for orderId: test-123
WARN [email-service] Duplicate event ignored for orderId: test-123
```

---

### 💀 Dead Letter Queue (DLQ) — Simulate a Consumer Failure

1. Temporarily add `throw new RuntimeException("Simulated failure");` at the top of `stock-service`'s `consume()` method.
2. Restart `stock-service` and send a valid order.
3. Watch the logs — you'll see **3 retry attempts** (1 second apart), then the event is forwarded to the DLT topic.
4. Verify the dead-lettered event:

```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order_topics.DLT \
  --from-beginning
```

5. Remove the `throw` and restart the service to restore normal operation.

---

### 📦 Verify Kafka Topic Partitions

After the first startup, confirm `order_topics` was created with 3 partitions:

```bash
kafka-topics.sh --describe \
  --bootstrap-server localhost:9092 \
  --topic order_topics
```

Expected output should show `PartitionCount: 3`.

---

> **Tip:** Install [`jq`](https://jqlang.github.io/jq/) (`brew install jq`) for pretty-printed JSON in curl responses.




