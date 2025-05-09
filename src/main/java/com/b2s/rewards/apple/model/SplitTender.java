package com.b2s.rewards.apple.model;

import org.joda.money.Money;

import java.math.BigDecimal;
import java.util.Optional;

public class SplitTender {
    private final Optional<Money> maxCashAmount;
    private final Optional<BigDecimal> maxPointsPercentage;

    private SplitTender(final Builder builder) {
        this.maxCashAmount = builder.maxCashAmount;
        this.maxPointsPercentage = builder.maxPointsPercentage;
    }

    public static Builder builder() {
        return new SplitTender.Builder();
    }

    public Optional<Money> getMaxCashAmount() {
        return maxCashAmount;
    }

    public Optional<BigDecimal> getMaxPointsPercentage() {
        return maxPointsPercentage;
    }

    public static class Builder {
        private Optional<Money> maxCashAmount = Optional.empty();
        private Optional<BigDecimal> maxPointsPercentage = Optional.empty();

        private Builder() {
        }

        public Builder withMaxCashAmount(final Money theMaxCashAmount) {
            this.maxCashAmount = Optional.ofNullable(theMaxCashAmount);
            return this;
        }

        public Builder withMaxPointsPercentage(final BigDecimal theMaxPointsPercentage) {
            this.maxPointsPercentage = Optional.ofNullable(theMaxPointsPercentage);
            return this;
        }

        public SplitTender build() {
            return new SplitTender(this);
        }
    }
}
