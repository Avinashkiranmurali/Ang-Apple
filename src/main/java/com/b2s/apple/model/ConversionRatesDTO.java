package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class ConversionRatesDTO {
    private final BigMoney costPerPoint;
    private final Optional<BigDecimal> rawConversionRate;
    private final ImmutableMap<CurrencyUnit, FxRateDTO> fxRates;

    private ConversionRatesDTO(final Builder builder) {
        this.costPerPoint = Optionals.checkPresent(builder.costPerPoint, "costPerPoint");
        this.rawConversionRate = builder.rawConversionRate;
        this.fxRates = builder.fxRates;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static ConversionRatesDTO create(
        @JsonProperty("costPerPoint") final BigMoney theCostPerPoint,
        @JsonProperty("rawConversionRate") final BigDecimal theRawConversionRate,
        @JsonProperty("fxRates") final ImmutableMap<CurrencyUnit, FxRateDTO> theFxRates) {
        return builder()
            .withCostPerPoint(theCostPerPoint)
            .withRawConversionRate(theRawConversionRate)
            .withFxRates(theFxRates)
            .build();
    }

    public BigMoney getCostPerPoint() {
        return this.costPerPoint;
    }

    public Optional<BigDecimal> getRawConversionRate() {
        return this.rawConversionRate;
    }

    public ImmutableMap<CurrencyUnit, FxRateDTO> getFxRates() {
        return this.fxRates;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ConversionRatesDTO other = (ConversionRatesDTO) o;
        return Objects.equals(this.costPerPoint, other.costPerPoint)
            && Objects.equals(this.rawConversionRate, other.rawConversionRate)
            && Objects.equals(this.fxRates, other.fxRates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(costPerPoint, rawConversionRate, fxRates);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withCostPerPoint(costPerPoint)
            .withRawConversionRate(rawConversionRate.orElse(null))
            .withFxRates(fxRates);
    }

    public static final class Builder {
        private Optional<BigMoney> costPerPoint = Optional.empty();
        private Optional<BigDecimal> rawConversionRate = Optional.empty();
        private ImmutableMap<CurrencyUnit, FxRateDTO> fxRates = ImmutableMap.of();

        private Builder() {
        }

        public Builder withCostPerPoint(final BigMoney theCostPerPoint) {
            this.costPerPoint = Optionals.from(theCostPerPoint);
            return this;
        }

        public Builder withRawConversionRate(final BigDecimal theRawConversionRate) {
            this.rawConversionRate = Optionals.from(theRawConversionRate);
            return this;
        }

        public Builder withFxRates(final ImmutableMap<CurrencyUnit, FxRateDTO> theFxRates) {
            this.fxRates = AdditionalCollections.asImmutableMap(theFxRates);
            return this;
        }

        public ConversionRatesDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ConversionRatesDTO(this));
        }
    }
}
