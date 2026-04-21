package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto getActiveCart(String username);

    ShoppingCartDto putProducts(String username, Map<UUID, Integer> products);

    Boolean deleteCart(String username);

    ShoppingCartDto deleteProducts(String username, List<UUID> productIds);

    ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest changeQuantity);
}
