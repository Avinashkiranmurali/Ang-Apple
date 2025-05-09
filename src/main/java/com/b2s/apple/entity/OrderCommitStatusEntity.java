package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "order_commit_status")
public class OrderCommitStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_commit_status_id")
    private Long orderCommitStatusId;

    @Column(name = "var_id")
    private String varId;
    @Column(name = "program_id")
    private String programId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "order_hash_code")
    private int orderHashCode;
    @Column(name = "order_description")
    private String orderDescription;
    @Column(name = "insert_time")
    private Date insertTime;
    @Column(name = "attr1")
    private String attr1;
    @Column(name = "attr2")
    private String attr2;

    public Long getOrderCommitStatusId() {
        return orderCommitStatusId;
    }

    public void setOrderCommitStatusId(final Long orderCommitStatusId) {
        this.orderCommitStatusId = orderCommitStatusId;
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

    public int getOrderHashCode() {
        return orderHashCode;
    }

    public void setOrderHashCode(final int orderHashCode) {
        this.orderHashCode = orderHashCode;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(final Date insertTime) {
        this.insertTime = insertTime;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(final String orderDescription) {
        this.orderDescription = orderDescription;
    }

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(final String attr1) {
        this.attr1 = attr1;
    }

    public String getAttr2() {
        return attr2;
    }

    public void setAttr2(final String attr2) {
        this.attr2 = attr2;
    }
}
