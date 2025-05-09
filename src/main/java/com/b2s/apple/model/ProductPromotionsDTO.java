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
public class ProductPromotionsDTO {
    private final Optional<PercentagePromotionDTO> percentage;
    private final Optional<CeilingPromotionDTO> ceiling;
    private final Optional<FixedPromotionDTO> fixed;
    private final Optional<CostPerPointPromotionDTO> costPerPoint;

    private ProductPromotionsDTO(final Builder builder) {
        this.percentage = builder.percentage;
        this.ceiling = builder.ceiling;
        this.fixed = builder.fixed;
        this.costPerPoint = builder.costPerPoint;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static ProductPromotionsDTO create(
        @JsonProperty("percentage") final PercentagePromotionDTO thePercentage,
        @JsonProperty("ceiling") final CeilingPromotionDTO theCeiling,
        @JsonProperty("fixed") final FixedPromotionDTO theFixed,
        @JsonProperty("costPerPoint") final CostPerPointPromotionDTO theCostPerPoint) {
        return builder()
            .withPercentage(thePercentage)
            .withCeiling(theCeiling)
            .withFixed(theFixed)
            .withCostPerPoint(theCostPerPoint)
            .build();
    }

    public Optional<PercentagePromotionDTO> getPercentage() {
        return this.percentage;
    }

    public Optional<CeilingPromotionDTO> getCeiling() {
        return this.ceiling;
    }

    public Optional<FixedPromotionDTO> getFixed() {
        return this.fixed;
    }

    public Optional<CostPerPointPromotionDTO> getCostPerPoint() {
        return this.costPerPoint;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ProductPromotionsDTO other = (ProductPromotionsDTO) o;
        return Objects.equals(this.percentage, other.percentage)
            && Objects.equals(this.ceiling, other.ceiling)
            && Objects.equals(this.fixed, other.fixed)
            && Objects.equals(this.costPerPoint, other.costPerPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(percentage, ceiling, fixed, costPerPoint);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withPercentage(percentage.orElse(null))
            .withCeiling(ceiling.orElse(null))
            .withFixed(fixed.orElse(null))
            .withCostPerPoint(costPerPoint.orElse(null));
    }

    public static final class Builder {
        private Optional<PercentagePromotionDTO> percentage = Optional.empty();
        private Optional<CeilingPromotionDTO> ceiling = Optional.empty();
        private Optional<FixedPromotionDTO> fixed = Optional.empty();
        private Optional<CostPerPointPromotionDTO> costPerPoint = Optional.empty();

        private Builder() {
        }

        public Builder withPercentage(final PercentagePromotionDTO thePercentage) {
            this.percentage = Optionals.from(thePercentage);
            return this;
        }

        public Builder withCeiling(final CeilingPromotionDTO theCeiling) {
            this.ceiling = Optionals.from(theCeiling);
            return this;
        }

        public Builder withFixed(final FixedPromotionDTO theFixed) {
            this.fixed = Optionals.from(theFixed);
            return this;
        }

        public Builder withCostPerPoint(final CostPerPointPromotionDTO theCostPerPoint) {
            this.costPerPoint = Optionals.from(theCostPerPoint);
            return this;
        }

        public ProductPromotionsDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ProductPromotionsDTO(this));
        }
    }
}
