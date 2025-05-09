package com.food.ordering.system.restaurant.service.domain;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalRequestHelper {

	 private final RestaurantDomainService restaurantDomainService;
	 private final RestaurantDataMapper restaurantDataMapper;
	 private final RestaurantRepository restaurantRepository;
	 private final OrderApprovalRepository orderApprovalRepository;
	 private final OrderOutboxHelper orderOutboxHelper;

	 @Transactional
	 public void persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {

		 if(isOutboxMessageProcessed(restaurantApprovalRequest)) {
			 log.info("An outbox message with saga id: {} already saved to database", restaurantApprovalRequest.getSagaId());
			 return;
		 }

		 log.info("Processing restaurant approval for order id: {}", restaurantApprovalRequest.getOrderId());
		 List<String> failureMessages = new ArrayList<>();
		 Restaurant restaurant = findRestaurant(restaurantApprovalRequest);

		 OrderApprovalEvent orderApprovalEvent = restaurantDomainService.validateOrder(restaurant, failureMessages);
		 orderApprovalRepository.save(restaurant.getOrderApproval());

		 orderOutboxHelper.saveOrderOutboxMessage(restaurantDataMapper.orderApprovalEventToOrderEventPayload(orderApprovalEvent),
				 orderApprovalEvent.getOrderApproval().getApprovalStatus(),
				 OutboxStatus.STARTED,
				 UUID.fromString(restaurantApprovalRequest.getSagaId()));
	 }

	 private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
		 Restaurant restaurant = restaurantDataMapper.restaurantApprovalRequestToRestaurant(restaurantApprovalRequest);
		 Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantInformation(restaurant);
		 if(restaurantResult.isEmpty()) {
			 log.error("Restaurant with id " + restaurant.getId().getValue() + " not found");
             throw new RestaurantNotFoundException("Restaurant with id " + restaurant.getId().getValue() + " not found");
		 }

		 Restaurant restaurantEntity = restaurantResult.get();
		 restaurant.setActive(restaurantEntity.isActive());
		 restaurant.getOrderDetail().getProducts().forEach(product ->
				 restaurantEntity.getOrderDetail().getProducts().forEach(p -> {
					 if (p.getId().equals(product.getId())) {
						 product.updateWithConfirmedNamePriceAndAvailability(p.getName(), p.getPrice(), p.isAvailable());
					 }
				 }));
		 restaurant.getOrderDetail().setId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())));

		 return restaurant;
	 }

	 private boolean isOutboxMessageProcessed(RestaurantApprovalRequest restaurantApprovalRequest) {
		 Optional<OrderOutboxMessage> orderOutboxMessage = orderOutboxHelper
				 .getCompletedOrderOutboxMessageBySagaIdAndOutboxStatus(UUID.fromString(restaurantApprovalRequest.getSagaId()), OutboxStatus.COMPLETED);
		 return orderOutboxMessage.isPresent();
	 }
}
