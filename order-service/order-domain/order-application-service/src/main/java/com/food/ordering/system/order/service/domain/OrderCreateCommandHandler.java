package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateCommandHandler {

	private final OrderCreateHelper orderCreateHelper;
	private final OrderDateMapper orderDateMapper;

	public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
		OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
		log.info("Order created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
		orderCreatedEvent.fire();

		return orderDateMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(), "Order Created Successfully");
	}
}
