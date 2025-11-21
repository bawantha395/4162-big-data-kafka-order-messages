package com.assignment;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.util.Collections;
import java.util.Properties;
import java.time.Duration;

public class OrderConsumer {
    private static double sum = 0;
    private static int count = 0;

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        props.put("group.id", "order-consumer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", System.getenv("SCHEMA_REGISTRY_URL"));
        props.put("specific.avro.reader", "true");
        props.put("auto.offset.reset", "earliest");

        KafkaConsumer<String, Order> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("orders"));

        // Producer for DLQ
        Properties prodProps = new Properties();
        prodProps.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        prodProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prodProps.put("value.serializer", KafkaAvroSerializer.class.getName());
        prodProps.put("schema.registry.url", System.getenv("SCHEMA_REGISTRY_URL"));

        KafkaProducer<String, Order> dlqProducer = new KafkaProducer<>(prodProps);

        while (true) {
            ConsumerRecords<String, Order> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Order> record : records) {
                Order order = record.value();
                boolean success = processOrder(order);
                if (!success) {
                    // retry
                    int retries = 3;
                    boolean retriedSuccess = false;
                    for (int i = 0; i < retries; i++) {
                        if (processOrder(order)) {
                            retriedSuccess = true;
                            break;
                        }
                    }
                    if (!retriedSuccess) {
                        // send to DLQ
                        ProducerRecord<String, Order> dlqRecord = new ProducerRecord<>("orders-dlq", order.getOrderId().toString(), order);
                        dlqProducer.send(dlqRecord);
                        System.out.println("Sent to DLQ: " + order);
                    } else {
                        success = true;
                    }
                }
                if (success) {
                    sum += order.getPrice();
                    count++;
                    System.out.println("Running average: " + (sum / count));
                }
            }
        }
    }

    private static boolean processOrder(Order order) {
        if ("FailItem".equals(order.getProduct())) {
            System.out.println("Failed to process: " + order);
            return false;
        }
        System.out.println("Processed: " + order);
        return true;
    }
}