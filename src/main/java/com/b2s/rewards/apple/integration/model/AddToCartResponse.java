package com.b2s.rewards.apple.integration.model;

/**
 * Created by srukmagathan on 24-05-2017.
 */
public class AddToCartResponse {

    private Long cartItemId;

    private boolean cartTotalModified;

    private boolean quantityLimitExceed=false;

    private boolean giftcardMaxQuantity = false;

    private boolean physicalGiftcardTotalValueFull = false;

    private boolean pricingFull = false;

    private Integer physicalGiftcardMaxValue;

    public boolean isPricingFull() {
        return pricingFull;
    }

    public void setPricingFull(final boolean pricingFull) {
        this.pricingFull = pricingFull;
    }

    public boolean isPhysicalGiftcardTotalValueFull() {
        return physicalGiftcardTotalValueFull;
    }

    public void setPhysicalGiftcardTotalValueFull(final boolean physicalGiftcardTotalValueFull) {
        this.physicalGiftcardTotalValueFull = physicalGiftcardTotalValueFull;
    }

    public Integer getPhysicalGiftcardMaxValue() {
        return physicalGiftcardMaxValue;
    }

    public void setPhysicalGiftcardMaxValue(final Integer physicalGiftcardMaxValue) {
        this.physicalGiftcardMaxValue = physicalGiftcardMaxValue;
    }

    public boolean isGiftcardMaxQuantity() {
        return giftcardMaxQuantity;
    }

    public void setGiftcardMaxQuantity(final boolean giftcardMaxQuantity) {
        this.giftcardMaxQuantity = giftcardMaxQuantity;
    }

    public boolean isQuantityLimitExceed() {
        return quantityLimitExceed;
    }

    public void setQuantityLimitExceed(final boolean quantityLimitExceed) {
        this.quantityLimitExceed = quantityLimitExceed;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(final Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public boolean isCartTotalModified() {
        return cartTotalModified;
    }

    public void setCartTotalModified(boolean cartTotalModified) {
        this.cartTotalModified = cartTotalModified;
    }
}
