package com.b2s.rewards.apple.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by vmurugesan on 08-12-2016.
 */
@Embeddable
public class OrderLineItemPK implements Serializable {

    @Column(name = "name")
    private String name;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "order_line")
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


    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
