package ru.practicum.handler.sensor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.EventProducer;
import ru.practicum.kafka.TopicsConfig;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SwitchSensorEventHandler implements SensorEventHandler {
    private final TopicsConfig topicsConfig;
    private final EventProducer producer;
    private String sensorEventsTopic;

    @PostConstruct
    public void initTopic() {
        sensorEventsTopic = topicsConfig.getSensors();
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        SwitchSensorAvro switchSensorAvro = SwitchSensorAvro.newBuilder()
                .setState(event.getSwitchSensor().getState())
                .build();

        SensorEventAvro sensorEventAvro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(switchSensorAvro)
                .build();

        producer.sendMessage(sensorEventsTopic, sensorEventAvro.getHubId(), sensorEventAvro);
    }
}