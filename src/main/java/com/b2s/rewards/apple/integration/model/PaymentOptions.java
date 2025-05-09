package com.b2s.rewards.apple.integration.model;

public enum PaymentOptions {
    POINTSONLY("pointsonly"), // Use Points Only
    POINTSFIXED("pointsfixed"), //Fixed Points
    SPLITPAY("splitpay"), // Use Split Pay
    CASHONLY("cashonly"), // No Points
    FINANCE("finance"), // Pay with finance
    SPLITPAY_FINANCE("splitpay_finance"), // split with finance option
    CASHONLY_FINANCE("cashonly_finance"), // cashonly with finance option
    PAYROLL_DEDUCTION("payroll_deduction"), // payroll deduction
    NOPAY("nopay"); //  Not payment, just place order and redirection to client

    private final String paymentOption;

    PaymentOptions(final String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public String getPaymentOption() {
        return paymentOption;
    }

}
