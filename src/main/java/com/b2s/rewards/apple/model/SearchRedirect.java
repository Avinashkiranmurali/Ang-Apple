package com.b2s.rewards.apple.model;

import com.b2s.service.utils.lang.Exceptions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SearchRedirect {
    private final String varId;
    private final String programId;
    private final String catalogId;
    private final String searchKeyword;
    private final String actionType;
    private final String value;
    private final boolean active;

    private SearchRedirect(final Builder builder) {
        this.varId = builder.varId;
        this.programId = builder.programId;
        this.catalogId = builder.catalogId;
        this.searchKeyword = builder.searchKeyword;
        this.actionType = builder.actionType;
        this.value = builder.value;
        this.active = builder.active;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getVarId() {
        return varId;
    }

    public String getProgramId() {
        return programId;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public String getActionType() {
        return actionType;
    }

    public String getValue() {
        return value;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("varId", varId).append("programId", programId)
            .append("catalogId", catalogId).append("searchKeyword", searchKeyword).append("actionType", actionType)
            .append("value", value).append("active", active).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(actionType).append(catalogId).append(active).append(searchKeyword)
            .append(value).append(varId).append(programId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SearchRedirect) == false) {
            return false;
        }
        SearchRedirect rhs = ((SearchRedirect) other);
        return new EqualsBuilder().append(actionType, rhs.actionType).append(catalogId, rhs.catalogId)
            .append(active, rhs.active).append(searchKeyword, rhs.searchKeyword).append(value, rhs.value)
            .append(varId, rhs.varId).append(programId, rhs.programId).isEquals();
    }

    public static final class Builder {
        private String varId;
        private String programId;
        private String catalogId;
        private String searchKeyword;
        private String actionType;
        private String value;
        private boolean active;

        public Builder withVarId(String varId) {
            this.varId = varId;
            return this;
        }

        public Builder withProgramId(String programId) {
            this.programId = programId;
            return this;
        }

        public Builder withCatalogId(String catalogId) {
            this.catalogId = catalogId;
            return this;
        }

        public Builder withSearchKeyword(String searchKeyword) {
            this.searchKeyword = searchKeyword;
            return this;
        }

        public Builder withActionType(String actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withActive(boolean active) {
            this.active = active;
            return this;
        }

        public SearchRedirect build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new SearchRedirect(this));
        }
    }
}