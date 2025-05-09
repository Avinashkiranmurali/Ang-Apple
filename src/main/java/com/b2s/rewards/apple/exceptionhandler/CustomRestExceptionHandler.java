package com.b2s.rewards.apple.exceptionhandler;

import com.b2s.rewards.common.exception.RestException;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.lang.StringUtils;
import com.b2s.rewards.apple.model.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@RestControllerAdvice
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomRestExceptionHandler.class);

    @ExceptionHandler({DataNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .withDetail("Data not found for given input")
                .withMessage(ex.getLocalizedMessage())
                .withError_code("404")
                .withStatus(HttpStatus.NOT_FOUND)
                .atTime(new Date())
                .build();
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    @ExceptionHandler({ArgumentsNotValidException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(ArgumentsNotValidException ex) {
        ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .withDetail("Not valid arguments")
                .withMessage(ex.getLocalizedMessage())
                .withError_code("400")
                .withStatus(HttpStatus.BAD_REQUEST)
                .atTime(new Date())
                .build();
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    @ExceptionHandler({InvalidResponseException.class})
    public ResponseEntity<ErrorResponse> handleInvalidResponseException(InvalidResponseException ex) {
        ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .withDetail("Error while processing response")
                .withMessage(ex.getLocalizedMessage())
                .withError_code("500")
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .atTime(new Date())
                .build();
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentTypeMismatchException ex) {
        ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
            .withDetail("Please check your input.")
            .withMessage("Invalid input.")
            .withError_code(HttpStatus.BAD_REQUEST.value())
            .withStatus(HttpStatus.BAD_REQUEST)
            .atTime(new Date())
            .build();
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }

    @ExceptionHandler(value = {RestException.class})
    protected ResponseEntity<ErrorResponse> handleConflict(final RestException ex, final WebRequest request) {
        final ErrorResponse errorResponse = ErrorResponse.builder()
            .withError_code(String.valueOf(ex.getHttpStatus().value()))
            .withMessage(ex.getMessage())
            .build();
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("Validation failure in the following request fields :");

        BindingResult result = ex.getBindingResult();
        StringBuilder error = new StringBuilder();
        result.getFieldErrors().forEach(fieldError -> {

            if (StringUtils.isBlank(error.toString())) {
                error.append("Error in the filed(s) : ").append(fieldError.getField());
            } else {
                error.append(" , ").append(fieldError.getField());
            }

            final StringBuilder errorMsg = new StringBuilder()
                    .append(fieldError.getObjectName())
                    .append(".")
                    .append(fieldError.getField())
                    .append(" : ")
                    .append(fieldError.getDefaultMessage())
                    .append(" : rejected value [")
                    .append(fieldError.getRejectedValue())
                    .append("]");
            log.error(errorMsg.toString());
        });
        if (ex.getBindingResult().getObjectName().equalsIgnoreCase("emailNotification")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(populateFieldErrorResponse(error.toString()));
        } else {
            return ResponseEntity.badRequest().body(error);
        }
    }

    private EmailNotification populateFieldErrorResponse(final String error) {
        EmailNotification errorInfo = new EmailNotification();
        errorInfo.setMessage(error);
        errorInfo.setProcessStatus(CommonConstants.StatusChangeQueueProcessStatus.FAILED.toString());
        errorInfo.setProcessDate(new Date());
        errorInfo.setProcessDescription(error);
        return errorInfo;
    }
}
