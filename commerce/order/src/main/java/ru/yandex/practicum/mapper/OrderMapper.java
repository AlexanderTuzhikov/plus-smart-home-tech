package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderProducts;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderDto toDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .shoppingCartId(order.getShoppingCartId())
                .products(productsToMap(order.getProducts()))
                .paymentId(order.getPaymentId())
                .orderState(order.getOrderState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .build();
    }

    public List<OrderProducts> productsToOrderItems(Map<UUID, Long> products) {
        return products.entrySet().stream()
                .map(entry -> {
                    OrderProducts orderProducts = new OrderProducts();
                    orderProducts.setProductId(entry.getKey());
                    orderProducts.setQuantity(entry.getValue());
                    return orderProducts;
                })
                .toList();
    }

    public Map<UUID, Long> productsToMap(List<OrderProducts> products) {
        return products.stream()
                .collect(Collectors.toMap(
                        OrderProducts::getProductId,
                        OrderProducts::getQuantity
                ));
    }
}