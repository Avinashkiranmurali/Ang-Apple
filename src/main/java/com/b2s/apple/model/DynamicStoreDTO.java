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
public class DynamicStoreDTO {

    private final BigDecimal varMargin;
    private final BigDecimal taxRate;

    private DynamicStoreDTO(final Builder builder) {
        this.varMargin = Optionals.checkPresent(builder.varMargin, "varMargin");
        this.taxRate = Optionals.checkPresent(builder.taxRate, "taxRate");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static DynamicStoreDTO create(
        @JsonProperty("varMargin") final BigDecimal varMargin,
        @JsonProperty("taxRate") final BigDecimal taxRate) {
        return builder()
            .withVarMargin(varMargin)
            .withTaxRate(taxRate)
            .build();
    }

    public BigDecimal getVarMargin() {
        return this.varMargin;
    }

    public BigDecimal getTaxRate() {
        return this.taxRate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final DynamicStoreDTO other = (DynamicStoreDTO) o;
        return this.varMargin != null && this.varMargin.compareTo(other.varMargin) == 0
            && this.taxRate != null && this.taxRate.compareTo(other.taxRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(varMargin, taxRate);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withVarMargin(varMargin)
            .withTaxRate(taxRate);
    }

    public static final class Builder {
        private Optional<BigDecimal> varMargin = Optional.empty();
        private Optional<BigDecimal> taxRate = Optional.empty();

        private Builder() {
        }

        public Builder withVarMargin(final BigDecimal theVarMargin) {
            this.varMargin = Optionals.from(theVarMargin);
            return this;
        }

        public Builder withTaxRate(final BigDecimal theTaxRate) {
            this.taxRate = Optionals.from(theTaxRate);
            return this;
        }

        public DynamicStoreDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new DynamicStoreDTO(this));
        }
    }
}
