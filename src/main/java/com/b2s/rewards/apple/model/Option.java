package com.b2s.rewards.apple.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Optional;

/**
 * Options with  name value pairs for any product or any category.
 */
public class Option implements Comparable<Option>, Serializable {

    private static final long serialVersionUID = 9115555840613222726L;
    private String name;
    private String value;
    private String key;
    private String i18Name;
    private int orderBy;  // TODO: need to decide on the priorities of options
    private Integer points;
    private String swatchImageUrl;

    public Option(final String name, final String value, final Optional<String> key) {
        this.name = name;
        this.value = value;
        if(key.isPresent()) {
            this.key = key.get();
        } else {
            this.key = value;
        }
    }

    public Option(final String name, final String value, final Optional<String> key, final Optional<String> i18Name) {
        this(name,value,key);

        if(i18Name.isPresent()) {
            this.i18Name = i18Name.get();
        } else {
            this.i18Name = name;
        }
    }

    public Option(){
    }

    public Option(final String key) {
        this.key = key;
        this.value = key;
    }

    // TO DO: for remaining attributes
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getI18Name() {
        return i18Name;
    }

    public void setI18Name(String i18Name) {
        this.i18Name = i18Name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(int orderBy) {
        this.orderBy = orderBy;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(final Integer points) {
        this.points = points;
    }

    public String getSwatchImageUrl() {
        return swatchImageUrl;
    }

    public void setSwatchImageUrl(final String swatchImageUrl) {
        this.swatchImageUrl = swatchImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        return new EqualsBuilder()
                .append(getKey(), option.getKey())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getKey())
                .toHashCode();
    }

    @Override
    public int compareTo(Option option) {
        return this.value.compareToIgnoreCase(option.getValue());
    }
}
