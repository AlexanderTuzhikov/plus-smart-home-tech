package ru.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.EventProducer;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    @Value("${telemetry.hubs.v1.topic}")
    private String hubEventsTopic;
    private final EventProducer producer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        List<ScenarioConditionAvro> condition = Optional.of(event.getScenarioAdded().getConditionList())
                .map(list -> list.stream()
                        .filter(Objects::nonNull) // пропускаем null-элементы
                        .map(this::getConditionsAvro)
                        .toList())
                .orElse(Collections.emptyList());

        List<DeviceActionAvro> actions = Optional.of(event.getScenarioAdded().getActionList())
                .map(list -> list.stream()
                        .filter(Objects::nonNull) // пропускаем null-элементы
                        .map(this::getActionsAvro)
                        .toList())
                .orElse(Collections.emptyList());

        ScenarioAddedEventAvro scenarioAddedEventAvro = ScenarioAddedEventAvro.newBuilder()
                .setName(event.getScenarioAdded().getName())
                .setConditions(condition)
                .setActions(actions)
                .build();

        HubEventAvro hubEventAvro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(scenarioAddedEventAvro)
                .build();

        producer.sendMessage(hubEventsTopic, hubEventAvro.getHubId(), hubEventAvro);
    }

    private ScenarioConditionAvro getConditionsAvro(ScenarioConditionProto scenarioConditionProto) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(scenarioConditionProto.getSensorId())
                .setType(ConditionTypeAvro.valueOf(scenarioConditionProto.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(scenarioConditionProto.getOperation().name()))
                .setValue(extractValue(scenarioConditionProto))
                .build();
    }

    private DeviceActionAvro getActionsAvro(DeviceActionProto deviceActionProto) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(deviceActionProto.getSensorId())
                .setType(ActionTypeAvro.valueOf(deviceActionProto.getType().name()))
                .setValue(deviceActionProto.getValue())
                .build();
    }

    private Object extractValue(ScenarioConditionProto proto) {
        return switch (proto.getValueCase()) {
            case INT_VALUE -> proto.getIntValue();
            case BOOL_VALUE -> proto.getBoolValue();
            case VALUE_NOT_SET -> null;
        };
    }
}