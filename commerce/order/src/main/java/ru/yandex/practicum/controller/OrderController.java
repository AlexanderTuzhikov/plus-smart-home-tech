package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.OrderFeignClient;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/order")
public class OrderController implements OrderFeignClient {
    private final OrderService orderService;

    @Override
    public Page<OrderDto> getOrders(@RequestParam("username") @NotBlank String username,
                                    Pageable pageable) {
        return orderService.getOrders(username, pageable);
    }

    @Override
    public OrderDto putOrder(@Valid @RequestBody CreateNewOrderRequest createNewOrderRequest) {
        return  orderService.createOrder(createNewOrderRequest);
    }

    @Override
    public OrderDto postReturnProducts(@Valid @RequestBody ProductReturnRequest productReturnRequest) {
        return orderService.returnProducts(productReturnRequest);
    }

    @Override
    public OrderDto postPaymentOrder(@RequestBody UUID orderId) {
        return orderService.payOrder(orderId);
    }

    @Override
    public OrderDto postFailedPaymentOrder(@RequestBody UUID orderId) {
        return orderService.failPayment(orderId);
    }

    @Override
    public OrderDto postDeliveryOrder(@RequestBody UUID orderId) {
        return orderService.startDelivery(orderId);
    }

    @Override
    public OrderDto postFailedDeliveryOrder(@RequestBody UUID orderId) {
        return orderService.failDelivery(orderId);
    }

    @Override
    public OrderDto postCompletedOrder(@RequestBody UUID orderId) {
        return orderService.completeDelivery(orderId);
    }

    @Override
    public OrderDto postCalculateTotalOrder(@RequestBody UUID orderId) {
        return orderService.calculateTotalOrder(orderId);
    }

    @Override
    public OrderDto postCalculateTotalDelivery(@RequestBody UUID orderId) {
        return orderService.calculateTotalDelivery(orderId);
    }

    @Override
    public OrderDto postAssemblyOrder(@RequestBody UUID orderId) {
        return orderService.assemblyOrder(orderId);
    }

    @Override
    public OrderDto postFailedAssemblyOrder(@RequestBody UUID orderId) {
        return orderService.failedAssemblyOrder(orderId);
    }
}