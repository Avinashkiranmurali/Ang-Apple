package com.b2s.rewards.apple.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Created by rpillai on 4/25/2018.
 */
public class ProgramConfig implements Serializable{

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    private ProgramConfig(final Builder builder) {
        Assert.hasText(builder.name, "Name is required");
        Assert.hasText(builder.value, "Value is required");
        this.name = builder.name;
        this.value = builder.value;
    }

    public static Builder builder() { return new Builder(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProgramConfig that = (ProgramConfig) o;

        return new EqualsBuilder()
                .append(getName(), that.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .toHashCode();
    }

    public static class Builder {
        private String name;
        private String value;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public ProgramConfig build() { return new ProgramConfig(this); }
    }
}
