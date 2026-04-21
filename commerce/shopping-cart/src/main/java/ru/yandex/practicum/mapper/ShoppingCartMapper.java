package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ShoppingCartMapper {

    @Mapping(target = "products", source = "items")
    ShoppingCartDto toShoppingCartDto(ShoppingCart shoppingCart);

    default Map<UUID, Integer> mapItems(List<CartItem> items) {
        if (items == null) return Map.of();

        return items.stream()
                .collect(Collectors.toMap(
                        CartItem::getProductId,
                        CartItem::getQuantity
                ));
    }
}