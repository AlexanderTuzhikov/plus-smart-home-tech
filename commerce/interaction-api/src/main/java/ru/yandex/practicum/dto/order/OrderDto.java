package ru.yandex.practicum.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class OrderDto {
    private UUID orderId;
    private UUID shoppingCartId;
    @NotNull
    @NotEmpty
    private Map<UUID, @Positive Long> products;
    private UUID paymentId;
    private UUID deliveryId;
    @NotNull
    private OrderState orderState;
    @NotNull
    private Double deliveryWeight;
    @NotNull
    private Double deliveryVolume;
    @NotNull
    private Boolean fragile;
    @NotNull
    @Positive
    private BigDecimal totalPrice;
    @NotNull
    @PositiveOrZero
    private BigDecimal deliveryPrice;
    @NotNull
    @Positive
    private BigDecimal productPrice;
}