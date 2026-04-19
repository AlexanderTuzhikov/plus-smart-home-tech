package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseFeighClient {
    @PutMapping
    void addProduct(@RequestBody NewProductInWarehouseRequest newProduct);

    @PostMapping("/check")
    BookedProductsDto checkAvailabilityForCart(@RequestBody ShoppingCartDto shoppingCartDto);

    @PostMapping("/add")
    void addProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest addProductDto);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();
}