package com.food.ordering.system.order.service.domain.event;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public abstract class OrderEvent implements DomainEvent<Order> {

	private Order order;
	private ZonedDateTime createdAt;

	public OrderEvent(Order order, ZonedDateTime createdAt) {
		this.order = order;
		this.createdAt = createdAt;
	}

	public Order getOrder() {
		return order;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}
}
