package com.b2s.rewards.apple.model;

import com.b2s.service.utils.lang.Exceptions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SearchRedirectModel {

    private final String alternateSearchText;
    private final String redirectURL;

    private SearchRedirectModel(final Builder builder) {
        this.alternateSearchText = builder.alternateSearchText;
        this.redirectURL = builder.redirectURL;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAlternateSearchText() {
        return alternateSearchText;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("alternateSearchText", alternateSearchText)
            .append("redirectURL", redirectURL)
            .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(alternateSearchText).append(redirectURL).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SearchRedirectModel) == false) {
            return false;
        }
        SearchRedirectModel rhs = ((SearchRedirectModel) other);
        return new EqualsBuilder().append(alternateSearchText, rhs.alternateSearchText)
            .append(redirectURL, rhs.redirectURL)
            .isEquals();
    }

    public static final class Builder {
        private String alternateSearchText;
        private String redirectURL;

        public Builder withAlternateSearchText(String alternateSearchText) {
            this.alternateSearchText = alternateSearchText;
            return this;
        }

        public Builder withRedirectURL(String redirectURL) {
            this.redirectURL = redirectURL;
            return this;
        }

        public SearchRedirectModel build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new SearchRedirectModel(this));
        }
    }

}
