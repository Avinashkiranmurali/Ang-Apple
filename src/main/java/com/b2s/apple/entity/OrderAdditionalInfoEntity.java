package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "orders_additional_info")
public class OrderAdditionalInfoEntity {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "client_bill_pay")
    private String clientBillPay;
    @Column(name = "attr1")
    private String attr1;
    @Column(name = "attr2")
    private String attr2;
    @Column(name = "attr3")
    private Date attr3;

    public String getClientBillPay() {
        return clientBillPay;
    }

    public void setClientBillPay(final String clientBillPay) {
        this.clientBillPay = clientBillPay;
    }

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(final String attr1) {
        this.attr1 = attr1;
    }

    public String getAttr2() {
        return attr2;
    }

    public void setAttr2(final String attr2) {
        this.attr2 = attr2;
    }

    public Date getAttr3() {
        return attr3;
    }

    public void setAttr3(final Date attr3) {
        this.attr3 = attr3;
    }


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }
}