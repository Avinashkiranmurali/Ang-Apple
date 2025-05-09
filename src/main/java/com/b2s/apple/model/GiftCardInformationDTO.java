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
public class GiftCardInformationDTO {
    private final GiftCardTypeValue giftCardType;
    private final Money denomination;

    private GiftCardInformationDTO(final Builder builder) {
        this.giftCardType = Optionals.checkPresent(builder.giftCardType, "giftCardType");
        this.denomination = Optionals.checkPresent(builder.denomination, "denomination");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static GiftCardInformationDTO create(
        @JsonProperty("giftCardType") final GiftCardTypeValue giftCardType,
        @JsonProperty("denomination") final Money denomination){
        return builder()
            .withGiftCardType(giftCardType)
            .withDenomination(denomination)
            .build();
    }

    public GiftCardTypeValue getGiftCardType() {
        return this.giftCardType;
    }

    public Money getDenomination() {
        return this.denomination;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final GiftCardInformationDTO other = (GiftCardInformationDTO) o;
        return this.giftCardType == other.giftCardType
            && Objects.equals(this.denomination, other.denomination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(giftCardType, denomination);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withGiftCardType(giftCardType)
            .withDenomination(denomination);
    }

    public static final class Builder {
        private Optional<GiftCardTypeValue> giftCardType = Optional.empty();
        private Optional<Money> denomination = Optional.empty();

        private Builder() {
        }

        public Builder withGiftCardType(final GiftCardTypeValue theGiftCardType) {
            this.giftCardType = Optionals.from(theGiftCardType);
            return this;
        }

        public Builder withDenomination(final Money theDenomination) {
            this.denomination = Optionals.from(theDenomination);
            return this;
        }

        public GiftCardInformationDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new GiftCardInformationDTO(this));
        }
    }
}
