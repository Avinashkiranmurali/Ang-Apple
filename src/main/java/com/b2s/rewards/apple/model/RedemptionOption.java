package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name="var_program_redemption_option")
public class RedemptionOption  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "payment_option")
    private String paymentOption;

    @Column(name = "limit_type")
    private String limitType;

    @Column(name = "payment_min_limit")
    private Integer paymentMinLimit;

    @Column(name = "payment_max_limit")
    private Integer paymentMaxLimit;

    @Column(name = "order_by")
    private Integer orderBy;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @Column(name = "last_updated_date")
    private Timestamp lastUpdatedDate;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
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

    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(final String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(final String limitType) {
        this.limitType = limitType;
    }

    public Integer getPaymentMinLimit() {
        return paymentMinLimit;
    }

    public void setPaymentMinLimit(final Integer paymentMinLimit) {
        this.paymentMinLimit = paymentMinLimit;
    }

    public Integer getPaymentMaxLimit() {
        return paymentMaxLimit;
    }

    public void setPaymentMaxLimit(final Integer paymentMaxLimit) {
        this.paymentMaxLimit = paymentMaxLimit;
    }

    public Integer getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(final Integer orderBy) {
        this.orderBy = orderBy;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(final Boolean active) {
        isActive = active;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(final String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(final String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Timestamp getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(final Timestamp lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
