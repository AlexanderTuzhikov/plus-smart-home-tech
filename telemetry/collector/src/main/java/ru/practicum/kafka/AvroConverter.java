package ru.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.model.hub.device.*;
import ru.practicum.model.hub.scenario.*;
import ru.practicum.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class AvroConverter {

    public Object convertToAvro(Object event) {
        log.info("Конвертация в avro объекта {}", event);

        return switch (event) {
            case ClimateSensorEvent e -> convert(e);
            case LightSensorEvent e -> convert(e);
            case MotionSensorEvent e -> convert(e);
            case SwitchSensorEvent e -> convert(e);
            case TemperatureSensorEvent e -> convert(e);
            case DeviceAddedEvent e -> convert(e);
            case DeviceRemovedEvent e -> convert(e);
            case ScenarioCondition e -> convert(e);
            case DeviceAction e -> convert(e);
            case ScenarioAddedEvent e -> convert(e);
            case ScenarioRemovedEvent e -> convert(e);
            default -> throw new IllegalArgumentException(
                    ("Неизвестный тип события: " + event.getClass().getSimpleName())
            );
        };
    }

    private SensorEventAvro convert(ClimateSensorEvent event) {
        ClimateSensorAvro avro = ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setHumidity(event.getHumidity())
                .setCo2Level(event.getCo2Level())
                .build();
        logDoneConvertMessage(avro);

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(avro)
                .build();
    }

    private SensorEventAvro convert(LightSensorEvent event) {
        LightSensorAvro avro = LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();
        logDoneConvertMessage(avro);

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(avro)
                .build();
    }

    private SensorEventAvro convert(MotionSensorEvent event) {
        MotionSensorAvro avro = MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setMotion(event.getMotion())
                .setVoltage(event.getVoltage())
                .build();
        logDoneConvertMessage(avro);

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(avro)
                .build();
    }

    private SensorEventAvro convert(SwitchSensorEvent event) {
        SwitchSensorAvro avro = SwitchSensorAvro.newBuilder()
                .setState(event.getState())
                .build();
        logDoneConvertMessage(avro);

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(avro)
                .build();
    }

    private SensorEventAvro convert(TemperatureSensorEvent event) {
        TemperatureSensorAvro avro = TemperatureSensorAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setTemperatureC(event.getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();
        logDoneConvertMessage(avro);

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(avro)
                .build();
    }

    private HubEventAvro convert(DeviceAddedEvent event) {
        DeviceAddedEventAvro avro = DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(convertToDeviceTypeAvro(event.getDeviceType()))
                .build();
        logDoneConvertMessage(avro);

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(avro)
                .build();
    }

    private HubEventAvro convert(DeviceRemovedEvent event) {
        DeviceRemovedEventAvro avro = DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
        logDoneConvertMessage(avro);

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(avro)
                .build();
    }

    private ScenarioConditionAvro convert(ScenarioCondition event) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(event.getSensorId())
                .setOperation(convertToConditionOperationAvro(event.getOperation()))
                .setType(convertToConditionTypeAvro(event.getType()))
                .setValue(event.getValue())
                .build();
    }

    private DeviceActionAvro convert(DeviceAction event) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(event.getSensorId())
                .setType(convertToActionTypeAvro(event.getType()))
                .setValue(event.getValue())
                .build();
    }

    private HubEventAvro convert(ScenarioAddedEvent event) {
        List<ScenarioConditionAvro> condition = Optional.ofNullable(event.getConditions())
                .map(list -> list.stream()
                        .filter(Objects::nonNull) // пропускаем null-элементы
                        .map(this::convert)
                        .toList())
                .orElse(Collections.emptyList());

        List<DeviceActionAvro> actions = Optional.ofNullable(event.getActions())
                .map(list -> list.stream()
                        .filter(Objects::nonNull) // пропускаем null-элементы
                        .map(this::convert)
                        .toList())
                .orElse(Collections.emptyList());

        ScenarioAddedEventAvro avro = ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(condition)
                .setActions(actions)
                .build();
        logDoneConvertMessage(avro);

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(avro)
                .build();
    }

    private HubEventAvro convert(ScenarioRemovedEvent event) {
        ScenarioRemovedEventAvro avro = ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
        logDoneConvertMessage(avro);

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(avro)
                .build();
    }

    private DeviceTypeAvro convertToDeviceTypeAvro(DeviceType type) {
        return switch (type) {
            case MOTION_SENSOR -> DeviceTypeAvro.MOTION_SENSOR;
            case TEMPERATURE_SENSOR -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            case LIGHT_SENSOR -> DeviceTypeAvro.LIGHT_SENSOR;
            case CLIMATE_SENSOR -> DeviceTypeAvro.CLIMATE_SENSOR;
            case SWITCH_SENSOR -> DeviceTypeAvro.SWITCH_SENSOR;
        };
    }

    private ConditionTypeAvro convertToConditionTypeAvro(ConditionType type) {
        return switch (type) {
            case MOTION -> ConditionTypeAvro.MOTION;
            case LUMINOSITY -> ConditionTypeAvro.LUMINOSITY;
            case SWITCH -> ConditionTypeAvro.SWITCH;
            case TEMPERATURE -> ConditionTypeAvro.TEMPERATURE;
            case CO2LEVEL -> ConditionTypeAvro.CO2LEVEL;
            case HUMIDITY -> ConditionTypeAvro.HUMIDITY;
        };
    }

    private ConditionOperationAvro convertToConditionOperationAvro(ConditionOperation type) {
        return switch (type) {
            case EQUALS -> ConditionOperationAvro.EQUALS;
            case GREATER_THAN -> ConditionOperationAvro.GREATER_THAN;
            case LOWER_THAN -> ConditionOperationAvro.LOWER_THAN;
        };
    }

    private ActionTypeAvro convertToActionTypeAvro(ActionType type) {
        return switch (type) {
            case ACTIVATE -> ActionTypeAvro.ACTIVATE;
            case DEACTIVATE -> ActionTypeAvro.DEACTIVATE;
            case INVERSE -> ActionTypeAvro.INVERSE;
            case SET_VALUE -> ActionTypeAvro.SET_VALUE;
        };
    }

    private void logDoneConvertMessage(Object avro) {
        log.info("Объект конвертирован в avro {}", avro);
    }
}