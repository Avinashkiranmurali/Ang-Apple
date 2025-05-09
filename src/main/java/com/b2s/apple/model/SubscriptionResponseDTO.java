package com.b2s.apple.model;

import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.b2s.service.utils.lang.string.ToStringGenerator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

public class SubscriptionResponseDTO {

    private final String redemptionUrl;
    private final Optional<Integer> remainingCount;
    private final Optional<OffsetDateTime> expirationDateTime;

    private SubscriptionResponseDTO(final Builder builder) {
        this.redemptionUrl = Optionals.checkPresent(builder.redemptionUrl, "redemptionUrl");
        this.remainingCount = builder.remainingCount;
        this.expirationDateTime = builder.expirationDateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    private static SubscriptionResponseDTO create(
        @JsonProperty("redemptionUrl") final String theRedemptionUrl,
        @JsonProperty("remainingCount") final Integer theRemainingCount,
        @JsonProperty("expirationDateTime") final OffsetDateTime theExpirationDateTime) {

        return builder()
            .withRedemptionUrl(theRedemptionUrl)
            .withRemainingCount(theRemainingCount)
            .withExpirationDateTime(theExpirationDateTime)
            .build();
    }

    public String getRedemptionUrl() {
        return this.redemptionUrl;
    }

    public Optional<Integer> getRemainingCount() {
        return this.remainingCount;
    }

    public Optional<OffsetDateTime> getExpirationDateTime() {
        return this.expirationDateTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final SubscriptionResponseDTO other = (SubscriptionResponseDTO) o;
        return Objects.equals(this.redemptionUrl, other.redemptionUrl)
            && Objects.equals(this.remainingCount, other.remainingCount)
            && Objects.equals(this.expirationDateTime, other.expirationDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redemptionUrl, remainingCount, expirationDateTime);
    }

    @Override
    public String toString() {
        return ToStringGenerator.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withRedemptionUrl(redemptionUrl)
            .withRemainingCount(remainingCount.orElse(null))
            .withExpirationDateTime(expirationDateTime.orElse(null));
    }

    public static final class Builder {
        private Optional<String> redemptionUrl = Optional.empty();
        private Optional<Integer> remainingCount = Optional.empty();
        private Optional<OffsetDateTime> expirationDateTime = Optional.empty();

        private Builder() {
        }

        public Builder withRedemptionUrl(final String theRedemptionUrl) {
            this.redemptionUrl = Optionals.from(theRedemptionUrl);
            return this;
        }

        public Builder withRemainingCount(final Integer theRemainingCount) {
            this.remainingCount = Optionals.from(theRemainingCount);
            return this;
        }

        public Builder withExpirationDateTime(final OffsetDateTime theExpirationDateTime) {
            this.expirationDateTime = Optionals.from(theExpirationDateTime);
            return this;
        }

        public SubscriptionResponseDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new SubscriptionResponseDTO(this));
        }
    }
}
