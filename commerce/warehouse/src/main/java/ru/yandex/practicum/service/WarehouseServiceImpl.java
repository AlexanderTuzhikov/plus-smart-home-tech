package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseProductRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseProductRepository warehouseRepository;
    private final WarehouseProductMapper warehouseMapper;

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    @Override
    @Transactional
    public void addProduct(NewProductInWarehouseRequest newProduct) {
        log.info("Запрос на добавление нового товара на склад: {}", newProduct);

        if (warehouseRepository.existsById(newProduct.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Товар уже зарегистрирован на складе"
            );
        }

        WarehouseProduct product = warehouseMapper.toWarehouseProduct(newProduct);
        warehouseRepository.save(product);
        log.info("Товар {} добавлен на склад", product.getProductId());
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkAvailabilityForCart(ShoppingCartDto shoppingCartDto) {
        log.info("Проверка количества товаров для корзины {}", shoppingCartDto.getShoppingCartId());
        return checkAvailabilityForProductsMap(shoppingCartDto.getProducts());
    }

    @Override
    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest addProductDto) {
        log.info("Запрос на обновление количества товара на склад: {}", addProductDto);
        UUID productId = addProductDto.getProductId();
        validateQuantity(addProductDto.getQuantity());
        WarehouseProduct product = checkIdExistsOrThrow(productId);
        product.setQuantity(product.getQuantity() + addProductDto.getQuantity());
        warehouseRepository.save(product);

        log.info("Количество товара {} обновлено. Новое количество: {}",
                productId, product.getQuantity());
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("Запрос на получение адреса склада");
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    private BookedProductsDto checkAvailabilityForProductsMap(Map<UUID, Integer> products) {
        Map<UUID, WarehouseProduct> warehouseProductsMap =
                getWarehouseProductsMap(products.keySet());

        return calculateAndValidateBookedProducts(products, warehouseProductsMap);
    }

    private BookedProductsDto calculateAndValidateBookedProducts(
            Map<UUID, Integer> requestedProducts,
            Map<UUID, WarehouseProduct> warehouseProductsMap
    ) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        boolean isAnyFragile = false;

        for (Map.Entry<UUID, Integer> entry : requestedProducts.entrySet()) {
            UUID productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            validateQuantity(requestedQuantity);

            WarehouseProduct product = warehouseProductsMap.get(productId);

            if (product == null) {
                throw new NoSpecifiedProductInWarehouseException(
                        "Продукт с ID " + productId + " не существует"
                );
            }

            checkEnoughQuantityOrThrow(product, requestedQuantity);

            BigDecimal qty = BigDecimal.valueOf(requestedQuantity);
            totalWeight = totalWeight.add(product.getWeight().multiply(qty));

            BigDecimal volume = product.getWidth()
                    .multiply(product.getHeight())
                    .multiply(product.getDepth())
                    .multiply(qty);
            totalVolume = totalVolume.add(volume);

            if (Boolean.TRUE.equals(product.getFragile())) {
                isAnyFragile = true;
            }
        }

        return new BookedProductsDto(
                totalWeight.doubleValue(),
                totalVolume.doubleValue(),
                isAnyFragile
        );
    }

    private WarehouseProduct checkIdExistsOrThrow(UUID productId) {
        return warehouseRepository.findById(productId).orElseThrow(() ->
                new NoSpecifiedProductInWarehouseException(
                        "Продукт с ID " + productId + " не существует"
                )
        );
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }
    }

    private void checkEnoughQuantityOrThrow(WarehouseProduct product, Integer requestedQuantity) {
        if (product.getQuantity() < requestedQuantity) {
            throw new ProductInShoppingCartLowQuantityInWarehouseException(
                    String.format(
                            "Не хватает товара %s. Требуется: %d, доступно: %d",
                            product.getProductId(),
                            requestedQuantity,
                            product.getQuantity()
                    )
            );
        }
    }

    private Map<UUID, WarehouseProduct> getWarehouseProductsMap(Set<UUID> productIds) {
        return toProductMap(warehouseRepository.findAllById(productIds));
    }

    private Map<UUID, WarehouseProduct> toProductMap(Collection<WarehouseProduct> products) {
        Map<UUID, WarehouseProduct> result = new HashMap<>();

        for (WarehouseProduct product : products) {
            result.put(product.getProductId(), product);
        }

        return result;
    }
}