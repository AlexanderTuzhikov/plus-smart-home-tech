package ru.practicum.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.KafkaProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MotionSensorEventHandler implements SensorEventHandler {
    @Value("${telemetry.sensors.v1.topic}")
    private String sensorEventsTopic;
    private final KafkaProducer producer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        MotionSensorAvro motionSensorAvro = MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getMotionSensor().getLinkQuality())
                .setMotion(event.getMotionSensor().getMotion())
                .setVoltage(event.getMotionSensor().getVoltage())
                .build();

        SensorEventAvro sensorEventAvro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(motionSensorAvro)
                .build();

        producer.sendMessage(sensorEventsTopic, sensorEventAvro.getHubId(), sensorEventAvro);
    }
}
