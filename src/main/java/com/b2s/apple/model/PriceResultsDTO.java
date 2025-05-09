package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.b2s.service.utils.lang.string.ToStringGenerator;
import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableMap;
import org.joda.money.CurrencyUnit;

import java.util.*;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class PriceResultsDTO {

    private final Optional<Integer> unitPoints;
    private final Optional<Integer> totalPoints;
    private final Optional<Integer> unpromotedUnitPoints;
    private final Optional<Integer> unpromotedTotalPoints;
    private final ImmutableMap<CurrencyUnit, CurrencyAmountDTO> currencies;

    private PriceResultsDTO(final Builder builder) {
        this.unitPoints = builder.unitPoints;
        this.totalPoints = builder.totalPoints;
        this.unpromotedUnitPoints = builder.unpromotedUnitPoints;
        this.unpromotedTotalPoints = builder.unpromotedTotalPoints;
        this.currencies = builder.currencies;
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @JsonCreator
    public static PriceResultsDTO create(
        @JsonProperty("unitPoints") final Integer theUnitPoints,
        @JsonProperty("totalPoints") final Integer theTotalPoints,
        @JsonProperty("unpromotedUnitPoints") final Integer theUnpromotedUnitPoints,
        @JsonProperty("unpromotedTotalPoints") final Integer theUnpromotedTotalPoints,
        @JsonProperty("currencies") final ImmutableMap<CurrencyUnit, CurrencyAmountDTO> theCurrencies) {
        return builder()
            .withUnitPoints(theUnitPoints)
            .withTotalPoints(theTotalPoints)
            .withUnpromotedUnitPoints(theUnpromotedUnitPoints)
            .withUnpromotedTotalPoints(theUnpromotedTotalPoints)
            .withCurrencies(theCurrencies)
            .build();
    }

    public Optional<Integer> getUnitPoints() {
        return this.unitPoints;
    }

    public Optional<Integer> getTotalPoints() {
        return this.totalPoints;
    }

    public Optional<Integer> getUnpromotedUnitPoints() {
        return this.unpromotedUnitPoints;
    }

    public Optional<Integer> getUnpromotedTotalPoints() {
        return this.unpromotedTotalPoints;
    }

    public ImmutableMap<CurrencyUnit, CurrencyAmountDTO> getCurrencies() {
        return this.currencies;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final PriceResultsDTO other = (PriceResultsDTO) o;
        return Objects.equals(this.unitPoints, other.unitPoints)
            && Objects.equals(this.totalPoints, other.totalPoints)
            && Objects.equals(this.unpromotedUnitPoints, other.unpromotedUnitPoints)
            && Objects.equals(this.unpromotedTotalPoints, other.unpromotedTotalPoints)
            && Objects.equals(this.currencies, other.currencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitPoints, totalPoints, unpromotedUnitPoints, unpromotedTotalPoints, currencies);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withUnitPoints(unitPoints.orElse(null))
            .withTotalPoints(totalPoints.orElse(null))
            .withUnpromotedUnitPoints(unpromotedUnitPoints.orElse(null))
            .withUnpromotedTotalPoints(unpromotedTotalPoints.orElse(null))
            .withCurrencies(currencies);
    }

    public static final class Builder {
        private Optional<Integer> unitPoints = Optional.empty();
        private Optional<Integer> totalPoints = Optional.empty();
        private Optional<Integer> unpromotedUnitPoints = Optional.empty();
        private Optional<Integer> unpromotedTotalPoints = Optional.empty();
        private ImmutableMap<CurrencyUnit, CurrencyAmountDTO> currencies = ImmutableMap.of();

        private Builder() {
        }

        public Builder withUnitPoints(final Integer theUnitPoints) {
            this.unitPoints = Optionals.from(theUnitPoints);
            return this;
        }

        public Builder withTotalPoints(final Integer theTotalPoints) {
            this.totalPoints = Optionals.from(theTotalPoints);
            return this;
        }

        public Builder withUnpromotedUnitPoints(final Integer theUnpromotedUnitPoints) {
            this.unpromotedUnitPoints = Optionals.from(theUnpromotedUnitPoints);
            return this;
        }

        public Builder withUnpromotedTotalPoints(final Integer theUnpromotedTotalPoints) {
            this.unpromotedTotalPoints = Optionals.from(theUnpromotedTotalPoints);
            return this;
        }

        public Builder withCurrencies(final ImmutableMap<CurrencyUnit, CurrencyAmountDTO> theCurrencies) {
            this.currencies = AdditionalCollections.asImmutableMap(theCurrencies);
            return this;
        }

        public PriceResultsDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new PriceResultsDTO(this));
        }
    }
}
