package ru.practicum.model.hub.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceAction {
    @NotBlank(message = "ID сенсора не может быть пустым")
    private String sensorId;
    @NotNull(message = "Тип действия не может быть пустым")
    private ActionType type;
    private Integer value;
}