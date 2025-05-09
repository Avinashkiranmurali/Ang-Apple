package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by rperumal on 3/15/16
 */

@Entity
@Table(name="order_attribute")
public class OrderAttributeValue implements Serializable {

    private static final long serialVersionUID = -7345366550356800025L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id")
    private Long orderId;

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