package ru.yandex.practicum.model;


import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Address {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}