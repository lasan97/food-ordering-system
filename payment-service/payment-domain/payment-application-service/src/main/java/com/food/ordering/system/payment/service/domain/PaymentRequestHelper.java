package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
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
public class PaymentRequestHelper {

	private final PaymentDomainService paymentDomainService;
	private final PaymentDataMapper paymentDataMapper;
	private final PaymentRepository paymentRepository;
	private final CreditEntryRepository creditEntryRepository;
	private final CreditHistoryRepository creditHistoryRepository;
	private final OrderOutboxHelper orderOutboxHelper;

	@Transactional
	public void persistPayment(PaymentRequest paymentRequest) {

		if(isOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.COMPLETED)) {
			log.info("An outbox message with saga id: {} is already saved to database",
					paymentRequest.getSagaId());
			return;
		}

		log.info("Received payment complete event for order id: {}", paymentRequest.getOrderId());
		Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
		CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
		List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
		List<String> failureMessages = new ArrayList<>();
		PaymentEvent paymentEvent = paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistories, failureMessages);
		persistDbObjects(payment, creditEntry, creditHistories, failureMessages);

		orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
				paymentEvent.getPayment().getPaymentStatus(),
				OutboxStatus.STARTED,
				UUID.fromString(paymentRequest.getSagaId()));
	}

	@Transactional
	public void persistCancelPayment(PaymentRequest paymentRequest) {
		if(isOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.CANCELLED)) {
			log.info("An outbox message with saga id: {} is already saved to database",
					paymentRequest.getSagaId());
			return;
		}

		log.info("Received payment rollback event for order id: {}", paymentRequest.getOrderId());
		Optional<Payment> paymentResponse = paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));

		if(paymentResponse.isEmpty()) {
			log.error("Payment with order id: {} could not be found", paymentRequest.getOrderId());
			throw new PaymentNotFoundException("Payment with order id: "
                    + paymentRequest.getOrderId() + " could not be found");
		}
		Payment payment = paymentResponse.get();
		CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
		List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
		List<String> failureMessages = new ArrayList<>();
		PaymentEvent paymentEvent = paymentDomainService.validateAndCancelPayment(payment, creditEntry, creditHistories, failureMessages);
		persistDbObjects(payment, creditEntry, creditHistories, failureMessages);

		orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
				paymentEvent.getPayment().getPaymentStatus(),
				OutboxStatus.STARTED,
				UUID.fromString(paymentRequest.getSagaId()));
	}

	private CreditEntry getCreditEntry(CustomerId customerId) {
		Optional<CreditEntry> creditEntry = creditEntryRepository.findByCustomerId(customerId);
		if(creditEntry.isEmpty()) {
			log.error("Could not find credit entry for customer: {}", customerId.getValue());
			throw new PaymentApplicationServiceException("Could not find credit entry for customer: "
					+ customerId.getValue());
		}
		return creditEntry.get();
	}

	private List<CreditHistory> getCreditHistory(CustomerId customerId) {
		Optional<List<CreditHistory>> creditHistories = creditHistoryRepository.findByCustomerId(customerId);
		if(creditHistories.isEmpty()) {
			log.error("Could not find credit history for customer: {}", customerId.getValue());
			throw new PaymentApplicationServiceException("Could not find credit history for customer: "
					+ customerId.getValue());
		}
		return creditHistories.get();
	}

	private void persistDbObjects(Payment payment, CreditEntry creditEntry, List<CreditHistory> creditHistories, List<String> failureMessages) {
		paymentRepository.save(payment);
		if(failureMessages.isEmpty()) {
			creditEntryRepository.save(creditEntry);
			creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
		}
	}

	private boolean isOutboxMessageProcessedForPayment(PaymentRequest paymentRequest,
													   PaymentStatus paymentStatus) {
		Optional<OrderOutboxMessage> orderOutboxMessage =
				orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(
						UUID.fromString(paymentRequest.getSagaId()),
						paymentStatus);
		return orderOutboxMessage.isPresent();
	}
}
