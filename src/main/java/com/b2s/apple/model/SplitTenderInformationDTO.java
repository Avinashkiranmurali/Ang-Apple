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
public class SplitTenderInformationDTO {

    private final Optional<Integer> pointsAmount;
    private final Optional<Money> cashAmount;
    private final Optional<Integer> currencyScale;
    private final Optional<Money> maxCashAmount;
    private final Optional<BigDecimal> maxPointsPercentage;

    private SplitTenderInformationDTO(final Builder builder) {
        this.pointsAmount = builder.pointsAmount;
        this.cashAmount = builder.cashAmount;
        this.currencyScale = builder.currencyScale;
        this.maxCashAmount = builder.maxCashAmount;
        this.maxPointsPercentage = builder.maxPointsPercentage;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static SplitTenderInformationDTO create(
        @JsonProperty("pointsAmount") final Integer pointsAmount,
        @JsonProperty("cashAmount") final Money cashAmount,
        @JsonProperty("currencyScale") final Integer currencyScale,
        @JsonProperty("maxCashAmount") final Money maxCashAmount,
        @JsonProperty("maxPointsPercentage") final BigDecimal maxPointsPercentage){
        return builder()
            .withPointsAmount(pointsAmount)
            .withCashAmount(cashAmount)
            .withCurrencyScale(currencyScale)
            .withMaxCashAmount(maxCashAmount)
            .withMaxPointsPercentage(maxPointsPercentage)
            .build();
    }

    public Optional<Integer> getPointsAmount() {
        return this.pointsAmount;
    }

    public Optional<Money> getCashAmount() {
        return this.cashAmount;
    }

    public Optional<Integer> getCurrencyScale() {
        return this.currencyScale;
    }

    public Optional<Money> getMaxCashAmount() {
        return this.maxCashAmount;
    }

    public Optional<BigDecimal> getMaxPointsPercentage() {
        return this.maxPointsPercentage;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final SplitTenderInformationDTO other = (SplitTenderInformationDTO) o;
        return Objects.equals(this.pointsAmount, other.pointsAmount)
            && Objects.equals(this.cashAmount, other.cashAmount)
            && Objects.equals(this.currencyScale, other.currencyScale)
            && Objects.equals(this.maxCashAmount, other.maxCashAmount)
            && Objects.equals(this.maxPointsPercentage, other.maxPointsPercentage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointsAmount, cashAmount, currencyScale, maxCashAmount, maxPointsPercentage);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withPointsAmount(pointsAmount.orElse(null))
            .withCashAmount(cashAmount.orElse(null))
            .withCurrencyScale(currencyScale.orElse(null))
            .withMaxCashAmount(maxCashAmount.orElse(null))
            .withMaxPointsPercentage(maxPointsPercentage.orElse(null));
    }

    public static final class Builder {
        private Optional<Integer> pointsAmount = Optional.empty();
        private Optional<Money> cashAmount = Optional.empty();
        private Optional<Integer> currencyScale = Optional.empty();
        private Optional<Money> maxCashAmount = Optional.empty();
        private Optional<BigDecimal> maxPointsPercentage = Optional.empty();

        private Builder() {
        }

        public Builder withPointsAmount(final Integer thePointsAmount) {
            this.pointsAmount = Optionals.from(thePointsAmount);
            return this;
        }

        public Builder withCashAmount(final Money theCashAmount) {
            this.cashAmount = Optionals.from(theCashAmount);
            return this;
        }

        public Builder withCurrencyScale(final Integer theCurrencyScale) {
            this.currencyScale = Optionals.from(theCurrencyScale);
            return this;
        }

        public Builder withMaxCashAmount(final Money theMaxCashAmount) {
            this.maxCashAmount = Optionals.from(theMaxCashAmount);
            return this;
        }

        public Builder withMaxPointsPercentage(final BigDecimal theMaxPointsPercentage) {
            this.maxPointsPercentage = Optionals.from(theMaxPointsPercentage);
            return this;
        }

        public SplitTenderInformationDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new SplitTenderInformationDTO(this));
        }
    }
}
