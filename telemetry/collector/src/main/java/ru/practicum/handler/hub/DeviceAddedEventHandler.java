package ru.practicum.handler.hub;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.EventProducer;
import ru.practicum.kafka.TopicsConfig;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final TopicsConfig topicsConfig;
    private final EventProducer producer;
    private String hubEventsTopic;

    @PostConstruct
    public void initTopic() {
        hubEventsTopic = topicsConfig.getHubs();
    }

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