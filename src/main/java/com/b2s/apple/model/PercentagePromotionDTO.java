package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class PercentagePromotionDTO {
    private final BigDecimal percentage;

    private PercentagePromotionDTO(final Builder builder) {
        this.percentage = Optionals.checkPresent(builder.percentage, "percentage");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static PercentagePromotionDTO create(@JsonProperty("percentage") final BigDecimal thePercentage) {
        return builder()
            .withPercentage(thePercentage)
            .build();
    }

    public BigDecimal getPercentage() {
        return this.percentage;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final PercentagePromotionDTO other = (PercentagePromotionDTO) o;
        return this.percentage != null && this.percentage.compareTo(other.percentage) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(percentage);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withPercentage(percentage);
    }

    public static final class Builder {
        private Optional<BigDecimal> percentage = Optional.empty();

        private Builder() {
        }

        public Builder withPercentage(final BigDecimal thePercentage) {
            this.percentage = Optionals.from(thePercentage);
            return this;
        }

        public PercentagePromotionDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new PercentagePromotionDTO(this));
        }
    }
}
