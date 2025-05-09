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
public class AddressDTO {
    private final Optional<String> line1;
    private final Optional<String> line2;
    private final Optional<String> city;
    private final Optional<String> stateCode;
    private final Optional<String> postalCode;
    private final String countryCode;

    private AddressDTO(final Builder builder) {
        this.line1 = builder.line1;
        this.line2 = builder.line2;
        this.city = builder.city;
        this.stateCode = builder.stateCode;
        this.postalCode = builder.postalCode;
        this.countryCode = Optionals.checkPresent(builder.countryCode, "countryCode");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static AddressDTO create(
        @JsonProperty("line1") final String line1,
        @JsonProperty("line2") final String line2,
        @JsonProperty("city") final String city,
        @JsonProperty("stateCode") final String stateCode,
        @JsonProperty("postalCode") final String postalCode,
        @JsonProperty("countryCode") final String countryCode) {
        return builder()
            .withLine1(line1)
            .withLine2(line2)
            .withCity(city)
            .withStateCode(stateCode)
            .withPostalCode(postalCode)
            .withCountryCode(countryCode)
            .build();
    }

    public Optional<String> getLine1() {
        return this.line1;
    }

    public Optional<String> getLine2() {
        return this.line2;
    }

    public Optional<String> getCity() {
        return this.city;
    }

    public Optional<String> getStateCode() {
        return this.stateCode;
    }

    public Optional<String> getPostalCode() {
        return this.postalCode;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final AddressDTO other = (AddressDTO) o;
        return Objects.equals(this.line1, other.line1)
            && Objects.equals(this.line2, other.line2)
            && Objects.equals(this.city, other.city)
            && Objects.equals(this.stateCode, other.stateCode)
            && Objects.equals(this.postalCode, other.postalCode)
            && Objects.equals(this.countryCode, other.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line1, line2, city, stateCode, postalCode, countryCode);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withLine1(line1.orElse(null))
            .withLine2(line2.orElse(null))
            .withCity(city.orElse(null))
            .withStateCode(stateCode.orElse(null))
            .withPostalCode(postalCode.orElse(null))
            .withCountryCode(countryCode);
    }

    public static final class Builder {
        private Optional<String> line1 = Optional.empty();
        private Optional<String> line2 = Optional.empty();
        private Optional<String> city = Optional.empty();
        private Optional<String> stateCode = Optional.empty();
        private Optional<String> postalCode = Optional.empty();
        private Optional<String> countryCode = Optional.empty();

        private Builder() {
        }

        public Builder withLine1(final String theLine1) {
            this.line1 = Optionals.from(theLine1);
            return this;
        }

        public Builder withLine2(final String theLine2) {
            this.line2 = Optionals.from(theLine2);
            return this;
        }

        public Builder withCity(final String theCity) {
            this.city = Optionals.from(theCity);
            return this;
        }

        public Builder withStateCode(final String theStateCode) {
            this.stateCode = Optionals.from(theStateCode);
            return this;
        }

        public Builder withPostalCode(final String thePostalCode) {
            this.postalCode = Optionals.from(thePostalCode);
            return this;
        }

        public Builder withCountryCode(final String theCountryCode) {
            this.countryCode = Optionals.from(theCountryCode);
            return this;
        }

        public AddressDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new AddressDTO(this));
        }
    }
}
