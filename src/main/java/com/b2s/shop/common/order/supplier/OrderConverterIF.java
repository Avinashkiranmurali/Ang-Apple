package com.b2s.shop.common.order.supplier;

import com.b2s.db.model.Order;
import com.b2s.shop.common.User;
import com.b2s.rewards.model.ShoppingCart;

public interface OrderConverterIF {
	
	public Order convert(ShoppingCart cart, User user);

}
