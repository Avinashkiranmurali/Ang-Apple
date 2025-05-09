package com.b2s.rewards.apple.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ssundaramoorthy on 7/8/2021.
 */

public class AMPConfig implements Serializable {
    private static final long serialVersionUID = 5419494999000721623L;
    private String itemId;
    private String category;
    private String updatedBy;
    private Date updateDate;
    private boolean useStaticLink;
    private Integer duration;

    private AMPConfig(final Builder builder) {
        setItemId(builder.itemId);
        setCategory(builder.category);
        setUpdatedBy(builder.updatedBy);
        setUpdateDate(builder.updateDate);
        setUseStaticLink(builder.useStaticLink);
        setDuration(builder.duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AMPConfig that = (AMPConfig) o;

        return new EqualsBuilder()
            .append(getCategory(), that.getCategory())
            .append(getItemId(), that.getItemId())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getCategory())
            .toHashCode();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(final Date updateDate) {
        this.updateDate = updateDate;
    }

    public boolean getUseStaticLink() {
        return useStaticLink;
    }

    public void setUseStaticLink(final boolean useStaticLink) {
        this.useStaticLink = useStaticLink;
    }

    public Integer getDuration() { return duration; }

    public void setDuration(final Integer duration) { this.duration = duration; }

    public static final class Builder {
        private String itemId;
        private String category;
        private String updatedBy;
        private Date updateDate;
        private boolean useStaticLink;
        private Integer duration;

        public Builder withItemId(final String val) {
            itemId = val;
            return this;
        }

        public Builder withCategory(final String val) {
            category = val;
            return this;
        }

        public Builder withUpdatedBy(final String val) {
            updatedBy = val;
            return this;
        }

        public Builder withUpdateDate(final Date val) {
            updateDate = val;
            return this;
        }

        public Builder withUseStaticLink(final boolean val) {
            useStaticLink = val;
            return this;
        }

        public Builder withDuration(final Integer val) {
            duration = val;
            return this;
        }

        public AMPConfig build() {
            return new AMPConfig(this);
        }
    }
}
