package com.b2s.apple.entity;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author rkumar 2019-12-06
 */
@Entity
@Table(name = "pricing_log")
public class PricingLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    @Column(name = "var_id")
    private String varId;
    @Column(name = "program_id")
    private String programId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "request")
    private String cartRequest;
    @Column(name = "response")
    private String cartResponse;
    @Column(name = "created_date")
    private Date createdDate;

    public BigInteger getId() {
        return id;
    }

    public void setId(final BigInteger id) {
        this.id = id;
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

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public String getCartRequest() {
        return cartRequest;
    }

    public void setCartRequest(final String cartRequest) {
        this.cartRequest = cartRequest;
    }

    public String getCartResponse() {
        return cartResponse;
    }

    public void setCartResponse(final String cartResponse) {
        this.cartResponse = cartResponse;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }
}
