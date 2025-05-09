package com.b2s.rewards.apple.model.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Created by rpillai on 8/3/2018.
 */
@JsonDeserialize(builder = PayerInformation.Builder.class)
public class PayerInformation {

    private String accountId;
    private String authorizationKey;
    private String paymentMethodId;

    public String getAccountId() {
        return accountId;
    }

    public String getAuthorizationKey() {
        return authorizationKey;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    private PayerInformation(Builder builder) {
        this.accountId = builder.accountId;
        this.authorizationKey = builder.authorizationKey;
        this.paymentMethodId = builder.paymentMethodId;
    }

    public static Builder builder() { return new Builder(); }

    @JsonPOJOBuilder(buildMethodName = "build")
    public static class Builder {
        private String accountId;
        private String authorizationKey;
        private String paymentMethodId;

        public Builder withAccountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder withAuthorizationKey(String authorizationKey) {
            this.authorizationKey = authorizationKey;
            return this;
        }

        public Builder withPaymentMethodId(String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
            return this;
        }

        public PayerInformation build() { return new PayerInformation(this); }
    }
}
