package com.assignment;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Collections;
import java.util.Properties;
import java.time.Duration;

public class OrderDLQConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        props.put("group.id", "dlq-consumer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", System.getenv("SCHEMA_REGISTRY_URL"));
        props.put("specific.avro.reader", "true");
        props.put("auto.offset.reset", "earliest");

        KafkaConsumer<String, Order> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("orders-dlq"));

        while (true) {
            ConsumerRecords<String, Order> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Order> record : records) {
                Order order = record.value();
                System.out.println("DLQ Message: " + order);
            }
        }
    }
}