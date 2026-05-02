package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;

import java.util.UUID;

public interface ShoppingStoreService {
    ProductDto putProduct(ProductDto productDto);

    ProductDto postUpdateProduct(ProductDto productDto);

    Boolean postSetQuantityState(SetProductQuantityStateRequest request);

    Boolean postDeleteProduct(UUID productId);

    ProductDto getProduct(UUID productId);

    Page<ProductDto> getProductsByCategory(ProductCategory productCategory, Pageable pageable);
}