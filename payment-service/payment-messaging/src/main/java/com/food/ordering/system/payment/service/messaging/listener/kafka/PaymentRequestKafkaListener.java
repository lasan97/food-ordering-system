package com.food.ordering.system.payment.service.messaging.listener.kafka;

import com.food.ordering.system.domain.event.payload.OrderPaymentEventPayload;
import com.food.ordering.system.kafak.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.messaging.DebeziumOp;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import debezium.order.payment_outbox.Envelope;
import debezium.order.payment_outbox.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
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
public class PaymentRequestKafkaListener implements KafkaConsumer<Envelope> {

	private final PaymentRequestMessageListener paymentRequestMessageListener;
	private final PaymentMessagingDataMapper paymentMessagingDataMapper;

	private final KafkaMessageHelper kafkaMessageHelper;

	@Override
	@KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
			topics = "${payment-service.payment-request-topic-name}")
	public void receive(@Payload List<Envelope> messages,
						@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
						@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
						@Header(KafkaHeaders.OFFSET) List<Long> offsets) {
		log.info("{} number of payment requests received ",
				messages.stream().filter(message -> message.getBefore() == null &&
						DebeziumOp.CREATE.name().equals(message.getOp())).toList().size());

		messages.forEach(avroModel -> {
			if(avroModel.getBefore() == null && DebeziumOp.CREATE.name().equals(avroModel.getOp())) {
				log.info("Incoming message in PaymentResponseKafkaListener: {}", avroModel);
				Value paymentRequestAvroModel = avroModel.getAfter();
				OrderPaymentEventPayload orderPaymentEventPayload = kafkaMessageHelper.getOrderEventPayload(paymentRequestAvroModel.getPayload(), OrderPaymentEventPayload.class);
				try {
					if(PaymentOrderStatus.PENDING.name().equals(orderPaymentEventPayload.getPaymentOrderStatus())) {
						log.info("Processing payment for order id: {}", orderPaymentEventPayload.getOrderId());
						paymentRequestMessageListener.completePayment(paymentMessagingDataMapper
								.paymentRequestAvroModelToPAymentRequest(orderPaymentEventPayload, paymentRequestAvroModel));
					} else if (PaymentOrderStatus.CANCELLED.name().equals(orderPaymentEventPayload.getPaymentOrderStatus())) {
						log.info("Cancelling payment for order id: {}", orderPaymentEventPayload.getOrderId());
						paymentRequestMessageListener.cancelPayment(paymentMessagingDataMapper
								.paymentRequestAvroModelToPAymentRequest(orderPaymentEventPayload, paymentRequestAvroModel));
					}
				} catch (DataAccessException e) {
					SQLException sqlException = (SQLException) e.getRootCause();
					if (sqlException != null && sqlException.getSQLState() != null &&
							PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
						// NO-OP for unique constraint exception
						log.error("Caught unique constraint exception with sql state: {} "
								+ "in PaymentRequestKafkaListener for order id: {}",
								sqlException.getSQLState(), orderPaymentEventPayload.getOrderId());
					} else {
						throw new PaymentApplicationServiceException("Throwing DataAccessException in"
								+ " PaymentRequestKafkaListener: " + e.getMessage(), e);
					}
				} catch (PaymentNotFoundException e) {
					// NO-OP for PaymentNotFoundException
					log.error("No payment found for order id: {}", orderPaymentEventPayload.getOrderId());
				}
			}
		});

	}
}
