package com.b2s.apple.model.finance.citi;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class EasyPaymentPlan {

    private Integer tenor;
    private Double effectiveInterestRate;
    private Double annualPercentageRate;
    private Double installmentAmount;
    private String oneTimeProcessingFeeIndicator;
    private Double oneTimeProcessingFeeAmount;
    private Double oneTimeProcessingFeePercentage;
    private String offerIndicator;

    private EasyPaymentPlan (final Builder builder) {
        this.tenor = builder.tenor;
        this.effectiveInterestRate = builder.effectiveInterestRate;
        this.annualPercentageRate = builder.annualPercentageRate;
        this.installmentAmount = builder.installmentAmount;
        this.oneTimeProcessingFeeIndicator = builder.oneTimeProcessingFeeIndicator;
        this.oneTimeProcessingFeeAmount = builder.oneTimeProcessingFeeAmount;
        this.oneTimeProcessingFeePercentage = builder.oneTimeProcessingFeePercentage;
        this.offerIndicator = builder.offerIndicator;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static EasyPaymentPlan create (
            @JsonProperty("tenor") final Integer tenor,
            @JsonProperty("effectiveInterestRate") final Double effectiveInterestRate,
            @JsonProperty("annualPercentageRate") final Double annualPercentageRate,
            @JsonProperty("installmentAmount") final Double installmentAmount,
            @JsonProperty("oneTimeProcessingFeeIndicator") final String oneTimeProcessingFeeIndicator,
            @JsonProperty("oneTimeProcessingFeeAmount") final Double oneTimeProcessingFeeAmount,
            @JsonProperty("oneTimeProcessingFeePercentage") final Double oneTimeProcessingFeePercentage,
            @JsonProperty("offerIndicator") final String offerIndicator) {
        return builder()
                .withTenor(tenor)
                .withEffectiveInterestRate(effectiveInterestRate)
                .withAnnualPercentageRate(annualPercentageRate)
                .withInstallmentAmount(installmentAmount)
                .withOneTimeProcessingFeeIndicator(oneTimeProcessingFeeIndicator)
                .withOneTimeProcessingFeeAmount(oneTimeProcessingFeeAmount)
                .withOneTimeProcessingFeePercentage(oneTimeProcessingFeePercentage)
                .withOfferIndicator(offerIndicator)
                .build();
    }

    public Integer getTenor() {
        return tenor;
    }

    public Double getEffectiveInterestRate() {
        return effectiveInterestRate;
    }

    public Double getAnnualPercentageRate() {
        return annualPercentageRate;
    }

    public Double getInstallmentAmount() {
        return installmentAmount;
    }

    public String getOneTimeProcessingFeeIndicator() {
        return oneTimeProcessingFeeIndicator;
    }

    public Double getOneTimeProcessingFeeAmount() {
        return oneTimeProcessingFeeAmount;
    }

    public Double getOneTimeProcessingFeePercentage() {
        return oneTimeProcessingFeePercentage;
    }

    public String getOfferIndicator() {
        return offerIndicator;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final EasyPaymentPlan other = (EasyPaymentPlan) o;
        return Objects.equals(this.tenor, other.tenor)
                && Objects.equals(this.effectiveInterestRate, other.effectiveInterestRate)
                && Objects.equals(this.annualPercentageRate, other.annualPercentageRate)
                && Objects.equals(this.installmentAmount, other.installmentAmount)
                && Objects.equals(this.oneTimeProcessingFeeIndicator, other.oneTimeProcessingFeeIndicator)
                && Objects.equals(this.oneTimeProcessingFeeAmount, other.oneTimeProcessingFeeAmount)
                && Objects.equals(this.oneTimeProcessingFeePercentage, other.oneTimeProcessingFeePercentage)
                && Objects.equals(this.offerIndicator, other.offerIndicator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenor, effectiveInterestRate, annualPercentageRate,
                installmentAmount, oneTimeProcessingFeeIndicator, oneTimeProcessingFeeAmount,
                oneTimeProcessingFeePercentage, offerIndicator);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
                .withTenor(tenor)
                .withEffectiveInterestRate(effectiveInterestRate)
                .withAnnualPercentageRate(annualPercentageRate)
                .withInstallmentAmount(installmentAmount)
                .withOneTimeProcessingFeeIndicator(oneTimeProcessingFeeIndicator)
                .withOneTimeProcessingFeeAmount(oneTimeProcessingFeeAmount)
                .withOneTimeProcessingFeePercentage(oneTimeProcessingFeePercentage)
                .withOfferIndicator(offerIndicator);
    }

    public static final class Builder {
        private Integer tenor;
        private Double effectiveInterestRate;
        private Double annualPercentageRate;
        private Double installmentAmount;
        private String oneTimeProcessingFeeIndicator;
        private Double oneTimeProcessingFeeAmount;
        private Double oneTimeProcessingFeePercentage;
        private String offerIndicator;

        private Builder() {
        }

        public Builder withTenor(final Integer tenor) {
            this.tenor = tenor;
            return this;
        }

        public Builder withEffectiveInterestRate(final Double effectiveInterestRate) {
            this.effectiveInterestRate = effectiveInterestRate;
            return this;
        }

        public Builder withAnnualPercentageRate(final Double annualPercentageRate) {
            this.annualPercentageRate = annualPercentageRate;
            return this;
        }

        public Builder withInstallmentAmount(final Double installmentAmount) {
            this.installmentAmount = installmentAmount;
            return this;
        }

        public Builder withOneTimeProcessingFeeIndicator(final String oneTimeProcessingFeeIndicator) {
            this.oneTimeProcessingFeeIndicator = oneTimeProcessingFeeIndicator;
            return this;
        }

        public Builder withOneTimeProcessingFeeAmount(final Double oneTimeProcessingFeeAmount) {
            this.oneTimeProcessingFeeAmount = oneTimeProcessingFeeAmount;
            return this;
        }

        public Builder withOneTimeProcessingFeePercentage(final Double oneTimeProcessingFeePercentage) {
            this.oneTimeProcessingFeePercentage = oneTimeProcessingFeePercentage;
            return this;
        }

        public Builder withOfferIndicator(final String offerIndicator) {
            this.offerIndicator = offerIndicator;
            return this;
        }

        public EasyPaymentPlan build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new EasyPaymentPlan(this));
        }
    }
}