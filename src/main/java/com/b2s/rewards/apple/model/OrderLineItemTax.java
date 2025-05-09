package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Created by rperumal on 4/20/2016
 */

@Entity
@Table(name="order_line_tax")
public class OrderLineItemTax implements Serializable {

    private static final long serialVersionUID = -3543806837742300805L;

    @EmbeddedId
    private OrderLineItemPK id;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "points")
    private Integer points;

    @Column(name = "create_time")
    private Timestamp createTime;


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}