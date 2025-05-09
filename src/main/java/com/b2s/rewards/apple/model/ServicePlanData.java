package com.b2s.rewards.apple.model;


import java.io.Serializable;
import java.util.Date;

/**
 * Created by srajendran on 11/1/2022.
 */
public class ServicePlanData implements Serializable {


    private static final long serialVersionUID = 8449576615252816679L;

    private final String planId;
    private final String planUrl;
    private final String hardwareSerialNumber;
    private final String hardwareDescription;
    private final Date planEndDate;
    private final Date lastUpdateDate;

    private ServicePlanData(Builder builder) {
        this.planId = builder.planId;
        this.planUrl = builder.planUrl;
        this.hardwareSerialNumber = builder.hardwareSerialNumber;
        this.hardwareDescription = builder.hardwareDescription;
        this.planEndDate = builder.planEndDate;
        this.lastUpdateDate = builder.lastUpdateDate;
    }

    public String getPlanId() {
        return planId;
    }

    public String getPlanUrl() {
        return planUrl;
    }

    public String getHardwareSerialNumber() {
        return hardwareSerialNumber;
    }

    public String getHardwareDescription() {
        return hardwareDescription;
    }

    public Date getPlanEndDate() {
        return planEndDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String planId;
        private String planUrl;
        private String hardwareSerialNumber;
        private String hardwareDescription;
        private Date planEndDate;
        private Date lastUpdateDate;

        public Builder withPlanId(final String planId) {
            this.planId = planId;
            return this;
        }

        public Builder withPlanUrl(final String planUrl) {
            this.planUrl = planUrl;
            return this;
        }

        public Builder withHardwareSerialNumber(final String hardwareSerialNumber) {
            this.hardwareSerialNumber = hardwareSerialNumber;
            return this;
        }

        public Builder withHardwareDescription(final String hardwareDescription) {
            this.hardwareDescription = hardwareDescription;
            return this;
        }

        public Builder withPlanEndDate(final Date planEndDate) {
            this.planEndDate = planEndDate;
            return this;
        }

        public Builder withLastUpdateDate(final Date lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public ServicePlanData build() {
            return new ServicePlanData(this);
        }

    }

    @Override
    public String toString() {
        return "ServicePlanData{" +
            "planId='" + planId + '\'' +
            ", planUrl='" + planUrl + '\'' +
            ", hardwareSerialNumber='" + hardwareSerialNumber + '\'' +
            ", hardwareDescription='" + hardwareDescription + '\'' +
            ", planEndDate=" + planEndDate +
            ", lastUpdateDate=" + lastUpdateDate +
            '}';
    }

}
