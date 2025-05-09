package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
* Created by srukmagathan on 7/22/2016.
 */
public class PurchaseSelectionInfoBSWIFT extends PurchaseSelectionInfo{

    @JsonProperty("clientId")
    private String clientId;
    @JsonProperty("userReturnToken")
    private String userReturnToken;
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("purchaseDate")
    private String purchaseDate;
    @JsonProperty("skuPurchased")
    private String skuPurchased;
    @JsonProperty("skuQuantity")
    private String skuQuantity;
    @JsonProperty("cartSubtotal")
    private String cartSubtotal;
    @JsonProperty("cartTaxes")
    private String cartTaxes;
    @JsonProperty("cartShipping")
    private String cartShipping;
    @JsonProperty("cartTotalInclusive")
    private String cartTotalInclusive;
    @JsonProperty("discountCodeUsed")
    private String discountCodeUsed;
    @JsonProperty("discountCodeValue")
    private String discountCodeValue;
    @JsonProperty("balanceRemaining")
    private String balanceRemaining;


    public String getCartShipping() {
        return cartShipping;
    }

    public void setCartShipping(final String cartShipping) {
        this.cartShipping = cartShipping;
    }

    public String getCartSubtotal() {
        return cartSubtotal;
    }

    public void setCartSubtotal(final String cartSubtotal) {
        this.cartSubtotal = cartSubtotal;
    }

    public String getCartTaxes() {
        return cartTaxes;
    }

    public void setCartTaxes(final String cartTaxes) {
        this.cartTaxes = cartTaxes;
    }

    public String getCartTotalInclusive() {
        return cartTotalInclusive;
    }

    public void setCartTotalInclusive(final String cartTotalInclusive) {
        this.cartTotalInclusive = cartTotalInclusive;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public void setUserReturnToken(final String userReturnToken) {
        this.userReturnToken = userReturnToken;
    }

    public String getDiscountCodeUsed() {
        return discountCodeUsed;
    }

    public void setDiscountCodeUsed(final String discountCodeUsed) {
        this.discountCodeUsed = discountCodeUsed;
    }

    public String getDiscountCodeValue() {
        return discountCodeValue;
    }

    public void setDiscountCodeValue(final String discountCodeValue) {
        this.discountCodeValue = discountCodeValue;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(final String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getSkuPurchased() {
        return skuPurchased;
    }

    public void setSkuPurchased(final String skuPurchased) {
        this.skuPurchased = skuPurchased;
    }

    public String getSkuQuantity() {
        return skuQuantity;
    }

    public void setSkuQuantity(final String skuQuantity) {
        this.skuQuantity = skuQuantity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getBalanceRemaining() {
        return balanceRemaining;
    }

    public void setBalanceRemaining(final String balanceRemaining) {
        this.balanceRemaining = balanceRemaining;
    }
}
