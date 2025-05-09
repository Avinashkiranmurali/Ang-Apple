package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by skathirvel on 06-05-2019.
 */
@JsonDeserialize(builder = EmployeeRequestConfirmationResponse.Builder.class)
public class EmployeeRequestConfirmationResponse {


    private final Long requestId;
    private final String payrollAgreementUrl;
    private final Long agreementId;

    private EmployeeRequestConfirmationResponse(final Builder builder) {
        this.requestId = builder.requestId;
        this.payrollAgreementUrl = builder.payrollAgreementUrl;
        this.agreementId = builder.agreementId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public String getPayrollAgreementUrl() {
        return payrollAgreementUrl;
    }

    public Long getAgreementId() {
        return agreementId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long requestId;
        private String payrollAgreementUrl;
        private Long agreementId;

        private Builder() {
        }

        public Builder withRequestId(final Long requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder withPayrollAgreementUrl(final String payrollAgreementUrl) {
            this.payrollAgreementUrl = payrollAgreementUrl;
            return this;
        }

        public Builder withAgreementId(final Long agreementId) {
            this.agreementId = agreementId;
            return this;
        }

        public EmployeeRequestConfirmationResponse build() {
            return new EmployeeRequestConfirmationResponse(this);
        }
    }

}
