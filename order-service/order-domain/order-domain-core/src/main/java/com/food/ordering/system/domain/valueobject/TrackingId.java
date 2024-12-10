package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

/**
 * @author martin
 * @description tracking id
 * @since 2024.12.10
 **********************************************************************************************************************/
public class TrackingId extends BaseId<UUID> {
	public TrackingId(UUID value) {
		super(value);
	}
}
