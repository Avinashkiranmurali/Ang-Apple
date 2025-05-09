package com.b2s.db.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * This is the model object for the ORDER_LINE_FEE table in the core database.  The ORDER_LINE_FEE table
 * contains the itemized fees for each order line item.
 *
 * User: cborn
 * Date: 9/16/13
 * Time: 10:20 AM
 */
public class OrderLineFee {
    private Long orderID;
    private Integer orderLine;
    private String name;
    private BigDecimal amount;
    private Integer points;
    private Date createTime = new Date();

    public Long getOrderID() {
        return orderID;
    }

    public Integer getOrderLine() {
        return orderLine;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getAmountAsCents() {
        if(amount != null) {
            return amount.multiply(new BigDecimal(100)).intValue();
        } else {
            return 0;
        }
    }

    public Integer getPoints() {
        return points;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setOrderID(Long orderID) {
        this.orderID = orderID;
    }

    public void setOrderLine(Integer orderLine) {
        this.orderLine = orderLine;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderLineFee that = (OrderLineFee) o;
        return new EqualsBuilder()
                .append(orderID, that.orderID)
                .append(orderLine, that.orderLine)
                .append(name, that.name)
                .append(amount, that.amount)
                .append(points, that.points)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13,31)
                .append(orderID)
                .append(orderLine)
                .append(name)
                .append(amount)
                .append(points)
                .toHashCode();
    }
}