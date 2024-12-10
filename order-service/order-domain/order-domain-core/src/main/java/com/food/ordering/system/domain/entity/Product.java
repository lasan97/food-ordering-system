package com.food.ordering.system.domain.entity;

import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.ProductId;

/**
 * @author martin
 * @description product
 * @since 2024.12.10
 **********************************************************************************************************************/
public class Product extends BaseEntity<ProductId> {

	private String name;
	private Money price;

	public Product(ProductId productId, String name, Money price) {
		super.setId(productId);
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public Money getPrice() {
		return price;
	}
}
