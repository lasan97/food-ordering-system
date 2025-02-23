package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.message.CustomerModel;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDateMapper;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.customer.CustomerMessageListener;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerMessageListenerImpl implements CustomerMessageListener {

	private final CustomerRepository customerRepository;
	private final OrderDateMapper orderDateMapper;

	@Override
	public void customerCreated(CustomerModel customerModel) {
		Customer customer = customerRepository.save(orderDateMapper.customerModelToCustomer(customerModel));

		if(customer == null) {
			log.info("Customer could not be created in order database with id: {}", customerModel.getId());
			throw new OrderDomainException("Customer could not be created in order database with id "
					+ customerModel.getId());
		}
		log.info("Customer is created in order database with id: {}", customer.getId());
	}
}
