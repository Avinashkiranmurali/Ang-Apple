package com.b2s.shop.common.order;

import com.b2s.shop.common.User;

import javax.servlet.http.HttpServletRequest;

public interface CartIF {

	public int getCartTotalPoints();
	public CartIF convert(HttpServletRequest request);
	public int getCartSupplierId();
	public CartIF refreshState(User user);
}
