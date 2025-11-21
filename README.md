# EG/2020/4162 
Big Data Kafka Order Messages Assignment

This project demonstrates an asynchronous order processing system using Apache Kafka, Zookeeper, Schema Registry, and Kafka UI. It includes a producer that generates orders, a consumer that processes them with retry logic and dead-letter queue (DLQ) handling, and a DLQ consumer for monitoring failed messages.

## Architecture

- **Zookeeper**: Manages Kafka cluster coordination.
- **Kafka Broker**: Handles message publishing and subscribing.
- **Schema Registry**: Manages Avro schemas for data serialization.
- **Kafka UI**: Web interface for monitoring Kafka topics and messages.
- **Producer**: Generates random orders and sends them to the "orders" topic.
- **Consumer**: Processes orders from the "orders" topic, performs aggregation, and handles failures by retrying or sending to DLQ.
- **DLQ Consumer**: Consumes failed messages from the "orders-dlq" topic for monitoring.

## Technologies Used

- Java 17
- Apache Kafka
- Apache Avro
- Confluent Schema Registry
- Docker & Docker Compose
- Maven

## Prerequisites

- Docker and Docker Compose installed on your system.

## Setup and Running

Clone the repository:

```
git clone https://github.com/bawantha395/4162-big-data-kafka-order-messages.git
cd 4162-big-data-kafka-order-messages
```

Start the infrastructure:

```
docker-compose up -d zookeeper broker schema-registry kafka-ui
```

This starts Zookeeper, Kafka broker, Schema Registry, and Kafka UI.

Build and run the applications:

- **Producer**:
  ```
  

```
docker-compose up --build
```

### View Logs

In separate terminal windows:

```
docker compose logs producer --follow --tail=10
docker compose logs consumer --follow --tail=10
docker compose logs dlq-consumer --follow --tail=10
```

Or all together:

```
docker compose logs producer consumer dlq-consumer --follow --tail=20
```

Open Docker Desktop and navigate to containers/apps to view logs.

### Access Kafka UI

Open http://localhost:8082 in your browser to monitor topics, messages, and consumers.

## How It Works

- The **Producer** generates orders with random products and prices every 10 seconds. It uses Avro serialization and sends to the "orders" topic.
- The **Consumer** subscribes to "orders", processes each order, and computes a running average price. For "FailItem" products, it simulates failure, retries up to 3 times, and if still failing, sends the message to the "orders-dlq" topic.
- The **DLQ Consumer** listens to "orders-dlq" and logs failed orders for manual review.

## Configuration

- Kafka Bootstrap Servers: broker:29092 (internal), localhost:9092 (external)
- Schema Registry: http://schema-registry:8081
- Topics: orders, orders-dlq

## Project Files

- `pom.xml`: Maven configuration with dependencies and build plugins.
- `Dockerfile`: Multi-stage build for creating the JAR.
- `docker-compose.yml`: Defines all services.
- `src/main/resources/avro/order.avsc`: Avro schema for Order.
- `src/main/java/com/assignment/OrderProducer.java`: Producer implementation.
- `src/main/java/com/assignment/OrderConsumer.java`: Consumer with retry and aggregation.
- `src/main/java/com/assignment/OrderDLQConsumer.java`: DLQ monitor.