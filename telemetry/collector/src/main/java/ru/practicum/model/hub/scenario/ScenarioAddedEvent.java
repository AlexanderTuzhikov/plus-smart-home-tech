package ru.practicum.model.hub.scenario;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.model.hub.HubEventType;
import ru.practicum.model.hub.device.DeviceAction;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioAddedEvent extends ScenarioEvent{
    @NotEmpty(message = "Список условий, которые связаны со сценарием не может быть пустым.")
    private List<ScenarioCondition> conditions;
    @NotEmpty(message = "Список действий, которые должны быть выполнены в рамках сценария не может быть пустым.")
    private List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED_EVENT;
    }
}