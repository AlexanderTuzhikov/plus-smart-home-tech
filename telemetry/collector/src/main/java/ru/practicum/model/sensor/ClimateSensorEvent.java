package ru.practicum.model.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ClimateSensorEvent extends SensorEvent{
    @NotNull(message = "Уровень температуры по шкале Цельсия не может быть NULL")
    private Integer temperatureC;
    @NotNull(message = "Уровень влажности не может быть NULL")
    private Integer humidity;
    @NotNull(message = "Уровень CO2 не может быть NULL")
    private Integer co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}