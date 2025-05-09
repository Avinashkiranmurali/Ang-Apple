package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by rperumal on 7/24/2015.
 */

@Entity
@Table(name="order_line_attribute")
public class OrderLineItemAttribute implements Serializable {

    private static final long serialVersionUID = -7790887581382180264L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "line_num")
    private Integer lineNum;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
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
}