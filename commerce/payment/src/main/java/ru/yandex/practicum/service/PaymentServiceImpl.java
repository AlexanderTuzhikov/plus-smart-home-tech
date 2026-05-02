package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.OrderFeignClient;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.payment.PaymentState;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderFeignClient orderClient;
    private final PaymentMapper paymentMapper;
    private final PaymentCalculator paymentCalculator;

    @Override
    @Transactional
    public PaymentDto createPayment(OrderDto orderDto) {
        log.info("Создание платежа для заказа {}", orderDto.getOrderId());

        PaymentCalculator.PaymentCalculation calc =
                paymentCalculator.calculate(orderDto);

        Payment payment = Payment.builder()
                .orderId(orderDto.getOrderId())
                .productsTotal(calc.productsCost())
                .deliveryTotal(calc.deliveryCost())
                .feeTotal(calc.fee())
                .state(PaymentState.PENDING)
                .build();

        paymentRepository.save(payment);
        PaymentDto newPaymentDto = paymentMapper.toDto(payment);
        log.info("Платеж создан: {}", newPaymentDto);

        return newPaymentDto;
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {
        log.info("Получение стоимости заказа {}", orderDto.getOrderId());
        BigDecimal totalCost = paymentCalculator.calculate(orderDto).total();
        log.info("Стоимость заказа: {}", totalCost);

        return totalCost;
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        log.info("Оплата заказа {}", paymentId);
        Payment payment = updateState(paymentId, PaymentState.SUCCESS);
        orderClient.postPaymentOrder(payment.getOrderId());
        log.info("Оплата заказа выполнена");
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        log.info("Ошибка оплаты заказа {}", paymentId);
        Payment payment = updateState(paymentId, PaymentState.FAILED);
        orderClient.postFailedPaymentOrder(payment.getOrderId());
        log.info("Ошибка оплаты заказа установлена {}", paymentId);
    }

    @Override
    public BigDecimal getProductCost(OrderDto orderDto) {
        log.info("Получение стоимости доставки заказа {}", orderDto.getOrderId());
        BigDecimal totalProductCost = paymentCalculator.calculate(orderDto).productsCost();
        log.info("Стоимость доставки заказа: {}", totalProductCost);

        return totalProductCost;
    }

    private Payment getPaymentOrThrow(UUID paymentId) {
        log.info("Проверка существования paymentId");
        return paymentRepository.findById(paymentId).orElseThrow(() ->
                new NotFoundException("Платеж с ID " + paymentId + " не существует"));
    }

    private Payment updateState(UUID paymentId, PaymentState newState) {
        Payment payment = getPaymentOrThrow(paymentId);
        payment.setState(newState);
        return payment;
    }
}