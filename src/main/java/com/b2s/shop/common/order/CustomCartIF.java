package com.b2s.shop.common.order;

import javax.servlet.http.HttpServletRequest;

public interface CustomCartIF {

	public int getCartTotalPoints();
	public CartIF convert(HttpServletRequest request);

}
