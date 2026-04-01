package ru.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Slf4j
@Component
public class SensorsSnapshotProducer {
    private Producer<String, SensorsSnapshotAvro> producer;

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.producer.key-serializer}")
    private String ketSerializer;
    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;
    @Value("${spring.kafka.producer.properties.enable.idempotence}")
    private Boolean enableIdempotence;
    @Value("${spring.kafka.producer.properties.acks}")
    private String acks;
    @Value("${spring.kafka.producer.properties.batch.size}")
    private Integer batchSize;
    @Value("${spring.kafka.producer.properties.linger.ms}")
    private Integer lingerMs;

    public Producer<String, SensorsSnapshotAvro> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    private void initProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ketSerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,enableIdempotence);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        config.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);

        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(config);
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