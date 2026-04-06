package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventServiceImpl implements HubEventService{
    private final HubEventTransactionServiceImpl hubEventTransactionServiceImpl;

    public void HubEventHandle(HubEventAvro hubEvent) {
        String hubId = hubEvent.getHubId();
        Object payload = hubEvent.getPayload();

        switch (payload) {
            case DeviceAddedEventAvro deviceAdded -> hubEventTransactionServiceImpl.deviceAdded(hubId, deviceAdded);
            case DeviceRemovedEventAvro removedEvent ->
                    hubEventTransactionServiceImpl.deviceRemoved(hubId, removedEvent);
            case ScenarioAddedEventAvro scenarioAdded ->
                    hubEventTransactionServiceImpl.scenarioAdded(hubId, scenarioAdded);
            case ScenarioRemovedEventAvro removedScenario ->
                    hubEventTransactionServiceImpl.scenarioRemoved(hubId, removedScenario);
            default -> throw new IllegalArgumentException("Неизвестный тип события: " + payload.getClass());
        }
    }
}