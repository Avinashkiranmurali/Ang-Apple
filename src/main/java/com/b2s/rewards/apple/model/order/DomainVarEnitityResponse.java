package com.b2s.rewards.apple.model.order;

import com.b2s.rewards.apple.model.AWPEmployeeGroupsResponse;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Optional;

/**
 * Created by sjayaraman on 1/31/2019.
 */
@JsonDeserialize(builder = DomainVarEnitityResponse.Builder.class)
public class DomainVarEnitityResponse {

    private final int id;
    private final String domain;
    private final String varId;
    private final String programId;
    private final String createdDate;
    private final String isActive;
    private final String loginType;
    private final String userId;
    private final String email;
    private final boolean displayTermsOfUse;
    private final Optional<AWPEmployeeGroupsResponse> employeeGroupDetails;

    public int getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public String getVarId() {
        return varId;
    }

    public String getProgramId() {
        return programId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getIsActive() {
        return isActive;
    }

    public String getLoginType() {
        return loginType;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public boolean isDisplayTermsOfUse() {
        return displayTermsOfUse;
    }

    public Optional<AWPEmployeeGroupsResponse> getEmployeeGroupDetails() {
        return employeeGroupDetails;
    }
    public static Builder builder() {
        return new Builder();
    }

    private DomainVarEnitityResponse(Builder builder){
        this.id=builder.id;
        this.domain=builder.domain;
        this.varId=builder.varId;
        this.programId=builder.programId;
        this.createdDate=builder.createdDate;
        this.isActive=builder.isActive;
        this.loginType=builder.loginType;
        this.userId=builder.userId;
        this.email=builder.email;
        this.displayTermsOfUse=builder.displayTermsOfUse;
        this.employeeGroupDetails=builder.employeeGroupDetails;
    }

    @JsonPOJOBuilder(buildMethodName = "build")
    public static class Builder{
        private int id;
        private String domain;
        private String varId;
        private String programId;
        private String createdDate;
        private String isActive;
        private String loginType;
        private String userId;
        private String email;
        private boolean displayTermsOfUse;
        private Optional<AWPEmployeeGroupsResponse> employeeGroupDetails;

        public Builder withId(final int id){
            this.id=id;
            return this;
        }
        public Builder withDomain(final String domain){
            this.domain=domain;
            return this;
        }
        public Builder withVarId(final String varId){
            this.varId=varId;
            return this;
        }
        public Builder withProgramId(final String programId){
            this.programId=programId;
            return this;
        }
        public Builder withCreatedDate(final String createdDate){
            this.createdDate=createdDate;
            return this;
        }
        public Builder withIsActive(final String isActive){
            this.isActive=isActive;
            return this;
        }
        public Builder withLoginType(final String iloginType){
            this.loginType=loginType;
            return this;
        }
        public Builder withUserId(final String userId){
            this.userId=userId;
            return this;
        }
        public Builder withEmail(final String email){
            this.email=email;
            return this;
        }
        public Builder withDisplayTermsOfUse(final boolean displayTermsOfUse){
            this.displayTermsOfUse=displayTermsOfUse;
            return this;
        }
        public Builder withEmployeeGroupDetails(final Optional<AWPEmployeeGroupsResponse> employeeGroupDetails){
            this.employeeGroupDetails=employeeGroupDetails;
            return this;
        }
        public DomainVarEnitityResponse build() { return new DomainVarEnitityResponse(this); }
    }
}
