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
public class CartCurrencyAmountDTO {
    private final Money totalPrice;
    private final Money unpromotedTotalPrice;

    private CartCurrencyAmountDTO(final Builder builder) {
        this.totalPrice = Optionals.checkPresent(builder.totalPrice, "totalPrice");
        this.unpromotedTotalPrice = Optionals.checkPresent(builder.unpromotedTotalPrice, "unpromotedTotalPrice");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CartCurrencyAmountDTO create(
        @JsonProperty("totalPrice") final Money theTotalPrice,
        @JsonProperty("unpromotedTotalPrice") final Money theUnpromotedTotalPrice) {
        return builder()
            .withTotalPrice(theTotalPrice)
            .withUnpromotedTotalPrice(theUnpromotedTotalPrice)
            .build();
    }

    public Money getTotalPrice() {
        return this.totalPrice;
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
        final CartCurrencyAmountDTO other = (CartCurrencyAmountDTO) o;
        return Objects.equals(this.totalPrice, other.totalPrice)
            && Objects.equals(this.unpromotedTotalPrice, other.unpromotedTotalPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalPrice, unpromotedTotalPrice);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withTotalPrice(totalPrice)
            .withUnpromotedTotalPrice(unpromotedTotalPrice);
    }

    public static final class Builder {
        private Optional<Money> totalPrice = Optional.empty();
        private Optional<Money> unpromotedTotalPrice = Optional.empty();

        private Builder() {
        }

        public Builder withTotalPrice(final Money theTotalPrice) {
            this.totalPrice = Optionals.from(theTotalPrice);
            return this;
        }

        public Builder withUnpromotedTotalPrice(final Money theUnpromotedTotalPrice) {
            this.unpromotedTotalPrice = Optionals.from(theUnpromotedTotalPrice);
            return this;
        }

        public CartCurrencyAmountDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CartCurrencyAmountDTO(this));
        }
    }
}
