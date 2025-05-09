package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.service.utils.lang.Exceptions;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

/**
 * @author rkumar 2019-09-10
 */
public class ProductsPricingResponseDTO {
    private final ImmutableList<ProductResponseDTO> products;
    private final ImmutableList<ProductErrorDTO> errors;

    private ProductsPricingResponseDTO(final Builder builder) {
        this.products = builder.products;
        this.errors = builder.errors;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static ProductsPricingResponseDTO create(
        @JsonProperty("products") final Iterable<ProductResponseDTO> theProducts,
        @JsonProperty("errors") final Iterable<ProductErrorDTO> theErrors) {
        return builder()
            .withProducts(theProducts)
            .withErrors(theErrors)
            .build();
    }

    public ImmutableList<ProductResponseDTO> getProducts() {
        return this.products;
    }

    public ImmutableList<ProductErrorDTO> getErrors() {
        return this.errors;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ProductsPricingResponseDTO other = (ProductsPricingResponseDTO) o;
        return Objects.equals(this.products, other.products)
            && Objects.equals(this.errors, other.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(products, errors);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withProducts(products)
            .withErrors(errors);
    }

    public static final class Builder {
        private ImmutableList<ProductResponseDTO> products = ImmutableList.of();
        private ImmutableList<ProductErrorDTO> errors = ImmutableList.of();

        private Builder() {
        }

        public Builder withProducts(final Iterable<ProductResponseDTO> theProducts) {
            this.products = AdditionalCollections.asImmutableList(theProducts);
            return this;
        }

        public Builder withErrors(final Iterable<ProductErrorDTO> theErrors) {
            this.errors = AdditionalCollections.asImmutableList(theErrors);
            return this;
        }

        public ProductsPricingResponseDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ProductsPricingResponseDTO(this));
        }
    }
}
