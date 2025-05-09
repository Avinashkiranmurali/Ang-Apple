package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class ProductErrorDTO {
    private final ProductRequestDTO request;
    private final String errorMessage;

    private ProductErrorDTO(final Builder builder) {
        this.request = Optionals.checkPresent(builder.request, "request");
        this.errorMessage = Optionals.checkPresent(builder.errorMessage, "errorMessage");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static ProductErrorDTO create(
        @JsonProperty("request") final ProductRequestDTO theRequest,
        @JsonProperty("errorMessage") final String theErrorMessage) {
        return builder()
            .withRequest(theRequest)
            .withErrorMessage(theErrorMessage)
            .build();
    }

    public ProductRequestDTO getRequest() {
        return this.request;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ProductErrorDTO other = (ProductErrorDTO) o;
        return Objects.equals(this.request, other.request)
            && Objects.equals(this.errorMessage, other.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, errorMessage);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withRequest(request)
            .withErrorMessage(errorMessage);
    }

    public static final class Builder {
        private Optional<ProductRequestDTO> request = Optional.empty();
        private Optional<String> errorMessage = Optional.empty();

        private Builder() {
        }

        public Builder withRequest(final ProductRequestDTO theRequest) {
            this.request = Optionals.from(theRequest);
            return this;
        }

        public Builder withErrorMessage(final String theErrorMessage) {
            this.errorMessage = Optionals.from(theErrorMessage);
            return this;
        }

        public ProductErrorDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ProductErrorDTO(this));
        }
    }
}
