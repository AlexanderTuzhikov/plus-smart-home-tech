package ru.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Properties;

@ConfigurationProperties(prefix = "collector.kafka")
@Getter
@Setter
public class KafkaConfig {
    private Map<String, String> eventProducerConfig;

    public Properties getProducerProperties() {
        Properties properties = new Properties();
        properties.putAll(eventProducerConfig);
        return properties;
    }
}
