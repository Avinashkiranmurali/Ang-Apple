package com.b2s.rewards.apple.model;

/**
 * Created by msankaradoss on 1/4/2019.
 */
public class AddToCartRequest {
    private String psId;
    private String servicePlanPsId;

    public String getPsId() {
        return psId;
    }

    public void setPsId(final String psId) {
        this.psId = psId;
    }

    public String getServicePlanPsId() {
        return servicePlanPsId;
    }

    public void setServicePlanPsId(final String servicePlanPsId) {
        this.servicePlanPsId = servicePlanPsId;
    }
}
