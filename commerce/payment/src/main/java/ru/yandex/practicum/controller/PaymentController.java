package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.PaymentFeignClient;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/payment")
public class PaymentController implements PaymentFeignClient {
    private final PaymentService paymentService;

    @Override
    public PaymentDto postPayment(@Valid @RequestBody OrderDto orderDto) {
        return paymentService.createPayment(orderDto);
    }

    @Override
    public BigDecimal postTotalCost(@Valid @RequestBody OrderDto orderDto) {
        return paymentService.getTotalCost(orderDto);
    }

    @Override
    public void postPaymentSuccess(@RequestBody UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public BigDecimal postProductCost(@RequestBody @Valid OrderDto orderDto) {
        return paymentService.getProductCost(orderDto);
    }

    @Override
    public void postPaymentFailed(@RequestBody UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}