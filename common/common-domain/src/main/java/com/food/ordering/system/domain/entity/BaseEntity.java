package com.food.ordering.system.domain.entity;

import java.util.Objects;

/**
 * @author martin
 * @description base entity
 * @since 2024.12.10
 **********************************************************************************************************************/
public abstract class BaseEntity<ID> {

	private ID id;

	public ID getId() {
		return id;
	}

	public void setId(ID id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		BaseEntity<?> that = (BaseEntity<?>) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
