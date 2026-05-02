package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.api.DeliveryFeignClient;
import ru.yandex.practicum.api.ShoppingStoreFeignClient;
import ru.yandex.practicum.config.PaymentCoefficientProperties;
import ru.yandex.practicum.dto.order.OrderDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class PaymentCalculator {
    private final ShoppingStoreFeignClient shoppingStoreClient;
    private final DeliveryFeignClient deliveryClient;
    private final PaymentCoefficientProperties properties;

    public PaymentCalculation calculate(OrderDto orderDto) {
        BigDecimal productsCost = calculateProducts(orderDto);
        BigDecimal deliveryCost = resolveDeliveryCost(orderDto);
        BigDecimal fee = calculateFee(productsCost);

        BigDecimal total = productsCost
                .add(deliveryCost)
                .add(fee);

        return new PaymentCalculation(productsCost, deliveryCost, fee, total);
    }

    private BigDecimal calculateProducts(OrderDto orderDto) {
        return orderDto.getProducts().entrySet().stream()
                .map(entry -> {
                    BigDecimal price = shoppingStoreClient
                            .getProduct(entry.getKey())
                            .getPrice();

                    return price.multiply(BigDecimal.valueOf(entry.getValue()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal resolveDeliveryCost(OrderDto orderDto) {
        if (orderDto.getDeliveryPrice() != null
                && orderDto.getDeliveryPrice().compareTo(BigDecimal.ZERO) > 0) {
            return orderDto.getDeliveryPrice();
        }

        return deliveryClient.postDeliveryCost(orderDto);
    }

    private BigDecimal calculateFee(BigDecimal productsCost) {
        return productsCost.multiply(
                properties.getNds()
                        .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
        );
    }

    public record PaymentCalculation(
            BigDecimal productsCost,
            BigDecimal deliveryCost,
            BigDecimal fee,
            BigDecimal total
    ) {}
}