package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.DeliveryCoefficientProperties;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.model.DeliveryCalculationContext;

import java.math.BigDecimal;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class DeliveryCalculator {
    private final DeliveryCoefficientProperties coeffs;

    public BigDecimal calculate(DeliveryCalculationContext ctx) {

        BigDecimal cost = coeffs.getBaseCost();

        cost = applyWarehouseCoefficient(cost, ctx.warehouseAddress());
        cost = applyFragile(cost, ctx.fragile());
        cost = applyWeight(cost, ctx.weight());
        cost = applyVolume(cost, ctx.volume());
        cost = applyDistance(cost, ctx.deliveryAddress(), ctx.warehouseAddress());

        return cost;
    }

    private BigDecimal applyWarehouseCoefficient(BigDecimal cost, AddressDto warehouse) {
        BigDecimal coef = BigDecimal.ZERO;

        if (isWarehouseAddressContains(warehouse, "ADDRESS_1")) {
            coef = coeffs.getAddress1();
        } else if (isWarehouseAddressContains(warehouse, "ADDRESS_2")) {
            coef = coeffs.getAddress2();
        }

        return cost.add(cost.multiply(coef));
    }

    private BigDecimal applyFragile(BigDecimal cost, Boolean fragile) {
        if (Boolean.TRUE.equals(fragile)) {
            return cost.add(cost.multiply(coeffs.getFragile()));
        }
        return cost;
    }

    private BigDecimal applyWeight(BigDecimal cost, BigDecimal weight) {
        return cost.add(coeffs.getWeight().multiply(weight));
    }

    private BigDecimal applyVolume(BigDecimal cost, BigDecimal volume) {
        return cost.add(coeffs.getVolume().multiply(volume));
    }

    private BigDecimal applyDistance(BigDecimal cost,
                                     AddressDto deliveryAddress,
                                     AddressDto warehouseAddress) {

        if (!deliveryAddress.getStreet().equals(warehouseAddress.getStreet())) {
            return cost.add(cost.multiply(coeffs.getDeliveryAddress()));
        }

        return cost;
    }

    private boolean isWarehouseAddressContains(AddressDto address, String str) {
        return Stream.of(
                address.getStreet(),
                address.getCountry(),
                address.getCity(),
                address.getHouse(),
                address.getFlat()
        ).anyMatch(v -> v != null && v.contains(str));
    }
}