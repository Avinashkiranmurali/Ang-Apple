package com.b2s.rewards.apple.exceptionhandler;

import com.b2s.service.utils.lang.Exceptions;
import org.springframework.http.HttpStatus;

import java.util.Date;

public class ErrorResponse {
    private HttpStatus status;
    private String error_code;
    private String message;
    private String detail;
    private Date date;

    private ErrorResponse(final ErrorResponseBuilder builder) {
        this.status = builder.status;
        this.error_code = builder.error_code;
        this.detail = builder.detail;
        this.message = builder.message;
        this.date = builder.date;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError_code() {
        return error_code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

    public Date getDate() {
        return date;
    }

    public static final class ErrorResponseBuilder {
        private HttpStatus status;
        private String error_code;
        private String message;
        private String detail;
        private Date date;

        public ErrorResponseBuilder withStatus(HttpStatus status) {
            this.status = status;
            return this;
        }

        public ErrorResponseBuilder withError_code(String error_code) {
            this.error_code = error_code;
            return this;
        }

        public ErrorResponseBuilder withError_code(int error_code) {
            this.error_code = String.valueOf(error_code);
            return this;
        }

        public ErrorResponseBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder withDetail(String detail) {
            this.detail = detail;
            return this;
        }

        public ErrorResponseBuilder atTime(Date date) {
            this.date = date;
            return this;
        }

        public ErrorResponse build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ErrorResponse(this));
        }

    }
}
