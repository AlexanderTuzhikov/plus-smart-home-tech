package ru.practicum.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class EventProducer {
    private final KafkaConfig kafkaConfig;
    private Producer<String, Object> producer;

    public Producer<String, Object> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    private void initProducer() {
        Properties config = kafkaConfig.getProducerProperties();
        producer = new KafkaProducer<>(config);
    }

    public void sendMessage(String topic, String key, Object value) {
        ProducerRecord<String, Object> record =
                new ProducerRecord<>(topic, key, value);

        getProducer().send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Сообщение отправлено: " + metadata.topic() +
                        ", партиция: " + metadata.partition() +
                        ", смещение: " + metadata.offset());
            } else {
                System.err.println("Ошибка отправки: " + exception.getMessage());
            }
        });
    }

    @PreDestroy
    public void destroy() {
        producer.close();
    }
}