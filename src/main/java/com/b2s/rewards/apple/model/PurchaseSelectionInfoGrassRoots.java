package com.b2s.rewards.apple.model;

/**
* Created by srukmagathan on 7/22/2016.
 */
public class PurchaseSelectionInfoGrassRoots extends PurchaseSelectionInfo{

    private Long orderId;
    private boolean isPayrollProviderRedirect;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public boolean getIsPayrollProviderRedirect() {
        return isPayrollProviderRedirect;
    }

    public void setIsPayrollProviderRedirect(final boolean isPayrollProviderRedirect) {
        this.isPayrollProviderRedirect = isPayrollProviderRedirect;
    }
}
