package com.b2s.rewards.common.exception;

import org.springframework.http.HttpStatus;

public class RestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final HttpStatus httpStatus;

	public RestException(String msg, HttpStatus httpStatus){
		super(msg);
		this.httpStatus = httpStatus;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
