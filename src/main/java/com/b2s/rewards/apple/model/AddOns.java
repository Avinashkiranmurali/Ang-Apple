package com.b2s.rewards.apple.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddOns implements Serializable {
    private static final long serialVersionUID = -754126523473265623L;

    private List<Product> servicePlans = new ArrayList<>();
    private List<Product> availableGiftItems = new ArrayList<>();

    public List<Product> getServicePlans() {
        return servicePlans;
    }

    public void setServicePlans(final List<Product> servicePlans) {
        this.servicePlans = servicePlans;
    }

    public List<Product> getAvailableGiftItems() {
        return availableGiftItems;
    }

    public void setAvailableGiftItems(final List<Product> availableGiftItems) {
        this.availableGiftItems = availableGiftItems;
    }
}