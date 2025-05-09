package com.b2s.apple.model;

import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.b2s.service.utils.lang.string.ToStringGenerator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

public class ErrorResponseDTO {
    public enum ErrorType {SERVER_ERROR, BAD_REQUEST, GATEWAY_TIMEOUT, NOT_FOUND}

    private final ErrorType type;
    private final String message;
    private final String timestamp;

    private ErrorResponseDTO(final Builder builder) {
        this.type = Optionals.checkPresent(builder.type, "type");
        this.message = Optionals.checkPresent(builder.message, "message");
        this.timestamp = Optionals.checkPresent(builder.timestamp, "timestamp");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    private static ErrorResponseDTO create(
        @JsonProperty("type") final ErrorType theType,
        @JsonProperty("message") final String theMessage,
        @JsonProperty("timestamp") final String theTimestamp) {

        return builder()
            .withType(theType)
            .withMessage(theMessage)
            .withTimestamp(theTimestamp)
            .build();
    }

    public ErrorType getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ErrorResponseDTO other = (ErrorResponseDTO) o;
        return this.type == other.type
            && Objects.equals(this.message, other.message)
            && Objects.equals(this.timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message, timestamp);
    }

    @Override
    public String toString() {
        return ToStringGenerator.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withType(type)
            .withMessage(message)
            .withTimestamp(timestamp);
    }

    public static final class Builder {
        private Optional<ErrorType> type = Optional.empty();
        private Optional<String> message = Optional.empty();
        private Optional<String> timestamp = Optional.empty();

        private Builder() {
        }

        public Builder withType(final ErrorType theType) {
            this.type = Optionals.from(theType);
            return this;
        }

        public Builder withMessage(final String theMessage) {
            this.message = Optionals.from(theMessage);
            return this;
        }

        public Builder withTimestamp(final String theTimestamp) {
            this.timestamp = Optionals.from(theTimestamp);
            return this;
        }

        public ErrorResponseDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ErrorResponseDTO(this));
        }
    }
}
