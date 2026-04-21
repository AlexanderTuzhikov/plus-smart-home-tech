package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.action.Action;
import ru.yandex.practicum.model.action.ActionType;
import ru.yandex.practicum.model.condition.Condition;
import ru.yandex.practicum.model.condition.ConditionOperation;
import ru.yandex.practicum.model.condition.ConditionType;
import ru.yandex.practicum.model.scenario.*;
import ru.yandex.practicum.model.sensor.Sensor;
import ru.yandex.practicum.repository.*;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubEventTransactionServiceImpl implements HubEventTransactionService {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @Transactional
    public void deviceAdded(String hubId, DeviceAddedEventAvro deviceAdded) {
        log.info("Запрос на добавление нового устройства: {}", deviceAdded);
        Sensor newSensor = Sensor.builder()
                .id(deviceAdded.getId())
                .hubId(hubId)
                .build();
        Sensor savedSensor = sensorRepository.save(newSensor);
        log.info("Новое устройство добавлено: {}", savedSensor);
    }

    @Transactional
    public void deviceRemoved(String hubId, DeviceRemovedEventAvro deviceRemoved) {
        String id = deviceRemoved.getId();
        log.info("Запрос на удаление устройства id= {}", id);
        Sensor sensor = checkSensorExists(id);
        sensorRepository.delete(sensor);
        log.info("Устройство удалено");
    }

    @Transactional
    public void scenarioAdded(String hubId, ScenarioAddedEventAvro scenarioAdded) {
        log.info("Запрос на добавление нового сценария: {}", scenarioAdded);
        Scenario newScenario = Scenario.builder()
                .hubId(hubId)
                .name(scenarioAdded.getName())
                .build();
        Scenario scenario = scenarioRepository.save(newScenario);
        saveConditions(scenario, scenarioAdded.getConditions());
        saveActions(scenario, scenarioAdded.getActions());
        log.info("Сценарий сохранен");
    }

    @Transactional
    public void scenarioRemoved(String hubId, ScenarioRemovedEventAvro scenarioRemoved) {
        log.info("Запрос на удаление сценария: {}", scenarioRemoved.getName());
        String name = scenarioRemoved.getName();
        Scenario scenario = checkScenarioExists(hubId, name);
        scenarioRepository.delete(scenario);
        log.info("Сценарий удален");
    }

    private void saveConditions(Scenario scenario, List<ScenarioConditionAvro> conditionsAvro) {
        for (ScenarioConditionAvro conditionAvro : conditionsAvro) {
            Condition condition = Condition.builder()
                    .type(ConditionType.valueOf(conditionAvro.getType().name()))
                    .operation(ConditionOperation.valueOf(conditionAvro.getOperation().name()))
                    .value(mapConditionValue(conditionAvro.getValue()))
                    .build();

            condition = conditionRepository.save(condition);

            Sensor sensor = checkSensorExists(conditionAvro.getSensorId());

            ScenarioConditionId scenarioConditionId = ScenarioConditionId.builder()
                    .scenarioId(scenario.getId())
                    .sensorId(sensor.getId())
                    .conditionId(condition.getId())
                    .build();

            ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                    .id(scenarioConditionId)
                    .scenario(scenario)
                    .sensor(sensor)
                    .condition(condition)
                    .build();

            scenarioConditionRepository.save(scenarioCondition);
        }
    }

    private void saveActions(Scenario scenario, List<DeviceActionAvro> actionAvros) {
        for (DeviceActionAvro actionAvro : actionAvros) {
            Action action = Action.builder()
                    .type(ActionType.valueOf(actionAvro.getType().name()))
                    .value(actionAvro.getValue())
                    .build();

            action = actionRepository.save(action);

            Sensor sensor = checkSensorExists(actionAvro.getSensorId());

            ScenarioActionId scenarioActionId = ScenarioActionId.builder()
                    .scenarioId(scenario.getId())
                    .sensorId(sensor.getId())
                    .actionId(action.getId())
                    .build();

            ScenarioAction scenarioAction = ScenarioAction.builder()
                    .id(scenarioActionId)
                    .scenario(scenario)
                    .sensor(sensor)
                    .action(action)
                    .build();

            scenarioActionRepository.save(scenarioAction);
        }
    }

    private Integer mapConditionValue(Object value) {
        return switch (value) {
            case Integer intValue -> intValue;
            case Boolean boolValue -> boolValue ? 1 : 0;
            default -> throw new IllegalArgumentException("Неизвестное значение для Condition.value: " + value);
        };
    }

    private Scenario checkScenarioExists(String hubId, String name) {
        return scenarioRepository.findByHubIdAndName(hubId, name).orElseThrow(
                () -> new NotFoundException("Сценарий не найден hubId: " + hubId + "name: " + name));
    }

    private Sensor checkSensorExists(String id) {
        return sensorRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Устройство не найден id: " + id));
    }
}