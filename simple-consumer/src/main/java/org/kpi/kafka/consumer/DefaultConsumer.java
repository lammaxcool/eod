package org.kpi.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class DefaultConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConsumer.class);

    public static void main(String[] args) {
        String bootstrapServers = "localhost:19092,localhost:29092,localhost:39092";
        String groupId = "default-consumer-group";
        String topic = "ORDERS_ENRICHED";

        var consumer = buildKafkaConsumer(bootstrapServers, groupId);

        // get a reference to the current thread
        final Thread mainThread = Thread.currentThread();

        addShutdownHook(consumer, mainThread);

        try {
            LOGGER.info("Consumer started");
            // subscribe consumer to our topic(s)
            consumer.subscribe(List.of(topic));

            // poll for new data
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    LOGGER.info("Key: " + record.key() + ", Value: " + record.value());
                    LOGGER.info("Partition: " + record.partition() + ", Offset:" + record.offset());
                }
            }

        } catch (WakeupException e) {
            LOGGER.info("Wake up exception!");
        } catch (Exception e) {
            LOGGER.error("Unexpected exception", e);
        } finally {
            consumer.close(); // this will also commit the offsets if need be.
            LOGGER.info("The consumer is now gracefully closed.");
        }

    }

    private static void addShutdownHook(KafkaConsumer<String, String> consumer, Thread mainThread) {
        // adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Detected a shutdown, let's exit by calling consumer.wakeup()...");
            consumer.wakeup();

            // join the main thread to allow the execution of the code in the main thread
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                LOGGER.error("Error trying to join main thread", e);
            }
        }));
    }

    private static KafkaConsumer<String, String> buildKafkaConsumer(String bootstrapServers, String groupId) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");

        return new KafkaConsumer<>(properties);
    }
}