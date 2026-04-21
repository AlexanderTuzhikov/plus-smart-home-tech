package ru.yandex.practicum.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class EventConsumer {
    private final KafkaConfig kafkaConfig;
    private Consumer<String, HubEventAvro> consumer;

    public Consumer<String, HubEventAvro> getConsumer() {
        if (consumer == null) {
            initConsumer();
        }
        return consumer;
    }

    private void initConsumer() {
        Properties config = kafkaConfig.getEventConsumerProperties();
        consumer = new KafkaConsumer<>(config);
    }
}