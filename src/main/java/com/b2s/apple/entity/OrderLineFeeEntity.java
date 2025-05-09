package com.b2s.apple.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Table(name = "ORDER_LINE_FEE")
@Entity
public class OrderLineFeeEntity implements Serializable {
    @EmbeddedId
    private OrderLineFeeId orderLineFeeId;

    @JoinColumns({
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
        @JoinColumn(name = "order_line", referencedColumnName = "line_num", insertable = false, updatable = false)
    })
    @ManyToOne
    private OrderLineEntity orderLine;

    @Column(name = "AMOUNT")
    private Long amount;
    @Column(name = "POINTS")
    private Long points;
    @Column(name = "CREATE_TIME")
    private Date createTime;

    public OrderLineEntity getOrderLine() {
        return orderLine;
    }

    public void setOrderLine(final OrderLineEntity orderLine) {
        this.orderLine = orderLine;
    }


    public Long getAmount() {
        return amount;
    }

    public void setAmount(final Long amount) {
        this.amount = amount;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(final Long points) {
        this.points = points;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(final Date createTime) {
        this.createTime = createTime;
    }

    public OrderLineFeeId getOrderLineFeeId() {
        return orderLineFeeId;
    }

    public void setOrderLineFeeId(final OrderLineFeeId orderLineFeeId) {
        this.orderLineFeeId = orderLineFeeId;
    }

    @Embeddable
    public static class OrderLineFeeId implements Serializable {

        @Column(name = "order_id")
        private Long orderId;

        @Column(name = "order_line")
        private Integer lineNum;

        @Column(name = "NAME")
        private String name;

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final OrderLineFeeId that = (OrderLineFeeId) o;
            return Objects.equals(orderId, that.orderId) &&
                Objects.equals(lineNum, that.lineNum) &&
                Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(orderId, lineNum, name);
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
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

}
