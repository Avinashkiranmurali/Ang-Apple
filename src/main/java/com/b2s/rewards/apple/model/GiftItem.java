package com.b2s.rewards.apple.model;

public class GiftItem {

    private String productId;
    private Double discount;
    private String discountType;
    private Engrave engrave;
    private String servicePlan;

    public String getProductId() {
        return productId;
    }

    public void setProductId(final String productId) {
        this.productId = productId;
    }

    public Engrave getEngrave() {
        return engrave;
    }

    public void setEngrave(final Engrave engrave) {
        this.engrave = engrave;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(final Double discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(final String discountType) {
        this.discountType = discountType;
    }

    public String getServicePlan() {
        return servicePlan;
    }

    public void setServicePlan(final String servicePlan) {
        this.servicePlan = servicePlan;
    }

    @Override
    public String toString() {
        return "GiftItem{" +
            "productId='" + productId + '\'' +
            ", discount=" + discount +
            ", discountType='" + discountType + '\'' +
            ", engrave=" + engrave +
            ", servicePlan='"+servicePlan +
            '}';
    }
}
