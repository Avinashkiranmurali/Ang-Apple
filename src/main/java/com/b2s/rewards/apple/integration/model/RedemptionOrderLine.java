package com.b2s.rewards.apple.integration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author rperumal
 */
public class RedemptionOrderLine {

    private String orderLineId;
    private String varOrderLineId;
    private String description;
    private String merchant;
    private Integer quantity;
    private String productId;
    private String productType;
    private String supplierProductId;
    private String totalPrice;
    private Integer totalPointsPrice;
    private String costPerPoint;
    private BigDecimal margin = BigDecimal.ZERO;
    private BigDecimal taxRate = BigDecimal.ZERO;
    private Map<String, Object> additionalInfo = new HashMap<>();
    private UnitPriceInfo unitPriceInfo;
    private ConversionRateInfo conversionRateInfo;
    private ProductDetails productDetails;
    private Double convRate;
    private ShipmentDeliveryInfo delivery;

    public String getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(String orderLineId) {
        this.orderLineId = orderLineId;
    }

    public String getVarOrderLineId() {
        return varOrderLineId;
    }

    public void setVarOrderLineId(String varOrderLineId) {
        this.varOrderLineId = varOrderLineId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSupplierProductId() {
        return supplierProductId;
    }

    public void setSupplierProductId(String supplierProductId) {
        this.supplierProductId = supplierProductId;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getTotalPointsPrice() {
        return totalPointsPrice;
    }

    public void setTotalPointsPrice(Integer totalPointsPrice) {
        this.totalPointsPrice = totalPointsPrice;
    }

    public String getCostPerPoint() {
        return costPerPoint;
    }

    public void setCostPerPoint(String costPerPoint) {
        this.costPerPoint = costPerPoint;
    }

    @JsonProperty
    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(final BigDecimal margin) {
        this.margin = Optional.ofNullable(margin).orElse(BigDecimal.ZERO);
    }

    // Ignoring this setter to avoid Jackson2 failure due to conflicting setters. This setter should
    // only be used when creating the request, not by the deserializer. Remove when introducing builder.
    @JsonIgnore
    public void setMargin(final Double margin) {
        this.margin = Optional.ofNullable(margin).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
    }

    @JsonProperty
    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(final BigDecimal taxRate) {
        this.taxRate = Optional.ofNullable(taxRate).orElse(BigDecimal.ZERO);
    }

    // Ignoring this setter to avoid Jackson2 failure due to conflicting setters. This setter should
    // only be used when creating the request, not by the deserializer. Remove when introducing builder.
    @JsonIgnore
    public void setTaxRate(final Integer taxRate) {
        this.taxRate = Optional.ofNullable(taxRate).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
    }

    public UnitPriceInfo getUnitPriceInfo() {
        return unitPriceInfo;
    }

    public void setUnitPriceInfo(UnitPriceInfo unitPriceInfo) {
        this.unitPriceInfo = unitPriceInfo;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public ConversionRateInfo getConversionRateInfo() {
        return conversionRateInfo;
    }

    public void setConversionRateInfo(final ConversionRateInfo conversionRateInfo) {
        this.conversionRateInfo = conversionRateInfo;
    }

    public ProductDetails getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(final ProductDetails productDetails) {
        this.productDetails = productDetails;
    }


    public Double getConvRate() {
        return convRate;
    }

    public void setConvRate(final Double convRate) {
        this.convRate = convRate;
    }

    public String getProductType() { return  productType; }

    public void setProductType(final String productType) { this.productType = productType; }

    public ShipmentDeliveryInfo getDelivery() { return delivery; }

    public void setDelivery(final ShipmentDeliveryInfo delivery) { this.delivery = delivery; }

}
