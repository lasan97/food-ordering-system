package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.mapper.OrderDateMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTrackingCommandHandler {

	private final OrderDateMapper orderDateMapper;
	private final OrderRepository orderRepository;

	@Transactional(readOnly = true)
	public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
		Optional<Order> orderResult = orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));

		if(orderResult.isEmpty()) {
			log.warn("Could not find order with trakcing id: {}", trackOrderQuery.getOrderTrackingId());
			throw new OrderNotFoundException("Could not find order with trakcing id:"
					+ trackOrderQuery.getOrderTrackingId());
		}
		return orderDateMapper.orderToTrackOrderResponse(orderResult.get());
	}
}
