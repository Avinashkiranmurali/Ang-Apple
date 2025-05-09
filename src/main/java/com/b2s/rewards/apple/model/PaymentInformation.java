package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
/**
 * Created by sjayaraman on 5/3/2019.
 */
@JsonDeserialize(builder = PaymentInformation.Builder.class)
public class PaymentInformation {

    private final Double totalUpgradePrice;
    private final int payPeriod;
    private final Double initialPayment;
    private final Double finalPayment;

    public PaymentInformation(Builder builder) {
        this.totalUpgradePrice = builder.totalUpgradePrice;
        this.payPeriod = builder.payPeriod;
        this.initialPayment = builder.initialPayment;
        this.finalPayment = builder.finalPayment;
    }

    public int getPayPeriod() {
        return payPeriod;
    }

    public Double getFinalPayment() {
        return finalPayment;
    }

    public Double getTotalUpgradePrice() {
        return totalUpgradePrice;
    }

    public Double getInitialPayment() {
        return initialPayment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Double totalUpgradePrice;
        private int payPeriod;
        private Double initialPayment;
        private Double finalPayment;


        public Builder withTotalUpgradePrice(final Double totalUpgradePrice) {
            this.totalUpgradePrice = totalUpgradePrice;
            return this;
        }

        public Builder withPayPeriod(final int payPeriod) {
            this.payPeriod = payPeriod;
            return this;
        }

        public Builder withInitialPayment(final Double initialPayment) {
            this.initialPayment = initialPayment;
            return this;
        }

        public Builder withFinalPayment(final Double finalPayment) {
            this.finalPayment = finalPayment;
            return this;
        }

        public PaymentInformation build(){
            return new PaymentInformation(this);
        }

    }

}
