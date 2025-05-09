package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

@Table(name = "order_line_adjustment")
@Entity
public class OrderLineAdjustmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column( name = "order_id")
    private Long orderId;

    @Column( name = "line_num")
    private Integer lineNum;


    @JoinColumns({
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
        @JoinColumn(name = "line_num", referencedColumnName = "line_num", insertable = false, updatable = false)
    })
    @ManyToOne
    private OrderLineEntity orderLine;


    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "adjustment_type")
    private String adjustmentType;

    @Column(name = "point_amount")
    private Long pointAmount;

    @Column(name = "price_amount")
    private Double priceAmount;

    @Column(name = "userid")
    private String userid;

    @Column(name = "comment")
    private String comment;

    @Column(name = "date_time")
    private Date dateTime;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "transaction_id")
    private String transactionId;


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

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(final Integer statusId) {
        this.statusId = statusId;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(final String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public Long getPointAmount() {
        return pointAmount;
    }

    public void setPointAmount(final Long pointAmount) {
        this.pointAmount = pointAmount;
    }

    public Double getPriceAmount() {
        return priceAmount;
    }

    public void setPriceAmount(final Double priceAmount) {
        this.priceAmount = priceAmount;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(final String userid) {
        this.userid = userid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(final Date dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public OrderLineEntity getOrderLine() {
        return orderLine;
    }

    public void setOrderLine(final OrderLineEntity orderLine) {
        this.orderLine = orderLine;
    }

}
