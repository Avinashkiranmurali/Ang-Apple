package com.b2s.apple.model;

import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.b2s.service.utils.lang.string.ToStringGenerator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

public class SubscriptionRequestDTO {

    private final String merchant;
    private final String subscriptionType;
    private final Optional<String> language;

    private SubscriptionRequestDTO(final Builder builder) {
        this.merchant = Optionals.checkPresent(builder.merchant, "merchant");
        this.subscriptionType = Optionals.checkPresent(builder.subscriptionType, "subscriptionType");
        this.language = builder.language;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    private static SubscriptionRequestDTO create(
        @JsonProperty("merchant") final String theMerchant,
        @JsonProperty("subscriptionType") final String theSubscriptionType,
        @JsonProperty("language") final String theLanguage) {

        return builder()
            .withMerchant(theMerchant)
            .withSubscriptionType(theSubscriptionType)
            .withLanguage(theLanguage)
            .build();
    }

    public String getMerchant() {
        return this.merchant;
    }

    public String getSubscriptionType() {
        return this.subscriptionType;
    }

    public Optional<String> getLanguage() {
        return this.language;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final SubscriptionRequestDTO other = (SubscriptionRequestDTO) o;
        return Objects.equals(this.merchant, other.merchant)
            && Objects.equals(this.subscriptionType, other.subscriptionType)
            && Objects.equals(this.language, other.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merchant, subscriptionType, language);
    }

    @Override
    public String toString() {
        return ToStringGenerator.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withMerchant(merchant)
            .withSubscriptionType(subscriptionType)
            .withLanguage(language.orElse(null));
    }

    public static final class Builder {
        private Optional<String> merchant = Optional.empty();
        private Optional<String> subscriptionType = Optional.empty();
        private Optional<String> language = Optional.empty();

        private Builder() {
        }

        public Builder withMerchant(final String theMerchant) {
            this.merchant = Optionals.from(theMerchant);
            return this;
        }

        public Builder withSubscriptionType(final String theSubscriptionType) {
            this.subscriptionType = Optionals.from(theSubscriptionType);
            return this;
        }

        public Builder withLanguage(final String theLanguage) {
            this.language = Optionals.from(theLanguage);
            return this;
        }

        public SubscriptionRequestDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new SubscriptionRequestDTO(this));
        }
    }
}
