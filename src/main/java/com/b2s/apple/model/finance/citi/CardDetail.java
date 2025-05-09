package com.b2s.apple.model.finance.citi;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class CardDetail {

    private final String cardId;
    private final String displayCardNumber;
    private final String localCardActivationIndicator;
    private final String overseasCardActivationIndicator;
    private final Boolean perpetualActivationFlag;
    private final String overseasCardActivationStartDate;
    private final String overseasCardActivationEndDate;
    private final Integer currentCreditLimitAmount;
    private final Integer maximumTemporaryCreditLimitAmount;
    private final String subCardType;
    private final String cardHolderType;
    private final List<CardFunctionsAllowed> cardFunctionsAllowed;
    private final String cardIssueReason;

    private CardDetail (final Builder builder) {
        this.cardId = builder.cardId;
        this.displayCardNumber = builder.displayCardNumber;
        this.localCardActivationIndicator = builder.localCardActivationIndicator;
        this.overseasCardActivationIndicator = builder.overseasCardActivationIndicator;
        this.perpetualActivationFlag = builder.perpetualActivationFlag;
        this.overseasCardActivationStartDate = builder.overseasCardActivationStartDate;
        this.overseasCardActivationEndDate = builder.overseasCardActivationEndDate;
        this.currentCreditLimitAmount = builder.currentCreditLimitAmount;
        this.maximumTemporaryCreditLimitAmount = builder.maximumTemporaryCreditLimitAmount;
        this.subCardType = builder.subCardType;
        this.cardHolderType = builder.cardHolderType;
        this.cardFunctionsAllowed = builder.cardFunctionsAllowed;
        this.cardIssueReason = builder.cardIssueReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CardDetail create (
            @JsonProperty("cardId") final String cardId,
            @JsonProperty("displayCardNumber") final String displayCardNumber,
            @JsonProperty("localCardActivationIndicator") final String localCardActivationIndicator,
            @JsonProperty("overseasCardActivationIndicator") final String overseasCardActivationIndicator,
            @JsonProperty("perpetualActivationFlag") final Boolean perpetualActivationFlag,
            @JsonProperty("overseasCardActivationStartDate") final String overseasCardActivationStartDate,
            @JsonProperty("overseasCardActivationEndDate") final String overseasCardActivationEndDate,
            @JsonProperty("currentCreditLimitAmount") final Integer currentCreditLimitAmount,
            //maximumTemporaryCreditLimitAmount
            @JsonProperty("maximumTemporaryCreditLimitAmount") final Integer maximumTemporaryCreditLimitAmount,
            @JsonProperty("subCardType") final String subCardType,
            @JsonProperty("cardHolderType") final String cardHolderType,
            @JsonProperty("cardFunctionsAllowed") final List<CardFunctionsAllowed> cardFunctionsAllowed,
            @JsonProperty("cardIssueReason") final String cardIssueReason) {
        return builder()
                .withCardId(cardId)
                .withDisplayCardNumber(displayCardNumber)
                .withLocalCardActivationIndicator(localCardActivationIndicator)
                .withOverseasCardActivationIndicator(overseasCardActivationIndicator)
                .withPerpetualActivationFlag(perpetualActivationFlag)
                .withOverseasCardActivationStartDate(overseasCardActivationStartDate)
                .withOverseasCardActivationEndDate(overseasCardActivationEndDate)
                .withCurrentCreditLimitAmount(currentCreditLimitAmount)
                .withSubCardType(subCardType)
                .withCardHolderType(cardHolderType)
                .withCardFunctionsAllowed(cardFunctionsAllowed)
                .withCardIssueReason(cardIssueReason)
                .build();
    }

    public String getCardId() {
        return cardId;
    }

    public String getDisplayCardNumber() {
        return displayCardNumber;
    }

    public String getLocalCardActivationIndicator() {
        return localCardActivationIndicator;
    }

    public String getOverseasCardActivationIndicator() {
        return overseasCardActivationIndicator;
    }

    public Boolean getPerpetualActivationFlag() {
        return perpetualActivationFlag;
    }

    public String getOverseasCardActivationStartDate() {
        return overseasCardActivationStartDate;
    }

    public String getOverseasCardActivationEndDate() {
        return overseasCardActivationEndDate;
    }

    public Integer getCurrentCreditLimitAmount() {
        return currentCreditLimitAmount;
    }

    public Integer getMaximumTemporaryCreditLimitAmount() {
        return maximumTemporaryCreditLimitAmount;
    }

    public String getSubCardType() {
        return subCardType;
    }

    public String getCardHolderType() {
        return cardHolderType;
    }

    public List<CardFunctionsAllowed> getCardFunctionsAllowed() {
        return cardFunctionsAllowed;
    }

    public String getCardIssueReason() {
        return cardIssueReason;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final CardDetail other = (CardDetail) o;
        return Objects.equals(this.cardId, other.cardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId, displayCardNumber, localCardActivationIndicator,
                overseasCardActivationIndicator, perpetualActivationFlag, overseasCardActivationStartDate,
                overseasCardActivationEndDate, currentCreditLimitAmount, maximumTemporaryCreditLimitAmount, subCardType,
                cardHolderType, cardFunctionsAllowed, cardIssueReason);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
                .withCardId(cardId)
                .withDisplayCardNumber(displayCardNumber)
                .withLocalCardActivationIndicator(localCardActivationIndicator)
                .withOverseasCardActivationIndicator(overseasCardActivationIndicator)
                .withPerpetualActivationFlag(perpetualActivationFlag)
                .withOverseasCardActivationStartDate(overseasCardActivationStartDate)
                .withOverseasCardActivationEndDate(overseasCardActivationEndDate)
                .withCurrentCreditLimitAmount(currentCreditLimitAmount)
                .withMaximumTemporaryCreditLimitAmount(maximumTemporaryCreditLimitAmount)
                .withSubCardType(subCardType)
                .withCardHolderType(cardHolderType)
                .withCardFunctionsAllowed(cardFunctionsAllowed)
                .withCardIssueReason(cardIssueReason);
    }

    public static final class Builder {
        private String cardId;
        private String displayCardNumber;
        private String localCardActivationIndicator;
        private String overseasCardActivationIndicator;
        private Boolean perpetualActivationFlag;
        private String overseasCardActivationStartDate;
        private String overseasCardActivationEndDate;
        private Integer currentCreditLimitAmount;
        private Integer maximumTemporaryCreditLimitAmount;
        private String subCardType;
        private String cardHolderType;
        private List<CardFunctionsAllowed> cardFunctionsAllowed = null;
        private String cardIssueReason;

        private Builder() {
        }

        public Builder withCardId(final String cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder withDisplayCardNumber(final String displayCardNumber) {
            this.displayCardNumber = displayCardNumber;
            return this;
        }

        public Builder withLocalCardActivationIndicator(final String localCardActivationIndicator) {
            this.localCardActivationIndicator = localCardActivationIndicator;
            return this;
        }

        public Builder withOverseasCardActivationIndicator(final String overseasCardActivationIndicator) {
            this.overseasCardActivationIndicator = overseasCardActivationIndicator;
            return this;
        }

        public Builder withPerpetualActivationFlag(final Boolean perpetualActivationFlag) {
            this.perpetualActivationFlag = perpetualActivationFlag;
            return this;
        }

        public Builder withOverseasCardActivationStartDate(final String overseasCardActivationStartDate) {
            this.overseasCardActivationStartDate = overseasCardActivationStartDate;
            return this;
        }

        public Builder withOverseasCardActivationEndDate(final String overseasCardActivationEndDate) {
            this.overseasCardActivationEndDate = overseasCardActivationEndDate;
            return this;
        }

        public Builder withCurrentCreditLimitAmount(final Integer currentCreditLimitAmount) {
            this.currentCreditLimitAmount = currentCreditLimitAmount;
            return this;
        }

        public Builder withMaximumTemporaryCreditLimitAmount(final Integer maximumTemporaryCreditLimitAmount) {
            this.maximumTemporaryCreditLimitAmount = maximumTemporaryCreditLimitAmount;
            return this;
        }

        public Builder withSubCardType(final String subCardType) {
            this.subCardType = subCardType;
            return this;
        }

        public Builder withCardHolderType(final String cardHolderType) {
            this.cardHolderType = cardHolderType;
            return this;
        }

        public Builder withCardFunctionsAllowed(final List<CardFunctionsAllowed> cardFunctionsAllowed) {
            this.cardFunctionsAllowed = cardFunctionsAllowed;
            return this;
        }

        public Builder withCardIssueReason(final String cardIssueReason) {
            this.cardIssueReason = cardIssueReason;
            return this;
        }

        public CardDetail build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CardDetail(this));
        }
    }
}