package ru.yandex.practicum.model;

import ru.yandex.practicum.dto.warehouse.AddressDto;

import java.math.BigDecimal;

public record DeliveryCalculationContext(
        BigDecimal weight,
        BigDecimal volume,
        Boolean fragile,
        AddressDto warehouseAddress,
        AddressDto deliveryAddress
) {}
