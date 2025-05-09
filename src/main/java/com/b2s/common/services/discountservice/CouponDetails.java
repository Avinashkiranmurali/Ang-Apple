package com.b2s.common.services.discountservice;

import org.json.JSONObject;

import java.util.Date;
import java.util.Set;

/**
 * Created by rpillai on 7/15/2016.
 */
public class CouponDetails {

    private Date createDate;
    private Double amountOff;
    private String typeOffer;
    private Date expiryFromDate;
    private Date expiryToDate;
    private String friendlyNameCCSet;
    private String friendlyNameDesc;
    private String couponDesc;
    private String multipleUserCoupon;
    private Long maximumClaimAllowed;
    private Set<CouponInfo> couponInfos;
    private Set<CouponFilterInfo> couponFilterInfos;
    private CouponVarConfigInfo couponVarConfig;

    private boolean isValid = true;
    private CouponStatus couponStatus;

    private CouponError couponError;

    // Parses the Error Message and returns the error code
    public String parseErrorMessage(final String message) {
        String errStatus = CouponStatus.AVAILABLE.value;
        try {
            final String errMsg = (String) new JSONObject(message).get("message");
            final String errorCode = errMsg.split(":")[0].trim();
            if (CouponStatus.NOT_FOUND.value.equalsIgnoreCase(errorCode)) {
                errStatus = CouponStatus.NOT_FOUND.value;
            } else if (CouponStatus.EXPIRED.value.equalsIgnoreCase(errorCode)) {
                errStatus = CouponStatus.EXPIRED.value;
            } else if (CouponStatus.USER_CLAIMED.value.equalsIgnoreCase(errorCode)) {
                errStatus = CouponStatus.USER_CLAIMED.value;
            } else if (CouponStatus.INVALID.value.equalsIgnoreCase(errorCode)) {
                errStatus = CouponStatus.INVALID.value;
            } else if (CouponStatus.CLAIMED.value.equalsIgnoreCase(errorCode)) {
                errStatus = CouponStatus.CLAIMED.value;
            } else if (CouponStatus.SAVE_FAILED.value.equalsIgnoreCase(errorCode)) {
                errStatus = CouponStatus.SAVE_FAILED.value;
            }
        } catch (final Exception e) {
            errStatus = CouponStatus.NOT_FOUND.value;
        }

        return errStatus;
    }

    enum CouponStatus {
        AVAILABLE("CC_0000"),
        NOT_FOUND("CC_0001"),
        EXPIRED("CC_0002"),
        USER_CLAIMED("CC_0003"),
        INVALID("CC_0004"),
        CLAIMED("CC_0005"),
        SAVE_FAILED("CC_0010");
        public final String value;

        CouponStatus(final String value) {
            this.value = value;
        }

        public static CouponStatus fromString(final String val) {
            if (val != null) {
                for (final CouponStatus cs : CouponStatus.values()) {
                    if (val.equalsIgnoreCase(cs.value)) {
                        return cs;
                    }
                }
            }
            return null;
        }
    }

    public CouponError getCouponError() {
        return couponError;
    }

    public void setCouponError(CouponError couponError) {
        this.couponError = couponError;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(final Date createDate) {
        this.createDate = createDate;
    }

    public Double getAmountOff() {
        return amountOff;
    }

    public void setAmountOff(final Double amountOff) {
        this.amountOff = amountOff;
    }

    public String getTypeOffer() {
        return typeOffer;
    }

    public void setTypeOffer(final String typeOffer) {
        this.typeOffer = typeOffer;
    }

    public Date getExpiryFromDate() {
        return expiryFromDate;
    }

    public void setExpiryFromDate(final Date expiryFromDate) {
        this.expiryFromDate = expiryFromDate;
    }

    public Date getExpiryToDate() {
        return expiryToDate;
    }

    public void setExpiryToDate(final Date expiryToDate) {
        this.expiryToDate = expiryToDate;
    }

    public String getFriendlyNameCCSet() {
        return friendlyNameCCSet;
    }

    public void setFriendlyNameCCSet(final String friendlyNameCCSet) {
        this.friendlyNameCCSet = friendlyNameCCSet;
    }

    public String getFriendlyNameDesc() {
        return friendlyNameDesc;
    }

    public void setFriendlyNameDesc(final String friendlyNameDesc) {
        this.friendlyNameDesc = friendlyNameDesc;
    }

    public String getCouponDesc() {
        return couponDesc;
    }

    public void setCouponDesc(final String couponDesc) {
        this.couponDesc = couponDesc;
    }

    public String getMultipleUserCoupon() {
        return multipleUserCoupon;
    }

    public void setMultipleUserCoupon(final String multipleUserCoupon) {
        this.multipleUserCoupon = multipleUserCoupon;
    }

    public Long getMaximumClaimAllowed() {
        return maximumClaimAllowed;
    }

    public void setMaximumClaimAllowed(final Long maximumClaimAllowed) {
        this.maximumClaimAllowed = maximumClaimAllowed;
    }

    public Set<CouponInfo> getCouponInfos() {
        return couponInfos;
    }

    public void setCouponInfos(final Set<CouponInfo> couponInfos) {
        this.couponInfos = couponInfos;
    }

    public Set<CouponFilterInfo> getCouponFilterInfos() {
        return couponFilterInfos;
    }

    public void setCouponFilterInfos(final Set<CouponFilterInfo> couponFilterInfos) {
        this.couponFilterInfos = couponFilterInfos;
    }

    public CouponVarConfigInfo getCouponVarConfig() {
        return couponVarConfig;
    }

    public void setCouponVarConfig(final CouponVarConfigInfo couponVarConfig) {
        this.couponVarConfig = couponVarConfig;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(final boolean isValid) {
        this.isValid = isValid;
    }

    public CouponStatus getCouponStatus() {
        return couponStatus;
    }

    public void setCouponStatus(final String couponErrorStatus) {
        this.couponStatus = CouponStatus.fromString(couponErrorStatus);
    }
}
