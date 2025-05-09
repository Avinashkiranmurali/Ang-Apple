package com.b2s.rewards.apple.model;


import javax.persistence.*;
import java.util.Optional;

/*** Created by srukmagathan on 8/25/2016.
 */

@Entity
@Table(name = "domain_var_mapping")
public class DomainVarMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "domain",unique = true)
    private String domain;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "created_date")
    private String createdDate;

    @Column(name = "is_Active")
    private String isActive;

    @Column(name = "login_type")
    private String loginType;

    @Transient
    private String userId;

    @Transient
    private String email;

    @Transient
    private boolean displayTermsOfUse;

    @Transient
    private Optional<AWPEmployeeGroupsResponse> employeeGroupDetails;

    public Optional<AWPEmployeeGroupsResponse> getEmployeeGroupDetails() {
        return employeeGroupDetails;
    }

    public void setEmployeeGroupDetails(final Optional<AWPEmployeeGroupsResponse> employeeGroupDetails) {
        this.employeeGroupDetails = employeeGroupDetails;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final String createdDate) {
        this.createdDate = createdDate;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

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

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(final String programId) {
        this.programId = programId;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(final String varId) {
        this.varId = varId;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(final String loginType) {
        this.loginType = loginType;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public boolean isDisplayTermsOfUse() {
        return displayTermsOfUse;
    }

    public void setDisplayTermsOfUse(final boolean displayTermsOfUse) {
        this.displayTermsOfUse = displayTermsOfUse;
    }
}
