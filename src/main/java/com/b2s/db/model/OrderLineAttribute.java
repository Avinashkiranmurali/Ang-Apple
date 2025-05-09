package com.b2s.db.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This is the model object for the ORDER_LINE_ATTRIBUTE table in the database.  This has engrave & gift mesaage details for engraveable apple product
 *
 * User: rperumal
 * Date: 9/23/15
  */
public class OrderLineAttribute {
    private Integer id;
    private Long orderId;
    private Integer lineNum;
    private String name;
    private String value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderLineAttribute that = (OrderLineAttribute) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(orderId, that.orderId)
                .append(lineNum, that.lineNum)
                .append(name, that.name)
                .append(value, that.value)
                .isEquals();

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13,31)
                .append(id)
                .append(orderId)
                .append(lineNum)
                .append(name)
                .append(value)
                .toHashCode();
    }

}