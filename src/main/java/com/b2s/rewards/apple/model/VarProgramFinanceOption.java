package com.b2s.rewards.apple.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;


@Entity
@Table(name="var_program_finance_option")
public class VarProgramFinanceOption implements Serializable {

    private static final long serialVersionUID = 8358244580302511232L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "installment")
    private Integer installment;

    @Column(name = "installment_period")
    private String installmentPeriod;

    @Column(name = "message_code")
    private String messageCode;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "order_by")
    private Integer orderBy;

    @Column(name = "lastupdate_user")
    private String lastupdateUser;

    @Column(name = "lastupdate_time")
    private Timestamp lastupdateTime;

    @Column(name = "establishment_fee_type")
    private String establishmentFeeType;

    @Column(name = "establishment_fee_rate")
    private Float establishmentFeeRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinColumns({
            @JoinColumn(name = "var_id", referencedColumnName = "varid", insertable = false, updatable = false),
            @JoinColumn(name = "program_id", referencedColumnName = "programid", insertable = false, updatable = false)
    })
    private VarProgram varProgram;

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

    public Integer getInstallment() {
        return installment;
    }

    public void setInstallment(Integer installment) {
        this.installment = installment;
    }

    public String getInstallmentPeriod() {
        return installmentPeriod;
    }

    public void setInstallmentPeriod(String installmentPeriod) {
        this.installmentPeriod = installmentPeriod;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(Integer orderBy) {
        this.orderBy = orderBy;
    }

    public String getLastupdateUser() {
        return lastupdateUser;
    }

    public void setLastupdateUser(String lastupdateUser) {
        this.lastupdateUser = lastupdateUser;
    }

    public Timestamp getLastupdateTime() {
        return lastupdateTime;
    }

    public void setLastupdateTime(Timestamp lastupdateTime) {
        this.lastupdateTime = lastupdateTime;
    }

    public String getEstablishmentFeeType() {
        return establishmentFeeType;
    }

    public void setEstablishmentFeeType(String establishmentFeeType) {
        this.establishmentFeeType = establishmentFeeType;
    }

    public Float getEstablishmentFeeRate() {
        return establishmentFeeRate;
    }

    public void setEstablishmentFeeRate(Float establishmentFeeRate) {
        this.establishmentFeeRate = establishmentFeeRate;
    }

    public VarProgram getVarProgram() {
        return varProgram;
    }

    public void setVarProgram(VarProgram varProgram) {
        this.varProgram = varProgram;
    }
}
