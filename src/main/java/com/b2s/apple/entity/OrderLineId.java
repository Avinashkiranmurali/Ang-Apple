package com.b2s.apple.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderLineId implements Serializable {

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "line_num")
    private Integer lineNum;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OrderLineId that = (OrderLineId) o;
        return orderId.equals(that.orderId) &&
            lineNum.equals(that.lineNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, lineNum);
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public void setLineNum(final Integer lineNum) {
        this.lineNum = lineNum;
    }

}
