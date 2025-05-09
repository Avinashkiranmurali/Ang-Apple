package com.b2s.apple.model.finance;

import com.b2s.service.utils.lang.Exceptions;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Card {

    private final String cardType;// VISA/MASTER_CARD/AMEX/...
    private final String cardId;
    private final String displayCardNumber;
    private final String expireMonth;
    private final String expireYear;
    private final String cvv;

    private Card(final Builder builder) {
        this.cardType = builder.cardType;
        this.cardId = builder.cardId;
        this.displayCardNumber = builder.displayCardNumber;
        this.expireMonth = builder.expireMonth;
        this.expireYear = builder.expireYear;
        this.cvv = builder.cvv;
    }

    @JsonCreator
    public static Card create(
            @JsonProperty("cardType") final String cardType,
            @JsonProperty("carId") final String carId,
            @JsonProperty("displayCardNumber") final String displayCardNumber,
            @JsonProperty("expireMonth") final String expireMonth,
            @JsonProperty("expireYear") final String expireYear,
            @JsonProperty("cvv") final String cvv){
        return builder()
                .withCardType(cardType)
                .withCardId(carId)
                .withDisplayCardNumber(displayCardNumber)
                .withExpireMonth(expireMonth)
                .withExpireYear(expireYear)
                .withCvv(cvv)
                .build();
    }

    public String getCardType() {
        return cardType;
    }

    public String getCardId() {
        return cardId;
    }

    public String getDisplayCardNumber() {
        return displayCardNumber;
    }

    public String getExpireMonth() {
        return expireMonth;
    }

    public String getExpireYear() {
        return expireYear;
    }

    public String getCvv() {
        return cvv;
    }

    public static Card.Builder builder() {
        return new Card.Builder();
    }
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final Card other = (Card) o;
        return Objects.equals(this.cardType, other.cardType)
                && Objects.equals(this.cardId, other.cardId)
                && Objects.equals(this.displayCardNumber, other.displayCardNumber)
                && Objects.equals(this.expireMonth, other.expireMonth)
                && Objects.equals(this.expireYear, other.expireYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardType, cardId, displayCardNumber, expireMonth, expireYear, cvv);
    }

    @Override
    public String toString() {

        return "Card{" +
                "cardType ='" + cardType + '\'' +
                ", expireMonth='" + expireMonth + '\'' +
                ", expireYear='" + expireYear + '\'' +
                '}';
    }

    public Builder toBuilder() {
        return builder()
                .withCardType(cardType)
                .withCardId(cardId)
                .withDisplayCardNumber(displayCardNumber)
                .withExpireMonth(expireMonth)
                .withExpireYear(expireYear)
                .withCvv(cvv);
    }
    public static final class Builder {
        private String cardType;// VISA/MASTER_CARD/AMEX/...
        private String cardId;
        private String displayCardNumber;//Protected!/ Never add to toString
        private String expireMonth;
        private String expireYear;
        private String cvv;

        private Builder() {
        }

        public Builder withCardType(final String cardType) {
            this.cardType = cardType;
            return this;
        }

        public Card.Builder withCardId(final String cardId) {
            this.cardId = cardId;
            return this;
        }

        public Card.Builder withDisplayCardNumber(final String displayCardNumber) {
            this.displayCardNumber = displayCardNumber;
            return this;
        }

        public Builder withExpireMonth(final String expireMonth) {
            this.expireMonth = expireMonth;
            return this;
        }

        public Builder withExpireYear(final String expireYear) {
            this.expireYear = expireYear;
            return this;
        }

        public Builder withCvv(final String cvv) {
            this.cvv = cvv;
            return this;
        }

        public Card build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new Card(this));
        }
    }
}
