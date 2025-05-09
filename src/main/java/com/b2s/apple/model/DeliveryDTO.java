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
public class DeliveryDTO {
    private final DeliveryMethodValue deliveryMethod;
    private final Optional<ShippingMethodValue> shippingMethod;
    private final Optional<AddressDTO> address;
    private final ImmutableList<ProductRequestDTO> products;


    private DeliveryDTO(final Builder builder) {
        this.deliveryMethod = Optionals.checkPresent(builder.deliveryMethod, "deliveryMethod");
        this.shippingMethod = builder.shippingMethod;
        this.address = builder.address;
        this.products = MoreIterables.notEmpty(builder.products, "products");
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static DeliveryDTO create(
        @JsonProperty("deliveryMethod") final DeliveryMethodValue theDeliveryMethod,
        @JsonProperty("shippingMethod") final ShippingMethodValue theShippingMethod,
        @JsonProperty("address") final AddressDTO theAddress,
        @JsonProperty("products") final Iterable<ProductRequestDTO> theProducts) {
        return builder()
            .withDeliveryMethod(theDeliveryMethod)
            .withShippingMethod(theShippingMethod)
            .withAddress(theAddress)
            .withProducts(theProducts)
            .build();
    }

    public DeliveryMethodValue getDeliveryMethod() {
        return this.deliveryMethod;
    }

    public Optional<ShippingMethodValue> getShippingMethod() {
        return this.shippingMethod;
    }

    public Optional<AddressDTO> getAddress() {
        return this.address;
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
        final DeliveryDTO other = (DeliveryDTO) o;
        return Objects.equals(this.deliveryMethod, other.deliveryMethod)
            && Objects.equals(this.shippingMethod, other.shippingMethod)
            && Objects.equals(this.address, other.address)
            && Objects.equals(this.products, other.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryMethod, shippingMethod, address, products);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withDeliveryMethod(deliveryMethod)
            .withShippingMethod(shippingMethod.orElse(null))
            .withAddress(address.orElse(null))
            .withProducts(products);
    }

    public static final class Builder {
        private Optional<DeliveryMethodValue> deliveryMethod = Optional.empty();
        private Optional<ShippingMethodValue> shippingMethod = Optional.empty();
        private Optional<AddressDTO> address = Optional.empty();
        private ImmutableList<ProductRequestDTO> products = ImmutableList.of();

        private Builder() {
        }

        public Builder withDeliveryMethod(final DeliveryMethodValue theDeliveryMethod) {
            this.deliveryMethod = Optionals.from(theDeliveryMethod);
            return this;
        }

        public Builder withShippingMethod(final ShippingMethodValue theShippingMethod) {
            this.shippingMethod = Optionals.from(theShippingMethod);
            return this;
        }

        public Builder withAddress(final AddressDTO theAddress) {
            this.address = Optionals.from(theAddress);
            return this;
        }

        public Builder withProducts(final Iterable<ProductRequestDTO> theProducts) {
            this.products = AdditionalCollections.asImmutableList(theProducts);
            return this;
        }

        public DeliveryDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new DeliveryDTO(this));
        }
    }
}
