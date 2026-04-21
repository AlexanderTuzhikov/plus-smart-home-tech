package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.WarehouseProduct;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface WarehouseProductMapper {
    @Mapping(target = "weight", source = "weight")
    @Mapping(target = "width", source = "dimension.width")
    @Mapping(target = "height", source = "dimension.height")
    @Mapping(target = "depth", source = "dimension.depth")
    @Mapping(target = "quantity", constant = "0")
    @Mapping(target = "fragile", defaultValue = "false")
    WarehouseProduct toWarehouseProduct(NewProductInWarehouseRequest newProductInWarehouseRequest);

    default BigDecimal map(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}