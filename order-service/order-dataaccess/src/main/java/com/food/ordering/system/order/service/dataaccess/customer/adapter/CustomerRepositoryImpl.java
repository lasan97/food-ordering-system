package com.food.ordering.system.order.service.dataaccess.customer.adapter;

import com.food.ordering.system.order.service.dataaccess.customer.mapper.CustomerDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.customer.repository.CustomerJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

	private final CustomerJpaRepository customerJpaRepository;
	private final CustomerDataAccessMapper customerDataAccessMapper;

	@Override
	public Optional<Customer> findCustomer(UUID customerId) {
		return customerJpaRepository.findById(customerId).map(customerDataAccessMapper::customerEntityToCustomer);
	}

	@Override
	@Transactional
	public Customer save(Customer customer) {
		return customerDataAccessMapper.customerEntityToCustomer(
				customerJpaRepository.save(
						customerDataAccessMapper.customerToCustomerEntity(customer)));
	}
}
