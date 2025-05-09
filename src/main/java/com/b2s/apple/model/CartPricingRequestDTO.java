package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.MoreIterables;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class CartPricingRequestDTO implements Serializable {

    private static final long serialVersionUID = 6035588235959710438L;
    private final transient ContextDTO context;
    private final transient ImmutableList<DeliveryDTO> deliveries;
    private final transient Optional<SplitTenderInformationDTO> splitTenderInformation;
    private final boolean preview;

    private CartPricingRequestDTO(final Builder builder) {
        this.context = Optionals.checkPresent(builder.context, "context");
        this.deliveries = MoreIterables.notEmpty(builder.deliveries, "deliveries");
        this.splitTenderInformation = builder.splitTenderInformation;
        this.preview = builder.preview.orElse(false);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public static CartPricingRequestDTO create(
        @JsonProperty("context") final ContextDTO context,
        @JsonProperty("deliveries") ImmutableList<DeliveryDTO> deliveries,
        @JsonProperty("splitTenderInformation") SplitTenderInformationDTO splitTenderInformation,
        @JsonProperty("preview") final boolean preview) {
        return builder()
            .withContext(context)
            .withDeliveries(deliveries)
            .withSplitTenderInformation(splitTenderInformation)
            .withPreview(preview)
            .build();
    }

    public ContextDTO getContext() {
        return this.context;
    }

    public ImmutableList<DeliveryDTO> getDeliveries() {
        return this.deliveries;
    }

    public Optional<SplitTenderInformationDTO> getSplitTenderInformation() {
        return this.splitTenderInformation;
    }

    public boolean isPreview() {
        return this.preview;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final CartPricingRequestDTO other = (CartPricingRequestDTO) o;
        return Objects.equals(this.context, other.context)
            && Objects.equals(this.deliveries, other.deliveries)
            && Objects.equals(this.splitTenderInformation, other.splitTenderInformation)
            && Objects.equals(this.preview, other.preview);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, deliveries, splitTenderInformation, preview);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withContext(context)
            .withDeliveries(deliveries)
            .withSplitTenderInformation(splitTenderInformation.orElse(null))
            .withPreview(preview);
    }

    public static final class Builder {
        private Optional<ContextDTO> context = Optional.empty();
        private ImmutableList<DeliveryDTO> deliveries = ImmutableList.of();
        private Optional<SplitTenderInformationDTO> splitTenderInformation = Optional.empty();
        private Optional<Boolean> preview = Optional.empty();

        private Builder() {
        }

        public Builder withContext(final ContextDTO theContext) {
            this.context = Optionals.from(theContext);
            return this;
        }

        public Builder withDeliveries(final Iterable<DeliveryDTO> theDeliveries) {
            this.deliveries = AdditionalCollections.asImmutableList(theDeliveries);
            return this;
        }

        public Builder withSplitTenderInformation(final SplitTenderInformationDTO theSplitTenderInformation) {
            this.splitTenderInformation = Optionals.from(theSplitTenderInformation);
            return this;
        }

        public Builder withPreview(final Boolean thePreview) {
            this.preview = Optionals.from(thePreview);
            return this;
        }

        public CartPricingRequestDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new CartPricingRequestDTO(this));
        }
    }
}