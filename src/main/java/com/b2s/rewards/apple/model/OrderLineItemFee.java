package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Created by rperumal on 4/20/2016
 */

@Entity
@Table(name="order_line_fee")
public class OrderLineItemFee implements Serializable {

    private static final long serialVersionUID = -2099309860547372967L;

    @EmbeddedId
    private OrderLineItemPK id;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "points")
    private Integer points;

    @Column(name = "create_time")
    private Timestamp createTime;

    public OrderLineItemPK getId() {
        return id;
    }

    public void setId(final OrderLineItemPK id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(final Integer points) {
        this.points = points;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(final Timestamp createTime) {
        this.createTime = createTime;
    }
}