package com.b2s.rewards.apple.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by rpillai on 6/24/2016.
 */
public class PaymentOption implements Serializable {

    private static final long serialVersionUID = 8568712057637532338L;

    private String paymentOption;

    private PaymentProvider paymentProvider;

    private String paymentTemplate;

    private Boolean isActive;

    private Integer orderBy;

    private Double paymentMinLimit;

    private Double paymentMaxLimit;

    private String supplementaryPaymentType;

    private String supplementaryPaymentLimitType;

    private Integer supplementaryPaymentMinLimit;

    private Integer supplementaryPaymentMaxLimit;

    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(final String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public PaymentProvider getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(final PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public String getPaymentTemplate() {
        return paymentTemplate;
    }

    public void setPaymentTemplate(final String paymentTemplate) {
        this.paymentTemplate = paymentTemplate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(final Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(final Integer orderBy) {
        this.orderBy = orderBy;
    }

    public Double getPaymentMinLimit() {
        return paymentMinLimit;
    }

    public void setPaymentMinLimit(final Double paymentMinLimit) {
        this.paymentMinLimit = paymentMinLimit;
    }

    public Double getPaymentMaxLimit() {
        return paymentMaxLimit;
    }

    public void setPaymentMaxLimit(final Double paymentMaxLimit) {
        this.paymentMaxLimit = paymentMaxLimit;
    }

    public String getSupplementaryPaymentType() {
        return supplementaryPaymentType;
    }

    public void setSupplementaryPaymentType(final String supplementaryPaymentType) {
        this.supplementaryPaymentType = supplementaryPaymentType;
    }

    public String getSupplementaryPaymentLimitType() {
        return supplementaryPaymentLimitType;
    }

    public void setSupplementaryPaymentLimitType(final String supplementaryPaymentLimitType) {
        this.supplementaryPaymentLimitType = supplementaryPaymentLimitType;
    }

    public Integer getSupplementaryPaymentMinLimit() {
        return supplementaryPaymentMinLimit;
    }

    public void setSupplementaryPaymentMinLimit(final Integer supplementaryPaymentMinLimit) {
        this.supplementaryPaymentMinLimit = supplementaryPaymentMinLimit;
    }

    public Integer getSupplementaryPaymentMaxLimit() {
        return supplementaryPaymentMaxLimit;
    }

    public void setSupplementaryPaymentMaxLimit(final Integer supplementaryPaymentMaxLimit) {
        this.supplementaryPaymentMaxLimit = supplementaryPaymentMaxLimit;
    }

    public enum PaymentProvider {
        PPC, GRASSROOTS;

        public static Optional<PaymentProvider> fromName(final String providerName) {
            return Optional.ofNullable(providerName)
                .flatMap(name -> Arrays.stream(values())
                    .filter(paymentProvider -> paymentProvider.name().equals(name)).findFirst());
        }
    }

}
