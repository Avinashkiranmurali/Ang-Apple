package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.sql.Timestamp;

@JsonDeserialize(builder = VarProgramRedemptionOption.Builder.class)
public class VarProgramRedemptionOption implements Serializable {

    private final Integer id;
    private final String varId;
    private final String programId;
    private final String paymentOption;
    private final String limitType;
    private final Integer paymentMinLimit;
    private final Integer paymentMaxLimit;
    private final Integer orderBy;
    private final Boolean isActive;
    private final String paymentProvider;
    private final String lastUpdatedBy;
    private final Timestamp lastUpdatedDate;



    private VarProgramRedemptionOption(final Builder builder){
        this.id = builder.id;
        this.varId = builder.varId;
        this.programId = builder.programId;
        this.paymentOption = builder.paymentOption;
        this.limitType = builder.limitType;
        this.paymentMinLimit = builder.paymentMinLimit;
        this.paymentMaxLimit = builder.paymentMaxLimit;
        this.orderBy = builder.orderBy;
        this.isActive = builder.isActive;
        this.paymentProvider = builder.paymentProvider;
        this.lastUpdatedBy = builder.lastUpdatedBy;
        this.lastUpdatedDate = builder.lastUpdatedDate;
    }

    public Integer getId() {
        return id;
    }

    public String getVarId() {
        return varId;
    }

    public String getProgramId() {
        return programId;
    }

    public String getPaymentOption() {
        return paymentOption;
    }

    public String getLimitType() {
        return limitType;
    }

    public Integer getPaymentMinLimit() {
        return paymentMinLimit;
    }

    public Integer getPaymentMaxLimit() {
        return paymentMaxLimit;
    }

    public Integer getOrderBy() {
        return orderBy;
    }

    public Boolean getActive() {
        return isActive;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public Timestamp getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Integer id;
        private String varId;
        private String programId;
        private String paymentOption;
        private String limitType;
        private Integer paymentMinLimit;
        private Integer paymentMaxLimit;
        private Integer orderBy;
        private Boolean isActive;
        private String paymentProvider;
        private String lastUpdatedBy;
        private Timestamp lastUpdatedDate;

        public Builder withId(final Integer id) {
            this.id = id;
            return this;
        }

        public Builder withVarId(final String varId) {
            this.varId = varId;
            return this;
        }

        public Builder withProgramId(final String programId) {
            this.programId = programId;
            return this;
        }

        public Builder withPaymentOption(final String paymentOption) {
            this.paymentOption = paymentOption;
            return this;
        }

        public Builder withLimitType(final String limitType) {
            this.limitType = limitType;
            return this;
        }

        public Builder withPaymentMinLimit(final Integer paymentMinLimit) {
            this.paymentMinLimit = paymentMinLimit;
            return this;
        }

        public Builder withPaymentMaxLimit(final Integer paymentMaxLimit) {
            this.paymentMaxLimit = paymentMaxLimit;
            return this;
        }

        public Builder withOrderBy(final Integer orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public Builder withActive(final Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder withPaymentProvider(final String paymentProvider) {
            this.paymentProvider = paymentProvider;
            return this;
        }

        public Builder withLastUpdatedBy(final String lastUpdatedBy) {
            this.lastUpdatedBy = lastUpdatedBy;
            return this;
        }

        public Builder withLastUpdatedDate(final Timestamp lastUpdatedDate) {
            this.lastUpdatedDate = lastUpdatedDate;
            return this;
        }

        public VarProgramRedemptionOption build(){
            return new VarProgramRedemptionOption(this);
        }
    }
}
