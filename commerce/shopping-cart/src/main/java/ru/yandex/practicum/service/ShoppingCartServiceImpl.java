package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.WarehouseFeighClient;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartMapper cartMapper;
    private final WarehouseFeighClient warehouseClient;

    @Override
    @Transactional(readOnly = true)
    public ShoppingCartDto getActiveCart(String username) {
        log.info("Запрос на получение активной козины для пользователя: {}", username);
        usernameNotEmptyOrThrow(username);
        ShoppingCart cart = getActiveCartOrThrow(username);
        log.info("Активная корзина найдена: {}", cart);

        return cartMapper.toShoppingCartDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartDto putProducts(String username, Map<UUID, Integer> products) {
        log.info("Запрос на добавление новых товаров в корзину пользователя: {}", username);
        usernameNotEmptyOrThrow(username);

        if (products == null || products.isEmpty()) {
            throw new ValidateException("Список товаров для добавления в корзину не должен быть пустым");
        }

        ShoppingCart cart = cartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> createNewActiveCart(username));

        setProductsToCart(cart, products);

        try {
            warehouseClient.checkAvailabilityForCart(cartMapper.toShoppingCartDto(cart));
        } catch (ProductInShoppingCartLowQuantityInWarehouseException e) {
            // Пробрасываем бизнес-исключение как есть
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Невозможно проверить наличие товаров: " + ex.getMessage());
        }

        cart = cartRepository.save(cart);

        return cartMapper.toShoppingCartDto(cart);
    }

    @Override
    @Transactional
    public Boolean deleteCart(String username) {
        log.info("Запрос на деактивацию активной козины для пользователя: {}", username);
        usernameNotEmptyOrThrow(username);
        ShoppingCart cart = getActiveCartOrThrow(username);
        cart.setActive(false);
        log.info("Корзина деактивирована");

        return true;
    }

    @Override
    @Transactional
    public ShoppingCartDto deleteProducts(String username, List<UUID> productIds) {
        log.info("Запрос на удаление товаров из корзины пользователя: {}", username);
        usernameNotEmptyOrThrow(username);
        ShoppingCart cart = getActiveCartOrThrow(username);

        if (productIds == null || productIds.isEmpty()) {
            throw new ValidateException("Список продуктов к удалению из корзины не должен быть пустым");
        }

        Set<UUID> idsToRemove = new HashSet<>(productIds);
        boolean removed = cart.getItems().removeIf(item -> idsToRemove.contains(item.getProductId()));

        if (!removed) {
            throw new NoProductsInShoppingCartException("Товары не найдены в корзине: " + productIds);
        }

        return cartMapper.toShoppingCartDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest changeQuantity) {
        log.info("Запрос на изменение количества товаров в корзине пользователя: {}", username);
        usernameNotEmptyOrThrow(username);
        ShoppingCart cart = getActiveCartOrThrow(username);
        UUID productId = changeQuantity.getProductId();
        Integer newQuantity = changeQuantity.getNewQuantity();

        if (newQuantity == null || newQuantity <= 0) {
            throw new ValidateException("Некорректное количество товара: " + newQuantity);
        }

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItemOpt.isEmpty()) {
            throw new NoProductsInShoppingCartException("Товар " + productId + " не найден в корзине " + username);
        }

        existingItemOpt.get().setQuantity(newQuantity);
        ShoppingCartDto shoppingCartDto = cartMapper.toShoppingCartDto(cart);
        warehouseClient.checkAvailabilityForCart(shoppingCartDto);

        return shoppingCartDto;
    }

    private ShoppingCart createNewActiveCart(String username) {
        log.info("Создание новой корзины для пользователя: {}", username);

        return ShoppingCart.builder()
                .username(username)
                .active(true)
                .build();
    }

    private void setProductsToCart(ShoppingCart cart, Map<UUID, Integer> products) {
        Map<UUID, CartItem> existingItems = cart.getItems().stream()
                .collect(Collectors.toMap(CartItem::getProductId, Function.identity()));

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == null || quantity <= 0) {
                throw new ValidateException("Количество продукта " + productId + " = " + quantity);
            }

            CartItem existingItem = existingItems.get(productId);

            if (existingItem != null) {
                existingItem.setQuantity(quantity);
            } else {
                CartItem newItem = new CartItem();
                newItem.setProductId(productId);
                newItem.setQuantity(quantity);
                newItem.setShoppingCart(cart);
                cart.getItems().add(newItem);
            }
        }
    }

    private void usernameNotEmptyOrThrow(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя не должно быть пустым");
        }
    }

    private ShoppingCart getActiveCartOrThrow(String username) {
        return cartRepository.findByUsernameAndActiveTrue(username).orElseThrow(() ->
                new NotFoundException("Не найдена активная корзина пользователя " + username));
    }
}