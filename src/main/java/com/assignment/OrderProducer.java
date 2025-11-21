package com.assignment;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.util.Properties;
import java.util.Random;

public class OrderProducer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", System.getenv("SCHEMA_REGISTRY_URL"));

        KafkaProducer<String, Order> producer = new KafkaProducer<>(props);
        Random random = new Random();
        int orderId = 1000;
        String[] products = {"Item1", "Item2", "FailItem", "Item3"};

        while (true) {
            Order order = new Order();
            order.setOrderId(String.valueOf(++orderId));
            order.setProduct(products[random.nextInt(products.length)]);
            order.setPrice(random.nextFloat() * 100);

            ProducerRecord<String, Order> record = new ProducerRecord<>("orders", order.getOrderId().toString(), order);
            producer.send(record);
            System.out.println("Sent: " + order);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                break;
            }
        }
        producer.close();
    }
}