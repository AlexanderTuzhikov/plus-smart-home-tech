package ru.yandex.practicum.kafka.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Component
public class SnapshotConsumer {
    private Consumer<String, SensorsSnapshotAvro> consumer;

    @Value("${spring.kafka.consumer.sensor.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.sensor.group.id}")
    private String group;
    @Value("${spring.kafka.consumer.sensor.key-deserializer}")
    private String keyDeserializer;
    @Value("${spring.kafka.consumer.sensor.value-deserializer}")
    private String valueDeserializer;
    @Value("${spring.kafka.consumer.sensor.enable.auto.commit}")
    private Boolean autoCommit;
    @Value("${spring.kafka.consumer.sensor.auto.offset.reset}")
    private String autoOffset;

    public Consumer<String, SensorsSnapshotAvro> getConsumer() {
        if (consumer == null) {
            initConsumer();
        }
        return consumer;
    }

    private void initConsumer() {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffset);

        consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(config);
    }
}