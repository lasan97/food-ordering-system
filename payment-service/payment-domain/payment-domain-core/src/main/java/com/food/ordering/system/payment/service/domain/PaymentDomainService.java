package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;

import java.util.List;

public interface PaymentDomainService {

	PaymentEvent validateAndInitiatePayment(Payment payment,
											CreditEntry creditEntry,
											List<CreditHistory> creditHistories,
											List<String> failureMessages);

	PaymentEvent validateAndCancelPayment(Payment payment,
										  CreditEntry creditEntry,
										  List<CreditHistory> creditHistories,
										  List<String> failureMessages);
}
