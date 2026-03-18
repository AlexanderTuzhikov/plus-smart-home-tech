package ru.practicum.model.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenarioCondition {
    @NotBlank(message = "ID сенсора не может быть пустым")
    private String sensorId;
    @NotNull(message = "Тип условий, которые могут использоваться в сценариях не может быть NULL")
    private ConditionType type;
    @NotNull(message = "Операция, которая может быть использованы в условиях не может быть NULL")
    private ConditionOperation operation;
    private Object value;
}