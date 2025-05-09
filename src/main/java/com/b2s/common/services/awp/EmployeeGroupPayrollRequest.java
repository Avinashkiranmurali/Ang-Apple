package com.b2s.common.services.awp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author rjesuraj Date : 6/19/2018 Time : 2:44 PM
 */

@JsonDeserialize(builder = EmployeeGroupPayrollRequest.Builder.class)
public class EmployeeGroupPayrollRequest {

    private final String employerId;
    private final String payrollFacilitatorName;
    private final String employeeGroupName;
    private final String userId;
    private final String locale;


    private EmployeeGroupPayrollRequest(final Builder builder) {
        this.employeeGroupName = builder.employeeGroupName;
        this.employerId = builder.employerId;
        this.payrollFacilitatorName = builder.payrollFacilitatorName;
        this.userId = builder.userId;
        this.locale = builder.locale;
    }

    public String getEmployerId() {
        return employerId;
    }

    public String getEmployeeGroupName() {
        return employeeGroupName;
    }

    public String getPayrollFacilitatorName() {
        return payrollFacilitatorName;
    }

    public String getUserId() {
        return userId;
    }

    public String getLocale(){
        return locale;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String employerId;
        private String employeeGroupName;
        private String payrollFacilitatorName;
        private String userId;
        private String locale;


        private Builder() {
        }

        public Builder withEmployerId(final String employerId) {
            this.employerId = employerId;
            return this;
        }

        public Builder withEmployeeGroupName(final String employeeGroupName) {
            this.employeeGroupName = employeeGroupName;
            return this;
        }

        public Builder withPayrollFacilitatorName(final String payrollFacilitatorName) {
            this.payrollFacilitatorName = payrollFacilitatorName;
            return this;
        }

        public Builder withUserId(final String userId){
            this.userId = userId;
            return this;
        }

        public Builder withLocale(final String locale){
            this.locale = locale;
            return this;
        }


        public EmployeeGroupPayrollRequest build() {
            return new EmployeeGroupPayrollRequest(this);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("EmployeeGroupPayrollRequest{employerId='")
            .append(employerId)
            .append('\'')
            .append(", employeeGroupName='")
            .append(employeeGroupName)
            .append('\'')
            .append(", payrollFacilitatorName='")
            .append(payrollFacilitatorName)
            .append('\'')
            .append(", locale")
            .append(locale)
            .append('\'')
            .append('}').toString();
    }
}
