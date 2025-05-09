package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;

/**
 * @author rjesuraj Date : 4/10/2018 Time : 12:15 PM
 */
@JsonDeserialize(builder = AWPProductGroup.Builder.class)
public class AWPProductGroup {

    private final long id;
    private final String name;
    private final String displayName;
    private final int maxQuantity;
    private final String imageUrl;
    private final String promoTag;
    private final int productCount;
    private final String employerContribution;
    private final Date eligibleDate;
    private final Boolean isEligible;
    private final Date lastRequestedDate;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPromoTag() {
        return promoTag;
    }

    public int getProductCount() {
        return productCount;
    }

    public String getEmployerContribution() {
        return employerContribution;
    }

    public Date getEligibleDate() { return eligibleDate; }

    public Boolean getIsEligible() { return isEligible; }

    public Date getLastRequestedDate() {
        return lastRequestedDate;
    }

    private AWPProductGroup(final Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.displayName = builder.displayName;
        this.maxQuantity = builder.maxQuantity;
        this.imageUrl = builder.imageUrl;
        this.promoTag = builder.promoTag;
        this.productCount=builder.productCount;
        this.employerContribution=builder.employerContribution;
        this.eligibleDate=builder.eligibleDate;
        this.isEligible=builder.isEligible;
        this.lastRequestedDate =builder.lastRequestedDate;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long id;
        private String name;
        private String displayName;
        private int maxQuantity;
        private String imageUrl;
        private String promoTag;
        private int productCount;
        private String employerContribution;
        private Date eligibleDate;
        private Boolean isEligible;
        private Date lastRequestedDate;


        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder withMaxQuantity(int maxQuantity) {
            this.maxQuantity = maxQuantity;
            return this;
        }

        public Builder withImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder withPromoTag(String promoTag) {
            this.promoTag = promoTag;
            return this;
        }

        public Builder withProductCount(int productCount) {
            this.productCount = productCount;
            return this;
        }

        public Builder withEmployerContribution(final String employerContribution){
            this.employerContribution=employerContribution;
            return this;
        }

        public Builder withEligibleDate(final Date eligibleDate) {
            this.eligibleDate = eligibleDate;
            return this;
        }

        public Builder withIsEligible(final Boolean isEligible) {
            this.isEligible = isEligible;
            return this;
        }

        public Builder withLastRequestedDate(final Date lastRequestedDate) {
            this.lastRequestedDate = lastRequestedDate;
            return this;
        }

        public AWPProductGroup build() { return new AWPProductGroup(this);}
    }
}
