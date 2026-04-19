package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.ShoppingStoreFeignClient;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping( "/api/v1/shopping-store")
public class ShoppingStoreController implements ShoppingStoreFeignClient {
    private final ShoppingStoreService storeService;

    @Override
    public ProductDto createProduct(@Valid ProductDto productDto) {
       return storeService.createProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(@Valid ProductDto productDto) {
        return storeService.updateProduct(productDto);
    }

    @Override
    public Boolean setQuantityState(SetProductQuantityStateRequest setProductQuantityStateRequest) {
        return storeService.setQuantityState(setProductQuantityStateRequest);
    }

    @Override
    public Boolean deleteProduct(@RequestBody UUID productId) {
        return storeService.deleteProduct(productId);
    }

    @Override
    public ProductDto getProduct(@PathVariable UUID productId) {
        return storeService.getProduct(productId);
    }

    @Override
    public Page<ProductDto> getProductsByCategory(
            @RequestParam ProductCategory category, Pageable pageable) {
        return storeService.getProductsByCategory(category, pageable);
    }
}