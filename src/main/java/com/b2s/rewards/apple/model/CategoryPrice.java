package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by rpillai on 7/27/2016.
 */
public class CategoryPrice implements Serializable {

    private static final long serialVersionUID = 269642017502951743L;
    private String categoryName;
    private Option option;

    private boolean isFree;

    private int startingFromPrice;
    private String fromPriceMessage;

    @JsonIgnore
    private transient double actualStartingFromPrice;

    @JsonIgnore
    private transient double highestPriceInCategory;

    @JsonIgnore
    private transient Price minDisplayPrice;

    @JsonIgnore
    private transient Price minUnpromotedDisplayPrice;

    @JsonIgnore
    private transient Price maxDisplayPrice;

    @JsonIgnore
    private transient Price maxUnpromotedDisplayPrice;

    public CategoryPrice(){}

    public CategoryPrice(String categoryName, boolean isFree, double actualStartingFromPrice) {
        this.categoryName = categoryName;
        this.isFree = isFree;
        this.actualStartingFromPrice = actualStartingFromPrice;
        this.startingFromPrice = BigDecimal.valueOf(actualStartingFromPrice).setScale(0, RoundingMode.HALF_UP).intValue();
    }


    public double getActualStartingFromPrice() {
        return actualStartingFromPrice;
    }

    public void setActualStartingFromPrice(double actualStartingFromPrice) {
        this.actualStartingFromPrice = actualStartingFromPrice;
        this.startingFromPrice = BigDecimal.valueOf(actualStartingFromPrice).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public String getFromPriceMessage() {
        return fromPriceMessage;
    }

    public void setFromPriceMessage(String fromPriceMessage) {
        this.fromPriceMessage = fromPriceMessage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(boolean isFree) {
        this.isFree = isFree;
    }

    public int getStartingFromPrice() {
        return startingFromPrice;
    }

    public void setStartingFromPrice(int startingFromPrice) {
        this.startingFromPrice = startingFromPrice;
    }

    public Price getMinDisplayPrice() {
        return minDisplayPrice;
    }

    public void setMinDisplayPrice(Price minDisplayPrice) {
        this.minDisplayPrice = minDisplayPrice;
    }

    public Price getMinUnpromotedDisplayPrice() {
        return minUnpromotedDisplayPrice;
    }

    public void setMinUnpromotedDisplayPrice(Price minUnpromotedDisplayPrice) {
        this.minUnpromotedDisplayPrice = minUnpromotedDisplayPrice;
    }

    public Price getMaxDisplayPrice() {
        return maxDisplayPrice;
    }

    public void setMaxDisplayPrice(Price maxDisplayPrice) {
        this.maxDisplayPrice = maxDisplayPrice;
    }

    public Price getMaxUnpromotedDisplayPrice() {
        return maxUnpromotedDisplayPrice;
    }

    public void setMaxUnpromotedDisplayPrice(Price maxUnpromotedDisplayPrice) {
        this.maxUnpromotedDisplayPrice = maxUnpromotedDisplayPrice;
    }

    public double getHighestPriceInCategory() {
        return highestPriceInCategory;
    }

    public void setHighestPriceInCategory(double highestPriceInCategory) {
        this.highestPriceInCategory = highestPriceInCategory;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }
}
