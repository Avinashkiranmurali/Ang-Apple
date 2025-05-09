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
public class PointsBasedStoreDTO {

    private final int basePointsPrice;
    private final BigDecimal conversionRate;

    private PointsBasedStoreDTO(final Builder builder) {
        this.basePointsPrice = Optionals.checkPresent(builder.basePointsPrice, "basePointsPrice");
        this.conversionRate = Optionals.checkPresent(builder.conversionRate, "conversionRate");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static PointsBasedStoreDTO create(
        @JsonProperty("basePointsPrice") final int basePointsPrice,
        @JsonProperty("conversionRate") final BigDecimal conversionRate){
        return builder()
            .withBasePointsPrice(basePointsPrice)
            .withConversionRate(conversionRate)
            .build();
    }

    public int getBasePointsPrice() {
        return this.basePointsPrice;
    }

    public BigDecimal getConversionRate() {
        return this.conversionRate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final PointsBasedStoreDTO other = (PointsBasedStoreDTO) o;
        return Objects.equals(this.basePointsPrice, other.basePointsPrice)
            && this.conversionRate != null && this.conversionRate.compareTo(other.conversionRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePointsPrice, conversionRate);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withBasePointsPrice(basePointsPrice)
            .withConversionRate(conversionRate);
    }

    public static final class Builder {
        private Optional<Integer> basePointsPrice = Optional.empty();
        private Optional<BigDecimal> conversionRate = Optional.empty();

        private Builder() {
        }

        public Builder withBasePointsPrice(final Integer theBasePointsPrice) {
            this.basePointsPrice = Optionals.from(theBasePointsPrice);
            return this;
        }

        public Builder withConversionRate(final BigDecimal theConversionRate) {
            this.conversionRate = Optionals.from(theConversionRate);
            return this;
        }

        public PointsBasedStoreDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new PointsBasedStoreDTO(this));
        }
    }
}
