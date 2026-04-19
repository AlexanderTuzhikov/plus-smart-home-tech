package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.ProductState;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidateException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.StoreRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingStoreServiceImpl implements ShoppingStoreService {
    private final StoreRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        log.info("Запрос на добавление нового продукта Product: {}", productDto);
        UUID productId = productDto.getProductId();

        if (productId != null) {
            checkIdIsNewOrThrow(productId);
        }

        Product product = productMapper.toProduct(productDto);
        Product newProduct = productRepository.save(product);
        log.info("Продукт сохранен с Id: {}", newProduct.getProductId());

        return productMapper.toProductDto(newProduct);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        log.info("Запрос на обновление продукта с Id: {}", productDto.getProductId());
        UUID productId = productDto.getProductId();
        checkIdExistsOrThrow(productId);
        Product product = productMapper.toProduct(productDto);
        Product updatedProduct = productRepository.save(product);
        log.info("Продукт обновлен с Product: {}", updatedProduct);

        return productMapper.toProductDto(updatedProduct);
    }

    @Override
    @Transactional
    public Boolean setQuantityState(SetProductQuantityStateRequest request) {
        log.info("Запрос на обновление остатка продукта с Id: {}", request.getProductId());
        UUID productId = request.getProductId();
        Product product = checkIdExistsOrThrow(productId);
        product.setQuantityState(request.getQuantityState());
        Product updatedProduct = productRepository.save(product);
        Boolean result = updatedProduct.getQuantityState().equals(request.getQuantityState());

        if (result) {
            log.info("Остатки продукта с Id: {} обновлены", productId);
        } else {
            log.warn("Ошибка при обновлении остатков продукта с Id: {}", productId);
        }

        return result;
    }

    @Override
    @Transactional
    public Boolean deleteProduct(UUID productId) {
        log.info("Запрос на удаление продукта с Id: {}", productId);
        Product product = checkIdExistsOrThrow(productId);
        product.setProductState(ProductState.DEACTIVATE);
        Product deletedProduct = productRepository.save(product);
        Boolean result = deletedProduct.getProductState().equals(ProductState.DEACTIVATE);

        if (result) {
            log.info("Продукт с Id: {} удален", productId);
        } else {
            log.warn("Ошибка удаления продукта с Id: {}", productId);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        log.info("Запрос на получение продукта с Id: {}", productId);
        Product product = checkIdExistsOrThrow(productId);
        log.info("Продукт найден Product: {}", product);

        return productMapper.toProductDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(ProductCategory productCategory, Pageable pageable) {
        log.info("Запрос на получение списка продуктов из категории: {}", productCategory);
        return productRepository.findAllByProductCategory(productCategory, pageable)
                .map(productMapper::toProductDto);
    }

    private void checkIdIsNewOrThrow(UUID productId) {
        if (productRepository.existsById(productId)) {
            throw new ValidateException(
                    String.format("Продукт с ID %s уже существует", productId)
            );
        }
    }

    private Product checkIdExistsOrThrow(UUID productId) {
        return productRepository.findById(productId).orElseThrow(() ->
                new NotFoundException("Продукт с ID " + productId + " не найден"));
    }
}
