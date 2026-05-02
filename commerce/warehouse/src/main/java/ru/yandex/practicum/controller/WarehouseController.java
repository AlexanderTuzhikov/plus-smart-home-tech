package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.WarehouseFeighClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping( "/api/v1/warehouse")
public class WarehouseController implements WarehouseFeighClient {
    private final WarehouseService warehouseService;

    @Override
    public void putProduct(@Valid @RequestBody NewProductInWarehouseRequest newProduct) {
        warehouseService.putProduct(newProduct);
    }

    @Override
    public BookedProductsDto postCheckAvailabilityForCart(@Valid @RequestBody ShoppingCartDto shoppingCartDto) {
        return warehouseService.postCheckAvailabilityForCart(shoppingCartDto);
    }

    @Override
    public void postAddProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest addProductDto) {
        warehouseService.postAddProductQuantity(addProductDto);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }

    @Override
    public void returnProducts(@Valid @RequestBody Map<UUID, Long> products) {
        warehouseService.returnProducts(products);
    }

    @Override
    public void ShippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest deliveryRequest) {
       warehouseService.shippedToDelivery(deliveryRequest);
    }
}