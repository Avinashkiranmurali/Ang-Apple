package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class FixedPromotionDTO {
    private final int points;

    private FixedPromotionDTO(final Builder builder) {
        this.points = Optionals.checkPresent(builder.points, "points");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static FixedPromotionDTO create(@JsonProperty("points") final Integer thePoints) {
        return builder()
            .withPoints(thePoints)
            .build();
    }

    public int getPoints() {
        return this.points;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final FixedPromotionDTO other = (FixedPromotionDTO) o;
        return Objects.equals(this.points, other.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withPoints(points);
    }

    public static final class Builder {
        private Optional<Integer> points = Optional.empty();

        private Builder() {
        }

        public Builder withPoints(final Integer thePoints) {
            this.points = Optionals.from(thePoints);
            return this;
        }

        public FixedPromotionDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new FixedPromotionDTO(this));
        }
    }
}
