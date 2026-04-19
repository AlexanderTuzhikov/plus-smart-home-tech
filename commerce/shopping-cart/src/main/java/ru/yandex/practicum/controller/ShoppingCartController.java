package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.ShoppingCartFeignClient;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartFeignClient {
    private final ShoppingCartService cartService;

    @Override
    public ShoppingCartDto getActiveCart(@RequestParam String username) {
        return cartService.getActiveCart(username);
    }

    @Override
    public ShoppingCartDto putProducts(
            @RequestParam String username,
            @RequestBody Map<UUID, Integer> items) {
        return cartService.putProducts(username, items);
    }

    @Override
    public Boolean deleteCart(@RequestParam String username) {
        return cartService.deleteCart(username);
    }

    @Override
    public ShoppingCartDto deleteProducts(
            @RequestParam String username,
            @RequestBody List<UUID> productIds
    ) {
        return cartService.deleteProducts(username, productIds);
    }

    @Override
    public ShoppingCartDto changeQuantity(
            @RequestParam String username,
            @Valid @RequestBody ChangeProductQuantityRequest changeQuantity
    ) {
        return cartService.changeQuantity(username, changeQuantity);
    }
}