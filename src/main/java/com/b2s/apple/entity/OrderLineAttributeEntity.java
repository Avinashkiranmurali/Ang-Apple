package com.b2s.apple.entity;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "order_line_attribute")
@Entity
public class OrderLineAttributeEntity implements Serializable {

    private static final long serialVersionUID = -7645187693838558183L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "line_num")
    private Integer lineNum;
    @Column(name = "name")
    private String name;
    @Column(name = "value")
    private String value;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
        @JoinColumn(name = "line_num", referencedColumnName = "line_num", insertable = false, updatable = false)
    })
    private OrderLineEntity orderLine;


    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public OrderLineEntity getOrderLine() {
        return orderLine;
    }

    public void setOrderLine(final OrderLineEntity orderLine) {
        this.orderLine = orderLine;
    }

}
