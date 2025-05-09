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
public class FxRateDTO {
    private final BigDecimal rate;
    private final BigDecimal inverseRate;

    private FxRateDTO(final Builder builder) {
        this.rate = Optionals.checkPresent(builder.rate, "rate");
        this.inverseRate = Optionals.checkPresent(builder.inverseRate, "inverseRate");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static FxRateDTO create(
        @JsonProperty("rate") final BigDecimal theRate,
        @JsonProperty("inverseRate") final BigDecimal theInverseRate) {
        return builder()
            .withRate(theRate)
            .withInverseRate(theInverseRate)
            .build();
    }

    public BigDecimal getRate() {
        return this.rate;
    }

    public BigDecimal getInverseRate() {
        return this.inverseRate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final FxRateDTO other = (FxRateDTO) o;
        return this.rate != null && this.rate.compareTo(other.rate) == 0
            && this.inverseRate != null && this.inverseRate.compareTo(other.inverseRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rate, inverseRate);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withRate(rate)
            .withInverseRate(inverseRate);
    }

    public static final class Builder {
        private Optional<BigDecimal> rate = Optional.empty();
        private Optional<BigDecimal> inverseRate = Optional.empty();

        private Builder() {
        }

        public Builder withRate(final BigDecimal theRate) {
            this.rate = Optionals.from(theRate);
            return this;
        }

        public Builder withInverseRate(final BigDecimal theInverseRate) {
            this.inverseRate = Optionals.from(theInverseRate);
            return this;
        }

        public FxRateDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new FxRateDTO(this));
        }
    }
}
