package ru.practicum.kafka;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class TelemetryProducer {
    private Producer<String, Object> producer;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.key-serializer}")
    private String keySerializer;
    @Value("${spring.kafka.value-serializer}")
    private String valueSerializer;
    @Value("${spring.kafka.producer.properties.enable.idempotence}")
    private Boolean enableIdempotence;
    @Value("${spring.kafka.producer.properties.acks}")
    private String acks;
    @Value("${spring.kafka.producer.properties.batch.size}")
    private Integer batchSize;
    @Value("${spring.kafka.producer.properties.linger.ms}")
    private Integer lingerMs;



    public Producer<String, Object> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    private void initProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,enableIdempotence);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        config.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);

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
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
