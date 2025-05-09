package com.b2s.rewards.apple.model;

import org.joda.money.Money;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Created by rpillai on 3/19/2018.
 */
public class Promotion {
    private final Optional<BigDecimal> discountPercentage;
    private final Optional<Money> fixedPointPrice;
    private final Optional<BigDecimal> costPerPoint;

    public Optional<BigDecimal> getDiscountPercentage() {
        return discountPercentage;
    }

    public Optional<Money> getFixedPointPrice() {
        return fixedPointPrice;
    }

    public Optional<BigDecimal> getCostPerPoint() {
        return costPerPoint;
    }

    private Promotion(final Builder builder) {
        this.discountPercentage = builder.discountPercentage;
        this.fixedPointPrice = builder.fixedPointPrice;
        this.costPerPoint = builder.costPerPoint;
    }

    public static Builder builder(){ return new Builder(); }

    public static class Builder {
        private Optional<BigDecimal> discountPercentage;
        private Optional<Money> fixedPointPrice;
        private Optional<BigDecimal> costPerPoint;

        public Builder() {
            discountPercentage = Optional.empty();
            fixedPointPrice = Optional.empty();
            costPerPoint = Optional.empty();
        }

        public Builder  withDiscountPercentage(final BigDecimal discountPercentage) {
            this.discountPercentage = Optional.ofNullable(discountPercentage);
            return this;
        }

        public Builder  withFixedPointPrice(final Money fixedPointPrice) {
            this.fixedPointPrice = Optional.ofNullable(fixedPointPrice);
            return this;
        }

        public Builder  withCostPerPoint(final BigDecimal theCostPerPoint) {
            this.costPerPoint = Optional.ofNullable(theCostPerPoint);
            return this;
        }

        public Promotion build() {
            return new Promotion(this);
        }

    }

}
