package ru.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Properties;

@ConfigurationProperties(prefix = "aggregator.kafka")
@Getter
@Setter
public class KafkaConfig {
    private Map<String, String> consumerConfig;
    private Map<String, String> producerConfig;

    public Properties getConsumerProperties() {
        Properties properties = new Properties();
        properties.putAll(consumerConfig);
        return properties;
    }

    public Properties getProducerProperties() {
        Properties properties = new Properties();
        properties.putAll(producerConfig);
        return properties;
    }
}
