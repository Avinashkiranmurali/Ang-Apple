package com.b2s.rewards.apple.model;

import javax.persistence.*;

/**
 * Created by rperumal on 2/11/2016
 *
 * Defines different pricing model structure for each market (US, UK, CA etc...)
 *
 */

@Entity
@Table(name="pricing_model_configuration")
public class PricingModelConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "price_type")
    private String priceType;

    @Column(name = "price_key")
    private String priceKey;

    @Column(name = "payment_value")
    private Double paymentValue;

    @Column(name = "payment_value_points")
    private Integer paymentValuePoints;

    @Column(name = "repayment_term")
    private Integer repaymentTerm;

    @Column(name = "months_subsidized")
    private Integer monthsSubsidized;

    @Column(name = "delta")
    private Double delta;

    @Column(name = "discount_tier1")
    private Double discountTier1;

    @Column(name = "discount_tier2")
    private Double discountTier2;

    @Column(name = "discount_tier3")
    private Double discountTier3;


    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(final String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(final String programId) {
        this.programId = programId;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(final String priceType) {
        this.priceType = priceType;
    }

    public String getPriceKey() {
        return priceKey;
    }

    public void setPriceKey(final String priceKey) {
        this.priceKey = priceKey;
    }

    public Double getPaymentValue() {
        return paymentValue;
    }

    public void setPaymentValue(final Double paymentValue) {
        this.paymentValue = paymentValue;
    }

    public Integer getPaymentValuePoints() {
        return paymentValuePoints;
    }

    public void setPaymentValuePoints(Integer paymentValuePoints) {
        this.paymentValuePoints = paymentValuePoints;
    }

    public Integer getRepaymentTerm() {
        return repaymentTerm;
    }

    public void setRepaymentTerm(final Integer repaymentTerm) {
        this.repaymentTerm = repaymentTerm;
    }

    public Integer getMonthsSubsidized() {
        return monthsSubsidized;
    }

    public void setMonthsSubsidized(final Integer monthsSubsidized) {
        this.monthsSubsidized = monthsSubsidized;
    }

    public Double getDelta() {
        return delta;
    }

    public void setDelta(final Double delta) {
        this.delta = delta;
    }

    public Double getDiscountTier1() {
        return discountTier1;
    }

    public void setDiscountTier1(final Double discountTier1) {
        this.discountTier1 = discountTier1;
    }

    public Double getDiscountTier2() {
        return discountTier2;
    }

    public void setDiscountTier2(final Double discountTier2) {
        this.discountTier2 = discountTier2;
    }

    public Double getDiscountTier3() {
        return discountTier3;
    }

    public void setDiscountTier3(final Double discountTier3) {
        this.discountTier3 = discountTier3;
    }
}