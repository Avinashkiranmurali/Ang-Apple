package com.b2s.apple.model.finance.citi;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "easyPaymentPlan"
})
public class EasyPaymentResponse {

    @JsonProperty("easyPaymentPlan")
    private List<EasyPaymentPlan> easyPaymentPlans;

    private EasyPaymentResponse (final Builder builder) {
        this.easyPaymentPlans = builder.easyPaymentPlans;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static EasyPaymentResponse create (
            @JsonProperty("cardDetails") final List<EasyPaymentPlan> easyPaymentPlans) {
        return builder()
                .withEasyPaymentPlans(easyPaymentPlans)
                .build();
    }

    public List<EasyPaymentPlan> getEasyPaymentPlans() {
        return easyPaymentPlans;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final EasyPaymentResponse other = (EasyPaymentResponse) o;
        return Objects.equals(this.easyPaymentPlans, other.easyPaymentPlans);
    }

    @Override
    public int hashCode() {
        return Objects.hash(easyPaymentPlans);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
                .withEasyPaymentPlans(easyPaymentPlans);
    }

    public static final class Builder {
        private List<EasyPaymentPlan> easyPaymentPlans;

        private Builder() {
        }

        public Builder withEasyPaymentPlans(final List<EasyPaymentPlan> easyPaymentPlans) {
            this.easyPaymentPlans = easyPaymentPlans;
            return this;
        }


        public EasyPaymentResponse build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new EasyPaymentResponse(this));
        }
    }
}
