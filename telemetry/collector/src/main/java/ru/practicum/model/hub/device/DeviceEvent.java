package ru.practicum.model.hub.device;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class DeviceEvent extends HubEvent {
    @NotBlank(message = "ID устройства не может быть пустым")
    private String id;

    @Override
    public abstract HubEventType getType();
}