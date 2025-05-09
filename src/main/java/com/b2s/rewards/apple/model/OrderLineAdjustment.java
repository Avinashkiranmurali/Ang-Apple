package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by vmurugesan on 29-11-2016.
 */
@Entity
@Table(name="order_line_adjustment")
public class OrderLineAdjustment implements Serializable {

    private static final long serialVersionUID = -4673126376064913456L;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "line_num")
    private Integer lineNum;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "adjustment_type")
    private String adjustmentType;

    @Column(name = "point_amount")
    private Integer pointAmount;

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

    @Column(name = "b2s_price_amount")
    private Double b2sPriceAmount;

    @Column(name = "var_confirmation_id")
    private String varConfirmationId;

    @Column(name = "b2s_shipping_amount")
    private Double b2sShippingAmount;

    @Column(name = "supplier_item_amount")
    private Double supplierItemAmount;

    @Column(name = "booking_quantity")
    private Integer bookingQuantity;

    @Column(name = "cc_points_amount")
    private Integer ccPointAmount;

    @Column(name = "cc_fees_difference")
    private Double ccFeesDifference;

    @Column(name = "var_markup_amount")
    private Double varMarkupAmount;

    @Column(name = "var_total_amount_due")
    private Double varTotalAmoutDue;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

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

    public Integer getPointAmount() {
        return pointAmount;
    }

    public void setPointAmount(final Integer pointAmount) {
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

    public Double getB2sPriceAmount() {
        return b2sPriceAmount;
    }

    public void setB2sPriceAmount(final Double b2sPriceAmount) {
        this.b2sPriceAmount = b2sPriceAmount;
    }

    public String getVarConfirmationId() {
        return varConfirmationId;
    }

    public void setVarConfirmationId(final String varConfirmationId) {
        this.varConfirmationId = varConfirmationId;
    }

    public Double getB2sShippingAmount() {
        return b2sShippingAmount;
    }

    public void setB2sShippingAmount(final Double b2sShippingAmount) {
        this.b2sShippingAmount = b2sShippingAmount;
    }

    public Double getSupplierItemAmount() {
        return supplierItemAmount;
    }

    public void setSupplierItemAmount(final Double supplierItemAmount) {
        this.supplierItemAmount = supplierItemAmount;
    }

    public Integer getBookingQuantity() {
        return bookingQuantity;
    }

    public void setBookingQuantity(final Integer bookingQuantity) {
        this.bookingQuantity = bookingQuantity;
    }

    public Integer getCcPointAmount() {
        return ccPointAmount;
    }

    public void setCcPointAmount(final Integer ccPointAmount) {
        this.ccPointAmount = ccPointAmount;
    }

    public Double getCcFeesDifference() {
        return ccFeesDifference;
    }

    public void setCcFeesDifference(final Double ccFeesDifference) {
        this.ccFeesDifference = ccFeesDifference;
    }

    public Double getVarMarkupAmount() {
        return varMarkupAmount;
    }

    public void setVarMarkupAmount(final Double varMarkupAmount) {
        this.varMarkupAmount = varMarkupAmount;
    }

    public Double getVarTotalAmoutDue() {
        return varTotalAmoutDue;
    }

    public void setVarTotalAmoutDue(final Double varTotalAmoutDue) {
        this.varTotalAmoutDue = varTotalAmoutDue;
    }


}
