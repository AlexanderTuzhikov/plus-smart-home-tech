package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.WarehouseFeighClient;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping( "/api/v1/warehouse")
public class WarehouseController implements WarehouseFeighClient {
    private final WarehouseService warehouseService;

    @Override
    public void addProduct(@Valid @RequestBody NewProductInWarehouseRequest newProduct) {
        warehouseService.addProduct(newProduct);
    }

    @Override
    public BookedProductsDto checkAvailabilityForCart(@Valid @RequestBody ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkAvailabilityForCart(shoppingCartDto);
    }

    @Override
    public void addProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest addProductDto) {
        warehouseService.addProductQuantity(addProductDto);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }
}