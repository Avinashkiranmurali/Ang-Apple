package com.b2s.rewards.apple.exceptionhandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ArgumentsNotValidException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
