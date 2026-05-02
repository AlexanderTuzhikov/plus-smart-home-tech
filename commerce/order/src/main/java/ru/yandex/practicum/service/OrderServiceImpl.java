package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.ShoppingCartFeignClient;
import ru.yandex.practicum.api.WarehouseFeighClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.OrderProducts;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseFeighClient warehouseClient;
    private final OrderMapper orderMapper;
    private final ShoppingCartFeignClient shoppingCartClient;

    @Override
    public Page<OrderDto> getOrders(String username, Pageable pageable) {
        log.info("Получение заказов пользователя: {}", username);
        ShoppingCartDto shoppingCartDto = shoppingCartClient.getActiveCart(username);
        UUID shoppingCartId = shoppingCartDto.getShoppingCartId();
        Page<Order> orders = orderRepository.getAllByShoppingCartId(shoppingCartId, pageable);
        log.info("Заказы найдены: {}", orders.getTotalElements());

        return orders.map(orderMapper::toDto);
    }

    @Override
    @Transactional
    public OrderDto createOrder(CreateNewOrderRequest newOrderRequest) {
        log.info("Создание нового заказа: {}", newOrderRequest);
        ShoppingCartDto shoppingCartDto = newOrderRequest.getShoppingCart();
        warehouseClient.postCheckAvailabilityForCart(shoppingCartDto);
        List<OrderProducts> orderProducts = orderMapper.productsToOrderItems(shoppingCartDto.getProducts());

        Order order = Order.builder()
                .shoppingCartId(shoppingCartDto.getShoppingCartId())
                .products(orderProducts)
                .orderState(OrderState.NEW)
                .build();

        Order newOrder = orderRepository.save(order);
        log.info("Заказ сохранен: {}", newOrder);

        return orderMapper.toDto(newOrder);
    }

    @Override
    @Transactional
    public OrderDto returnProducts(ProductReturnRequest returnRequest) {
        log.info("Возврат товаров на склад: {}", returnRequest);
        UUID orderId = returnRequest.getOrderId();
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(OrderState.PRODUCT_RETURNED);
        Map<UUID, Long> products = returnRequest.getProducts();
        warehouseClient.returnProducts(products);
        OrderDto orderDto = orderMapper.toDto(order);
        log.info("Товары вернулись на склад: {}", orderDto);

        return orderDto;
    }

    @Override
    @Transactional
    public OrderDto payOrder(UUID orderId) {
        log.info("Статус успешной оплаты заказа: {}", orderId);
        OrderDto orderDto = updateState(orderId, OrderState.PAID);
        log.info("Статус успешной оплаты заказа установлен: {}", orderDto);

        return orderDto;
    }

    @Transactional
    public OrderDto failPayment(UUID orderId) {
        log.info("Статус ошибки оплаты заказа: {}", orderId);
        OrderDto orderDto = updateState(orderId, OrderState.PAYMENT_FAILED);
        log.info("Статус ошибки оплаты заказа установлен:  {}", orderDto);

        return orderDto;
    }

    @Override
    @Transactional
    public OrderDto startDelivery(UUID orderId) {
        log.info("Статус доставка заказа: {}", orderId);
        OrderDto orderDto = updateState(orderId, OrderState.DELIVERED);
        log.info("Статус доставка заказа установлен: {}", orderDto);

        return orderDto;
    }

    @Override
    @Transactional
    public OrderDto failDelivery(UUID orderId) {
        log.info("Статус ошибка доставки заказа: {}", orderId);
        OrderDto orderDto = updateState(orderId, OrderState.DELIVERY_FAILED);
        log.info("Статус ошибки доставки заказа установлен: {}", orderDto);

        return orderDto;
    }

    @Override
    @Transactional
    public OrderDto completeDelivery(UUID orderId) {
        log.info("Статус завершение заказа: {}", orderId);
        OrderDto orderDto = updateState(orderId, OrderState.COMPLETED);
        log.info("Статус завершения заказа установлен: {}", orderDto);

        return orderDto;
    }

    @Override
    @Transactional
    public OrderDto calculateTotalOrder(UUID orderId) {
        log.info("Статус расчета стоимости заказа: {}", orderId);
        OrderDto orderDto = updateState(orderId, OrderState.ON_PAYMENT);
        log.info("Статус расчета стоимости заказа установлен: {}", orderDto);

        return orderDto;
    }

    @Override
    @Transactional
    public OrderDto calculateTotalDelivery(UUID orderId) {
        log.info("Статус расчета стоимости доставки: {}", orderId);
        OrderDto orderDto = updateState(orderId, OrderState.ON_DELIVERY);
        log.info("Статус расчета стоимости доставки установлена: {}", orderDto);

        return orderDto;
    }

    @Override
    @Transactional
    public OrderDto assemblyOrder(UUID orderId) {
        log.info("Статус заказа сборка: {}", orderId);
        OrderDto orderDto = updateState(orderId, OrderState.ASSEMBLED);
        log.info("Статус сборки заказа установлен: {}", orderDto);

        return orderDto;
    }

    @Override
    @Transactional
    public OrderDto failedAssemblyOrder(UUID orderId) {
        log.info("Статус ошибки сборки заказа: {}", orderId);
        OrderDto orderDto = updateState(orderId, OrderState.ASSEMBLY_FAILED);
        log.info("Статус ошибки сборки заказа установлен: {}", orderId);

        return orderDto;
    }

    private OrderDto updateState(UUID orderId, OrderState newState) {
        Order order = getOrderOrThrow(orderId);
        order.setOrderState(newState);
        return orderMapper.toDto(order);
    }

    private Order getOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ " + orderId + " не найден"));
    }
}