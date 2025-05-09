package com.b2s.rewards.apple.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;


public class Subscription implements Comparable<Subscription>, Serializable {

    private static final long serialVersionUID = -4976010095774787068L;

    private String itemId;
    private boolean addedToCart;
    private Integer duration;

    public Subscription(final String itemId, final Integer duration, final boolean addedToCart) {
        this.itemId = itemId;
        this.addedToCart = addedToCart;
        this.duration = duration;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public boolean isAddedToCart() {
        return addedToCart;
    }

    public void setAddedToCart(final boolean addedToCart) {
        this.addedToCart = addedToCart;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(final Integer duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Subscription subscription = (Subscription) o;

        return new EqualsBuilder()
            .append(getItemId(), subscription.getItemId())
            .isEquals();
    }


    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getItemId())
            .toHashCode();
    }


    @Override
    public int compareTo(final Subscription subscription) {
        return this.itemId.compareToIgnoreCase(subscription.getItemId());
    }

}