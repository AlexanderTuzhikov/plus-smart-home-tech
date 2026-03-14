package ru.practicum.kafka;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TelemetryService {
    AvroConverter avroConverter;
    TelemetryProducer telemetryProducer;

    public void sendSensorEvents(String topic, String key, Object event) {
        Object avro = avroConverter.convertToAvro(event);
        logMessageSendToProducer(avro);
        telemetryProducer.sendMessage(topic, key, avro);
    }

    public void sendHubEvents(String topic, String key, Object event) {
        Object avro = avroConverter.convertToAvro(event);
        logMessageSendToProducer(avro);
        telemetryProducer.sendMessage(topic, key, avro);
    }

    private void logMessageSendToProducer(Object avro) {
        log.info("Отправка события {} в Producer для записи", avro);
    }
}