package com.b2s.apple.model.finance.citi;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class CardDetails {

    private final List<CardDetail> cardDetails;

    private CardDetails (final Builder builder) {
        this.cardDetails = builder.cardDetails;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CardDetails create (
            @JsonProperty("cardDetails") final List<CardDetail> cardDetails) {
        return builder()
                .withCardDetails(cardDetails)
                .build();
    }

    public List<CardDetail> getCardDetails() {
        return cardDetails;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final CardDetails other = (CardDetails) o;
        return Objects.equals(this.cardDetails, other.cardDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardDetails);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
                .withCardDetails(cardDetails);
    }

    public static final class Builder {
        private List<CardDetail> cardDetails;

        private Builder() {
        }

        public Builder withCardDetails(final List<CardDetail> cardDetails) {
            this.cardDetails = cardDetails;
            return this;
        }


        public CardDetails build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CardDetails(this));
        }
    }
}
