package com.food.ordering.system.order.service.messaging.mapper;

import com.food.ordering.system.domain.event.payload.RestaurantOrderEventPayload;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.kafka.order.avro.model.*;
import com.food.ordering.system.order.service.domain.dto.message.CustomerModel;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.domain.event.payload.OrderApprovalEventPayload;
import com.food.ordering.system.domain.event.payload.PaymentOrderEventPayload;
import debezium.payment.order_outbox.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderMessagingDataMapper {

	public PaymentResponse paymentResponseAvroModelToPaymentResponse(PaymentOrderEventPayload paymentOrderEventPayload,
																	 Value paymentResponseAvroModel) {
		return PaymentResponse.builder()
                .id(paymentResponseAvroModel.getId())
                .sagaId(paymentResponseAvroModel.getSagaId())
				.paymentId(paymentOrderEventPayload.getPaymentId())
                .customerId(paymentOrderEventPayload.getCustomerId())
                .orderId(paymentOrderEventPayload.getOrderId())
                .price(paymentOrderEventPayload.getPrice())
                .createdAt(Instant.parse(paymentResponseAvroModel.getCreatedAt()))
                .paymentStatus(PaymentStatus.valueOf(paymentOrderEventPayload.getPaymentStatus()))
                .failureMessages(paymentOrderEventPayload.getFailureMessages())
                .build();
	}

	public RestaurantApprovalResponse approvalResponseAvroModelToApprovalResponse(RestaurantOrderEventPayload restaurantOrderEventPayload,
																				  debezium.restaurant.order_outbox.Value restaurantApprovalResponseAvroModel) {
		return RestaurantApprovalResponse.builder()
				.id(restaurantApprovalResponseAvroModel.getId())
				.sagaId(restaurantApprovalResponseAvroModel.getSagaId())
				.restaurantId(restaurantOrderEventPayload.getRestaurantId())
				.orderId(restaurantOrderEventPayload.getOrderId())
				.createdAt(Instant.parse(restaurantApprovalResponseAvroModel.getCreatedAt()))
				.orderApprovalStatus(OrderApprovalStatus.valueOf(restaurantOrderEventPayload.getOrderApprovalStatus()))
				.failureMessages(restaurantOrderEventPayload.getFailureMessages())
				.build();
	}

	public RestaurantApprovalRequestAvroModel orderApprovalEventToRestaurantApprovalRequestAvroModel(String sagaId,
																									 OrderApprovalEventPayload orderApprovalEventPayload) {
		return RestaurantApprovalRequestAvroModel.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setSagaId(sagaId)
				.setOrderId(orderApprovalEventPayload.getOrderId())
				.setRestaurantId(orderApprovalEventPayload.getRestaurantId())
				.setRestaurantOrderStatus(RestaurantOrderStatus.valueOf(orderApprovalEventPayload.getRestaurantOrderStatus()))
				.setProducts(orderApprovalEventPayload.getProducts().stream().map(orderApprovalEventProduct ->
						Product.newBuilder()
								.setId(orderApprovalEventProduct.getId())
								.setQuantity(orderApprovalEventProduct.getQuantity())
								.build()).collect(Collectors.toList()))
				.setPrice(orderApprovalEventPayload.getPrice())
				.setCreatedAt(orderApprovalEventPayload.getCreatedAt().toInstant())
				.build();
	}

	public CustomerModel customerAvroModelToCustomerModel(CustomerAvroModel customerAvroModel) {
		return CustomerModel.builder()
				.id(customerAvroModel.getId())
				.username(customerAvroModel.getUsername())
				.firstName(customerAvroModel.getFirstName())
				.lastName(customerAvroModel.getLastName())
				.build();
	}
}
