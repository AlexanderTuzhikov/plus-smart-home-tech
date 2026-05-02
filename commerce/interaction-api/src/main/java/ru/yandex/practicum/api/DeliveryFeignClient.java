package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "delivery", path = "/api/v1/delivery")
public interface DeliveryFeignClient{
    @PutMapping
    DeliveryDto putDelivery(@RequestBody DeliveryDto deliveryDto);

    @PostMapping("/successful")
    void postDeliverySuccessful(@RequestBody UUID deliveryId);

    @PostMapping("/picked")
    void postDeliveryPicked(@RequestBody UUID deliveryId);

    @PostMapping("/failed")
    void postDeliveryFailed(@RequestBody UUID deliveryId);

    @PostMapping("/cost")
    BigDecimal postDeliveryCost(@RequestBody @Valid OrderDto order);
}