package com.food.ordering.system.payment.service.domain.exception;

import com.food.ordering.system.domain.exception.DomainException;

public class PaymentFotFoundException extends DomainException {
	public PaymentFotFoundException(String message) {
		super(message);
	}

	public PaymentFotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
