package com.b2s.shop.common.order.supplier;


import com.b2s.db.model.Order;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.msg.Message;
import com.b2s.rewards.model.ShoppingCart;

public interface SupplierOrderManagerIF {


	public boolean placeOrder(ShoppingCart cart, Order order, User user);

	public Message getMessage();
	
	public boolean postOrderUpdate(ShoppingCart cart, Order order, User user);
	
}
