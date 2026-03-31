package ru.practicum.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.EventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ClimateSensorEventHandler implements SensorEventHandler {
    @Value("${telemetry.sensors.v1.topic}")
    private String sensorEventsTopic;
    private final EventProducer producer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        ClimateSensorAvro climateSensorAvro = ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getClimateSensor().getTemperatureC())
                .setHumidity(event.getClimateSensor().getHumidity())
                .setCo2Level(event.getClimateSensor().getCo2Level())
                .build();

        SensorEventAvro sensorEventAvro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(climateSensorAvro)
                .build();

        producer.sendMessage(sensorEventsTopic, sensorEventAvro.getHubId(), sensorEventAvro);
    }
}