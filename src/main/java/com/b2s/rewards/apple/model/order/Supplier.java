package com.b2s.rewards.apple.model.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Created by rpillai on 8/3/2018.
 */
@JsonDeserialize(builder = Supplier.Builder.class)
public class Supplier {

    private PayerInformation payerInformation;

    public PayerInformation getPayerInformation() {
        return payerInformation;
    }

    private Supplier(Builder builder) {
        this.payerInformation = builder.payerInformation;
    }

    public static Builder builder() { return new Builder(); }

    @JsonPOJOBuilder(buildMethodName = "build")
    public static class Builder {
        private PayerInformation payerInformation;

        public Builder withPayerInformation(PayerInformation payerInformation) {
            this.payerInformation = payerInformation;
            return this;
        }

        public Supplier build() { return new Supplier(this); }
    }
}
