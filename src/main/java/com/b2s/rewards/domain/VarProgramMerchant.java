package com.b2s.rewards.domain;

import com.b2s.ui.model.NavigationLink;

/**
 * @author dmontoya
 * @version 1.0, 8/28/13 5:11 PM
 * @since b2r-rewardstep 6.0
 */
public class VarProgramMerchant {

    private Integer merchantId;
    /**
     * Identifies the merchant in the product service. Normally is the merchant short name.
     */
    private String code;
    private String name;
    private NavigationLink link;
    private boolean active;
    private boolean displayedByDefault;
    /**
     * Merchant code used by the VAR. This code is required when mapping from our merchants to the merchant codes used
     * by the VAR.
     */
    private String varMerchantId;
    /**
     * Merchant code used by the VAR's points bank. This code is used to map our merchants codes to the merchant codes
     * used by the VAR when placing orders. In most cases this code will be the same as <code>varMerchantId</code>.
     * @see varMerchantId
     */
    private String pointsBankMerchantId;

    public VarProgramMerchant(final Integer merchantId, final String code, final String name, final NavigationLink link, final boolean active) {
        this.merchantId = merchantId;
        this.code = code;
        this.name = name;
        this.link = link;
        this.active = active;
    }

    public VarProgramMerchant(final Integer merchantId, final String code, final String name, final NavigationLink link, final boolean active, final boolean displayedByDefault) {
        this.merchantId = merchantId;
        this.code = code;
        this.name = name;
        this.link = link;
        this.active = active;
        this.displayedByDefault = displayedByDefault;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public NavigationLink getLink() {
        return link;
    }

    public void setLink(final NavigationLink link) {
        this.link = link;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean isDisplayedByDefault() {
        return displayedByDefault;
    }

    public void setDisplayedByDefault(final boolean displayedByDefault) {
        this.displayedByDefault = displayedByDefault;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getVarMerchantId() {
        return varMerchantId;
    }

    public void setVarMerchantId(final String varMerchantId) {
        this.varMerchantId = varMerchantId;
    }

    public String getPointsBankMerchantId() {
        return pointsBankMerchantId;
    }

    public void setPointsBankMerchantId(final String pointsBankMerchantId) {
        this.pointsBankMerchantId = pointsBankMerchantId;
    }

    public Integer getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(final Integer merchantId) {
        this.merchantId = merchantId;
    }
}
