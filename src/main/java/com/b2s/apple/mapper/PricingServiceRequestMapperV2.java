package com.b2s.apple.mapper;

import com.b2s.apple.model.*;
import com.b2s.rewards.apple.integration.model.PaymentOptions;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.model.Merchant;
import com.b2s.service.product.common.domain.attributes.CommonAttributes;
import com.b2s.service.utils.lang.AdditionalCollections;
import com.b2s.shop.common.User;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class PricingServiceRequestMapperV2 extends  PricingMapperV2{
    private static final Logger LOGGER = LoggerFactory.getLogger(PricingServiceRequestMapperV2.class);
    private static final Integer DEFAULT_QUANTITY = 1;

    public ProductRequestDTO from(final Product product, final CurrencyUnit currency, Integer index) {
        final Offer offer = product.getDefaultOffer();
        final Money basePrice = Money.of(currency, offer.getOrgItemPrice());
        final Money baseShippingCost = Money.of(currency,
            (Objects.isNull(offer.getOrgShippingPrice())) ? 0 : offer.getOrgShippingPrice());

        ProductRequestDTO.Builder productRequestBuilder = ProductRequestDTO.builder()
            .withIndex(index)
            .withSku(product.getSku())
            .withProductType(ProductTypeValue.MERCHANDISE)
            .withMerchant(Merchant.APPLE.getSimpleName())
            .withQuantity(DEFAULT_QUANTITY)
            .withBasePrice(basePrice)
            .withBaseShippingCost(baseShippingCost)
            .withBrand(product.getBrand())
            .withSupplierTax(product.getOffers().get(0).getSupplierSalesTax())
            .withCategorizations(null); //Categorization is NOT mandatory at this point of time for Apple


        ProductPromotionsDTO.Builder eligiblePromotionsBuilder = ProductPromotionsDTO.builder();

        product.getPromotion().ifPresent(promotion -> {
            promotion.getDiscountPercentage()
                    .ifPresent(discountPercentage -> eligiblePromotionsBuilder.withPercentage(
                            PercentagePromotionDTO.builder()
                                    .withPercentage(discountPercentage.divide(new BigDecimal(100)))
                                    .build()));
            promotion.getFixedPointPrice()
                    .ifPresent(fixedPointPrice -> eligiblePromotionsBuilder.withFixed(
                            FixedPromotionDTO.builder()
                                    .withPoints(fixedPointPrice.getAmountMajorInt())
                                    .build()));
            promotion.getCostPerPoint()
                    .ifPresent(costPerPoint -> eligiblePromotionsBuilder.withCostPerPoint(
                            CostPerPointPromotionDTO.builder()
                                    .withCostPerPoint(BigMoney.of(currency, costPerPoint))
                                    .build()));
        });

        // Future change as part of 6.x --> Add Promotions based on costPerPoint and ceiling
        productRequestBuilder.withEligiblePromotions(eligiblePromotionsBuilder.build());

        return productRequestBuilder.build();
    }

    public ContextDTO transform(final Program program, final User user) {
        final Object defaultProgramIdObj = program.getConfig().get(CommonConstants.DEFAULT_PS_PROGRAM);
        final Object defaultVarIdObj = program.getConfig().get(CommonConstants.DEFAULT_PS_VAR);
        final String programId = (Objects.nonNull(defaultProgramIdObj) && StringUtils.isNotBlank(defaultProgramIdObj.toString())) ? defaultProgramIdObj.toString() : program.getProgramId();
        final String varId = (Objects.nonNull(defaultVarIdObj) && StringUtils.isNotBlank(defaultVarIdObj.toString())) ? defaultVarIdObj.toString() : program.getVarId();
        final String state = (Objects.nonNull(user.getShipTo()) && (StringUtils.isNotBlank(user.getShipTo().getState())))? user.getShipTo().getState() : user.getState();

        final List<CurrencyUnit> theTargetCurrencies = new ArrayList<>();
        theTargetCurrencies.add(program.getTargetCurrency());
        theTargetCurrencies.add(CommonAttributes.POINTS_CURRENCY_UNIT);

        ContextDTO.Builder context = ContextDTO.builder()
            .withVarId(varId)
            .withVarProgramId(programId)
            .withOrderType(user.isProxyUser()? OrderTypeValue.OFFLINE:OrderTypeValue.ONLINE)
            .withPricingTier(program.getPricingTier())
            .withStateCode(state)
            .withCountryCode(user.getCountry())
            .withTargetCurrencies(AdditionalCollections.asImmutableList(theTargetCurrencies));

        return context.build();
    }

    public CartPricingRequestDTO from(final Cart cart, final Program program, final boolean hasShippingAddress,
        final User user, final Integer addPoints){

        if(user == null){
            throw new IllegalArgumentException("The user cannot be null");
        }

        final ContextDTO context = transform(program,user);

        final List<DeliveryDTO> deliveries = buildDeliveries(cart, program, hasShippingAddress);

        SplitTenderInformationDTO.Builder splitTenderInformationBuilder = SplitTenderInformationDTO.builder();

        LOGGER.info("PricingServiceRequestMapperV2: NewCheckoutFlow for Supplemental payment...");

        buildTenderInformationWithMaxCashOrMaxPoints(program, splitTenderInformationBuilder);

        splitTenderInformationBuilder.withPointsAmount(addPoints);

        if ((Boolean) program.getConfig()
            .getOrDefault(CommonConstants.SPLIT_PAY_CURRENCY_ROUNDING, Boolean.FALSE)) {
            splitTenderInformationBuilder.withCurrencyScale(CommonConstants.CURRENCY_ROUNDING_SCALE_ZERO);
        }

        CartPricingRequestDTO.Builder cartRequestBuilder = CartPricingRequestDTO.builder()
            .withContext(context)
            .withDeliveries(deliveries)
            .withPreview(!hasShippingAddress);

        setTenderInformation(cart.getCartItems(), splitTenderInformationBuilder.build(), cartRequestBuilder);

        return cartRequestBuilder.build();
    }

    private List<DeliveryDTO> buildDeliveries(Cart cart, Program program, boolean hasShippingAddress) {
        final List<DeliveryDTO> deliveries = new ArrayList<>(); // Seems different delivery option is not there?
        final AtomicInteger index = new AtomicInteger(1);
        final String shippingMethod = cart.getCartItems().stream()
                .filter(cartItem -> StringUtils.isNotBlank(cartItem.getShippingMethod()))
                .findFirst()
                .map(CartItem::getShippingMethod)
                .orElse(StringUtils.EMPTY);
        final DeliveryMethodValue deliveryMethod = retrieveDeliveryMethod(shippingMethod);
        AddressDTO address = null;
        if (cart.getShippingAddress() != null && hasShippingAddress) {

            AddressDTO.Builder addressBuilder = AddressDTO.builder()
                    .withLine1(cart.getShippingAddress().getAddress1())
                    .withLine2(cart.getShippingAddress().getAddress2())
                    .withCountryCode(cart.getShippingAddress().getCountry());
            buildAddressWithCity(cart, addressBuilder);
            buildAddressWithStateCode(cart, addressBuilder);
            buildAddressWithPostalCode(cart, addressBuilder);

            address = addressBuilder.build();
        }
        List<ProductRequestDTO> productRequests =
                cart.getCartItems()
                        .stream()
                        .filter(cartItem -> cartItem.getSupplierId() != CommonConstants.SUPPLIER_TYPE_CREDIT)
                        .map(shoppingCartItem -> createProductRequestDTOFromCartItem(program, index, shoppingCartItem))
                        .collect(Collectors.toList());
        DeliveryDTO delivery = DeliveryDTO.builder()
                .withDeliveryMethod(deliveryMethod)
                .withProducts(productRequests)
                .withShippingMethod(ShippingMethodValue.STANDARD)
                .withAddress(address).build();
        deliveries.add(delivery);
        return deliveries;
    }

    private ProductRequestDTO createProductRequestDTOFromCartItem(Program program, AtomicInteger index, CartItem shoppingCartItem) {
        ProductRequestDTO productRequestFromMapper = from(shoppingCartItem.getProductDetail(),
                program.getTargetCurrency(), index.getAndIncrement());

        ProductRequestDTO.Builder productRequestBuilder =
                createProductRequestBuilder(shoppingCartItem, productRequestFromMapper);
        setProductTypeForPriceCalculation(shoppingCartItem, productRequestBuilder);

        return productRequestBuilder.build();
    }

    private ProductRequestDTO.Builder createProductRequestBuilder(final CartItem shoppingCartItem,
        final ProductRequestDTO productRequestFromMapper) {
        ProductRequestDTO.Builder productRequestBuilder = productRequestFromMapper.toBuilder();
        productRequestBuilder
            .withQuantity(Objects.nonNull(shoppingCartItem.getQuantity())?
                shoppingCartItem.getQuantity():DEFAULT_QUANTITY);
        if(Objects.nonNull(shoppingCartItem.getProductDetail().getDefaultOffer().getDiscountPrice())){

            productRequestBuilder.withDiscountPriceInformation(DiscountPriceInfoDTO
                .builder()
                .withBasePrice(shoppingCartItem.getProductDetail().getDefaultOffer().getDiscountPrice())
                .withSupplierSalesTax(shoppingCartItem.getProductDetail().getDefaultOffer().getDiscountSupplierSalesTax())
                .build()
            );
        }
        return productRequestBuilder;
    }

    private void buildTenderInformationWithMaxCashOrMaxPoints(final Program program,
        final SplitTenderInformationDTO.Builder splitTenderInformationBuilder) {
        if(MapUtils.isNotEmpty(program.getRedemptionOptions())) {
            program.getRedemptionOptions().forEach((optionType, varProgramRedemptionOptions) -> {

                if (optionType.equalsIgnoreCase(PaymentOptions.SPLITPAY.getPaymentOption())) {
                    varProgramRedemptionOptions.forEach(varProgramRedemptionOption -> {
                        if (varProgramRedemptionOption.getLimitType().equalsIgnoreCase(CommonConstants.DOLLAR)) {
                            splitTenderInformationBuilder.withMaxCashAmount(Money
                                .of(program.getTargetCurrency(), varProgramRedemptionOption.getPaymentMaxLimit()));
                        }

                        if (varProgramRedemptionOption.getLimitType()
                            .equalsIgnoreCase(CommonConstants.PERCENTAGE)) {
                            splitTenderInformationBuilder.withMaxPointsPercentage(BigDecimal
                                .valueOf(varProgramRedemptionOption.getPaymentMaxLimit().doubleValue() / 100));
                        }
                    });
                }
            });
        }
    }

    private void buildAddressWithPostalCode(final Cart cart, final AddressDTO.Builder addressBuilder) {
        if (!CommonConstants.COUNTRIES_WITH_NO_POSTALCODE.contains(cart.getShippingAddress().getCountry())) {
            addressBuilder.withPostalCode(cart.getShippingAddress().getZip5());
        }
    }

    private void buildAddressWithStateCode(final Cart cart, final AddressDTO.Builder addressBuilder) {
        // Add state if country not GB, MX, TW, SG, HK, MY, TH, AE, PH, BH
        if (!CommonConstants.COUNTRIES_WITH_NO_STATE.contains(cart.getShippingAddress().getCountry())) {
            addressBuilder.withStateCode(cart.getShippingAddress().getState());
        }
    }

    private void buildAddressWithCity(final Cart cart, final AddressDTO.Builder addressBuilder) {
        // Add CITY if country not SG
        if (!CommonConstants.COUNTRIES_WITH_NO_CITY.contains(cart.getShippingAddress().getCountry())) {
            addressBuilder.withCity(cart.getShippingAddress().getCity());
        }
    }

    private void setProductTypeForPriceCalculation(final CartItem shoppingCartItem,
        final ProductRequestDTO.Builder productRequestBuilder) {
        //setting product type for price calculation
        if (Objects.nonNull(shoppingCartItem.getSupplierId()) && CommonConstants.SUPPLIER_TYPE_GIFTCARD_S.equals(String.valueOf(shoppingCartItem.getSupplierId()))) {

            GiftCardInformationDTO.Builder giftCardInformation = GiftCardInformationDTO.builder()
                .withDenomination(shoppingCartItem.getGiftCardDenomination());

            if(Objects.nonNull(shoppingCartItem.getProductDetail()) && Objects.nonNull(shoppingCartItem.getProductDetail().getOptions())){
                final GiftCardTypeValue giftCardTypeValue = getGiftCardTypeValue(shoppingCartItem);
                if (null != giftCardTypeValue) {
                    giftCardInformation.withGiftCardType(giftCardTypeValue);
                }
            }

            productRequestBuilder
                .withProductType(ProductTypeValue.GIFT_CARD)
                .withGiftCardInformation(giftCardInformation.build());
        } else if (Objects.nonNull(shoppingCartItem.getSupplierId()) && CommonConstants.SUPPLIER_TYPE_SERVICE_PLAN_S.equals(String.valueOf(shoppingCartItem.getSupplierId()))) {
            productRequestBuilder
                    .withProductType(ProductTypeValue.SERVICE_PLAN);
        }
        else{
            productRequestBuilder
                .withProductType(ProductTypeValue.MERCHANDISE);
        }
    }

    private GiftCardTypeValue getGiftCardTypeValue(final CartItem shoppingCartItem) {
        List<Option> giftcardOptions = shoppingCartItem.getProductDetail().getOptions();
        for(Option option:giftcardOptions){
            if(CommonConstants.GIFTCARDS_DELIVERYMETHOD.equalsIgnoreCase(option.getName())){
                if(CommonConstants.GIFTCARDS_ECARD.equalsIgnoreCase(option.getKey())){
                    return GiftCardTypeValue.ELECTRONIC;
                } else {
                    return GiftCardTypeValue.PHYSICAL;
                }
            }
        }
        return null;
    }

    /**
     * Set Tender Information
     *
     * @param cartItems
     * @param splitTenderInformationBuilder
     * @param cartRequestBuilder
     */
    private void setTenderInformation(final List<CartItem> cartItems,
        final SplitTenderInformationDTO splitTenderInformationBuilder,
        final CartPricingRequestDTO.Builder cartRequestBuilder) {
        // if the cart contains only free gift items, then we don't need split tender information
        if (!cartItems.stream()
            .filter(cartItem -> !Double.valueOf(100.00).equals(cartItem.getDiscount()))
            .findFirst()
            .isEmpty()) {
            cartRequestBuilder.withSplitTenderInformation(splitTenderInformationBuilder);
        }
    }
}
