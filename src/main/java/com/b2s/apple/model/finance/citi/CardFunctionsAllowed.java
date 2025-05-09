package com.b2s.apple.model.finance.citi;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


public class CardFunctionsAllowed {

    private final String cardFunction;

    private CardFunctionsAllowed (final Builder builder) {
        this.cardFunction = builder.cardFunction;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CardFunctionsAllowed create (
            @JsonProperty("cardFunction") final String cardFunction) {
        return builder()
                .withCardFunction(cardFunction)
                .build();
    }

    public String getCardFunction() {
        return cardFunction;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final CardFunctionsAllowed other = (CardFunctionsAllowed) o;
        return Objects.equals(this.cardFunction, other.cardFunction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardFunction);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
                .withCardFunction(cardFunction);
    }

    public static final class Builder {
        private String cardFunction;

        private Builder() {
        }

        public Builder withCardFunction(final String cardFunction) {
            this.cardFunction = cardFunction;
            return this;
        }


        public CardFunctionsAllowed build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CardFunctionsAllowed(this));
        }
    }
}