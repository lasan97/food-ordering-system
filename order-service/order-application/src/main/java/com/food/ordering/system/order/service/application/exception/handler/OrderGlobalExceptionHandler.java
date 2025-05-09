package com.food.ordering.system.order.service.application.exception.handler;

import com.food.ordering.system.application.handler.ErrorDTO;
import com.food.ordering.system.application.handler.GlobalExceptionHandler;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class OrderGlobalExceptionHandler extends GlobalExceptionHandler {

	@ResponseBody
	@ExceptionHandler(value = {OrderDomainException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	private ErrorDTO  handlerException(OrderDomainException orderDomainException) {
			log.error(orderDomainException.getMessage(), orderDomainException);
			return ErrorDTO.builder()
					.code(HttpStatus.BAD_REQUEST.getReasonPhrase())
					.message(orderDomainException.getMessage())
					.build();
	}

	@ResponseBody
	@ExceptionHandler(value = {OrderNotFoundException.class})
	@ResponseStatus(HttpStatus.NOT_FOUND)
	private ErrorDTO handlerException(OrderNotFoundException orderNotFoundException) {
		log.error(orderNotFoundException.getMessage(), orderNotFoundException);
		return ErrorDTO.builder()
				.code(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message(orderNotFoundException.getMessage())
				.build();
	}
}
