package com.b2s.common.services.discountservice;

/**
 * Created by rpillai on 7/15/2016.
 */
public class CouponInfo {

    private String couponCode;
    private String friendlyDesc;
    private Long amountOff;
    private String typeOffer;

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getFriendlyDesc() {
        return friendlyDesc;
    }

    public void setFriendlyDesc(String friendlyDesc) {
        this.friendlyDesc = friendlyDesc;
    }

    public Long getAmountOff() {
        return amountOff;
    }

    public void setAmountOff(Long amountOff) {
        this.amountOff = amountOff;
    }

    public String getTypeOffer() {
        return typeOffer;
    }

    public void setTypeOffer(String typeOffer) {
        this.typeOffer = typeOffer;
    }
}
