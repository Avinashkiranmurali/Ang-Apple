package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.joda.money.CurrencyUnit;

import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class ContextDTO {

    private final String varId;
    private final String varProgramId;
    private final String countryCode;
    private final Optional<String> stateCode;
    private final Optional<String> pricingTier;
    private final Optional<OrderTypeValue> orderType;
    private final ImmutableList<CurrencyUnit> targetCurrencies;

    private ContextDTO(final Builder builder) {
        this.varId = Optionals.checkPresent(builder.varId, "varId");
        this.varProgramId = Optionals.checkPresent(builder.varProgramId, "varProgramId");
        this.countryCode = Optionals.checkPresent(builder.countryCode, "countryCode");
        this.stateCode = builder.stateCode;
        this.pricingTier = builder.pricingTier;
        this.orderType = builder.orderType;
        this.targetCurrencies = builder.targetCurrencies;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static ContextDTO create(
        @JsonProperty("varId") final String varId,
        @JsonProperty("varProgramId") final String varProgramId,
        @JsonProperty("countryCode") final String countryCode,
        @JsonProperty("stateCode") final String stateCode,
        @JsonProperty("pricingTier") final String pricingTier,
        @JsonProperty("orderType") final OrderTypeValue orderType,
        @JsonProperty("targetCurrencies") final ImmutableList<CurrencyUnit> targetCurrencies) {
        return builder()
            .withVarId(varId)
            .withVarProgramId(varProgramId)
            .withCountryCode(countryCode)
            .withStateCode(stateCode)
            .withPricingTier(pricingTier)
            .withOrderType(orderType)
            .withTargetCurrencies(targetCurrencies)
            .build();
    }

    public String getVarId() {
        return this.varId;
    }

    public String getVarProgramId() {
        return this.varProgramId;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public Optional<String> getStateCode() {
        return this.stateCode;
    }

    public Optional<String> getPricingTier() {
        return this.pricingTier;
    }

    public Optional<OrderTypeValue> getOrderType() {
        return this.orderType;
    }

    public ImmutableList<CurrencyUnit> getTargetCurrencies() {
        return this.targetCurrencies;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ContextDTO other = (ContextDTO) o;
        return Objects.equals(this.varId, other.varId)
            && Objects.equals(this.varProgramId, other.varProgramId)
            && Objects.equals(this.countryCode, other.countryCode)
            && Objects.equals(this.stateCode, other.stateCode)
            && Objects.equals(this.pricingTier, other.pricingTier)
            && Objects.equals(this.orderType, other.orderType)
            && Objects.equals(this.targetCurrencies, other.targetCurrencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varId, varProgramId, countryCode, stateCode, pricingTier, orderType, targetCurrencies);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withVarId(varId)
            .withVarProgramId(varProgramId)
            .withCountryCode(countryCode)
            .withStateCode(stateCode.orElse(null))
            .withPricingTier(pricingTier.orElse(null))
            .withOrderType(orderType.orElse(null))
            .withTargetCurrencies(targetCurrencies);
    }

    public static final class Builder {
        private Optional<String> varId = Optional.empty();
        private Optional<String> varProgramId = Optional.empty();
        private Optional<String> countryCode = Optional.empty();
        private Optional<String> stateCode = Optional.empty();
        private Optional<String> pricingTier = Optional.empty();
        private Optional<OrderTypeValue> orderType = Optional.empty();
        private ImmutableList<CurrencyUnit> targetCurrencies = ImmutableList.of();

        private Builder() {
        }

        public Builder withVarId(final String theVarId) {
            this.varId = Optionals.from(theVarId);
            return this;
        }

        public Builder withVarProgramId(final String theVarProgramId) {
            this.varProgramId = Optionals.from(theVarProgramId);
            return this;
        }

        public Builder withCountryCode(final String theCountryCode) {
            this.countryCode = Optionals.from(theCountryCode);
            return this;
        }

        public Builder withStateCode(final String theStateCode) {
            this.stateCode = Optionals.from(theStateCode);
            return this;
        }

        public Builder withPricingTier(final String thePricingTier) {
            this.pricingTier = Optionals.from(thePricingTier);
            return this;
        }

        public Builder withOrderType(final OrderTypeValue theOrderType) {
            this.orderType = Optionals.from(theOrderType);
            return this;
        }

        public Builder withTargetCurrencies(final Iterable<CurrencyUnit> theTargetCurrencies) {
            this.targetCurrencies = AdditionalCollections.asImmutableList(theTargetCurrencies);
            return this;
        }

        public ContextDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ContextDTO(this));
        }
    }
}
