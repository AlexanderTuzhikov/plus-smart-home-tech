package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.DeliveryFeignClient;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/api/v1/delivery")
public class DeliveryController implements DeliveryFeignClient {
    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto putDelivery(@Valid @RequestBody DeliveryDto deliveryDto) {
        return deliveryService.createDelivery(deliveryDto);
    }

    @Override
    public void postDeliverySuccessful(@RequestBody UUID orderId) {
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    public void postDeliveryPicked(@RequestBody UUID orderId) {
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    public void postDeliveryFailed(@RequestBody UUID orderId) {
        deliveryService.deliveryFailed(orderId);
    }

    @Override
    public BigDecimal postDeliveryCost(@RequestBody @Valid OrderDto orderDto) {
        BigDecimal deliveryCost = deliveryService.getDeliveryCost(orderDto);
        log.info("Стоимость доставки заказа {}", deliveryCost);
        return deliveryCost;
    }
}