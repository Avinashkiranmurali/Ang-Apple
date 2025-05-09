package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.MoreIterables;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class ProductsPricingRequestDTO {

    private final ContextDTO context;
    private final ImmutableList<ProductRequestDTO> products;

    private ProductsPricingRequestDTO(final Builder builder) {
        this.context = Optionals.checkPresent(builder.context, "context");
        this.products = MoreIterables.notEmpty(builder.products, "products");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static ProductsPricingRequestDTO create(
        @JsonProperty("context") final ContextDTO context,
        @JsonProperty("products") final ImmutableList<ProductRequestDTO> products){
        return builder()
            .withContext(context)
            .withProducts(products)
            .build();
    }

    public ContextDTO getContext() {
        return this.context;
    }

    public ImmutableList<ProductRequestDTO> getProducts() {
        return this.products;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ProductsPricingRequestDTO other = (ProductsPricingRequestDTO) o;
        return Objects.equals(this.context, other.context)
            && Objects.equals(this.products, other.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, products);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withContext(context)
            .withProducts(products);
    }

    public static final class Builder {
        private Optional<ContextDTO> context = Optional.empty();
        private ImmutableList<ProductRequestDTO> products = ImmutableList.of();

        private Builder() {
        }

        public Builder withContext(final ContextDTO theContext) {
            this.context = Optionals.from(theContext);
            return this;
        }

        public Builder withProducts(final Iterable<ProductRequestDTO> theProducts) {
            this.products = AdditionalCollections.asImmutableList(theProducts);
            return this;
        }

        public ProductsPricingRequestDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ProductsPricingRequestDTO(this));
        }
    }
}
