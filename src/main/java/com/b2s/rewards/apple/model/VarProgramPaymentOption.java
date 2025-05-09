package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by rpillai on 6/23/2016.
 */
@Entity
@Table(name="var_program_payment_option")
public class VarProgramPaymentOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "payment_option")
    private String paymentOption;

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(name = "payment_template")
    private String paymentTemplate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "order_by")
    private Integer orderBy;

    @Column(name = "payment_min_limit")
    private Integer paymentMinLimit;

    @Column(name = "payment_max_limit")
    private Integer paymentMaxLimit;

    @Column(name = "supplementary_payment_type")
    private String supplementaryPaymentType;

    @Column(name = "supplementary_payment_limit_type")
    private String supplementaryPaymentLimitType;

    @Column(name = "supplementary_payment_min_limit")
    private Integer supplementaryPaymentMinLimit;

    @Column(name = "supplementary_payment_max_limit")
    private Integer supplementaryPaymentMaxLimit;

    @Column(name = "lastupdate_user")
    private String lastupdateUser;

    @Column(name = "lastupdate_time")
    private Timestamp lastupdateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(final String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(final String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public String getPaymentTemplate() {
        return paymentTemplate;
    }

    public void setPaymentTemplate(final String paymentTemplate) {
        this.paymentTemplate = paymentTemplate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(final Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(final Integer orderBy) {
        this.orderBy = orderBy;
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

    public String getSupplementaryPaymentType() {
        return supplementaryPaymentType;
    }

    public void setSupplementaryPaymentType(final String supplementaryPaymentType) {
        this.supplementaryPaymentType = supplementaryPaymentType;
    }

    public String getSupplementaryPaymentLimitType() {
        return supplementaryPaymentLimitType;
    }

    public void setSupplementaryPaymentLimitType(final String supplementaryPaymentLimitType) {
        this.supplementaryPaymentLimitType = supplementaryPaymentLimitType;
    }

    public Integer getSupplementaryPaymentMinLimit() {
        return supplementaryPaymentMinLimit;
    }

    public void setSupplementaryPaymentMinLimit(final Integer supplementaryPaymentMinLimit) {
        this.supplementaryPaymentMinLimit = supplementaryPaymentMinLimit;
    }

    public Integer getSupplementaryPaymentMaxLimit() {
        return supplementaryPaymentMaxLimit;
    }

    public void setSupplementaryPaymentMaxLimit(final Integer supplementaryPaymentMaxLimit) {
        this.supplementaryPaymentMaxLimit = supplementaryPaymentMaxLimit;
    }

    public String getLastupdateUser() {
        return lastupdateUser;
    }

    public void setLastupdateUser(final String lastupdateUser) {
        this.lastupdateUser = lastupdateUser;
    }

    public Timestamp getLastupdateTime() {
        return lastupdateTime;
    }

    public void setLastupdateTime(final Timestamp lastupdateTime) {
        this.lastupdateTime = lastupdateTime;
    }
}
