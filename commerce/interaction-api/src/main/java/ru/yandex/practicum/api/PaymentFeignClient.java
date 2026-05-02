package ru.yandex.practicum.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentFeignClient {
    @PostMapping
    PaymentDto postPayment(@RequestBody OrderDto orderDto);

    @PostMapping("/totalCost")
    BigDecimal postTotalCost(@RequestBody OrderDto orderDto);

    @PostMapping("/refund")
    void postPaymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/productCost")
    BigDecimal postProductCost(@RequestBody OrderDto order);

    @PostMapping("/failed")
    void postPaymentFailed(@RequestBody UUID paymentId);
}