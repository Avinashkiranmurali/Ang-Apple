package com.b2s.rewards.apple.model;

import java.io.Serializable;
import java.util.Currency;
import java.util.Map;

/**
 * @author dmontoya
 * @version 1.0, 9/13/13 5:12 PM
 * @since b2r-rewardstep 6.0
 */
public class CartTotal implements Serializable {

    private static final long serialVersionUID = -4508366183210813112L;
    private Price price;
    private Price discountedPrice;
    private double discountAmount;
    private Price shippingPrice;
    private Map<String, Tax> taxes;
    private Map<String, Fee> fees;
    private Price expeditedShippingPrice;
    private Price itemsSubtotalPrice;
    private Price discountedItemsSubtotalPrice;
    private Price totalTaxes;
    private Price totalFees;
    private Currency currency;
    private boolean isActual = false;
    private boolean isDiscountApplied = false;
    private Price establishmentFees;

    private Double payPerPeriod;
    private int payPeriods;

    private int discountCodePerOrder;

    public int getDiscountCodePerOrder() {
        return discountCodePerOrder;
    }

    public void setDiscountCodePerOrder(final int discountCodePerOrder) {
        this.discountCodePerOrder = discountCodePerOrder;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(final Price price) {
        this.price = price;
    }

    public Price getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(Price discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public Price getShippingPrice() {
        return shippingPrice;
    }

    public void setShippingPrice(final Price shippingPrice) {
        this.shippingPrice = shippingPrice;
    }

    public Map<String, Tax> getTaxes() {
        return taxes;
    }

    public void setTaxes(final Map<String, Tax> taxes) {
        this.taxes = taxes;
    }

    public Map<String, Fee> getFees() {
        return fees;
    }

    public void setFees(final Map<String, Fee> fees) {
        this.fees = fees;
    }

    public Price getItemsSubtotalPrice() {
        return itemsSubtotalPrice;
    }

    public void setItemsSubtotalPrice(final Price itemsSubtotalPrice) {
        this.itemsSubtotalPrice = itemsSubtotalPrice;
    }

    public Price getDiscountedItemsSubtotalPrice() {
        return discountedItemsSubtotalPrice;
    }

    public void setDiscountedItemsSubtotalPrice(Price discountedItemsSubtotalPrice) {
        this.discountedItemsSubtotalPrice = discountedItemsSubtotalPrice;
    }

    public Price getTotalTaxes() {
        return totalTaxes;
    }

    public void setTotalTaxes(final Price totalTaxes) {
        this.totalTaxes = totalTaxes;
    }

    public Price getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(final Price totalFees) {
        this.totalFees = totalFees;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(final Currency currency) {
        this.currency = currency;
    }

    public Price getExpeditedShippingPrice() {
        return expeditedShippingPrice;
    }

    public void setExpeditedShippingPrice(Price expeditedShippingPrice) {
        this.expeditedShippingPrice = expeditedShippingPrice;
    }

    public boolean isActual() {
        return isActual;
    }

    public void setIsActual(boolean isActual) {
        this.isActual = isActual;
    }

    public Double getPayPerPeriod() {
        return payPerPeriod;
    }

    public void setPayPerPeriod(Double payPerPeriod) {
        this.payPerPeriod = payPerPeriod;
    }

    public boolean isDiscountApplied() {
        return isDiscountApplied;
    }

    public void setIsDiscountApplied(boolean isDiscountApplied) {
        this.isDiscountApplied = isDiscountApplied;
    }

    public int getPayPeriods() {
        return payPeriods;
    }

    public void setPayPeriods(final int payPeriods) {
        this.payPeriods = payPeriods;
    }

    public Price getEstablishmentFees() {
        return establishmentFees;
    }

    public void setEstablishmentFees(final Price establishmentFees) {
        this.establishmentFees = establishmentFees;
    }
}
