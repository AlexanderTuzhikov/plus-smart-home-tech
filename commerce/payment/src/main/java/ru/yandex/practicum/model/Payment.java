package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.dto.payment.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "payments", schema = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "products_total", precision = 10, scale = 2)
    private BigDecimal productsTotal;

    @Column(name = "delivery_total", precision = 10, scale = 2)
    private BigDecimal deliveryTotal;

    @Column(name = "fee_total", precision = 10, scale = 2)
    private BigDecimal feeTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PaymentState state;

    @Transient
    public BigDecimal getTotalPayment() {
        return productsTotal
                .add(deliveryTotal)
                .add(feeTotal);
    }
}