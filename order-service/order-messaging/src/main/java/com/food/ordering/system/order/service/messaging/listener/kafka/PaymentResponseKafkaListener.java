package com.food.ordering.system.order.service.messaging.listener.kafka;

import com.food.ordering.system.kafak.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.messaging.DebeziumOp;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.domain.event.payload.PaymentOrderEventPayload;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import debezium.payment.order_outbox.Envelope;
import debezium.payment.order_outbox.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseKafkaListener implements KafkaConsumer<Envelope> {

	private final PaymentResponseMessageListener paymentResponseMessageListener;
	private final OrderMessagingDataMapper orderMessagingDataMapper;

	private final KafkaMessageHelper kafkaMessageHelper;

	@Override
	@KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
			topics = "${order-service.payment-response-topic-name}")
	public void receive(
			@Payload List<Envelope> messages,
			@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {
		log.info("{} number of payment responses received ",
				messages.stream().filter(message -> message.getBefore() == null &&
						DebeziumOp.CREATE.getValue().equals(message.getOp())).toList().size());

		messages.forEach(avroModel -> {
			if(avroModel.getBefore() == null && DebeziumOp.CREATE.getValue().equals(avroModel.getOp())) {
				log.info("Incoming message in PaymentResponseKafkaListener: {}", avroModel);
				Value paymentResponseAvroModel = avroModel.getAfter();
				PaymentOrderEventPayload paymentOrderEventPayload = kafkaMessageHelper.getOrderEventPayload(paymentResponseAvroModel.getPayload(), PaymentOrderEventPayload.class);
				try {
					if(PaymentStatus.COMPLETED.name().equals(paymentResponseAvroModel.getPaymentStatus())) {
						log.info("Processing successful payment for order id: {}", paymentOrderEventPayload.getOrderId());
						paymentResponseMessageListener.paymentCompleted(orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(paymentOrderEventPayload, paymentResponseAvroModel));
					} else if (PaymentStatus.CANCELLED.name().equals(paymentResponseAvroModel.getPaymentStatus()) ||
							PaymentStatus.FAILED.name().equals(paymentResponseAvroModel.getPaymentStatus())) {
						log.info("Processing unsuccessful payment for order id: {}", paymentOrderEventPayload.getOrderId());
						paymentResponseMessageListener.paymentCancelled(orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(paymentOrderEventPayload, paymentResponseAvroModel));
					}
				} catch (OptimisticLockingFailureException e) {
					// No-Op for optimistic lock. This means another thread finished the work, do not throw error to prevent reading the data from kafka again
					// 다른 스레드에서 작업이 완료되었으므로 의도된 No operation. 해당 메세지가 다시 동작되지 않게 에러를 무시하시오.
					log.error("Caught optimistic locking exception in PaymentResponseKafkaListener for order id: {}",
							paymentOrderEventPayload.getOrderId());
				} catch (OrderNotFoundException e) {
					// No-Op for OrderNotFoundException
					log.error("No order found for order id: {}", paymentOrderEventPayload.getOrderId());
				} catch (DataAccessException e) {
					SQLException sqlException = (SQLException) e.getRootCause();
					if(sqlException != null && sqlException.getSQLState() != null && PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
						// NO-OP for unique constraint exception
						log.error("Caught unique constraint exception with sql state: {} "
										+ "in PaymentResponseKafkaListener for order id :{}",
								sqlException.getSQLState(), paymentOrderEventPayload.getOrderId());
					}
				}
			}
		});
	}
}