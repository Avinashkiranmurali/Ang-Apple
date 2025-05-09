package com.b2s.apple.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author rjesuraj Date : 8/20/2019 Time : 3:28 PM
 */
@Entity
@Table(name = "message_exceptions")
public class MessageExceptionsEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long messageExceptionsId;

    @Column(name = "varid")
    private String varId;
    @Column(name = "programid")
    private String programid;
    @Column(name = "classname")
    private String classname;
    @Column(name = "descn")
    private String descn;
    @Column(name = "detail")
    private String detail;
    @Column(name = "create_datetime")
    private Timestamp createDatetime;

    public String getVarId() {
        return varId;
    }

    public void setVarId(final String varId) {
        this.varId = varId;
    }

    public String getProgramid() {
        return programid;
    }

    public void setProgramid(final String programid) {
        this.programid = programid;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(final String classname) {
        this.classname = classname;
    }

    public String getDescn() {
        return descn;
    }

    public void setDescn(final String descn) {
        this.descn = descn;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(final String detail) {
        this.detail = detail;
    }

    public Timestamp getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(final Timestamp createDatetime) {
        this.createDatetime = createDatetime;
    }

    public Long getMessageExceptionsId() {
        return messageExceptionsId;
    }

    public void setMessageExceptionsId(final Long messageExceptionsId) {
        this.messageExceptionsId = messageExceptionsId;
    }
}
