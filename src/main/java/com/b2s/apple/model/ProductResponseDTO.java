package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.Objects;
import java.util.Optional;

/**
 * @author rkumar 2019-09-10
 */
public class ProductResponseDTO {
    private final int index;
    private final Optional<String> merchant;
    private final Optional<String> sku;
    private final int quantity;
    private final Optional<Integer> fixedPointPrice;
    private final Optional<Boolean> expeditedShippingRequired;
    private final Optional<ProductPromotionsDTO> appliedPromotions;
    private final ImmutableMap<String, PriceResultsDTO> prices;
    private final Optional<ConversionRatesDTO> conversionRates;
    private final Optional<PointsEarnInformationDTO> pointsEarnInformation;
    private final PricingParametersDTO pricingParameters;

    private ProductResponseDTO(final Builder builder) {
        this.index = Optionals.checkPresent(builder.index, "index");
        this.merchant = builder.merchant;
        this.sku = builder.sku;
        this.quantity = Optionals.checkPresent(builder.quantity, "quantity");
        this.fixedPointPrice = builder.fixedPointPrice;
        this.expeditedShippingRequired = builder.expeditedShippingRequired;
        this.appliedPromotions = builder.appliedPromotions;
        this.prices = builder.prices;
        this.conversionRates = builder.conversionRates;
        this.pointsEarnInformation = builder.pointsEarnInformation;
        this.pricingParameters = Optionals.checkPresent(builder.pricingParameters, "pricingParameters");
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @JsonCreator
    public static ProductResponseDTO create(
        @JsonProperty("index") final Integer theIndex,
        @JsonProperty("merchant") final String theMerchant,
        @JsonProperty("sku") final String theSku,
        @JsonProperty("quantity") final Integer theQuantity,
        @JsonProperty("fixedPointPrice") final Integer theFixedPointPrice,
        @JsonProperty("expeditedShippingRequired") final Boolean theExpeditedShippingRequired,
        @JsonProperty("appliedPromotions") final ProductPromotionsDTO theAppliedPromotions,
        @JsonProperty("prices") final ImmutableMap<String, PriceResultsDTO> thePrices,
        @JsonProperty("conversionRates") final ConversionRatesDTO theConversionRates,
        @JsonProperty("pointsEarnInformation") final PointsEarnInformationDTO thePointsEarnInformation,
        @JsonProperty("pricingParameters") final PricingParametersDTO thePricingParameters) {
        return builder()
            .withIndex(theIndex)
            .withMerchant(theMerchant)
            .withSku(theSku)
            .withQuantity(theQuantity)
            .withFixedPointPrice(theFixedPointPrice)
            .withExpeditedShippingRequired(theExpeditedShippingRequired)
            .withAppliedPromotions(theAppliedPromotions)
            .withPrices(thePrices)
            .withConversionRates(theConversionRates)
            .withPointsEarnInformation(thePointsEarnInformation)
            .withPricingParameters(thePricingParameters)
            .build();
    }

    public int getIndex() {
        return this.index;
    }

    public Optional<String> getMerchant() {
        return this.merchant;
    }

    public Optional<String> getSku() {
        return this.sku;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public Optional<Integer> getFixedPointPrice() {
        return this.fixedPointPrice;
    }

    public Optional<Boolean> getExpeditedShippingRequired() {
        return this.expeditedShippingRequired;
    }

    public Optional<ProductPromotionsDTO> getAppliedPromotions() {
        return this.appliedPromotions;
    }

    public ImmutableMap<String, PriceResultsDTO> getPrices() {
        return this.prices;
    }

    public Optional<ConversionRatesDTO> getConversionRates() {
        return this.conversionRates;
    }

    public Optional<PointsEarnInformationDTO> getPointsEarnInformation() {
        return this.pointsEarnInformation;
    }

    public PricingParametersDTO getPricingParameters() {
        return this.pricingParameters;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ProductResponseDTO other = (ProductResponseDTO) o;
        return Objects.equals(this.index, other.index)
            && Objects.equals(this.merchant, other.merchant)
            && Objects.equals(this.sku, other.sku)
            && Objects.equals(this.quantity, other.quantity)
            && Objects.equals(this.fixedPointPrice, other.fixedPointPrice)
            && Objects.equals(this.expeditedShippingRequired, other.expeditedShippingRequired)
            && Objects.equals(this.appliedPromotions, other.appliedPromotions)
            && Objects.equals(this.prices, other.prices)
            && Objects.equals(this.conversionRates, other.conversionRates)
            && Objects.equals(this.pointsEarnInformation, other.pointsEarnInformation)
            && Objects.equals(this.pricingParameters, other.pricingParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index,
            merchant,
            sku,
            quantity,
            fixedPointPrice,
            expeditedShippingRequired,
            appliedPromotions,
            prices,
            conversionRates,
            pointsEarnInformation,
            pricingParameters);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withIndex(index)
            .withMerchant(merchant.orElse(null))
            .withSku(sku.orElse(null))
            .withQuantity(quantity)
            .withFixedPointPrice(fixedPointPrice.orElse(null))
            .withExpeditedShippingRequired(expeditedShippingRequired.orElse(null))
            .withAppliedPromotions(appliedPromotions.orElse(null))
            .withPrices(prices)
            .withConversionRates(conversionRates.orElse(null))
            .withPointsEarnInformation(pointsEarnInformation.orElse(null))
            .withPricingParameters(pricingParameters);
    }

    public static final class Builder {
        private Optional<Integer> index = Optional.empty();
        private Optional<String> merchant = Optional.empty();
        private Optional<String> sku = Optional.empty();
        private Optional<Integer> quantity = Optional.empty();
        private Optional<Integer> fixedPointPrice = Optional.empty();
        private Optional<Boolean> expeditedShippingRequired = Optional.empty();
        private Optional<ProductPromotionsDTO> appliedPromotions = Optional.empty();
        private ImmutableMap<String, PriceResultsDTO> prices = ImmutableMap.of();
        private Optional<ConversionRatesDTO> conversionRates = Optional.empty();
        private Optional<PointsEarnInformationDTO> pointsEarnInformation = Optional.empty();
        private Optional<PricingParametersDTO> pricingParameters = Optional.empty();

        private Builder() {
        }

        public Builder withIndex(final Integer theIndex) {
            this.index = Optionals.from(theIndex);
            return this;
        }

        public Builder withMerchant(final String theMerchant) {
            this.merchant = Optionals.from(theMerchant);
            return this;
        }

        public Builder withSku(final String theSku) {
            this.sku = Optionals.from(theSku);
            return this;
        }

        public Builder withQuantity(final Integer theQuantity) {
            this.quantity = Optionals.from(theQuantity);
            return this;
        }

        public Builder withFixedPointPrice(final Integer theFixedPointPrice) {
            this.fixedPointPrice = Optionals.from(theFixedPointPrice);
            return this;
        }

        public Builder withExpeditedShippingRequired(final Boolean theExpeditedShippingRequired) {
            this.expeditedShippingRequired = Optionals.from(theExpeditedShippingRequired);
            return this;
        }

        public Builder withAppliedPromotions(final ProductPromotionsDTO theAppliedPromotions) {
            this.appliedPromotions = Optionals.from(theAppliedPromotions);
            return this;
        }

        public Builder withPrices(final ImmutableMap<String, PriceResultsDTO> thePrices) {
            this.prices = AdditionalCollections.asImmutableMap(thePrices);
            return this;
        }

        public Builder withConversionRates(final ConversionRatesDTO theConversionRates) {
            this.conversionRates = Optionals.from(theConversionRates);
            return this;
        }

        public Builder withPointsEarnInformation(final PointsEarnInformationDTO thePointsEarnInformation) {
            this.pointsEarnInformation = Optionals.from(thePointsEarnInformation);
            return this;
        }

        public Builder withPricingParameters(final PricingParametersDTO thePricingParameters) {
            this.pricingParameters = Optionals.from(thePricingParameters);
            return this;
        }

        public ProductResponseDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ProductResponseDTO(this));
        }
    }
}
