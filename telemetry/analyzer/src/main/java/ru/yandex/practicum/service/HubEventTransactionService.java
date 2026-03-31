package ru.yandex.practicum.service;

import ru.yandex.practicum.kafka.telemetry.event.*;

public interface HubEventTransactionService {
    void deviceAdded(String hubId, DeviceAddedEventAvro deviceAdded);

    void deviceRemoved(String hubId, DeviceRemovedEventAvro deviceRemoved);

    void scenarioAdded(String hubId, ScenarioAddedEventAvro scenarioAdded);

    void scenarioRemoved(String hubId, ScenarioRemovedEventAvro scenarioRemoved);
}