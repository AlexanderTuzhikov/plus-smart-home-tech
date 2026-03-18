package ru.practicum.model.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class TemperatureSensorEvent extends SensorEvent{
    @NotNull(message = "Уровень температуры по шкале Цельсия не может быть NULL")
    private Integer temperatureC;
    @NotNull(message = "Уровень температуры по шкале Фаренгейта не может быть NULL")
    private Integer temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}