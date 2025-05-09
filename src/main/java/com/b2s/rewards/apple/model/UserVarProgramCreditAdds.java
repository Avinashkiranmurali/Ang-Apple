package com.b2s.rewards.apple.model;

import org.joda.money.Money;

public class UserVarProgramCreditAdds {

    private final Float ccVarMargin;
    private final Money ccVarPrice;
    private final Money ccVarProfit;
    private final Integer pointsPurchased;
    private final Money effectiveConversionRate;

    private UserVarProgramCreditAdds(final Builder builder) {
        this.ccVarMargin = builder.ccVarMargin;
        this.ccVarPrice = builder.ccVarPrice;
        this.ccVarProfit = builder.ccVarProfit;
        this.pointsPurchased = builder.pointsPurchased;
        this.effectiveConversionRate = builder.effectiveConversionRate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Float getCcVarMargin() {
        return ccVarMargin;
    }

    public Money getCcVarPrice() {
        return ccVarPrice;
    }

    public Money getCcVarProfit() {
        return ccVarProfit;
    }

    public Integer getPointsPurchased() {
        return pointsPurchased;
    }

    public Money getEffectiveConversionRate() {
        return effectiveConversionRate;
    }

    public static final class Builder {
        private Float ccVarMargin;
        private Money ccVarPrice;
        private Money ccVarProfit;
        private Integer pointsPurchased;
        private Money effectiveConversionRate;

        private Builder() {
        }

        public Builder withCcVarMargin(final Float theCcVarMargin) {
            this.ccVarMargin = theCcVarMargin;
            return this;
        }

        public Builder withCcVarPrice(final Money theCcVarPrice) {
            this.ccVarPrice = theCcVarPrice;
            return this;
        }

        public Builder withCcVarProfit(final Money theCcVarProfit) {
            this.ccVarProfit = theCcVarProfit;
            return this;
        }

        public Builder withPointsPurchased(final Integer thePointsPurchased) {
            this.pointsPurchased = thePointsPurchased;
            return this;
        }

        public Builder withEffectiveConversionRate(final Money theEffectiveConversionRate) {
            this.effectiveConversionRate = theEffectiveConversionRate;
            return this;
        }

        public UserVarProgramCreditAdds build() {
            return new UserVarProgramCreditAdds(this);
        }
    }
}
