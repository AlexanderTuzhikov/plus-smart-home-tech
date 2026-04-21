package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class AggregationServiceImpl implements AggregationService {
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
                .setSensorsState(new HashMap<>(Map.of(sensorId, sensorState)))
                .build();

        sensorsSnapshotAvroMap.put(hubId, snapshot);

        log.info("Создан новый snapshot: {}", snapshot);
        return Optional.of(snapshot);
    }

    private Optional<SensorsSnapshotAvro> updateSnapshotIfNewer(SensorsSnapshotAvro snapshot,
                                                                SensorEventAvro event) {
        Map<String, SensorStateAvro> sensorStateMap = new HashMap<>(snapshot.getSensorsState());
        String sensorId = event.getId();
        snapshot.setSensorsState(sensorStateMap);
        SensorStateAvro oldState = sensorStateMap.get(sensorId);

        if (oldState != null) {

            if (oldState.getTimestamp().isAfter(event.getTimestamp())
                    || oldState.getData().equals(event.getPayload())) {
                log.debug("Получен Snapshot c устаревшими данными");
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        sensorStateMap.put(sensorId, newState);
        snapshot.setTimestamp(event.getTimestamp());

        return Optional.of(snapshot);
    }
}