package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.money.BigMoney;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class SplitTenderResultDTO {

    private final Money ccBuyInAmount;
    private final int ccBuyInPoints;
    private final Money preCCMarkupAmount;
    private final Money ccB2sProfit;
    private final Money ccVarProfit;
    private final BigDecimal ccB2sMargin;
    private final BigDecimal ccVarMargin;
    private final BigDecimal effectiveB2sMargin;
    private final BigDecimal effectiveVarMargin;
    private final Money ccVarPrice;
    private final Optional<Money> ccPointsEligible;
    private final BigDecimal effectiveConversionRate;
    private final BigDecimal pointPurchaseRate;
    private final Optional<Integer> ccBuyInPointsLimit;
    private final Optional<Money> ccBuyInAmountLimit;
    private final Optional<BigMoney> earnRate;
    private final Optional<Integer> earnedPoints;
    private final Optional<Money> ccTaxAmount;

    private SplitTenderResultDTO(final Builder builder) {
        this.ccBuyInAmount = Optionals.checkPresent(builder.ccBuyInAmount, "ccBuyInAmount");
        this.ccBuyInPoints = Optionals.checkPresent(builder.ccBuyInPoints, "ccBuyInPoints");
        this.preCCMarkupAmount = Optionals.checkPresent(builder.preCCMarkupAmount, "preCCMarkupAmount");
        this.ccB2sProfit = Optionals.checkPresent(builder.ccB2sProfit, "ccB2sProfit");
        this.ccVarProfit = Optionals.checkPresent(builder.ccVarProfit, "ccVarProfit");
        this.ccB2sMargin = Optionals.checkPresent(builder.ccB2sMargin, "ccB2sMargin");
        this.ccVarMargin = Optionals.checkPresent(builder.ccVarMargin, "ccVarMargin");
        this.effectiveB2sMargin = Optionals.checkPresent(builder.effectiveB2sMargin, "effectiveB2sMargin");
        this.effectiveVarMargin = Optionals.checkPresent(builder.effectiveVarMargin, "effectiveVarMargin");
        this.ccVarPrice = Optionals.checkPresent(builder.ccVarPrice, "ccVarPrice");
        this.ccPointsEligible = builder.ccPointsEligible;
        this.effectiveConversionRate = Optionals.checkPresent(
            builder.effectiveConversionRate,
            "effectiveConversionRate");
        this.pointPurchaseRate = Optionals.checkPresent(builder.pointPurchaseRate, "pointPurchaseRate");
        this.ccBuyInPointsLimit = builder.ccBuyInPointsLimit;
        this.ccBuyInAmountLimit = builder.ccBuyInAmountLimit;
        this.earnRate = builder.earnRate;
        this.earnedPoints = builder.earnedPoints;
        this.ccTaxAmount = builder.ccTaxAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @JsonCreator
    public static SplitTenderResultDTO create(
        @JsonProperty("ccBuyInAmount") final Money theCcBuyInAmount,
        @JsonProperty("ccBuyInPoints") final Integer theCcBuyInPoints,
        @JsonProperty("preCCMarkupAmount") final Money thePreCCMarkupAmount,
        @JsonProperty("ccB2sProfit") final Money theCcB2sProfit,
        @JsonProperty("ccVarProfit") final Money theCcVarProfit,
        @JsonProperty("ccB2sMargin") final BigDecimal theCcB2sMargin,
        @JsonProperty("ccVarMargin") final BigDecimal theCcVarMargin,
        @JsonProperty("effectiveB2sMargin") final BigDecimal theEffectiveB2sMargin,
        @JsonProperty("effectiveVarMargin") final BigDecimal theEffectiveVarMargin,
        @JsonProperty("ccVarPrice") final Money theCcVarPrice,
        @JsonProperty("ccPointsEligible") final Money theCcPointsEligible,
        @JsonProperty("effectiveConversionRate") final BigDecimal theEffectiveConversionRate,
        @JsonProperty("pointPurchaseRate") final BigDecimal thePointPurchaseRate,
        @JsonProperty("ccBuyInPointsLimit") final Integer theCcBuyInPointsLimit,
        @JsonProperty("ccBuyInAmountLimit") final Money theCcBuyInAmountLimit,
        @JsonProperty("earnRate") final BigMoney theEarnRate,
        @JsonProperty("earnedPoints") final Integer theEarnedPoints,
        @JsonProperty("ccTaxAmount") final Money theCcTaxAmount) {
        return builder()
            .withCcBuyInAmount(theCcBuyInAmount)
            .withCcBuyInPoints(theCcBuyInPoints)
            .withPreCCMarkupAmount(thePreCCMarkupAmount)
            .withCcB2sProfit(theCcB2sProfit)
            .withCcVarProfit(theCcVarProfit)
            .withCcB2sMargin(theCcB2sMargin)
            .withCcVarMargin(theCcVarMargin)
            .withEffectiveB2sMargin(theEffectiveB2sMargin)
            .withEffectiveVarMargin(theEffectiveVarMargin)
            .withCcVarPrice(theCcVarPrice)
            .withCcPointsEligible(theCcPointsEligible)
            .withEffectiveConversionRate(theEffectiveConversionRate)
            .withPointPurchaseRate(thePointPurchaseRate)
            .withCcBuyInPointsLimit(theCcBuyInPointsLimit)
            .withCcBuyInAmountLimit(theCcBuyInAmountLimit)
            .withEarnRate(theEarnRate)
            .withEarnedPoints(theEarnedPoints)
            .withCcTaxAmount(theCcTaxAmount)
            .build();
    }

    public Money getCcBuyInAmount() {
        return this.ccBuyInAmount;
    }

    public int getCcBuyInPoints() {
        return this.ccBuyInPoints;
    }

    public Money getPreCCMarkupAmount() {
        return this.preCCMarkupAmount;
    }

    public Money getCcB2sProfit() {
        return this.ccB2sProfit;
    }

    public Money getCcVarProfit() {
        return this.ccVarProfit;
    }

    public BigDecimal getCcB2sMargin() {
        return this.ccB2sMargin;
    }

    public BigDecimal getCcVarMargin() {
        return this.ccVarMargin;
    }

    public BigDecimal getEffectiveB2sMargin() {
        return this.effectiveB2sMargin;
    }

    public BigDecimal getEffectiveVarMargin() {
        return this.effectiveVarMargin;
    }

    public Money getCcVarPrice() {
        return this.ccVarPrice;
    }

    public Optional<Money> getCcPointsEligible() {
        return this.ccPointsEligible;
    }

    public BigDecimal getEffectiveConversionRate() {
        return this.effectiveConversionRate;
    }

    public BigDecimal getPointPurchaseRate() {
        return this.pointPurchaseRate;
    }

    public Optional<Integer> getCcBuyInPointsLimit() {
        return this.ccBuyInPointsLimit;
    }

    public Optional<Money> getCcBuyInAmountLimit() {
        return this.ccBuyInAmountLimit;
    }

    public Optional<BigMoney> getEarnRate() {
        return this.earnRate;
    }

    public Optional<Integer> getEarnedPoints() {
        return this.earnedPoints;
    }

    public Optional<Money> getCcTaxAmount() {
        return this.ccTaxAmount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final SplitTenderResultDTO other = (SplitTenderResultDTO) o;
        return Objects.equals(this.ccBuyInAmount, other.ccBuyInAmount)
            && Objects.equals(this.ccBuyInPoints, other.ccBuyInPoints)
            && Objects.equals(this.preCCMarkupAmount, other.preCCMarkupAmount)
            && Objects.equals(this.ccB2sProfit, other.ccB2sProfit)
            && Objects.equals(this.ccVarProfit, other.ccVarProfit)
            && Objects.equals(this.ccB2sMargin, other.ccB2sMargin)
            && Objects.equals(this.ccVarMargin, other.ccVarMargin)
            && Objects.equals(this.effectiveB2sMargin, other.effectiveB2sMargin)
            && Objects.equals(this.effectiveVarMargin, other.effectiveVarMargin)
            && Objects.equals(this.ccVarPrice, other.ccVarPrice)
            && Objects.equals(this.ccPointsEligible, other.ccPointsEligible)
            && Objects.equals(this.effectiveConversionRate, other.effectiveConversionRate)
            && Objects.equals(this.pointPurchaseRate, other.pointPurchaseRate)
            && Objects.equals(this.ccBuyInPointsLimit, other.ccBuyInPointsLimit)
            && Objects.equals(this.ccBuyInAmountLimit, other.ccBuyInAmountLimit)
            && Objects.equals(this.earnRate, other.earnRate)
            && Objects.equals(this.earnedPoints, other.earnedPoints)
            && Objects.equals(this.ccTaxAmount, other.ccTaxAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ccBuyInAmount,
            ccBuyInPoints,
            preCCMarkupAmount,
            ccB2sProfit,
            ccVarProfit,
            ccB2sMargin,
            ccVarMargin,
            effectiveB2sMargin,
            effectiveVarMargin,
            ccVarPrice,
            ccPointsEligible,
            effectiveConversionRate,
            pointPurchaseRate,
            ccBuyInPointsLimit,
            ccBuyInAmountLimit,
            earnRate,
            earnedPoints,
            ccTaxAmount);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withCcBuyInAmount(ccBuyInAmount)
            .withCcBuyInPoints(ccBuyInPoints)
            .withPreCCMarkupAmount(preCCMarkupAmount)
            .withCcB2sProfit(ccB2sProfit)
            .withCcVarProfit(ccVarProfit)
            .withCcB2sMargin(ccB2sMargin)
            .withCcVarMargin(ccVarMargin)
            .withEffectiveB2sMargin(effectiveB2sMargin)
            .withEffectiveVarMargin(effectiveVarMargin)
            .withCcVarPrice(ccVarPrice)
            .withCcPointsEligible(ccPointsEligible.orElse(null))
            .withEffectiveConversionRate(effectiveConversionRate)
            .withPointPurchaseRate(pointPurchaseRate)
            .withCcBuyInPointsLimit(ccBuyInPointsLimit.orElse(null))
            .withCcBuyInAmountLimit(ccBuyInAmountLimit.orElse(null))
            .withEarnRate(earnRate.orElse(null))
            .withEarnedPoints(earnedPoints.orElse(null))
            .withCcTaxAmount(ccTaxAmount.orElse(null));
    }

    public static final class Builder {
        private Optional<Money> ccBuyInAmount = Optional.empty();
        private Optional<Integer> ccBuyInPoints = Optional.empty();
        private Optional<Money> preCCMarkupAmount = Optional.empty();
        private Optional<Money> ccB2sProfit = Optional.empty();
        private Optional<Money> ccVarProfit = Optional.empty();
        private Optional<BigDecimal> ccB2sMargin = Optional.empty();
        private Optional<BigDecimal> ccVarMargin = Optional.empty();
        private Optional<BigDecimal> effectiveB2sMargin = Optional.empty();
        private Optional<BigDecimal> effectiveVarMargin = Optional.empty();
        private Optional<Money> ccVarPrice = Optional.empty();
        private Optional<Money> ccPointsEligible = Optional.empty();
        private Optional<BigDecimal> effectiveConversionRate = Optional.empty();
        private Optional<BigDecimal> pointPurchaseRate = Optional.empty();
        private Optional<Integer> ccBuyInPointsLimit = Optional.empty();
        private Optional<Money> ccBuyInAmountLimit = Optional.empty();
        private Optional<BigMoney> earnRate = Optional.empty();
        private Optional<Integer> earnedPoints = Optional.empty();
        private Optional<Money> ccTaxAmount = Optional.empty();

        private Builder() {
        }

        public Builder withCcBuyInAmount(final Money theCcBuyInAmount) {
            this.ccBuyInAmount = Optionals.from(theCcBuyInAmount);
            return this;
        }

        public Builder withCcBuyInPoints(final Integer theCcBuyInPoints) {
            this.ccBuyInPoints = Optionals.from(theCcBuyInPoints);
            return this;
        }

        public Builder withPreCCMarkupAmount(final Money thePreCCMarkupAmount) {
            this.preCCMarkupAmount = Optionals.from(thePreCCMarkupAmount);
            return this;
        }

        public Builder withCcB2sProfit(final Money theCcB2sProfit) {
            this.ccB2sProfit = Optionals.from(theCcB2sProfit);
            return this;
        }

        public Builder withCcVarProfit(final Money theCcVarProfit) {
            this.ccVarProfit = Optionals.from(theCcVarProfit);
            return this;
        }

        public Builder withCcB2sMargin(final BigDecimal theCcB2sMargin) {
            this.ccB2sMargin = Optionals.from(theCcB2sMargin);
            return this;
        }

        public Builder withCcVarMargin(final BigDecimal theCcVarMargin) {
            this.ccVarMargin = Optionals.from(theCcVarMargin);
            return this;
        }

        public Builder withEffectiveB2sMargin(final BigDecimal theEffectiveB2sMargin) {
            this.effectiveB2sMargin = Optionals.from(theEffectiveB2sMargin);
            return this;
        }

        public Builder withEffectiveVarMargin(final BigDecimal theEffectiveVarMargin) {
            this.effectiveVarMargin = Optionals.from(theEffectiveVarMargin);
            return this;
        }

        public Builder withCcVarPrice(final Money theCcVarPrice) {
            this.ccVarPrice = Optionals.from(theCcVarPrice);
            return this;
        }

        public Builder withCcPointsEligible(final Money theCcPointsEligible) {
            this.ccPointsEligible = Optionals.from(theCcPointsEligible);
            return this;
        }

        public Builder withEffectiveConversionRate(final BigDecimal theEffectiveConversionRate) {
            this.effectiveConversionRate = Optionals.from(theEffectiveConversionRate);
            return this;
        }

        public Builder withPointPurchaseRate(final BigDecimal thePointPurchaseRate) {
            this.pointPurchaseRate = Optionals.from(thePointPurchaseRate);
            return this;
        }

        public Builder withCcBuyInPointsLimit(final Integer theCcBuyInPointsLimit) {
            this.ccBuyInPointsLimit = Optionals.from(theCcBuyInPointsLimit);
            return this;
        }

        public Builder withCcBuyInAmountLimit(final Money theCcBuyInAmountLimit) {
            this.ccBuyInAmountLimit = Optionals.from(theCcBuyInAmountLimit);
            return this;
        }

        public Builder withEarnRate(final BigMoney theEarnRate) {
            this.earnRate = Optionals.from(theEarnRate);
            return this;
        }

        public Builder withEarnedPoints(final Integer theEarnedPoints) {
            this.earnedPoints = Optionals.from(theEarnedPoints);
            return this;
        }

        public Builder withCcTaxAmount(final Money theCcTaxAmount) {
            this.ccTaxAmount = Optionals.from(theCcTaxAmount);
            return this;
        }

        public SplitTenderResultDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new SplitTenderResultDTO(this));
        }
    }
}
