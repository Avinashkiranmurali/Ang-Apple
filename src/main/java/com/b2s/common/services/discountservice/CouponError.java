package com.b2s.common.services.discountservice;

/**
 * Created by rpillai on 8/9/2016.
 */
public class CouponError {

    private String discountCode;
    private String errorCode;
    private String errorHeader;
    private String errorMessage;

    public CouponError(){}

    public CouponError(String discountCode, String errorCode, String errorHeader, String errorMessage) {
        this.discountCode = discountCode;
        this.errorCode = errorCode;
        this.errorHeader = errorHeader;
        this.errorMessage = errorMessage;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorHeader() {
        return errorHeader;
    }

    public void setErrorHeader(String errorHeader) {
        this.errorHeader = errorHeader;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
