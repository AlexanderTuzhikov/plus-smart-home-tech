package ru.practicum.service;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Optional;

public interface AggregationService {
    Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event);
}
