package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class AggregationService {
    private final Map<String, SensorsSnapshotAvro> sensorsSnapshotAvroMap = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        log.info("Получен запрос на обновление данных event: {}", event);
        String hubId = event.getHubId();
        SensorsSnapshotAvro snapshot;

        if (sensorsSnapshotAvroMap.containsKey(hubId)) {
            snapshot = sensorsSnapshotAvroMap.get(hubId);
            log.info("Найден подходящий snapshot для обновления: {}", snapshot);
        } else {
            return newSensorSnapshot(event);
        }

        return updateSnapshotIfNewer(snapshot, event);
    }

    private Optional<SensorsSnapshotAvro> newSensorSnapshot(SensorEventAvro event) {
        String hubId = event.getHubId();
        Object sensorEvent = event.getPayload();
        String sensorId = event.getId();

        SensorStateAvro sensorState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(sensorEvent)
                .build();

        SensorsSnapshotAvro snapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(event.getTimestamp())
                .setSensorsState(Map.of(sensorId, sensorState))
                .build();

        sensorsSnapshotAvroMap.put(hubId, snapshot);

        log.info("Создан новый snapshot: {}", snapshot);
        return Optional.of(snapshot);
    }

    private Optional<SensorsSnapshotAvro> updateSnapshotIfNewer(SensorsSnapshotAvro snapshot,
                                                                SensorEventAvro event) {
        Map<String, SensorStateAvro> sensorStateMap = new HashMap<>(snapshot.getSensorsState());
        String sensorId = event.getId();
        SensorStateAvro oldState = sensorStateMap.get(sensorId);

        if (oldState != null) {
            if (oldState.getTimestamp().isAfter(event.getTimestamp())
                    || Objects.equals(oldState.getData(), event.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder(oldState)
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        sensorStateMap.put(sensorId, newState);

        SensorsSnapshotAvro updated = SensorsSnapshotAvro.newBuilder(snapshot)
                .setTimestamp(event.getTimestamp())
                .setSensorsState(sensorStateMap)
                .build();

        String hubId = updated.getHubId();
        sensorsSnapshotAvroMap.put(hubId, updated);

        return Optional.of(updated);
    }
}
