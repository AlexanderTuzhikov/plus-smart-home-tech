package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.OrderFeignClient;
import ru.yandex.practicum.api.WarehouseFeighClient;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.AddressMapper;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.model.DeliveryCalculationContext;
import ru.yandex.practicum.model.DeliveryMapper;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryMapper deliveryMapper;
    private final DeliveryRepository deliveryRepository;
    private final OrderFeignClient orderClient;
    private final WarehouseFeighClient warehouseClient;
    private final DeliveryCalculator deliveryCalculator;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        log.info("Создать новую доставку {}", deliveryDto);
        Delivery delivery = deliveryMapper.toDelivery(deliveryDto);
        delivery.setDeliveryState(DeliveryState.CREATED);
        Delivery newDelivery = deliveryRepository.save(delivery);
        log.info("Создана новая доставка {}", newDelivery);

        return deliveryMapper.toDto(newDelivery);
    }

    @Override
    @Transactional
    public void deliverySuccessful(UUID orderId) {
        log.info("Доставка заказа {}", orderId);
        Delivery delivery = getByOrderIdOrThrow(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        log.info("Заказ доставлен {}", delivery);
    }

    @Override
    @Transactional
    public void deliveryPicked(UUID orderId) {
        log.info("Получение товара в доставку заказа {}", orderId);
        Delivery delivery = getByOrderIdOrThrow(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        orderClient.postAssemblyOrder(orderId);
        ShippedToDeliveryRequest deliveryRequest = new ShippedToDeliveryRequest(orderId, delivery.getDeliveryId());
        warehouseClient.ShippedToDelivery(deliveryRequest);
        log.info("Товара получен в доставку заказа {}", orderId);
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID orderId) {
        log.info("Ошибка доставки заказа {}", orderId);
        Delivery delivery = getByOrderIdOrThrow(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        log.info("Установлена ошибка доставки заказа {}", delivery);
    }

    @Override
    public BigDecimal getDeliveryCost(OrderDto orderDto) {
        log.info("Рассчитать полную стоимость доставки заказа {}", orderDto);
        AddressDto warehouse = warehouseClient.getWarehouseAddress();
        Delivery delivery = getByOrderIdOrThrow(orderDto.getOrderId());
        DeliveryCalculationContext ctx = toContext(orderDto, delivery, warehouse);
        BigDecimal deliveryCost = deliveryCalculator.calculate(ctx);
        log.info("Стоимость доставки заказа {}", deliveryCost);

        return deliveryCost;
    }

    private DeliveryCalculationContext toContext(OrderDto orderDto, Delivery delivery, AddressDto warehouse) {
        return new DeliveryCalculationContext(
                BigDecimal.valueOf(orderDto.getDeliveryWeight()),
                BigDecimal.valueOf(orderDto.getDeliveryVolume()),
                orderDto.getFragile(),
                warehouse,
                addressMapper.toDto(delivery.getFromAddress())
        );
    }

    private Delivery getByOrderIdOrThrow(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId).orElseThrow(() ->
                new NotFoundException("Доставка по заказу с ID " + orderId + " не существует"));
    }
}