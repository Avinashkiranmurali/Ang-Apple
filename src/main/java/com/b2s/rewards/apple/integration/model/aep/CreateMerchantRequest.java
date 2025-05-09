package com.b2s.rewards.apple.integration.model.aep;

import javax.validation.constraints.NotNull;

public class CreateMerchantRequest {

    @NotNull
    private Integer supplierId;
    @NotNull
    private Integer merchantId;
    @NotNull
    private String merchantName;
    @NotNull
    private String merchantSimpleName;
    @NotNull
    private Double taxRate;

    public Integer getSupplierId() {
        return supplierId;
    }

    public Integer getMerchantId() {
        return merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantSimpleName() {
        return merchantSimpleName;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public void setMerchantId(Integer merchantId) {
        this.merchantId = merchantId;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setMerchantSimpleName(String merchantSimpleName) {
        this.merchantSimpleName = merchantSimpleName;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }
}
