package com.b2s.rewards.apple.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by vmurugesan on 10/11/2016.
 */

@Embeddable
public class OrderLineItemId implements Serializable {

    @Column(name = "order_id")
     private Integer orderId;

    @Column(name = "line_num")
    private Integer lineNum;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(final Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public void setLineNum(final Integer lineNum) {
        this.lineNum = lineNum;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OrderLineItemId that = (OrderLineItemId) o;
        return Objects.equals(orderId, that.orderId) &&
            Objects.equals(lineNum, that.lineNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, lineNum);
    }
}