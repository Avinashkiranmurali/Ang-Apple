package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.money.BigMoney;

import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class CostPerPointPromotionDTO {

    private final BigMoney costPerPoint;

    private CostPerPointPromotionDTO(final Builder builder) {
        this.costPerPoint = Optionals.checkPresent(builder.costPerPoint, "costPerPoint");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CostPerPointPromotionDTO create(@JsonProperty("costPerPoint") final BigMoney theCostPerPoint) {
        return builder()
            .withCostPerPoint(theCostPerPoint)
            .build();
    }

    public BigMoney getCostPerPoint() {
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
        final CostPerPointPromotionDTO other = (CostPerPointPromotionDTO) o;
        return Objects.equals(this.costPerPoint, other.costPerPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(costPerPoint);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withCostPerPoint(costPerPoint);
    }

    public static final class Builder {
        private Optional<BigMoney> costPerPoint = Optional.empty();

        private Builder() {
        }

        public Builder withCostPerPoint(final BigMoney theCostPerPoint) {
            this.costPerPoint = Optionals.from(theCostPerPoint);
            return this;
        }

        public CostPerPointPromotionDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CostPerPointPromotionDTO(this));
        }
    }
}
