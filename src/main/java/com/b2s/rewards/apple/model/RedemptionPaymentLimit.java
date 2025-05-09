package com.b2s.rewards.apple.model;

import java.io.Serializable;

public class RedemptionPaymentLimit implements Serializable {

    private static final long serialVersionUID = 4148099864046114493L;
    private final Price pointsMaxLimit;
    private final Price pointsMinLimit;
    private final Price cashMaxLimit;
    private final Price cashMinLimit;
    private final Price useMaxPoints;
    private final Price useMinPoints;
    private final Price cartMaxLimit;

    private RedemptionPaymentLimit(final Builder builder) {
        this.pointsMaxLimit = builder.pointsMaxLimit;
        this.pointsMinLimit = builder.pointsMinLimit;
        this.cashMaxLimit = builder.cashMaxLimit;
        this.cashMinLimit = builder.cashMinLimit;
        this.useMaxPoints=builder.useMaxPoints;
        this.useMinPoints=builder.useMinPoints;
        this.cartMaxLimit=builder.cartMaxLimit;
    }

    public static Builder builder() {
        return new Builder();
    }


    public Price getPointsMaxLimit() {
        return pointsMaxLimit;
    }

    public Price getPointsMinLimit() {
        return pointsMinLimit;
    }

    public Price getCashMaxLimit() {
        return cashMaxLimit;
    }

    public Price getCashMinLimit() {
        return cashMinLimit;
    }

    public Price getUseMaxPoints() {
        return useMaxPoints;
    }

    public Price getUseMinPoints() {
        return useMinPoints;
    }

    public Price getCartMaxLimit() {
        return cartMaxLimit;
    }

    public static final class Builder {
        private Price pointsMaxLimit;
        private Price pointsMinLimit;
        private Price cashMaxLimit;
        private Price cashMinLimit;
        private Price useMaxPoints;
        private Price useMinPoints;
        private Price cartMaxLimit;

        private Builder() {
        }

        public Builder withPointsMaxLimit(final Price thePointsMaxLimit) {
            this.pointsMaxLimit = thePointsMaxLimit;
            return this;
        }

        public Builder withPointsMinLimit(final Price thePointsMinLimit) {
            this.pointsMinLimit = thePointsMinLimit;
            return this;
        }

        public Builder withCashMaxLimit(final Price theCashMaxLimit) {
            this.cashMaxLimit = theCashMaxLimit;
            return this;
        }

        public Builder withCashMinLimit(final Price theCashMinLimit) {
            this.cashMinLimit = theCashMinLimit;
            return this;
        }

        public Builder withUseMaxPoints(final Price useMaxPoints){
            this.useMaxPoints = useMaxPoints;
            return this;
        }

        public Builder withUseMinPoints(final Price useMinPoints){
            this.useMinPoints=useMinPoints;
            return this;
        }

        public Builder withCartMaxLimit(final Price cartMaxLimit){
            this.cartMaxLimit=cartMaxLimit;
            return this;
        }

        public RedemptionPaymentLimit build() {
            return new RedemptionPaymentLimit(this);
        }
    }
}