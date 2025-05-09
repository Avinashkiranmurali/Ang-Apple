package com.b2s.common.services.awp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author rjesuraj Date : 6/19/2018 Time : 12:06 PM
 */
@JsonDeserialize(builder = PayrollFacilitator.Builder.class)
public class PayrollFacilitator {
    private final String name;
    private final Set<String> countryCodes;

    private PayrollFacilitator(final Builder builder) {
        this.name = builder.name;
        this.countryCodes = builder.countryCodes;
    }

    public String getName() {
        return name;
    }

    public Set<String> getCountryCodes() {
        return Collections.unmodifiableSet(countryCodes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private final Set<String> countryCodes = new HashSet<>();

        private Builder() {
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withCountryCodes(final Set<String> countryCodes) {
            this.countryCodes.addAll(countryCodes);
            return this;
        }

        public PayrollFacilitator build() {
            return new PayrollFacilitator(this);
        }

    }
}
