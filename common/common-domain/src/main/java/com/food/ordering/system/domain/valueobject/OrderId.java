package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

/**
 * @author martin
 * @description order id
 * @since 2024.12.10
 **********************************************************************************************************************/
public class OrderId extends BaseId<UUID> {

	public OrderId(UUID value) {
		super(value);
	}
}
