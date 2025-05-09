package com.b2s.shop.util;

import com.b2s.db.model.Order;
import com.b2s.shop.common.order.msg.Message;
import com.b2s.shop.common.order.var.OrderCodeStatus;

public class MessageUtils {

	/**
	 * Creates and returns success message if VAROrderManager successfully places an order
	 * @param VAROrderId
	 * @param order
     * @return msg, the created success Message
     */
	public static Message setSuccessMessage(String VAROrderId, Order order) {
		Message msg = new Message();
		msg.setSuccess(true);
		msg.setContentText("VAROrderManager successfully placed order: " + order.getOrderId());
		msg.setVAROrderId(VAROrderId);
		msg.setCode(OrderCodeStatus.SUCCESS.getValue());
		return msg;
	}

	/**
	 * Creates and returns failure message if VAROrderManager fails to place an order
	 * @param VAROrderId
	 * @param order
     * @return msg, the created failure Message
     */
	public static Message setFailureMessage(String VAROrderId, Order order) {
		Message msg = new Message();
		msg.setSuccess(false);
		msg.setContentText("ERROR SENDING ORDER :" + order.getOrderId());
		msg.setVAROrderId(VAROrderId);
		msg.setCode(OrderCodeStatus.FAIL.getValue());
		return msg;
	}
}
