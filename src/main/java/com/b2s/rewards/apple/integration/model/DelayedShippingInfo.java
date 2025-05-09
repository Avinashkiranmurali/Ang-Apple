package com.b2s.rewards.apple.integration.model;

import com.b2s.util.Assert;

/**
 * Represents the delayed shipping information.
 *
 */
public class DelayedShippingInfo {

    private final String shippingAvailability;
    private final String asOfDate;

    private DelayedShippingInfo(final Builder builder) {
        Assert.notNull(builder.shippingAvailability, "shippingAvailability must not be null.");
        Assert.notNull(builder.asOfDate, "asOfDate must not be null.");

        this.shippingAvailability = builder.shippingAvailability;
        this.asOfDate = builder.asOfDate;
    }

    public String getShippingAvailability() {
        return shippingAvailability;
    }

    public String getAsOfDate() {
        return asOfDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String shippingAvailability;
        private String asOfDate;

        public Builder withShippingAvailability(final String shippingAvailability) {
            this.shippingAvailability = shippingAvailability;
            return this;
        }

        public Builder withAsOfDate(final String asOfDate) {
            this.asOfDate = asOfDate;
            return this;
        }

        public DelayedShippingInfo build() {
            return new DelayedShippingInfo(this);
        }
    }

}
