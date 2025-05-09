package com.b2s.rewards.apple.model;

import com.b2s.rewards.model.Merchant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.beanutils.BeanUtils;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Price information for a product.
 * This will be populated with the details from PricingService.
 *
 * Created by rperumal on 04/08/2015.
 */

/* NOTE:  TO DO: This object will be renamed once brose_product service is refactored */

public class Offer implements Serializable {

    private static final long serialVersionUID = -7036492749574752405L;
    private transient Price basePrice;
    private Double payPerPeriodPrice;
    private int payPeriods;
    private String payDuration;
    private Double upgradeCost;
    private int earnPoints;

    private Double convRate;
    @JsonIgnore
    private Double inverseRate;
    @JsonIgnore
    private Money roundingIncrement;
    @JsonIgnore
    private transient Double orgItemPrice;
    @JsonIgnore
    private transient Double orgShippingPrice;

    private transient Double orgSupplierTaxPrice;
    private transient Double msrpPrice;
    @JsonIgnore
    private transient Double b2sItemMargin;
    @JsonIgnore
    private transient Double b2sShippingMargin;
    @JsonIgnore
    private transient Double varItemMargin;
    @JsonIgnore
    private transient Double varShippingMargin;
    private String sku;
    private String appleSku;
    private Double payPerPeriodTotalPrice; // TO calculate total pay period price using quantity

    private Price displayPrice = new Price(); // User display price
    private Price b2sItemPrice = new Price();  //Price with margins and conversion rate. No taxes, fees or shipping.
    private Price b2sShippingPrice = new Price(); //Base Shipping Price + B2S Margins
    private Price varPrice = new Price(); //Base Shipping Price + B2S Margins
    private Price unpromotedVarPrice = new Price();
    private Price supplierTaxPrice = new Price(); //Supplier Tax price

    @JsonIgnore
    private Money discountPrice;
    @JsonIgnore
    private Money supplierSalesTax;
    @JsonIgnore
    private Money suppliersSalesTax;
    @JsonIgnore
    private Price unpromotedSupplierTaxPrice = new Price();
    @JsonIgnore
    private Integer b2sServiceFeeRateBps;
    @JsonIgnore
    private Price b2sServiceFee = new Price();

    private Price totalPrice = new Price(); //Total price in dollars multiplied by quantity. It includes taxes & fees.
    private Price unitTotalPrice = new Price(); // This price includes taxes and fees for a single unit
    private Price unpromotedDisplayPrice = new Price();
    private Map<String, Fee> fees = new HashMap<>();  //Fees applied in the price calculation. Uses the fee id as the Map key.
    @JsonIgnore
    private Map<String, Fee> unPromotedFees = new HashMap<>();
    private Map<String, Tax> tax = new HashMap<>();  //Various taxes applied in the price calculation. Uses the fee id as the Map key.
    @JsonIgnore
    private Map<String, Tax> unPromotedTax = new HashMap<>();
    @JsonIgnore
    private Price b2sItemProfitPrice;
    // B2S Item profit in Currency (USD) based on B2S_Margin
    @JsonIgnore
    private Price varItemProfitPrice;  // VAR Item profit in Currency (USD) based on VAR_Margin
    @JsonIgnore
    private Double b2sShippingProfitPrice;
    @JsonIgnore
    private Double varShippingProfitPrice;
    private double discountApplied;

    private boolean isEligibleForPayrollDeduction;
    @JsonIgnore
    private Price unpromotedSupplierItemPrice = new Price();

    @JsonIgnore
    private Money discountSupplierSalesTax;

    @JsonIgnore
    private Money retailUnitBasePrice;

    @JsonIgnore
    private Money retailUnitTaxPrice;

    @JsonIgnore
    private Money bridge2UnitBasePrice;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public double getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(final double discountApplied) {
        this.discountApplied = discountApplied;
    }

    public boolean getIsEligibleForPayrollDeduction() {
        return isEligibleForPayrollDeduction;
    }

    public void setIsEligibleForPayrollDeduction(boolean isEligibleForPayrollDeduction) {
        this.isEligibleForPayrollDeduction = isEligibleForPayrollDeduction;
    }

    public Price getUnpromotedDisplayPrice() {
        return unpromotedDisplayPrice;
    }

    public void setUnpromotedDisplayPrice(Price unpromotedDisplayPrice) {
        this.unpromotedDisplayPrice = unpromotedDisplayPrice;
    }

    public int getEarnPoints() {
        return earnPoints;
    }

    public void setEarnPoints(int earnPoints) {
        this.earnPoints = earnPoints;
    }

    public Money getRoundingIncrement() {
        return roundingIncrement;
    }

    public void setRoundingIncrement(Money roundingIncrement) {
        this.roundingIncrement = roundingIncrement;
    }

    public Double getConvRate() {
        return convRate;
    }

    public void setConvRate(Double convRate) {
        this.convRate = convRate;
    }

    public Double getInverseRate() {
        return inverseRate;
    }

    public void setInverseRate(Double inverseRate) {
        this.inverseRate = inverseRate;
    }

    public Price getB2sItemProfitPrice() {
        return b2sItemProfitPrice;
    }

    public void setB2sItemProfitPrice(final Price b2sItemProfitPrice) {
        this.b2sItemProfitPrice = b2sItemProfitPrice;
    }

    public Double getB2sShippingProfitPrice() {
        return b2sShippingProfitPrice;
    }

    public void setB2sShippingProfitPrice(final Double b2sShippingProfitPrice) {
        this.b2sShippingProfitPrice = b2sShippingProfitPrice;
    }

    public Price getVarItemProfitPrice() {
        return varItemProfitPrice;
    }

    public void setVarItemProfitPrice(final Price varItemProfitPrice) {
        this.varItemProfitPrice = varItemProfitPrice;
    }

    public Double getVarShippingProfitPrice() {
        return varShippingProfitPrice;
    }

    public void setVarShippingProfitPrice(final Double varShippingProfitPrice) {
        this.varShippingProfitPrice = varShippingProfitPrice;
    }

    public Double getOrgItemPrice() { return orgItemPrice; }

    public void setOrgItemPrice(Double orgItemPrice) { this.orgItemPrice = orgItemPrice; }

    public Double getOrgShippingPrice() {
        return orgShippingPrice;
    }

    public void setOrgShippingPrice(final Double orgShippingPrice) {
        this.orgShippingPrice = orgShippingPrice;
    }

    public Double getOrgSupplierTaxPrice() {
        return orgSupplierTaxPrice;
    }

    public void setOrgSupplierTaxPrice(final Double orgSupplierTaxPrice) {
        this.orgSupplierTaxPrice = orgSupplierTaxPrice;
    }

    public Double getMsrpPrice() {
        return msrpPrice;
    }

    public void setMsrpPrice(final Double msrpPrice) {
        this.msrpPrice = msrpPrice;
    }

    public Price getDisplayPrice() {
        return displayPrice;
    }

    public void setDisplayPrice(final Price displayPrice) {
        this.displayPrice = displayPrice;
    }

    public Price getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(final Price basePrice) {
        this.basePrice = basePrice;
    }

    public Price getB2sItemPrice() {
        return b2sItemPrice;
    }

    public void setB2sItemPrice(Price b2sItemPrice) {
        this.b2sItemPrice = b2sItemPrice;
    }

    public Price getB2sShippingPrice() {
        return b2sShippingPrice;
    }

    public void setB2sShippingPrice(Price b2sShippingPrice) {
        this.b2sShippingPrice = b2sShippingPrice;
    }

    public Price getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Price totalPrice) {
        this.totalPrice = totalPrice;
    }

    //NOTE: Pricing service is not calculating/sending this at this time
    public Map<String, Fee> getFees() {
        return fees;
    }

    public void setFees(Map<String, Fee> fees) {
        this.fees = fees;
    }

    public Map<String, Tax> getTax() {
        return tax;
    }

    public void setTax(Map<String, Tax> tax) {
        this.tax = tax;
    }

    public Double getB2sItemMargin() {
        return b2sItemMargin;
    }

    public void setB2sItemMargin(Double b2sItemMargin) {
        this.b2sItemMargin = b2sItemMargin;
    }

    public Double getB2sShippingMargin() {
        return b2sShippingMargin;
    }

    public void setB2sShippingMargin(Double b2sShippingMargin) {
        this.b2sShippingMargin = b2sShippingMargin;
    }

    public Double getVarItemMargin() {
        return varItemMargin;
    }

    public void setVarItemMargin(Double varItemMargin) {
        this.varItemMargin = varItemMargin;
    }

    public Double getVarShippingMargin() {
        return varShippingMargin;
    }

    public void setVarShippingMargin(Double varShippingMargin) {
        this.varShippingMargin = varShippingMargin;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Price getUnitTotalPrice() {
        return unitTotalPrice;
    }

    public void setUnitTotalPrice(Price unitTotalPrice) {
        this.unitTotalPrice = unitTotalPrice;
    }

    public Price getVarPrice() {
        return varPrice;
    }

    public void setVarPrice(Price varPrice) {
        this.varPrice = varPrice;
    }

    public Price getSupplierTaxPrice() {
        return supplierTaxPrice;
    }

    public void setSupplierTaxPrice(Price supplierTaxPrice) {
        this.supplierTaxPrice = supplierTaxPrice;
    }

    public Price getB2sServiceFee() {

        return b2sServiceFee;
    }

    public Integer getB2sServiceFeeRateBps() {

        return b2sServiceFeeRateBps;
    }

    public void setB2sServiceFeeRateBps(final Integer b2sServiceFeeRateBps) {

        this.b2sServiceFeeRateBps = b2sServiceFeeRateBps;
    }

    public void setB2sServiceFee(final Price b2sServiceFee) {

        this.b2sServiceFee = b2sServiceFee;
    }

    public Double getPayPerPeriodPrice() {
        return payPerPeriodPrice;
    }

    public void setPayPerPeriodPrice(Double payPerPeriodPrice) {
        this.payPerPeriodPrice = payPerPeriodPrice;
    }

    public int getPayPeriods() {
        return payPeriods;
    }

    public void setPayPeriods(final int payPeriods) {
        this.payPeriods = payPeriods;
    }

    public String getPayDuration() {
        return payDuration;
    }

    public void setPayDuration(final String payDuration) {
        this.payDuration = payDuration;
    }

    public Double getUpgradeCost() {
        return upgradeCost;
    }

    public void setUpgradeCost(final Double upgradeCost) {
        this.upgradeCost = upgradeCost;
    }

    public Price getUnpromotedVarPrice() {
        return unpromotedVarPrice;
    }

    public void setUnpromotedVarPrice(Price unpromotedVarPrice) {
        this.unpromotedVarPrice = unpromotedVarPrice;
    }

    public Price getUnpromotedSupplierTaxPrice() {
        return unpromotedSupplierTaxPrice;
    }

    public void setUnpromotedSupplierTaxPrice(final Price unpromotedSupplierTaxPrice) {
        this.unpromotedSupplierTaxPrice = unpromotedSupplierTaxPrice;
    }

    public Map<String, Fee> getUnPromotedFees() {
        return unPromotedFees;
    }

    public void setUnPromotedFees(final Map<String, Fee> unPromotedFees) {
        this.unPromotedFees = unPromotedFees;
    }

    public Map<String, Tax> getUnPromotedTax() {
        return unPromotedTax;
    }

    public void setUnPromotedTax(final Map<String, Tax> unPromotedTax) {
        this.unPromotedTax = unPromotedTax;
    }

    public Price getUnpromotedSupplierItemPrice() {
        return unpromotedSupplierItemPrice;
    }

    public void setUnpromotedSupplierItemPrice(final Price unpromotedSupplierItemPrice) {
        this.unpromotedSupplierItemPrice = unpromotedSupplierItemPrice;
    }

    public String getAppleSku() {
        return appleSku;
    }

    public void setAppleSku(final String appleSku) {
        this.appleSku = appleSku;
    }

    /**
     * Converts core model shopping cart to apple model cart.
     * @param offer
     * @return
     */
    public static Offer transform(com.b2s.rewards.model.Offer offer){
        Offer appleOffer = new Offer();
        try {
            BeanUtils.copyProperties(appleOffer, offer);
        }catch(IllegalAccessException iAE){
            LOG.error("Property access Exception while accessing Offer for AppleProduct", iAE);
        }catch(InvocationTargetException iTE){
            LOG.error("Property write Exception while setting Offer for AppleProduct", iTE);
        }
        return appleOffer;
    }

    public Double getPayPerPeriodTotalPrice() {
        return payPerPeriodTotalPrice;
    }

    public void setPayPerPeriodTotalPrice(final Double payPerPeriodTotalPrice) {
        this.payPerPeriodTotalPrice = payPerPeriodTotalPrice;
    }

    public Money getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(final Money discountPrice) {
        this.discountPrice = discountPrice;
    }

    public Money getSuppliersSalesTax() {
        return suppliersSalesTax;
    }

    public void setSuppliersSalesTax(final Money suppliersSalesTax) {
        this.suppliersSalesTax = suppliersSalesTax;
    }

    public Money getSupplierSalesTax() {
        return supplierSalesTax;
    }

    public void setSupplierSalesTax(final Money supplierSalesTax) {
        this.supplierSalesTax = supplierSalesTax;
    }

    public void setDiscountSupplierSalesTax(final Money discountSupplierSalesTax) {
        this.discountSupplierSalesTax = discountSupplierSalesTax;
    }

    public Money getDiscountSupplierSalesTax() {
        return discountSupplierSalesTax;
    }

    public Money getRetailUnitBasePrice() {
        return retailUnitBasePrice;
    }

    public void setRetailUnitBasePrice(final Money retailUnitBasePrice) {
        this.retailUnitBasePrice = retailUnitBasePrice;
    }

    public Money getRetailUnitTaxPrice() {
        return retailUnitTaxPrice;
    }

    public void setRetailUnitTaxPrice(final Money retailUnitTaxPrice) {
        this.retailUnitTaxPrice = retailUnitTaxPrice;
    }

    public Money getBridge2UnitBasePrice() {
        return bridge2UnitBasePrice;
    }

    public void setBridge2UnitBasePrice(final Money bridge2UnitBasePrice) {
        this.bridge2UnitBasePrice = bridge2UnitBasePrice;
    }

    @JsonIgnore
    private transient Merchant merchant;
    private Integer quantityRestrictionLimit;

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(final Merchant merchant) {
        this.merchant = merchant;
    }

    public Integer getQuantityRestrictionLimit() {
        return quantityRestrictionLimit;
    }

    public void setQuantityRestrictionLimit(final Integer quantityRestrictionLimit) {
        this.quantityRestrictionLimit = quantityRestrictionLimit;
    }
}
