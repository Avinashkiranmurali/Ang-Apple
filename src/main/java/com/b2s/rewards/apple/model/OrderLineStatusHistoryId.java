package com.b2s.rewards.apple.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents the composite pseudo id of the order_line_status_history table.
 */
@Embeddable
public class OrderLineStatusHistoryId implements Serializable {

    private static final long serialVersionUID = 1253580230941172203L;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "line_num")
    private Integer lineNum;

    @ManyToOne
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    private OrderLineStatus status;

    @Column(name = "date_time", nullable = false)
    private Timestamp modifiedDate;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final OrderLineStatusHistoryId that = (OrderLineStatusHistoryId) o;

        if (!orderId.equals(that.orderId)) {
            return false;
        }
        if (!lineNum.equals(that.lineNum)) {
            return false;
        }
        if (!status.equals(that.status)) {
            return false;
        }
        return modifiedDate.equals(that.modifiedDate);
    }

    @Override
    public int hashCode() {
        int result = orderId.hashCode();
        result = 31 * result + lineNum.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + modifiedDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // The id is just the entity, so the class name (OrderLineStatusHistory) is that of the entity.
        return String.format("OrderLineStatusHistory {orderId=%d, lineNum=%d, status=%d|%s, modifiedDate=%s}",
            orderId,
            lineNum,
            status.getStatusId(),
            status.getStatusDesc(),
            modifiedDate);
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

    public OrderLineStatus getStatus() {
        return status;
    }

    public void setStatus(final OrderLineStatus status) {
        this.status = status;
    }

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(final Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

}
