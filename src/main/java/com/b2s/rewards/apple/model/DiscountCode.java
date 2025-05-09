package com.b2s.rewards.apple.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by rpillai on 7/6/2016.
 */
public class DiscountCode implements Serializable {

    private static final long serialVersionUID = -593996111302663408L;
    private String discountCode;
    private String shortDescription;
    private String longDescription;
    private String discountType;
    private Double discountAmount;

    public DiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public DiscountCode(String discountCode, String shortDescription) {
        this.discountCode = discountCode;
        this.shortDescription = shortDescription;
    }

    public DiscountCode(String discountCode, String shortDescription, String longDescription, String discountType, Double discountAmount) {
        this.discountCode = discountCode;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
    }

    public DiscountCode() {}

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscountCode that = (DiscountCode) o;
        return Objects.equals(getDiscountCode(), that.getDiscountCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDiscountCode());
    }
}
