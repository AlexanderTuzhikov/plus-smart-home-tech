package ru.yandex.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "analyzer.kafka")
@Getter
@Setter
public class KafkaConfig {
    private Map<String, String> eventConsumerConfig;
    private Map<String, String> snapshotConsumerConfig;

    public Properties getEventConsumerProperties() {
        Properties properties = new Properties();
        properties.putAll(eventConsumerConfig);
        return properties;
    }

    public Properties getSnapshotConsumerProperties() {
        Properties properties = new Properties();
        properties.putAll(snapshotConsumerConfig);
        return properties;
    }
}
