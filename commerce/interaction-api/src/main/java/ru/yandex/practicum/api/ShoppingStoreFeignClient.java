package ru.yandex.practicum.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreFeignClient {
    @PutMapping
    ProductDto putProduct(@RequestBody ProductDto productDto);

    @PostMapping
    ProductDto postUpdateProduct(@RequestBody ProductDto productDto);

    @PostMapping("/quantityState")
    Boolean postSetQuantityState(SetProductQuantityStateRequest request);

    @PostMapping("/removeProductFromStore")
    Boolean postDeleteProduct(@RequestBody UUID productId);

    @GetMapping("{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);

    @GetMapping
    Page<ProductDto> getProductsByCategory(@RequestParam("category") ProductCategory category, Pageable pageable);
}