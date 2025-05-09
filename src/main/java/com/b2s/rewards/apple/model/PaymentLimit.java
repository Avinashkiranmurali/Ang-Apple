package com.b2s.rewards.apple.model;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author rjesuraj Date : 9/19/2017 Time : 4:32 PM
 */
public class PaymentLimit implements Serializable {

    private static final long serialVersionUID = -2606872237964891880L;
    @SuppressWarnings("squid:S1948")
    private Optional<Double> paymentMinLimit = Optional.empty();
    @SuppressWarnings("squid:S1948")
    private Optional<Double> paymentMaxLimit = Optional.empty();
    private boolean minNotMet = false;
    private boolean maxExceed = false;

    public Optional<Double> getPaymentMinLimit() {
        return paymentMinLimit;
    }

    public void setPaymentMinLimit(final Double paymentMinLimit) {
        this.paymentMinLimit = Optional.ofNullable(paymentMinLimit);
    }

    public Optional<Double> getPaymentMaxLimit() {
        return paymentMaxLimit;
    }

    public void setPaymentMaxLimit(final Double paymentMaxLimit) {
        this.paymentMaxLimit = Optional.ofNullable(paymentMaxLimit);
    }

    public boolean isMinNotMet() {
        return minNotMet;
    }

    public void setMinNotMet(final boolean minNotMet) {
        this.minNotMet = minNotMet;
    }

    public boolean isMaxExceed() {
        return maxExceed;
    }

    public void setMaxExceed(final boolean maxExceed) {
        this.maxExceed = maxExceed;
    }
}
