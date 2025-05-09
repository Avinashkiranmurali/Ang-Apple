package com.b2s.rewards.apple.integration.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rperumal
 */
public class UnitPriceInfo {

    private String supplierPrice;
    private String supplierShippingCost;
    private String supplierTax;
    private String shippingCost;
    private String markups;
    private String fees;
    private String taxes;
    private String unitPrice;
    private Integer unitPointsPrice;
    private Integer itemPoints;
    private Map<String, String> additionalCost = new HashMap<String, String>();


    public String getSupplierPrice() {
        return supplierPrice;
    }

    public void setSupplierPrice(String supplierPrice) {
        this.supplierPrice = supplierPrice;
    }

    public String getSupplierShippingCost() {
        return supplierShippingCost;
    }

    public void setSupplierShippingCost(String supplierShippingCost) {
        this.supplierShippingCost = supplierShippingCost;
    }

    public String getSupplierTax() {
        return supplierTax;
    }

    public void setSupplierTax(String supplierTax) {
        this.supplierTax = supplierTax;
    }

    public String getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(String shippingCost) {
        this.shippingCost = shippingCost;
    }

    public String getMarkups() {
        return markups;
    }

    public void setMarkups(String markups) {
        this.markups = markups;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getTaxes() {
        return taxes;
    }

    public void setTaxes(String taxes) {
        this.taxes = taxes;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getUnitPointsPrice() {
        return unitPointsPrice;
    }

    public void setUnitPointsPrice(Integer unitPointsPrice) {
        this.unitPointsPrice = unitPointsPrice;
    }

    public Integer getItemPoints() {
        return itemPoints;
    }

    public void setItemPoints(Integer itemPoints) {
        this.itemPoints = itemPoints;
    }

    public Map<String, String> getAdditionalCost() {
        return additionalCost;
    }

    public void setAdditionalCost(Map<String, String> additionalCost) {
        this.additionalCost = additionalCost;
    }
}
