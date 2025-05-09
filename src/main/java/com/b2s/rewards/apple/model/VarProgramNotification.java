package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by rpillai on 8/24/2016.
 */
@Entity
@Table(name="var_program_notification")
public class VarProgramNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "type")
    private String type;

    @Column(name = "name")
    private String name;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "locale")
    private String locale;

    @Column(name = "subject")
    private String subject;

    @Column(name = "is_active")
    private Boolean isActive;

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

    public String getType() {
        return type;
    }

    public void setType(String emailType) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
}
