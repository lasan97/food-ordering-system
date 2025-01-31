package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDateMapper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateCommandHandler {

	private final OrderCreateHelper orderCreateHelper;
	private final OrderDateMapper orderDateMapper;
	private final PaymentOutboxHelper paymentOutboxHelper;
	private final OrderSagaHelper orderSagaHelper;

	@Transactional
	public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
		OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
		log.info("Order created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
		CreateOrderResponse createOrderResponse = orderDateMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(),
				"Order Created Successfully");

		paymentOutboxHelper.savePaymentOutboxMessage(orderDateMapper.orderCreatedEventToOrderPaymentEventPayload(orderCreatedEvent),
				orderCreatedEvent.getOrder().getOrderStatus(),
				orderSagaHelper.orderStatusToSagaStatus(orderCreatedEvent.getOrder().getOrderStatus()),
				OutboxStatus.STARTED,
				UUID.randomUUID());

		log.info("Returning CreateOrderResponse with order id: {}", orderCreatedEvent.getOrder().getId());

		return createOrderResponse;
	}
}
