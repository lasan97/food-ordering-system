package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

/**
 * @author martin
 * @description order item id
 * @since 2024.12.10
 **********************************************************************************************************************/
public class OrderItemId extends BaseId<Long> {
	public OrderItemId(Long value) {
		super(value);
	}
}
