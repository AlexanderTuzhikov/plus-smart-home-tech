package ru.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.EventProducer;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    @Value("${telemetry.hubs.v1.topic}")
    private String hubEventsTopic;
    private final EventProducer producer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        DeviceTypeAvro deviceTypeAvro = DeviceTypeAvro.valueOf(event.getDeviceAdded().getType().name());

        DeviceAddedEventAvro deviceAddedEventAvro = DeviceAddedEventAvro.newBuilder()
                .setId(event.getDeviceAdded().getId())
                .setType(deviceTypeAvro)
                .build();

        HubEventAvro hubEventAvro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(deviceAddedEventAvro)
                .build();

        producer.sendMessage(hubEventsTopic, hubEventAvro.getHubId(), hubEventAvro);
    }
}
