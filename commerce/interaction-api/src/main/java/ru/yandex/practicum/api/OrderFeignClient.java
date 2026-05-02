package ru.yandex.practicum.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;

import java.util.UUID;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderFeignClient {
    @GetMapping
    Page<OrderDto> getOrders(@RequestParam("username") String username, @SpringQueryMap Pageable pageable);

    @PutMapping
    OrderDto putOrder(@RequestBody CreateNewOrderRequest newOrderRequest);

    @PostMapping("/return")
    OrderDto postReturnProducts(@RequestBody ProductReturnRequest returnRequest);

    @PostMapping("/payment")
    OrderDto postPaymentOrder(@RequestBody UUID orderId);

    @PostMapping("/payment/failed")
    OrderDto postFailedPaymentOrder(@RequestBody UUID orderId);

    @PostMapping("/delivery")
    OrderDto postDeliveryOrder(@RequestBody UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto postFailedDeliveryOrder(@RequestBody UUID orderId);

    @PostMapping("/completed")
    OrderDto postCompletedOrder(@RequestBody UUID orderId);

    @PostMapping("/calculate/total")
    OrderDto postCalculateTotalOrder(@RequestBody UUID orderId);

    @PostMapping("/calculate/delivery")
    OrderDto postCalculateTotalDelivery(@RequestBody UUID orderId);

    @PostMapping("/assembly")
    OrderDto postAssemblyOrder(@RequestBody UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto postFailedAssemblyOrder(@RequestBody UUID orderId);
}