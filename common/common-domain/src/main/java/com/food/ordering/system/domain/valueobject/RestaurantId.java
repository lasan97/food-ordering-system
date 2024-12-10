package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

/**
 * @author martin
 * @description restaurant id
 * @since 2024.12.10
 **********************************************************************************************************************/
public class RestaurantId extends BaseId<UUID> {

	public RestaurantId(UUID value) {
		super(value);
	}
}
