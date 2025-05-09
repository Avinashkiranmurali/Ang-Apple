package com.b2s.rewards.apple.model;

import java.io.Serializable;
import java.util.Date;

public class RecentlyViewedProduct implements Serializable {
    private final String varId;
    private final String programId;
    private final String userId;
    private final String productId;
    private final Date viewedDateTime;

    private RecentlyViewedProduct(Builder builder) {
        this.varId = builder.varId;
        this.programId = builder.programId;
        this.userId = builder.userId;
        this.viewedDateTime = builder.viewedDateTime;
        this.productId = builder.productId;
    }

    public String getVarId() {
        return varId;
    }

    public String getProgramId() {
        return programId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public Date getViewedDateTime() {
        return viewedDateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String varId;
        private String programId;
        private String userId;
        private String productId;
        private Date viewedDateTime;

        public Builder withVarId(final String varId) {
            this.varId = varId;
            return this;
        }

        public Builder withProgramId(final String programId) {
            this.programId = programId;
            return this;
        }

        public Builder withUserId(final String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withProductId(final String productId) {
            this.productId = productId;
            return this;
        }

        public Builder withViewedDateTime(final Date viewedDateTime) {
            this.viewedDateTime = viewedDateTime;
            return this;
        }

        public RecentlyViewedProduct build() {
            return new RecentlyViewedProduct(this);
        }
    }

    @Override
    public String toString() {
        return "RecentlyViewedProduct{" +
            "varId='" + varId + '\'' +
            ", programId='" + programId + '\'' +
            ", userId='" + userId + '\'' +
            ", productId='" + productId + '\'' +
            ", viewedDateTime=" + viewedDateTime +
            '}';
    }
}