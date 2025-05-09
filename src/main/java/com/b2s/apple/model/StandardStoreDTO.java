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
public class StandardStoreDTO {

    private final int storeId;

    private StandardStoreDTO(final Builder builder) {
        this.storeId = Optionals.checkPresent(builder.storeId, "storeId");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static StandardStoreDTO create(@JsonProperty("storeId") final int storeId){
        return builder()
            .withStoreId(storeId)
            .build();
    }

    public int getStoreId() {
        return this.storeId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final StandardStoreDTO other = (StandardStoreDTO) o;
        return Objects.equals(this.storeId, other.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withStoreId(storeId);
    }

    public static final class Builder {
        private Optional<Integer> storeId = Optional.empty();

        private Builder() {
        }

        public Builder withStoreId(final Integer theStoreId) {
            this.storeId = Optionals.from(theStoreId);
            return this;
        }

        public StandardStoreDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new StandardStoreDTO(this));
        }
    }
}
