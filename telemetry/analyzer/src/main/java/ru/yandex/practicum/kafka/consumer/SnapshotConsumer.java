package ru.yandex.practicum.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class SnapshotConsumer {
    private final KafkaConfig kafkaConfig;
    private Consumer<String, SensorsSnapshotAvro> consumer;

    public Consumer<String, SensorsSnapshotAvro> getConsumer() {
        if (consumer == null) {
            initConsumer();
        }
        return consumer;
    }

    private void initConsumer() {
        Properties config = kafkaConfig.getSnapshotConsumerProperties();
        consumer = new KafkaConsumer<>(config);
    }
}