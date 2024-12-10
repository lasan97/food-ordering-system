package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

/**
 * @author martin
 * @description customer id
 * @since 2024.12.10
 **********************************************************************************************************************/
public class CustomerId extends BaseId<UUID> {

	public CustomerId(UUID value) {
		super(value);
	}
}
