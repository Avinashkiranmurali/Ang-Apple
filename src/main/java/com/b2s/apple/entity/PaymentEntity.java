package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "payment")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "var_id")
    private String varId;
    @Column(name = "program_id")
    private String programId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "name")
    private String name;
    @Column(name = "cc_num")
    private String ccNum;
    @Column(name = "address1")
    private String address1;
    @Column(name = "address2")
    private String address2;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Column(name = "zip")
    private String zip;
    @Column(name = "country")
    private String country;
    @Column(name = "phone")
    private String phone;
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "transaction_time")
    private Date transactionTime;
    @Column(name = "transaction_type")
    private String transactionType;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "points")
    private Integer points;
    @Column(name = "response_message")
    private String responseMessage;
    @Column(name = "response_code")
    private String responseCode;
    @Column(name = "item_amount")
    private Double itemAmount;
    @Column(name = "var_margin")
    private Integer varMargin;
    @Column(name = "var_profit")
    private Double varProfit;
    @Column(name = "b2s_margin")
    private Integer b2sMargin;
    @Column(name = "b2s_profit")
    private Double b2sProfit;
    @Column(name = "var_payment_ref_id")
    private String varPaymentRefId;
    @Column(name = "var_payment_time")
    private Date varPaymentTime;
    @Column(name = "lastuser")
    private String lastuser;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(final Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(final String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(final String programId) {
        this.programId = programId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCcNum() {
        return ccNum;
    }

    public void setCcNum(final String ccNum) {
        this.ccNum = ccNum;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(final String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(final String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(final String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(final Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(final String transactionType) {
        this.transactionType = transactionType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(final Double amount) {
        this.amount = amount;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(final Integer points) {
        this.points = points;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(final String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(final String responseCode) {
        this.responseCode = responseCode;
    }

    public Double getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(final Double itemAmount) {
        this.itemAmount = itemAmount;
    }

    public Integer getVarMargin() {
        return varMargin;
    }

    public void setVarMargin(final Integer varMargin) {
        this.varMargin = varMargin;
    }

    public Double getVarProfit() {
        return varProfit;
    }

    public void setVarProfit(final Double varProfit) {
        this.varProfit = varProfit;
    }

    public Integer getB2sMargin() {
        return b2sMargin;
    }

    public void setB2sMargin(final Integer b2sMargin) {
        this.b2sMargin = b2sMargin;
    }

    public Double getB2sProfit() {
        return b2sProfit;
    }

    public void setB2sProfit(final Double b2sProfit) {
        this.b2sProfit = b2sProfit;
    }

    public String getVarPaymentRefId() {
        return varPaymentRefId;
    }

    public void setVarPaymentRefId(final String varPaymentRefId) {
        this.varPaymentRefId = varPaymentRefId;
    }

    public Date getVarPaymentTime() {
        return varPaymentTime;
    }

    public void setVarPaymentTime(final Date varPaymentTime) {
        this.varPaymentTime = varPaymentTime;
    }

    public String getLastuser() {
        return lastuser;
    }

    public void setLastuser(final String lastuser) {
        this.lastuser = lastuser;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
}