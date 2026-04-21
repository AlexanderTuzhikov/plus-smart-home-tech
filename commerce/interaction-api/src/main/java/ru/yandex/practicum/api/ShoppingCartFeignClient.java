package ru.yandex.practicum.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart")
public interface ShoppingCartFeignClient {
    @GetMapping
    ShoppingCartDto getActiveCart(@RequestParam("username") String username);

    @PutMapping
    ShoppingCartDto putProducts(@RequestParam("username") String username, @RequestBody Map<UUID, Integer> items);

    @DeleteMapping
    Boolean deleteCart(@RequestParam("username") String username);

    @PostMapping("/remove")
    ShoppingCartDto deleteProducts(@RequestParam("username") String username, @RequestBody List<UUID> productIds);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeQuantity(
            @RequestParam("username") String username,
            @RequestBody ChangeProductQuantityRequest changeQuantity
    );
}