package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "payment.coefficient")
public class PaymentCoefficientProperties {
    private BigDecimal nds = BigDecimal.ZERO;
}