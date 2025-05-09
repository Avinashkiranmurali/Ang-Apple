package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class PricingParametersDTO {
    private final int roundingIncrement;
    private final Optional<BigDecimal> b2sMargin;
    private final Optional<BigDecimal> varMargin;
    private final Optional<BigDecimal> b2sServiceFeeRate;
    private final Optional<Money> retailUnitBasePrice;
    private final Optional<Money> retailUnitTaxPrice;
    private final Optional<Money> bridge2UnitBasePrice;

    private PricingParametersDTO(final Builder builder) {
        this.roundingIncrement = Optionals.checkPresent(builder.roundingIncrement, "roundingIncrement");
        this.b2sMargin = builder.b2sMargin;
        this.varMargin = builder.varMargin;
        this.b2sServiceFeeRate = builder.b2sServiceFeeRate;
        this.retailUnitBasePrice = builder.retailUnitBasePrice;
        this.retailUnitTaxPrice = builder.retailUnitTaxPrice;
        this.bridge2UnitBasePrice = builder.bridge2UnitBasePrice;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static PricingParametersDTO create(
        @JsonProperty("roundingIncrement") final Integer theRoundingIncrement,
        @JsonProperty("b2sMargin") final BigDecimal theB2sMargin,
        @JsonProperty("varMargin") final BigDecimal theVarMargin,
        @JsonProperty("b2sServiceFeeRate") final BigDecimal theB2sServiceFeeRate,
        @JsonProperty("retailUnitBasePrice") final Money theRetailUnitBasePrice,
        @JsonProperty("retailUnitTaxPrice") final Money theRetailUnitTaxPrice,
        @JsonProperty("bridge2UnitBasePrice") final Money theBridge2UnitBasePrice) {
        return builder()
            .withRoundingIncrement(theRoundingIncrement)
            .withB2sMargin(theB2sMargin)
            .withVarMargin(theVarMargin)
            .withB2sServiceFeeRate(theB2sServiceFeeRate)
            .withRetailUnitBasePrice(theRetailUnitBasePrice)
            .withRetailUnitTaxPrice(theRetailUnitTaxPrice)
            .withBridge2UnitTaxPrice(theBridge2UnitBasePrice)
            .build();
    }

    public int getRoundingIncrement() {
        return this.roundingIncrement;
    }

    public Optional<BigDecimal> getB2sMargin() {
        return this.b2sMargin;
    }

    public Optional<BigDecimal> getVarMargin() {
        return this.varMargin;
    }

    public Optional<BigDecimal> getB2sServiceFeeRate() {
        return this.b2sServiceFeeRate;
    }

    public Optional<Money> getRetailUnitBasePrice() {
        return retailUnitBasePrice;
    }

    public Optional<Money> getRetailUnitTaxPrice() {
        return retailUnitTaxPrice;
    }

    public Optional<Money> getBridge2UnitBasePrice() {
        return bridge2UnitBasePrice;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final PricingParametersDTO other = (PricingParametersDTO) o;
        return Objects.equals(this.roundingIncrement, other.roundingIncrement)
            && Objects.equals(this.b2sMargin, other.b2sMargin)
            && Objects.equals(this.varMargin, other.varMargin)
            && Objects.equals(this.b2sServiceFeeRate, other.b2sServiceFeeRate)
            && Objects.equals(this.retailUnitBasePrice, other.retailUnitBasePrice)
            && Objects.equals(this.retailUnitTaxPrice, other.retailUnitTaxPrice)
            && Objects.equals(this.bridge2UnitBasePrice, other.bridge2UnitBasePrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roundingIncrement, b2sMargin, varMargin, b2sServiceFeeRate, retailUnitBasePrice, retailUnitTaxPrice, bridge2UnitBasePrice);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withRoundingIncrement(roundingIncrement)
            .withB2sMargin(b2sMargin.orElse(null))
            .withVarMargin(varMargin.orElse(null))
            .withB2sServiceFeeRate(b2sServiceFeeRate.orElse(null))
            .withRetailUnitBasePrice(retailUnitBasePrice.orElse(null))
            .withRetailUnitTaxPrice(retailUnitTaxPrice.orElse(null))
            .withBridge2UnitTaxPrice(bridge2UnitBasePrice.orElse(null));
    }

    public static final class Builder {
        private Optional<Integer> roundingIncrement = Optional.empty();
        private Optional<BigDecimal> b2sMargin = Optional.empty();
        private Optional<BigDecimal> varMargin = Optional.empty();
        private Optional<BigDecimal> b2sServiceFeeRate = Optional.empty();
        private Optional<Money> retailUnitBasePrice = Optional.empty();
        private Optional<Money> retailUnitTaxPrice = Optional.empty();
        private Optional<Money> bridge2UnitBasePrice = Optional.empty();

        private Builder() {
        }

        public Builder withRoundingIncrement(final Integer theRoundingIncrement) {
            this.roundingIncrement = Optionals.from(theRoundingIncrement);
            return this;
        }

        public Builder withB2sMargin(final BigDecimal theB2sMargin) {
            this.b2sMargin = Optionals.from(theB2sMargin);
            return this;
        }

        public Builder withVarMargin(final BigDecimal theVarMargin) {
            this.varMargin = Optionals.from(theVarMargin);
            return this;
        }

        public Builder withB2sServiceFeeRate(final BigDecimal theB2sServiceFeeRate) {
            this.b2sServiceFeeRate = Optionals.from(theB2sServiceFeeRate);
            return this;
        }

        public Builder withRetailUnitBasePrice(final Money theRetailUnitBasePrice) {
            this.retailUnitBasePrice = Optionals.from(theRetailUnitBasePrice);
            return this;
        }

        public Builder withRetailUnitTaxPrice(final Money theRetailUnitTaxPrice) {
            this.retailUnitTaxPrice = Optionals.from(theRetailUnitTaxPrice);
            return this;
        }

        public Builder withBridge2UnitTaxPrice(final Money theBridge2UnitTaxPrice) {
            this.bridge2UnitBasePrice = Optionals.from(theBridge2UnitTaxPrice);
            return this;
        }

        public PricingParametersDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new PricingParametersDTO(this));
        }
    }
}
