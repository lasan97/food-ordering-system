package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

/**
 * @author martin
 * @description product id
 * @since 2024.12.10
 **********************************************************************************************************************/
public class ProductId extends BaseId<UUID> {

	public ProductId(UUID value) {
		super(value);
	}
}
