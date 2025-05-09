package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class CartPricingResponseDTO implements Serializable {

    private static final long serialVersionUID = -5731111732348000681L;
    private final transient ImmutableList<ProductResponseDTO> products;
    private final transient ImmutableList<ProductErrorDTO> errors;
    private final transient ImmutableMap<String, CartPriceResultsDTO> prices;
    private final transient Optional<SplitTenderResultDTO> splitTenderResult;

    private CartPricingResponseDTO(final Builder builder) {
        this.products = builder.products;
        this.errors = builder.errors;
        this.prices = builder.prices;
        this.splitTenderResult = builder.splitTenderResult;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CartPricingResponseDTO create(
        @JsonProperty("products") final Iterable<ProductResponseDTO> theProducts,
        @JsonProperty("errors") final Iterable<ProductErrorDTO> theErrors,
        @JsonProperty("prices") final ImmutableMap<String, CartPriceResultsDTO> thePrices,
        @JsonProperty("splitTenderResult") final SplitTenderResultDTO theSplitTenderResult) {
        return builder()
            .withProducts(theProducts)
            .withErrors(theErrors)
            .withPrices(thePrices)
            .withSplitTenderResult(theSplitTenderResult)
            .build();
    }

    public ImmutableList<ProductResponseDTO> getProducts() {
        return this.products;
    }

    public ImmutableList<ProductErrorDTO> getErrors() {
        return this.errors;
    }

    public ImmutableMap<String, CartPriceResultsDTO> getPrices() {
        return this.prices;
    }

    public Optional<SplitTenderResultDTO> getSplitTenderResult() {
        return this.splitTenderResult;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final CartPricingResponseDTO other = (CartPricingResponseDTO) o;
        return Objects.equals(this.products, other.products)
            && Objects.equals(this.errors, other.errors)
            && Objects.equals(this.prices, other.prices)
            && Objects.equals(this.splitTenderResult, other.splitTenderResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(products, errors, prices, splitTenderResult);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withProducts(products)
            .withErrors(errors)
            .withPrices(prices)
            .withSplitTenderResult(splitTenderResult.orElse(null));
    }

    public static final class Builder {
        private ImmutableList<ProductResponseDTO> products = ImmutableList.of();
        private ImmutableList<ProductErrorDTO> errors = ImmutableList.of();
        private ImmutableMap<String, CartPriceResultsDTO> prices = ImmutableMap.of();
        private Optional<SplitTenderResultDTO> splitTenderResult = Optional.empty();

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

        public Builder withPrices(final ImmutableMap<String, CartPriceResultsDTO> thePrices) {
            this.prices = AdditionalCollections.asImmutableMap(thePrices);
            return this;
        }

        public Builder withSplitTenderResult(final SplitTenderResultDTO theSplitTenderResult) {
            this.splitTenderResult = Optionals.from(theSplitTenderResult);
            return this;
        }

        public CartPricingResponseDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CartPricingResponseDTO(this));
        }
    }
}
