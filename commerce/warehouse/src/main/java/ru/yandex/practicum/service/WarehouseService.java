package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {
    void createProduct(NewProductInWarehouseRequest newProduct);

    BookedProductsDto checkAvailabilityForCart(ShoppingCartDto shoppingCartDto);

    void addProductQuantity(AddProductToWarehouseRequest addProductDto);

    AddressDto getWarehouseAddress();

    void returnProducts(Map<UUID, Long> products);

    void shippedToDelivery(ShippedToDeliveryRequest deliveryRequest);
}