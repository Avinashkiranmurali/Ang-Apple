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
 * @author rkumar 2019-09-10
 */
public class CurrencyAmountDTO {

    private final Money unitPrice;
    private final Money totalPrice;
    private final Money unpromotedUnitPrice;
    private final Money unpromotedTotalPrice;

    private CurrencyAmountDTO(final Builder builder) {
        this.unitPrice = Optionals.checkPresent(builder.unitPrice, "unitPrice");
        this.totalPrice = Optionals.checkPresent(builder.totalPrice, "totalPrice");
        this.unpromotedUnitPrice = Optionals.checkPresent(builder.unpromotedUnitPrice, "unpromotedUnitPrice");
        this.unpromotedTotalPrice = Optionals.checkPresent(builder.unpromotedTotalPrice, "unpromotedTotalPrice");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CurrencyAmountDTO create(
        @JsonProperty("unitPrice") final Money theUnitPrice,
        @JsonProperty("totalPrice") final Money theTotalPrice,
        @JsonProperty("unpromotedUnitPrice") final Money theUnpromotedUnitPrice,
        @JsonProperty("unpromotedTotalPrice") final Money theUnpromotedTotalPrice) {
        return builder()
            .withUnitPrice(theUnitPrice)
            .withTotalPrice(theTotalPrice)
            .withUnpromotedUnitPrice(theUnpromotedUnitPrice)
            .withUnpromotedTotalPrice(theUnpromotedTotalPrice)
            .build();
    }

    public Money getUnitPrice() {
        return this.unitPrice;
    }

    public Money getTotalPrice() {
        return this.totalPrice;
    }

    public Money getUnpromotedUnitPrice() {
        return this.unpromotedUnitPrice;
    }

    public Money getUnpromotedTotalPrice() {
        return this.unpromotedTotalPrice;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final CurrencyAmountDTO other = (CurrencyAmountDTO) o;
        return Objects.equals(this.unitPrice, other.unitPrice)
            && Objects.equals(this.totalPrice, other.totalPrice)
            && Objects.equals(this.unpromotedUnitPrice, other.unpromotedUnitPrice)
            && Objects.equals(this.unpromotedTotalPrice, other.unpromotedTotalPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitPrice, totalPrice, unpromotedUnitPrice, unpromotedTotalPrice);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withUnitPrice(unitPrice)
            .withTotalPrice(totalPrice)
            .withUnpromotedUnitPrice(unpromotedUnitPrice)
            .withUnpromotedTotalPrice(unpromotedTotalPrice);
    }

    public static final class Builder {
        private Optional<Money> unitPrice = Optional.empty();
        private Optional<Money> totalPrice = Optional.empty();
        private Optional<Money> unpromotedUnitPrice = Optional.empty();
        private Optional<Money> unpromotedTotalPrice = Optional.empty();

        private Builder() {
        }

        public Builder withUnitPrice(final Money theUnitPrice) {
            this.unitPrice = Optionals.from(theUnitPrice);
            return this;
        }

        public Builder withTotalPrice(final Money theTotalPrice) {
            this.totalPrice = Optionals.from(theTotalPrice);
            return this;
        }

        public Builder withUnpromotedUnitPrice(final Money theUnpromotedUnitPrice) {
            this.unpromotedUnitPrice = Optionals.from(theUnpromotedUnitPrice);
            return this;
        }

        public Builder withUnpromotedTotalPrice(final Money theUnpromotedTotalPrice) {
            this.unpromotedTotalPrice = Optionals.from(theUnpromotedTotalPrice);
            return this;
        }

        public CurrencyAmountDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CurrencyAmountDTO(this));
        }
    }
}
