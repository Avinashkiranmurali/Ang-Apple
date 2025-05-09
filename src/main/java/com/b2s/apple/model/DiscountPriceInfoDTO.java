package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.money.Money;

import java.util.Objects;
import java.util.Optional;

/**
 * @author efu 2019-09-19
 */
public class DiscountPriceInfoDTO {

    private final Money basePrice;
    private final Money supplierSalesTax;

    private DiscountPriceInfoDTO(final Builder builder) {
        this.basePrice = Optionals.checkPresent(builder.basePrice, "basePrice");
        this.supplierSalesTax = Optionals.checkPresent(builder.supplierSalesTax, "supplierSalesTax");
    }

    @JsonCreator
    public static DiscountPriceInfoDTO create(
        @JsonProperty("basePrice") final Money basePrice,
        @JsonProperty("supplierSalesTax") final Money supplierSalesTax) {
        return builder()
            .withBasePrice(basePrice)
            .withSupplierSalesTax(supplierSalesTax)
            .build();
    }

    public Money getBasePrice() {
        return basePrice;
    }

    public Money getSupplierSalesTax() {
        return supplierSalesTax;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final DiscountPriceInfoDTO other = (DiscountPriceInfoDTO) o;
        return Objects.equals(this.basePrice, other.basePrice)
            && Objects.equals(this.supplierSalesTax, other.supplierSalesTax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePrice, supplierSalesTax);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withBasePrice(basePrice)
            .withSupplierSalesTax(supplierSalesTax);
    }

    public static final class Builder {
        private Optional<Money> basePrice = Optional.empty();
        private Optional<Money> supplierSalesTax = Optional.empty();

        private Builder() {
        }

        public DiscountPriceInfoDTO.Builder withBasePrice(final Money theBasePrice) {
            this.basePrice = Optionals.from(theBasePrice);
            return this;
        }

        public DiscountPriceInfoDTO.Builder withSupplierSalesTax(final Money theSupplierSalesTax) {
            this.supplierSalesTax = Optionals.from(theSupplierSalesTax);
            return this;
        }

        public DiscountPriceInfoDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new DiscountPriceInfoDTO(this));
        }
    }
}
