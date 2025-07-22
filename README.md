
# 📦 Microservices in Kafka Event-Driven Architecture

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
