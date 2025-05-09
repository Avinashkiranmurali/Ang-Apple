package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.joda.money.CurrencyUnit;

import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class CartPriceResultsDTO {

    private final Optional<Integer> totalPoints;
    private final Optional<Integer> unpromotedTotalPoints;
    private final ImmutableMap<CurrencyUnit, CartCurrencyAmountDTO> currencies;

    private CartPriceResultsDTO(final Builder builder) {
        this.totalPoints = builder.totalPoints;
        this.unpromotedTotalPoints = builder.unpromotedTotalPoints;
        this.currencies = builder.currencies;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CartPriceResultsDTO create(
        @JsonProperty("totalPoints") final Integer theTotalPoints,
        @JsonProperty("unpromotedTotalPoints") final Integer theUnpromotedTotalPoints,
        @JsonProperty("currencies") final ImmutableMap<CurrencyUnit, CartCurrencyAmountDTO> theCurrencies) {
        return builder()
            .withTotalPoints(theTotalPoints)
            .withUnpromotedTotalPoints(theUnpromotedTotalPoints)
            .withCurrencies(theCurrencies)
            .build();
    }

    public Optional<Integer> getTotalPoints() {
        return this.totalPoints;
    }

    public Optional<Integer> getUnpromotedTotalPoints() {
        return this.unpromotedTotalPoints;
    }

    public ImmutableMap<CurrencyUnit, CartCurrencyAmountDTO> getCurrencies() {
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
        final CartPriceResultsDTO other = (CartPriceResultsDTO) o;
        return Objects.equals(this.totalPoints, other.totalPoints)
            && Objects.equals(this.unpromotedTotalPoints, other.unpromotedTotalPoints)
            && Objects.equals(this.currencies, other.currencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalPoints, unpromotedTotalPoints, currencies);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withTotalPoints(totalPoints.orElse(null))
            .withUnpromotedTotalPoints(unpromotedTotalPoints.orElse(null))
            .withCurrencies(currencies);
    }

    public static final class Builder {
        private Optional<Integer> totalPoints = Optional.empty();
        private Optional<Integer> unpromotedTotalPoints = Optional.empty();
        private ImmutableMap<CurrencyUnit, CartCurrencyAmountDTO> currencies = ImmutableMap.of();

        private Builder() {
        }

        public Builder withTotalPoints(final Integer theTotalPoints) {
            this.totalPoints = Optionals.from(theTotalPoints);
            return this;
        }

        public Builder withUnpromotedTotalPoints(final Integer theUnpromotedTotalPoints) {
            this.unpromotedTotalPoints = Optionals.from(theUnpromotedTotalPoints);
            return this;
        }

        public Builder withCurrencies(final ImmutableMap<CurrencyUnit, CartCurrencyAmountDTO> theCurrencies) {
            this.currencies = AdditionalCollections.asImmutableMap(theCurrencies);
            return this;
        }

        public CartPriceResultsDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CartPriceResultsDTO(this));
        }
    }
}
