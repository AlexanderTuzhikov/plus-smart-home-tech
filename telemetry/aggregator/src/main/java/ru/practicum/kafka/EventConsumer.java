package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class EventConsumer {
    private final KafkaConfig kafkaConfig;
    private Consumer<String, SensorEventAvro> consumer;

    public Consumer<String, SensorEventAvro> getConsumer() {
        if (consumer == null) {
            initConsumer();
        }
        return consumer;
    }

    private void initConsumer() {
        Properties config = kafkaConfig.getConsumerProperties();
        consumer = new KafkaConsumer<>(config);
    }
}