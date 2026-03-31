package ru.yandex.practicum.model.sensor;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sensors")
public class Sensor {
    @Id
    @Column(name = "id", nullable = false)
    String id;

    @Column(name = "hub_id", nullable = false)
    String hubId;
}