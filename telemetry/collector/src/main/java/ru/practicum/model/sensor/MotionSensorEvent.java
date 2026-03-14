package ru.practicum.model.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MotionSensorEvent extends SensorEvent{
    @NotNull(message = "Качество связи не может быть NULL")
    private Integer linkQuality;
    @NotNull(message = "Наличие/отсутствие движения не может быть NULL")
    private Boolean motion;
    @NotNull(message = "Напряжение не может быть NULL")
    private Integer voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}