package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by sjayaraman on 5/3/2019.
 */
@JsonDeserialize(builder = PayrollAgreementDetails.Builder.class)
public class PayrollAgreementDetails {

    private final String employerName;
    private final String employeeName;
    private final String employeeEmail;
    private final String agreementText;
    private final Long templateId;
    private final Long templateVersion;
    private final PaymentInformation paymentInformation;

    public PayrollAgreementDetails(Builder builder) {
        this.employerName = builder.employerName;
        this.employeeName = builder.employeeName;
        this.employeeEmail = builder.employeeEmail;
        this.agreementText = builder.agreementText;
        this.templateId = builder.templateId;
        this.templateVersion = builder.templateVersion;
        this.paymentInformation = builder.paymentInformation;
    }

    public String getEmployerName() {
        return employerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public String getAgreementText() {
        return agreementText;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public PaymentInformation getPaymentInformation() {
        return paymentInformation;
    }

    public Long getTemplateVersion() {
        return templateVersion;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private  String employerName;
        private  String employeeName;
        private  String employeeEmail;
        private  String agreementText;
        private  Long templateId;
        private Long templateVersion;
        private  PaymentInformation paymentInformation;

        public Builder withEmployerName(final String employerName){
            this.employerName = employerName;
            return this;
        }
        public Builder withEmployeeName(final String employeeName){
            this.employeeName = employeeName;
            return this;
        }
        public Builder withEmployeeEmail(final String employeeEmail){
            this.employeeEmail = employeeEmail;
            return this;
        }
        public  Builder withAgreementText(final String agreementText){
            this.agreementText = agreementText;
            return this;
        }
        public Builder withTemplateId(final Long templateId){
            this.templateId = templateId;
            return this;
        }
        public Builder withPaymentInformation(final PaymentInformation paymentInformation){
            this.paymentInformation = paymentInformation;
            return this;
        }

        public Builder withTemplateVersion(final Long templateVersion){
            this.templateVersion = templateVersion;
            return this;
        }

        public PayrollAgreementDetails build(){
            return new PayrollAgreementDetails(this);
        }
    }
}
