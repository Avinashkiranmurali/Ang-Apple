package com.b2s.rewards.apple.model;

/*** Created by ssrinivasan on 8/23/2016.
 */
public class OrderStatusUpdate {
    public enum Status {
        SUBMITTED,
        CANCELLED,
        FAILED
    }
    private String status;

    private String partnerId;

    private String partnerReferenceId;

    private String pdAmount;

    private String ccAmount;

    private String taxes;

    private String fees;

    private String currencyType;

    public String getCcAmount() {
        return ccAmount;
    }

    public void setCcAmount(final String ccAmount) {
        this.ccAmount = ccAmount;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(final String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerReferenceId() {
        return partnerReferenceId;
    }

    public void setPartnerReferenceId(final String partnerReferenceId) {
        this.partnerReferenceId = partnerReferenceId;
    }

    public String getPdAmount() {
        return pdAmount;
    }

    public void setPdAmount(final String pdAmount) {
        this.pdAmount = pdAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(final String currencyType) {
        this.currencyType = currencyType;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(final String fees) {
        this.fees = fees;
    }

    public String getTaxes() {
        return taxes;
    }

    public void setTaxes(final String taxes) {
        this.taxes = taxes;
    }

    @Override
    public String toString() {
        return "status:"+status+" partnerId:"+partnerId+" partnerReferenceId:"+partnerReferenceId+" pdAmount:"+pdAmount+
                " ccAmount:"+ccAmount+" taxes:"+taxes+ " fees:"+fees +" currencyType:"+currencyType;
    }
}
