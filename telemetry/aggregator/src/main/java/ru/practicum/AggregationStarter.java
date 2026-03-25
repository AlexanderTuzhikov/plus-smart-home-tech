package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.SensorEventConsumer;
import ru.practicum.kafka.SensorsSnapshotProducer;
import ru.practicum.service.AggregationService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final SensorEventConsumer sensorEventConsumer;
    private final SensorsSnapshotProducer sensorsSnapshotProducer;
    private final AggregationService service;

    private final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    @Value("${telemetry.sensors.v1.topic}")
    private List<String> topics;
    @Value("${telemetry.snapshots.v1.topic}")
    private String snapshotsTopic;

    public void start() {
        Consumer<String, SensorEventAvro> consumer = sensorEventConsumer.getConsumer();
        Producer<String, SensorsSnapshotAvro> producer = sensorsSnapshotProducer.getProducer();

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(topics);

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.info("Обработка входящего сообщения SensorEvent: key={}, value={}", record.key(), record.value());

                    Optional<SensorsSnapshotAvro> updatedSnapshot = service.updateState(record.value());

                    if (updatedSnapshot.isPresent()) {
                        SensorsSnapshotAvro sensorsSnapshotAvro = updatedSnapshot.get();
                        String key = sensorsSnapshotAvro.getHubId();
                        sensorsSnapshotProducer.sendMessage(snapshotsTopic, key, sensorsSnapshotAvro);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}

