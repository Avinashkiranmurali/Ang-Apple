package com.b2s.shop.common.order;

import com.b2s.db.model.OrderLine;

import java.util.List;

public interface OrderIF {
	
	 List<OrderLine> getOrderLines();
     
     void setOrderLines(List<OrderLine> orderlines);
	 
	 String getIsApplySuperSaverShipping();

}
