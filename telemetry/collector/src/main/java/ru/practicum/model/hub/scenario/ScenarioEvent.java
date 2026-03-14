package ru.practicum.model.hub.scenario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class ScenarioEvent extends HubEvent {
    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 3, message = "Минимальная длинна имени = 3, максимальная = 2147483647 символов")
    private String name;

    @Override
    public abstract HubEventType getType();
}