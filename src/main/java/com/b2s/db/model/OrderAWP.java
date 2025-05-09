package com.b2s.db.model;


public class OrderAWP extends Order {

    private String payrollAgreementUrl;

    public String getPayrollAgreementUrl() {
        return payrollAgreementUrl;
    }

    public void setPayrollAgreementUrl(final String payrollAgreementUrl) {
        this.payrollAgreementUrl = payrollAgreementUrl;
    }
}
