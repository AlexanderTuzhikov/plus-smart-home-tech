package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProducer {
    private final KafkaConfig kafkaConfig;
    private Producer<String, SensorsSnapshotAvro> producer;

    public Producer<String, SensorsSnapshotAvro> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    private void initProducer() {
        Properties config = kafkaConfig.getProducerProperties();
        producer = new KafkaProducer<>(config);
    }

    public void sendMessage(String topic, String key, SensorsSnapshotAvro value) {
        ProducerRecord<String, SensorsSnapshotAvro> record =
                new ProducerRecord<>(topic, key, value);

        getProducer().send(record, (metadata, exception) -> {
            if (exception == null) {
                log.info("Сообщение отправлено: {}, партиция: {}, смещение: {}", metadata.topic(),
                        metadata.partition(), metadata.offset());
            } else {
                log.error("Ошибка отправки: {}", exception.getMessage());
            }
        });
    }
}