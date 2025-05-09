package com.b2s.shop.common.order.msg;

import com.b2s.shop.common.order.var.OrderCodeStatus;

public class Message implements MessageIF{
	
	boolean success = Boolean.FALSE;
	int code;
	private boolean promotionUseExceeded = false;
	private boolean timedOut = false;
	String supplierOrderId;
	String VAROrderId;
	String B2ROrderId;
	boolean orderOnHold;
	
	String contentText;

    public Message(boolean success, int code, String supplierOrderId, String VAROrderId, String b2ROrderId, String contentText) {
        this.success = success;
        this.code = code;
        this.supplierOrderId = supplierOrderId;
        this.VAROrderId = VAROrderId;
        B2ROrderId = b2ROrderId;
        this.contentText = contentText;
    }

    public Message() {
        //intentional for backwards compatibility
    }

    public static Message createSuccessfulOrderMessage(int contextText, String varOrderId)
    {
        return new Message(Boolean.TRUE, OrderCodeStatus.SUCCESS.getValue(),"",varOrderId,"", createSuccessfulMessageDescription(contextText));
    }
    public static Message createFailedOrderMessage(int contextText, String varOrderId)
    {
        return new Message(Boolean.FALSE, OrderCodeStatus.FAIL.getValue(),"",varOrderId,"", createFailedMessageDescription(contextText));
    }

    private static String createSuccessfulMessageDescription(int contextText) {
        return String.format("Order number: %d was successfully placed.", contextText);
    }

    private static String createFailedMessageDescription(int contextText) {
        return String.format("Order number: %d was NOT successfully placed.", contextText);
    }

	public boolean isPromotionUseExceeded() {
		return promotionUseExceeded;
	}

	public void setPromotionUseExceeded(boolean promotionUseExceeded) {
		this.promotionUseExceeded = promotionUseExceeded;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}

	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success){
		this.success=success;
	}

	public String getContentText() {
		return contentText;
	}
	public void setContentText(String contentText){
		this.contentText=contentText;
	}
	
	public String getSupplierOrderId(){
		return supplierOrderId;
	}
	public void setSupplierOrderId(String supplierOrderId){
		this.supplierOrderId=supplierOrderId;
	}
	
	public int getCode(){
		return code;
	}
	public void setCode(int code){
		this.code=code;
	}
	
	public String getVAROrderId(){
		return VAROrderId;
	}
	public void setVAROrderId(String VAROrderId){
		this.VAROrderId=VAROrderId;
	}
	
	public String getB2ROrderId(){
		return B2ROrderId;
	}
	public void setB2ROrderId(String B2ROrderId){
		this.B2ROrderId=B2ROrderId;
	}

	public boolean isOrderOnHold() {
		return orderOnHold;
	}

	public void setOrderOnHold(boolean orderOnHold) {
		this.orderOnHold = orderOnHold;
	}
}
