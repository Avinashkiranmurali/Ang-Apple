package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

@Table(name = "var_program_account_activity")
@Entity
public class VarProgramAccountActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "var_id")
    private String varId;
    @Column(name = "program_id")
    private String programId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "tran_type")
    private String tranType;
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "line_num")
    private Integer lineNum;
    @Column(name = "create_date")
    private Date createDate;
    @Column(name = "name")
    private String name;
    @Column(name = "comment")
    private String comment;
    @Column(name = "amount")
    private Integer amount;
    @Column(name = "admin_user")
    private String adminUser;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(final Long activityId) {
        this.activityId = activityId;
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

    public String getTranType() {
        return tranType;
    }

    public void setTranType(final String tranType) {
        this.tranType = tranType;
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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(final Date createDate) {
        this.createDate = createDate;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(final Integer amount) {
        this.amount = amount;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(final String adminUser) {
        this.adminUser = adminUser;
    }

}
