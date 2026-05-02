package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.model.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "totalPayment", expression = "java(payment.getTotalPayment())")
    PaymentDto toDto(Payment payment);
}