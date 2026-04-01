package ru.yandex.practicum.kafka.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.Properties;

@Component
public class HubEventConsumer {
    private Consumer<String, HubEventAvro> consumer;

    @Value("${spring.kafka.consumer.hub.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.hub.group.id}")
    private String group;
    @Value("${spring.kafka.consumer.hub.key-deserializer}")
    private String keyDeserializer;
    @Value("${spring.kafka.consumer.hub.value-deserializer}")
    private String valueDeserializer;
    @Value("${spring.kafka.consumer.hub.enable.auto.commit}")
    private Boolean autoCommit;
    @Value("${spring.kafka.consumer.hub.auto.offset.reset}")
    private String autoOffset;

    public Consumer<String, HubEventAvro> getConsumer() {
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