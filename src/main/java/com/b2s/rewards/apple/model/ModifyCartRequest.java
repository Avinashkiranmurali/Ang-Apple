package com.b2s.rewards.apple.model;

/**
 * Created by rpillai on 7/13/2016.
 */
public class ModifyCartRequest {

    private String selectedPaymentOption;
    public String timeZoneId;
    public String selectedRedemptionOption;

    public String getSelectedPaymentOption() {
        return selectedPaymentOption;
    }

    public void setSelectedPaymentOption(String selectedPaymentOption) {
        this.selectedPaymentOption = selectedPaymentOption;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(final String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getSelectedRedemptionOption() {
        return selectedRedemptionOption;
    }

    public void setSelectedRedemptionOption(final String selectedRedemptionOption) {
        this.selectedRedemptionOption = selectedRedemptionOption;
    }
}
