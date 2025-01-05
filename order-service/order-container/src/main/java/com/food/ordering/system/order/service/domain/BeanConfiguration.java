package com.food.ordering.system.order.service.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

	@Bean
	public OrderDomainService orderDomainService() {
		return new OrderDomainServiceImpl();
	}
}
