package ru.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telemetry.topics")
@Getter
@Setter
public class TopicsConfig {
    private String sensors;
    private String hubs;
    private String snapshots;
}
