package com.b2s.rewards.apple.model;

import javax.persistence.*;

/**
 * @author rjesuraj Date : 2/27/2017 Time : 3:04 PM
 */
@Entity
@Table (name = "var_program_domain_user_restriction")
public class VarProgramDomainUserRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column (name = "user_id")
    private String userId;

    @Column(name = "is_Active")
    private String isActive;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "login_type")
    private String loginType;

    @Column(name = "auth_type")
    private String authType;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(final String isActive) {
        this.isActive = isActive;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(final String loginType) {
        this.loginType = loginType;
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

    public String getVarId() {
        return varId;
    }

    public void setVarId(final String varId) {
        this.varId = varId;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(final String authType) {
        this.authType = authType;
    }
}
