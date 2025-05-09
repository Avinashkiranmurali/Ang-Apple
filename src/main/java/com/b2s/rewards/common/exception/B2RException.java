package com.b2s.rewards.common.exception;

public class B2RException extends Exception {

	private String errorCode;

	private static final long serialVersionUID = 1L;

	public B2RException(String msg) {
		super(msg);
	}

	public B2RException(String msg, String errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
