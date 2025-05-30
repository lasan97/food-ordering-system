package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestMessageListenerImpl implements PaymentRequestMessageListener {

	private final PaymentRequestHelper paymentRequestHelper;

	@Override
	public void completePayment(PaymentRequest paymentRequest) {
		paymentRequestHelper.persistPayment(paymentRequest);
	}

	@Override
	public void cancelPayment(PaymentRequest paymentRequest) {
		paymentRequestHelper.persistCancelPayment(paymentRequest);
	}
}
