package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class CustomStoreInformationDTO {

    private final Optional<StandardStoreDTO> standard;
    private final Optional<DynamicStoreDTO> dynamic;
    private final Optional<PointsBasedStoreDTO> pointsBased;

    private CustomStoreInformationDTO(final Builder builder) {

        final long candidateCount = Arrays.asList(builder.standard, builder.dynamic, builder.pointsBased).stream()
            .filter(Optional::isPresent)
            .count();
        if (candidateCount != 1) {
            throw new IllegalStateException("Exactly one custom store type must be specified");
        }

        this.standard = builder.standard;
        this.dynamic = builder.dynamic;
        this.pointsBased = builder.pointsBased;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CustomStoreInformationDTO create(
        @JsonProperty("standard") final StandardStoreDTO standard,
        @JsonProperty("dynamic") final DynamicStoreDTO dynamic,
        @JsonProperty("pointsBased") final PointsBasedStoreDTO pointsBased) {
        return builder()
            .withStandard(standard)
            .withDynamic(dynamic)
            .withPointsBased(pointsBased)
            .build();
    }

    public Optional<StandardStoreDTO> getStandard() {
        return this.standard;
    }

    public Optional<DynamicStoreDTO> getDynamic() {
        return this.dynamic;
    }

    public Optional<PointsBasedStoreDTO> getPointsBased() {
        return this.pointsBased;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final CustomStoreInformationDTO other = (CustomStoreInformationDTO) o;
        return Objects.equals(this.standard, other.standard)
            && Objects.equals(this.dynamic, other.dynamic)
            && Objects.equals(this.pointsBased, other.pointsBased);
    }

    @Override
    public int hashCode() {
        return Objects.hash(standard, dynamic, pointsBased);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withStandard(standard.orElse(null))
            .withDynamic(dynamic.orElse(null))
            .withPointsBased(pointsBased.orElse(null));
    }

    public static final class Builder {
        private Optional<StandardStoreDTO> standard = Optional.empty();
        private Optional<DynamicStoreDTO> dynamic = Optional.empty();
        private Optional<PointsBasedStoreDTO> pointsBased = Optional.empty();

        private Builder() {
        }

        public Builder withStandard(final StandardStoreDTO theStandard) {
            this.standard = Optionals.from(theStandard);
            return this;
        }

        public Builder withDynamic(final DynamicStoreDTO theDynamic) {
            this.dynamic = Optionals.from(theDynamic);
            return this;
        }

        public Builder withPointsBased(final PointsBasedStoreDTO thePointsBased) {
            this.pointsBased = Optionals.from(thePointsBased);
            return this;
        }

        public CustomStoreInformationDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CustomStoreInformationDTO(this));
        }
    }
}
