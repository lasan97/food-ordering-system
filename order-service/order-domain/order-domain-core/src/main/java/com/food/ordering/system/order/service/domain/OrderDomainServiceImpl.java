package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.domain.exception.DomainException;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.food.ordering.system.domain.DomainConstants.UTC;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

	@Override
	public OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant) {
		validateRestaurant(restaurant);
		setOrderProductInformation(order, restaurant);
		order.validateOrder();
		order.initializeOrder();
		log.info("Order with id: {} in initiated", order.getId().getValue());

		return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
	}

	@Override
	public OrderPaidEvent payOrder(Order order) {
		order.pay();
		log.info("Order with id: {} is paid", order.getId().getValue());

		return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
	}

	@Override
	public void approveOrder(Order order) {
		order.approve();
		log.info("Order with id: {} is approved", order.getId().getValue());
	}

	@Override
	public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {
		order.initCancel(failureMessages);
		log.info("Order payment is cancelling for order id: {}", order.getId().getValue());
		return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
	}

	@Override
	public void cancelOrder(Order order, List<String> failureMessages) {
		order.cancel(failureMessages);
		log.info("Order with id: {} is cancelled", order.getId().getValue());
	}

	private void validateRestaurant(Restaurant restaurant) {
		if(!restaurant.isActive()) {
			throw new DomainException("Restaurant with id " + restaurant.getId().getValue()
					+ " is currently not active");
		}
	}

	private void setOrderProductInformation(Order order, Restaurant restaurant) {
		Map<ProductId, Product> productMap = new HashMap<>();
		restaurant.getProducts().forEach(restaurantProduct -> productMap.put(restaurantProduct.getId(), restaurantProduct));

		order.getItems().forEach(orderItem -> {
			Product currentProduct = orderItem.getProduct();
			if (productMap.containsKey(currentProduct.getId())) {
				Product restaurantProduct = productMap.get(currentProduct.getId());
				currentProduct.updateWithConfirmedNameAndPrice(
						restaurantProduct.getName(),
						restaurantProduct.getPrice()
				);
			}
		});
	}
}
