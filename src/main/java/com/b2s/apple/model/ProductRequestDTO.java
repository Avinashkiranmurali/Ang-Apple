package com.b2s.apple.model;

import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.service.utils.lang.Exceptions;
import com.b2s.service.utils.lang.Optionals;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.joda.money.Money;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author rkumar 2019-09-10
 */
public class ProductRequestDTO {

    private final int index;
    private final ProductTypeValue productType;
    private final Optional<String> merchant;
    private final Optional<String> brand;
    private final Optional<String> sku;
    private final Optional<Integer> quantity;
    private final Optional<Integer> fixedPointPrice;
    private final Money basePrice;
    private final Optional<Money> baseShippingCost;
    private final Optional<Money> msrp;
    private final Optional<Money> supplierTax;
    private final Optional<Money> supplierFees;
    private final ImmutableList<ImmutableList<CategorizationDTO>> categorizations;
    private final Optional<ProductPromotionsDTO> eligiblePromotions;
    private final Optional<GiftCardInformationDTO> giftCardInformation;
    private final Optional<CustomStoreInformationDTO> customStoreInformation;
    private final Optional<DiscountPriceInfoDTO> discountPriceInformation;

    private ProductRequestDTO(final Builder builder) {
        this.index = Optionals.checkPresent(builder.index, "index");
        this.productType = Optionals.checkPresent(builder.productType, "productType");
        this.merchant = builder.merchant;
        this.brand = builder.brand;
        this.sku = builder.sku;
        this.quantity = builder.quantity;
        this.fixedPointPrice = builder.fixedPointPrice;
        this.basePrice = Optionals.checkPresent(builder.basePrice, "basePrice");
        this.baseShippingCost = builder.baseShippingCost;
        this.msrp = builder.msrp;
        this.supplierTax = builder.supplierTax;
        this.supplierFees = builder.supplierFees;
        this.categorizations = builder.categorizations;
        this.eligiblePromotions = builder.eligiblePromotions;
        this.giftCardInformation = builder.giftCardInformation;
        this.customStoreInformation = builder.customStoreInformation;
        this.discountPriceInformation = builder.discountPriceInformation;
    }

    @JsonCreator
    public static ProductRequestDTO create(
        @JsonProperty("index") final int index,
        @JsonProperty("productType") final ProductTypeValue productType,
        @JsonProperty("merchant") final String merchant,
        @JsonProperty("brand") final String brand,
        @JsonProperty("sku") final String sku,
        @JsonProperty("quantity") final Integer quantity,
        @JsonProperty("fixedPointPrice") final Integer fixedPointPrice,
        @JsonProperty("basePrice") final Money basePrice,
        @JsonProperty("baseShippingCost") final Money baseShippingCost,
        @JsonProperty("msrp") final Money msrp,
        @JsonProperty("supplierTax") final Money supplierTax,
        @JsonProperty("supplierFees") final Money supplierFees,
        @JsonProperty("categorizations") final ImmutableList<ImmutableList<CategorizationDTO>> categorizations,
        @JsonProperty("eligiblePromotions") final ProductPromotionsDTO eligiblePromotions,
        @JsonProperty("giftCardInformation") final GiftCardInformationDTO giftCardInformation,
        @JsonProperty("customStoreInformation") final CustomStoreInformationDTO customStoreInformation,
        @JsonProperty("discountPriceInformation") final DiscountPriceInfoDTO discountPriceInformation) {
        return builder()
            .withIndex(index)
            .withProductType(productType)
            .withMerchant(merchant)
            .withBrand(brand)
            .withSku(sku)
            .withQuantity(quantity)
            .withFixedPointPrice(fixedPointPrice)
            .withBasePrice(basePrice)
            .withBaseShippingCost(baseShippingCost)
            .withMsrp(msrp)
            .withSupplierTax(supplierTax)
            .withSupplierFees(supplierFees)
            .withCategorizations(categorizations)
            .withEligiblePromotions(eligiblePromotions)
            .withGiftCardInformation(giftCardInformation)
            .withCustomStoreInformation(customStoreInformation)
            .withDiscountPriceInformation(discountPriceInformation)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getIndex() {
        return this.index;
    }

    public ProductTypeValue getProductType() {
        return this.productType;
    }

    public Optional<String> getMerchant() {
        return this.merchant;
    }

    public Optional<String> getBrand() {
        return this.brand;
    }

    public Optional<String> getSku() {
        return this.sku;
    }

    public Optional<Integer> getQuantity() {
        return this.quantity;
    }

    public Optional<Integer> getFixedPointPrice() {
        return this.fixedPointPrice;
    }

    public Money getBasePrice() {
        return this.basePrice;
    }

    public Optional<Money> getBaseShippingCost() {
        return this.baseShippingCost;
    }

    public Optional<Money> getMsrp() {
        return this.msrp;
    }

    public Optional<Money> getSupplierTax() {
        return this.supplierTax;
    }

    public Optional<Money> getSupplierFees() {
        return this.supplierFees;
    }

    public ImmutableList<ImmutableList<CategorizationDTO>> getCategorizations() {
        return this.categorizations;
    }

    public Optional<ProductPromotionsDTO> getEligiblePromotions() {
        return this.eligiblePromotions;
    }

    public Optional<GiftCardInformationDTO> getGiftCardInformation() {
        return this.giftCardInformation;
    }

    public Optional<CustomStoreInformationDTO> getCustomStoreInformation() {
        return this.customStoreInformation;
    }

    public Optional<DiscountPriceInfoDTO> getDiscountPriceInformation() {
        return discountPriceInformation;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ProductRequestDTO other = (ProductRequestDTO) o;
        return Objects.equals(this.index, other.index)
            && Objects.equals(this.productType, other.productType)
            && Objects.equals(this.merchant, other.merchant)
            && Objects.equals(this.brand, other.brand)
            && Objects.equals(this.sku, other.sku)
            && Objects.equals(this.quantity, other.quantity)
            && Objects.equals(this.fixedPointPrice, other.fixedPointPrice)
            && Objects.equals(this.basePrice, other.basePrice)
            && Objects.equals(this.baseShippingCost, other.baseShippingCost)
            && Objects.equals(this.msrp, other.msrp)
            && Objects.equals(this.supplierTax, other.supplierTax)
            && Objects.equals(this.supplierFees, other.supplierFees)
            && Objects.equals(this.categorizations, other.categorizations)
            && Objects.equals(this.eligiblePromotions, other.eligiblePromotions)
            && Objects.equals(this.giftCardInformation, other.giftCardInformation)
            && Objects.equals(this.customStoreInformation, other.customStoreInformation)
            && Objects.equals(this.discountPriceInformation, other.discountPriceInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            index,
            productType,
            merchant,
            brand,
            sku,
            quantity,
            fixedPointPrice,
            basePrice,
            baseShippingCost,
            msrp,
            supplierTax,
            supplierFees,
            categorizations,
            eligiblePromotions,
            giftCardInformation,
            customStoreInformation,
            discountPriceInformation);
    }

    @Override
    public String toString() {
        return ToStringConvertor.createFor(this);
    }

    public Builder toBuilder() {
        return builder()
            .withIndex(index)
            .withProductType(productType)
            .withMerchant(merchant.orElse(null))
            .withBrand(brand.orElse(null))
            .withSku(sku.orElse(null))
            .withQuantity(quantity.orElse(null))
            .withFixedPointPrice(fixedPointPrice.orElse(null))
            .withBasePrice(basePrice)
            .withBaseShippingCost(baseShippingCost.orElse(null))
            .withMsrp(msrp.orElse(null))
            .withSupplierTax(supplierTax.orElse(null))
            .withSupplierFees(supplierFees.orElse(null))
            .withCategorizations(categorizations)
            .withEligiblePromotions(eligiblePromotions.orElse(null))
            .withGiftCardInformation(giftCardInformation.orElse(null))
            .withCustomStoreInformation(customStoreInformation.orElse(null))
            .withDiscountPriceInformation(discountPriceInformation.orElse(null));
    }

    public static final class Builder {
        private static final AtomicInteger COUNTER = new AtomicInteger(0);
        private Optional<Integer> index = Optional.of(COUNTER.incrementAndGet());
        private Optional<ProductTypeValue> productType = Optional.empty();
        private Optional<String> merchant = Optional.empty();
        private Optional<String> brand = Optional.empty();
        private Optional<String> sku = Optional.empty();
        private Optional<Integer> quantity = Optional.empty();
        private Optional<Integer> fixedPointPrice = Optional.empty();
        private Optional<Money> basePrice = Optional.empty();
        private Optional<Money> baseShippingCost = Optional.empty();
        private Optional<Money> msrp = Optional.empty();
        private Optional<Money> supplierTax = Optional.empty();
        private Optional<Money> supplierFees = Optional.empty();
        private ImmutableList<ImmutableList<CategorizationDTO>> categorizations = ImmutableList.of();
        private Optional<ProductPromotionsDTO> eligiblePromotions = Optional.empty();
        private Optional<GiftCardInformationDTO> giftCardInformation = Optional.empty();
        private Optional<CustomStoreInformationDTO> customStoreInformation = Optional.empty();
        private Optional<DiscountPriceInfoDTO> discountPriceInformation = Optional.empty();

        private Builder() {
        }

        public Builder withIndex(final Integer theIndex) {
            this.index = Optionals.from(theIndex);
            return this;
        }

        public Builder withProductType(final ProductTypeValue theProductType) {
            this.productType = Optionals.from(theProductType);
            return this;
        }

        public Builder withMerchant(final String theMerchant) {
            this.merchant = Optionals.from(theMerchant);
            return this;
        }

        public Builder withBrand(final String theBrand) {
            this.brand = Optionals.from(theBrand);
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

        public Builder withBasePrice(final Money theBasePrice) {
            this.basePrice = Optionals.from(theBasePrice);
            return this;
        }

        public Builder withBaseShippingCost(final Money theBaseShippingCost) {
            this.baseShippingCost = Optionals.from(theBaseShippingCost);
            return this;
        }

        public Builder withMsrp(final Money theMsrp) {
            this.msrp = Optionals.from(theMsrp);
            return this;
        }

        public Builder withSupplierTax(final Money theSupplierTax) {
            this.supplierTax = Optionals.from(theSupplierTax);
            return this;
        }

        public Builder withSupplierFees(final Money theSupplierFees) {
            this.supplierFees = Optionals.from(theSupplierFees);
            return this;
        }

        public Builder withCategorizations(final Iterable<ImmutableList<CategorizationDTO>> theCategorizations) {
            return this;
        }

        public Builder withEligiblePromotions(final ProductPromotionsDTO theEligiblePromotions) {
            this.eligiblePromotions = Optionals.from(theEligiblePromotions);
            return this;
        }

        public Builder withGiftCardInformation(final GiftCardInformationDTO theGiftCardInformation) {
            this.giftCardInformation = Optionals.from(theGiftCardInformation);
            return this;
        }

        public Builder withCustomStoreInformation(final CustomStoreInformationDTO theCustomStoreInformation) {
            this.customStoreInformation = Optionals.from(theCustomStoreInformation);
            return this;
        }

        public Builder withDiscountPriceInformation(final DiscountPriceInfoDTO theDiscountPriceInformation) {
            this.discountPriceInformation = Optionals.from(theDiscountPriceInformation);
            return this;
        }

        public ProductRequestDTO build() {
            return Exceptions.illegalArgumentToIllegalState(() -> new ProductRequestDTO(this));
        }
    }
}
