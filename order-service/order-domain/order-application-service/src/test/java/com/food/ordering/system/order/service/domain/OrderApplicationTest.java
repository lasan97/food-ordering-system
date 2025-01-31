package com.food.ordering.system.order.service.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDateMapper;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationTest {

	@Autowired
	private OrderApplicationService orderApplicationService;
	@Autowired
	private OrderDateMapper orderDateMapper;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private RestaurantRepository restaurantRepository;
	@Autowired
	private PaymentOutboxRepository paymentOutboxRepository;
	@Autowired
	private ObjectMapper objectMapper;

	private CreateOrderCommand createOrderCommand;
	private CreateOrderCommand createOrderCommandWrongPrice;
	private CreateOrderCommand createOrderCommandWrongProductPrice;
	private final UUID CUSTOMER_ID = UUID.fromString("13bb955d-fa30-4665-84eb-7e7b311b0975");
	private final UUID RESTAURANT_ID = UUID.fromString("e02a44c2-d69a-4390-8bc4-8c8a486f41c7");
	private final UUID PRODUCT_ID = UUID.fromString("3d86874b-7021-4e62-a735-e231ace73cfe");
	private final UUID ORDER_ID = UUID.fromString("f4e0882e-df02-4acc-8bb4-f9ffd8d4bae9");
	private final UUID SAGA_ID = UUID.fromString("f4e0882e-df02-4acc-8bb4-f9ffd8d4bae9");
	private final BigDecimal PRICE = new BigDecimal("200.00");

	@BeforeAll
	public void init() {
		createOrderCommand = CreateOrderCommand.builder()
				.customerId(CUSTOMER_ID)
				.restaurantId(RESTAURANT_ID)
				.address(OrderAddress.builder()
						.street("street")
						.postalCode("1000GU")
						.city("SEOUL")
						.build())
				.price(PRICE)
				.items(List.of(OrderItem.builder()
								.productId(PRODUCT_ID)
								.quantity(1)
								.price(new BigDecimal("50.00"))
								.subTotal(new BigDecimal("50.00"))
								.build(),
						OrderItem.builder()
								.productId(PRODUCT_ID)
								.quantity(3)
								.price(new BigDecimal("50.00"))
								.subTotal(new BigDecimal("150.00"))
								.build()))
				.build();

		createOrderCommandWrongPrice = CreateOrderCommand.builder()
				.customerId(CUSTOMER_ID)
				.restaurantId(RESTAURANT_ID)
				.address(OrderAddress.builder()
						.street("street")
						.postalCode("1000GU")
						.city("SEOUL")
						.build())
				.price(new BigDecimal("250.00"))
				.items(List.of(OrderItem.builder()
								.productId(PRODUCT_ID)
								.quantity(1)
								.price(new BigDecimal("50.00"))
								.subTotal(new BigDecimal("50.00"))
								.build(),
						OrderItem.builder()
								.productId(PRODUCT_ID)
								.quantity(3)
								.price(new BigDecimal("50.00"))
								.subTotal(new BigDecimal("150.00"))
								.build()))
				.build();

		createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
				.customerId(CUSTOMER_ID)
				.restaurantId(RESTAURANT_ID)
				.address(OrderAddress.builder()
						.street("street")
						.postalCode("1000GU")
						.city("SEOUL")
						.build())
				.price(new BigDecimal("210.00"))
				.items(List.of(OrderItem.builder()
								.productId(PRODUCT_ID)
								.quantity(1)
								.price(new BigDecimal("60.00"))
								.subTotal(new BigDecimal("60.00"))
								.build(),
						OrderItem.builder()
								.productId(PRODUCT_ID)
								.quantity(3)
								.price(new BigDecimal("50.00"))
								.subTotal(new BigDecimal("150.00"))
								.build()))
				.build();

		Customer customer = new Customer();
		customer.setId(new CustomerId(CUSTOMER_ID));

		Restaurant restaurantResponse = Restaurant.builder()
				.restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
				.products(List.of(
						new Product(new ProductId(PRODUCT_ID), "product-1",
								new Money(new BigDecimal("50.00"))),
						new Product(new ProductId(PRODUCT_ID), "product-2",
								new Money(new BigDecimal("50.00")))))
				.active(true)
				.build();

		Order order = orderDateMapper.createOrderCommandToOrder(createOrderCommand);
		order.setId(new OrderId(ORDER_ID));

		when(customerRepository.findCustomer(CUSTOMER_ID))
				.thenReturn(Optional.of(customer));
		when(restaurantRepository.findRestaurantInformation(orderDateMapper.createOrderCommandToRestaurant(createOrderCommand)))
				.thenReturn(Optional.of(restaurantResponse));
		when(orderRepository.save(any(Order.class))).thenReturn(order);
		when(paymentOutboxRepository.save(any(OrderPaymentOutboxMessage.class))).thenReturn(getOrderPaymentOutboxMessage());
	}

	@Test
	void testCreateOrder() {
		CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
		assertEquals(OrderStatus.PENDING, createOrderResponse.getOrderStatus());
		assertEquals("Order Created Successfully", createOrderResponse.getMessage());
		assertNotNull(createOrderResponse.getOrderTrackingId());
	}

	@Test
	void testCreateOrderWithWrongTotalPrice() {
		OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
				() -> orderApplicationService.createOrder(createOrderCommandWrongPrice));
		assertEquals("Total price: 250.00 is not equal to Order items total: 200.00",
				orderDomainException.getMessage());
	}

	@Test
	void testCreateOrderWithWrongProductPrice() {
		OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
				() -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));
		assertEquals(orderDomainException.getMessage(),
				"Order item price: 60.00 is not valid for product" + PRODUCT_ID);
	}

	void testCreateOrderWithPassiveRestaurant() {
		Restaurant restaurantResponse = Restaurant.builder()
				.restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
				.products(List.of(
						new Product(new ProductId(PRODUCT_ID), "product-1",
								new Money(new BigDecimal("50.00"))),
						new Product(new ProductId(PRODUCT_ID), "product-2",
								new Money(new BigDecimal("50.00")))))
				.active(false)
				.build();

		when(restaurantRepository.findRestaurantInformation(orderDateMapper.createOrderCommandToRestaurant(createOrderCommand)))
				.thenReturn(Optional.of(restaurantResponse));
		OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
				() -> orderApplicationService.createOrder(createOrderCommand));

		assertEquals(orderDomainException.getMessage(),
				"Restaurant with id " + RESTAURANT_ID + " is currently not active");
	}

	private OrderPaymentOutboxMessage getOrderPaymentOutboxMessage() {
		OrderPaymentEventPayload orderPaymentEventPayload = OrderPaymentEventPayload.builder()
				.orderId(ORDER_ID.toString())
				.customerId(CUSTOMER_ID.toString())
				.price(PRICE)
				.createdAt(ZonedDateTime.now())
				.paymentOrderStatus(PaymentOrderStatus.PENDING.name())
				.build();

		return OrderPaymentOutboxMessage.builder()
				.id(UUID.randomUUID())
				.sagaId(SAGA_ID)
				.createdAt(ZonedDateTime.now())
				.type(ORDER_SAGA_NAME)
				.payload(createPayload(orderPaymentEventPayload))
				.orderStatus(OrderStatus.PENDING)
				.sagaStatus(SagaStatus.STARTED)
				.outboxStatus(OutboxStatus.STARTED)
				.version(0)
				.build();
	}

	private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
		try {
			return objectMapper.writeValueAsString(orderPaymentEventPayload);
		} catch (JsonProcessingException e) {
			throw new OrderDomainException("Cannot create OrderPaymentEventPayload object");
		}
	}
}
