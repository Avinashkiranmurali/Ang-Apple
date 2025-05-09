package com.b2s.rewards.apple.model;

import java.util.Map;

public class GiftItemRequest {

    private String productId;
    private Map engrave;
    private String servicePlan;

    public String getProductId() {
        return productId;
    }

    public void setProductId(final String productId) {
        this.productId = productId;
    }

    public Map getEngrave() {
        return engrave;
    }

    public void setEngrave(final Map engrave) {
        this.engrave = engrave;
    }

    public String getServicePlan() {
        return servicePlan;
    }

    public void setServicePlan(final String servicePlan) {
        this.servicePlan = servicePlan;
    }
}
