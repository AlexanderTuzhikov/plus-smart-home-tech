package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.kafka.TelemetryService;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.sensor.SensorEvent;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class TelemetryController {
    private final TelemetryService telemetryService;
    @Value("${telemetry.sensors.v1.topic}")
    String sensorEventsTopic;
    @Value("${telemetry.hubs.v1.topic}")
    String hubEventsTopic;

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvents(@Valid @RequestBody SensorEvent event) {
        telemetryService.sendSensorEvents(sensorEventsTopic, event.getHubId(), event);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubsEvents(@Valid @RequestBody HubEvent event) {
        telemetryService.sendHubEvents(hubEventsTopic, event.getHubId(), event);

        return ResponseEntity.ok().build();
    }
}