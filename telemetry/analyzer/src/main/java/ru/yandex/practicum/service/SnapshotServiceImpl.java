package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.condition.Condition;
import ru.yandex.practicum.model.condition.ConditionType;
import ru.yandex.practicum.model.scenario.Scenario;
import ru.yandex.practicum.model.scenario.ScenarioAction;
import ru.yandex.practicum.model.scenario.ScenarioCondition;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;
    private final ScenarioRepository scenarioRepository;

    @Transactional
    public void handleSnapshot(SensorsSnapshotAvro sensorsSnapshotAvro) {
        String hubId = sensorsSnapshotAvro.getHubId();
        log.info("Получен снимок сенсоров: hubId={}, количествоСенсоров={}",
                hubId, sensorsSnapshotAvro.getSensorsState().size());
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        log.debug("Найдены сценарии: hubId={}, количество={}", hubId, scenarios.size());


        if (scenarios.isEmpty()) {
            log.debug("Сценарии отсутствуют: hubId={}", hubId);
            return;
        }

        for (Scenario scenario : scenarios) {
            log.info("Проверка сценария: hubId={}, сценарий={}, условий={}, действий={}",
                    hubId,
                    scenario.getName(),
                    scenario.getConditions().size(),
                    scenario.getActions().size());

            if (checkScenarioConditions(scenario, sensorsSnapshotAvro)) {
                executeScenarioActions(scenario);
            } else {
                log.debug("Scenario conditions not met: {}", scenario.getName());
            }
        }
    }

    private boolean checkScenarioConditions(Scenario scenario, SensorsSnapshotAvro sensorsSnapshotAvro) {
        Map<String, SensorStateAvro> sensorState = sensorsSnapshotAvro.getSensorsState();//получаем данные сенсоров
        Set<ScenarioCondition> scenarioConditions = scenario.getConditions();

        for (ScenarioCondition scenarioCondition : scenarioConditions) {
            String sensorId = scenarioCondition.getSensor().getId();
            SensorStateAvro sensorStateAvro = sensorState.get(sensorId);

            if (sensorStateAvro == null) {
                log.warn("Сенсор отсутствует в снимке: hubId={}, scenario={}, sensorId={}",
                        scenario.getHubId(), scenario.getName(), sensorId);
                return false;
            }

            boolean conditionMet = checkCondition(scenarioCondition.getCondition(), sensorStateAvro.getData());
            log.debug("Проверка условий: sensor={}, type={}, operation={}, expected={}, result={}",
                    sensorId, scenarioCondition.getCondition().getType(), scenarioCondition.getCondition().getOperation(),
                    scenarioCondition.getCondition().getValue(), conditionMet);

            if (!conditionMet) {
                log.info("Сценарий не выполнен: hubId={}, scenario={}, причина=условие не выполнено, sensorId={}",
                        scenario.getHubId(), scenario.getName(), sensorId);
                return false;
            }
        }

        return true;
    }

    private boolean checkCondition(Condition condition, Object sensorData) {
        Integer actualValue = extractValue(condition.getType(), sensorData);
        Integer expectedValue = condition.getValue();

        log.debug("Сравнение значений:: actual={}, expected={}, operation={}",
                actualValue, expectedValue, condition.getOperation());

        return switch (condition.getOperation()) {
            case EQUALS -> actualValue.equals(expectedValue);
            case GREATER_THAN -> actualValue > expectedValue;
            case LOWER_THAN -> actualValue < expectedValue;
        };
    }

    private Integer extractValue(ConditionType type, Object data) {
        if (data == null) {
            throw new IllegalArgumentException("Data = null");
        }

        return switch (type) {
            case TEMPERATURE -> switch (data) {
                case TemperatureSensorAvro temperatureSensorAvro -> temperatureSensorAvro.getTemperatureC();
                case ClimateSensorAvro climateSensorAvro -> climateSensorAvro.getTemperatureC();
                default -> throw new IllegalArgumentException(
                        "Неизвестный тип данных для TEMPERATURE: " + data.getClass()
                );
            };
            case HUMIDITY -> switch (data) {
                case ClimateSensorAvro climateSensorAvro -> climateSensorAvro.getHumidity();
                default -> throw new IllegalArgumentException(
                        "Неизвестный тип данных для HUMIDITY: " + data.getClass()
                );
            };
            case CO2LEVEL -> switch (data) {
                case ClimateSensorAvro climateSensorAvro -> climateSensorAvro.getCo2Level();
                default -> throw new IllegalArgumentException(
                        "Неизвестный тип данных для CO2LEVEL: " + data.getClass()
                );
            };
            case LUMINOSITY -> switch (data) {
                case LightSensorAvro lightSensorAvro -> lightSensorAvro.getLuminosity();
                default -> throw new IllegalArgumentException(
                        "Неизвестный тип данных для LUMINOSITY: " + data.getClass()
                );
            };
            case MOTION -> switch (data) {
                case MotionSensorAvro motionSensorAvro -> motionSensorAvro.getMotion() ? 1 : 0;
                default -> throw new IllegalArgumentException(
                        "Неизвестный тип данных для MOTION: " + data.getClass()
                );
            };
            case SWITCH -> switch (data) {
                case SwitchSensorAvro switchSensorAvro -> switchSensorAvro.getState() ? 1 : 0;
                default -> throw new IllegalArgumentException(
                        "Неизвестный тип данных для SWITCH: " + data.getClass()
                );
            };
        };
    }

    private void executeScenarioActions(Scenario scenario) {
        log.info("Выполнение сценария:: hubId={}, scenario={}", scenario.getHubId(), scenario.getName());

        if (scenario.getActions().isEmpty()) {
            log.warn("У сценария отсутствуют действия: {}", scenario.getName());
            return;
        }

        for (ScenarioAction scenarioAction : scenario.getActions()) {

            try {
                DeviceActionProto action = toDeviceActionProto(scenarioAction);
                Instant timestamp = Instant.now();

                DeviceActionRequest request = DeviceActionRequest.newBuilder()
                        .setHubId(scenario.getHubId())
                        .setScenarioName(scenario.getName())
                        .setAction(action)
                        .setTimestamp(Timestamp.newBuilder()
                                .setSeconds(timestamp.getEpochSecond())
                                .setNanos(timestamp.getNano())
                                .build())
                        .build();

                log.info("Отправка действия: hubId={}, scenario={}, sensor={}, type={}",
                        scenario.getHubId(), scenario.getName(), action.getSensorId(), action.getType());

                hubRouterClient.handleDeviceAction(request);

                log.info("Действие успешно выполнено: hubId={}, scenario={}, sensor={}",
                        scenario.getHubId(), scenario.getName(), action.getSensorId());
            } catch (Exception e) {
                log.error("Ошибка при выполнении действия: {}", scenario.getName(), e);
            }
        }
    }

    private DeviceActionProto toDeviceActionProto(ScenarioAction scenarioAction) {
        DeviceActionProto.Builder builder = DeviceActionProto.newBuilder()
                .setSensorId(scenarioAction.getSensor().getId())
                .setType(ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto.valueOf(
                        scenarioAction.getAction().getType().name()));

        if (scenarioAction.getAction().getValue() != null) {
            builder.setValue(scenarioAction.getAction().getValue());
        }

        return builder.build();
    }
}