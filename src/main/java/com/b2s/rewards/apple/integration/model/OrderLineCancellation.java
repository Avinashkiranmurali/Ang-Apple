package com.b2s.rewards.apple.integration.model;

/**
 * @author rpillai
 */
public class OrderLineCancellation {

    private String costPerPoint;
    private String orderLineId;
    private Integer quantityToCancel;
    private Integer totalPointsRefund;
    private String varOrderLineId;
    private String productId;
    private UnitPriceInfo unitPriceInfo;
    private Delivery delivery;
    private CancelReason cancelReason;
    private int originalQuantity;
    private String merchant;

    public String getCostPerPoint() {
        return costPerPoint;
    }

    public void setCostPerPoint(String costPerPoint) {
        this.costPerPoint = costPerPoint;
    }

    public String getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(String orderLineId) {
        this.orderLineId = orderLineId;
    }

    public int getQuantityToCancel() {
        return quantityToCancel;
    }

    public void setQuantityToCancel(int quantityToCancel) {
        this.quantityToCancel = quantityToCancel;
    }

    public int getTotalPointsRefund() {
        return totalPointsRefund;
    }

    public void setTotalPointsRefund(int totalPointsRefund) {
        this.totalPointsRefund = totalPointsRefund;
    }

    public String getVarOrderLineId() {
        return varOrderLineId;
    }

    public void setVarOrderLineId(String varOrderLineId) {
        this.varOrderLineId = varOrderLineId;
    }

    public UnitPriceInfo getUnitPriceInfo() {
        return unitPriceInfo;
    }

    public void setUnitPriceInfo(UnitPriceInfo unitPriceInfo) {
        this.unitPriceInfo = unitPriceInfo;
    }

    public String getProductId() { return productId; }

    public void setProductId(String productId) { this.productId = productId; }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public CancelReason getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(final CancelReason cancelReason) {
        this.cancelReason = cancelReason;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(final int originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }
}
