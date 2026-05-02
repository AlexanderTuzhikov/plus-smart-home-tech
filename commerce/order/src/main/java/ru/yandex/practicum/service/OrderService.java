package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;

import java.util.UUID;

public interface OrderService {
    Page<OrderDto> getOrders(String username, Pageable pageable);

    OrderDto createOrder(CreateNewOrderRequest newOrderRequest);

    OrderDto returnProducts(ProductReturnRequest returnRequest);

    OrderDto payOrder(UUID orderId);

    OrderDto failPayment(UUID orderId);

    OrderDto startDelivery(UUID orderId);

    OrderDto failDelivery(UUID orderId);

    OrderDto completeDelivery(UUID orderId);

    OrderDto calculateTotalOrder(UUID orderId);

    OrderDto calculateTotalDelivery(UUID orderId);

    OrderDto assemblyOrder(UUID orderId);

    OrderDto failedAssemblyOrder(UUID orderId);
}
