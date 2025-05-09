package com.b2s.apple.mapper;

import com.b2s.apple.model.*;
import com.b2s.common.util.ValidationUtil;
import com.b2s.db.model.BundledPricingOption;
import com.b2s.rewards.apple.model.PaymentOption;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.PricingUtil;
import com.b2s.rewards.model.ProductError;
import com.b2s.rewards.model.ProductErrorType;
import com.b2s.service.product.common.domain.attributes.CommonAttributes;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.b2s.rewards.apple.util.PricingUtil.isEligibleForPayrollDeduction;
import static com.b2s.rewards.common.util.CommonConstants.*;

/**
 * @author rkumar 2019-09-11
 */

@Component
public class PricingServiceResponseMapperV2 extends PricingMapperV2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(PricingServiceResponseMapperV2.class);

    public static final BigDecimal BASIS_POINTS_MULTIPLIER = BigDecimal.valueOf(10000);

    @Autowired
    private Properties applicationProperties;

    public void populateCartPrices(final CartPricingResponseDTO cartPricingResponseDTO, final User user,
                                   final Cart shoppingCart, final Program program) {
        ValidationUtil.illegalArgumentIfNull(program.getTargetCurrency(), "targetCurrency in user.program");
        final CurrencyUnit targetCurrency = program.getTargetCurrency();

        throwExceptionIfTrue(cartPricingResponseDTO.getPrices().isEmpty(), "cart response prices cannot be null.");

        final StringBuffer errors = new StringBuffer();
        Optional.ofNullable(cartPricingResponseDTO.getErrors()).ifPresent(productErrorDTOS ->
            productErrorDTOS.forEach(productErrorDTO -> {
                final Product product = findProductByPsid(productErrorDTO.getRequest().getSku().get(), shoppingCart);
                Optional.ofNullable(product).ifPresent(p -> {
                    LOGGER.error("P$ Error on the product sku: {}", product.getSku());
                    errors.append("\n"+productErrorDTO.getErrorMessage()+" for product "+product.getSku());
                    product.addProductError(
                        new ProductError(ProductErrorType.PRICING_ERROR, productErrorDTO.getErrorMessage()));
                });
            })
        );

        throwExceptionIfTrue(
            cartPricingResponseDTO.getProducts().isEmpty() || !cartPricingResponseDTO.getErrors().isEmpty(),
            "Either products in cart response is Empty or found below errors " + errors);

        final Map<String, CartPriceResultsDTO> cartPrices = cartPricingResponseDTO.getPrices();
        final List<ProductResponseDTO> products = cartPricingResponseDTO.getProducts();

        final CartTotal cartTotal = getCartTotal(user, shoppingCart, program, cartPrices);

        shoppingCart.setCartTotal(cartTotal);

        // Initialise the actual cart total as display cart total
        shoppingCart.setDisplayCartTotal(cartTotal);
        overrideDisplayCartTotal(shoppingCart, program);

        if (CollectionUtils.isNotEmpty(shoppingCart.getCartItems())) {
            Integer index = 1;
            for (final CartItem cartItem : shoppingCart.getCartItems()) {
                final Optional<ProductResponseDTO> optionalProductResponseDTO = findProductByIndex(index++, products);
                if (optionalProductResponseDTO.isPresent()) {
                    ProductResponseDTO productResponseDTO = optionalProductResponseDTO.get();
                    final Offer offer = cartItem.getProductDetail().getDefaultOffer();

                    populateConversionRate(shoppingCart, productResponseDTO, offer);

                    Optional.ofNullable(productResponseDTO.getPricingParameters()).ifPresent(pricingParametersDTO -> {
                        offer.setRoundingIncrement(
                            Money.of(targetCurrency, pricingParametersDTO.getRoundingIncrement()));
                        populateRetailPriceInfo(pricingParametersDTO, offer);

                        //Set Bridge2 Unit Base Price for DAC pricing
                        offer.setBridge2UnitBasePrice(pricingParametersDTO.getBridge2UnitBasePrice().orElse(null));
                    });

                    throwExceptionIfTrue(productResponseDTO.getPrices().isEmpty(), "product response prices cannot be null.");

                    final Map<String, PriceResultsDTO> productPrices = productResponseDTO.getPrices();

                    offer.setB2sItemPrice(getUnitPrice(productPrices, DISPLAY_ITEM_PRICE, targetCurrency));

                    //Updates display price only for DGwP flow
                    updateDisplayPrice(program, targetCurrency, cartItem, offer, productPrices);

                    offer.setB2sShippingPrice(getUnitPrice(productPrices, DISPLAY_SHIPPING_COST, targetCurrency));

                    // Price including taxes and fees multiplied by quantity
                    offer.setTotalPrice(getProductPrice(productPrices, DISPLAY_PRICE, targetCurrency));
                    // Price including taxes and fees for a single unit
                    offer.setUnitTotalPrice(getUnitPrice(productPrices, DISPLAY_PRICE, targetCurrency));

                    offer.setVarPrice(getUnitPrice(productPrices, VAR_PRICE, targetCurrency));
                    offer.setUnpromotedVarPrice(getUndiscountedUnitPrice(productPrices, VAR_PRICE, targetCurrency));
                    offer.setUnpromotedSupplierItemPrice(
                            getUndiscountedUnitPrice(productPrices, BASE_PRICE, targetCurrency));
                    offer.setB2sItemProfitPrice(getUnitPrice(productPrices, BRIDGE2_ITEM_MARKUP, targetCurrency));
                    offer.setVarItemProfitPrice(getUnitPrice(productPrices, VAR_ITEM_MARKUP, targetCurrency));
                    offer
                            .setB2sItemMargin(productResponseDTO.getPricingParameters().getB2sMargin().orElse(BigDecimal.ZERO).doubleValue());
                    offer
                            .setB2sShippingMargin(
                                    productResponseDTO.getPricingParameters().getB2sMargin().orElse(BigDecimal.ZERO).doubleValue());
                    offer
                            .setVarItemMargin(productResponseDTO.getPricingParameters().getVarMargin().orElse(BigDecimal.ZERO).doubleValue());
                    offer
                            .setVarShippingMargin(
                                    productResponseDTO.getPricingParameters().getVarMargin().orElse(BigDecimal.ZERO).doubleValue());
                    offer.setB2sShippingProfitPrice(
                            getUnitPrice(productPrices, BRIDGE2_SHIPPING_MARKUP, targetCurrency).getAmount());
                    offer.setVarShippingProfitPrice(
                            getUnitPrice(productPrices, VAR_SHIPPING_MARKUP, targetCurrency).getAmount());

                    populateServiceFee(targetCurrency, productResponseDTO, offer);

                    final String country = Optional.ofNullable(shoppingCart.getShippingAddress()).map(address ->
                        Optional.ofNullable(address.getCountry()).orElse("")
                    ).orElse("");
                    populateTaxes(productResponseDTO, targetCurrency, offer, country);
                    populateUnpromotedTaxes(productResponseDTO, targetCurrency, offer, country);
                    populateFees(productResponseDTO, targetCurrency, offer);
                    populateUnpromotedFees(productResponseDTO, targetCurrency, offer);
                }
            }
        }
    }

    private CartTotal getCartTotal(final User user, final Cart shoppingCart, final Program program,
        final Map<String, CartPriceResultsDTO> cartPrices) {
        final CurrencyUnit targetCurrency = program.getTargetCurrency();

        final CartTotal cartTotal = new CartTotal();
        cartTotal.setPrice(getPrice(cartPrices, DISPLAY_PRICE, targetCurrency));
        cartTotal.setItemsSubtotalPrice(getPrice(cartPrices, DISPLAY_ITEM_PRICE, targetCurrency));
        cartTotal.setShippingPrice(getPrice(cartPrices, DISPLAY_SHIPPING_COST, targetCurrency));
        cartTotal.setTotalFees(getPrice(cartPrices, DISPLAY_FEES, targetCurrency));
        cartTotal.setTotalTaxes(getPrice(cartPrices, DISPLAY_TAXES, targetCurrency));
        cartTotal.setEstablishmentFees(getEstablishmentPrice(program, targetCurrency));
        cartTotal.setCurrency(targetCurrency.toCurrency());

        setCartTotalDiscountInfo(shoppingCart, program, targetCurrency, cartPrices, cartTotal);

        cartTotal.setFees(Collections.<String, Fee>emptyMap());
        cartTotal.setTaxes(Collections.<String, Tax>emptyMap());

        Price price = getCartTotalPrice(cartTotal);

        if (price != null) {
            final boolean isEligibleForPayrollDeduction =
                    isEligibleForPayrollDeduction(user, program, cartTotal.getItemsSubtotalPrice());
            shoppingCart.setIsEligibleForPayrollDeduction(isEligibleForPayrollDeduction);

            final Optional<PaymentOption> paymentOption = program.getPayments().stream().filter(
                    paymentOption1 -> paymentOption1 != null && paymentOption1.getIsActive() &&
                            CAT_PAYROLLDEDUCTION_STR.equals(paymentOption1.getPaymentOption())).findFirst();

            final Double totalCost = price.getAmount();
            Double estimatedDownPayment = calculateEstimatedDownPayment(paymentOption, totalCost);

            setPayPeriodInfo(program, cartTotal, totalCost);
            shoppingCart.setEstimatedDownPayment(estimatedDownPayment);
        }
        return cartTotal;
    }

    private void populateConversionRate(final Cart shoppingCart, final ProductResponseDTO productResponseDTO, final Offer offer) {
        final ConversionRatesDTO conversionRatesDTO = productResponseDTO.getConversionRates().orElse(null);
        if (Objects.nonNull(conversionRatesDTO) && MapUtils.isNotEmpty(conversionRatesDTO.getFxRates()) &&
                conversionRatesDTO.getFxRates()
                        .containsKey(CommonAttributes.POINTS_CURRENCY_UNIT)) {
            final FxRateDTO fxRateDTO =
                    conversionRatesDTO.getFxRates()
                            .get(CommonAttributes.POINTS_CURRENCY_UNIT);

            Optional.ofNullable(fxRateDTO).ifPresent(f ->
                Optional.ofNullable(f.getRate()).ifPresent(rate -> {
                    offer.setConvRate(fxRateDTO.getRate().doubleValue());
                    offer.setInverseRate(fxRateDTO.getInverseRate().doubleValue());
                    shoppingCart.setConvRate(fxRateDTO.getRate().doubleValue());
                }));
        }
    }

    //Updates display price only for DGwP flow
    private void updateDisplayPrice(final Program program, final CurrencyUnit targetCurrency, final CartItem cartItem,
        final Offer offer, final Map<String, PriceResultsDTO> productPrices) {
        if (StringUtils.isNotBlank(cartItem.getDiscountType())) {
            if (BundledPricingOption.BUNDLED.equals(program.getBundledPricingOption())) {
                offer.setDisplayPrice(getUnitPrice(productPrices, DISPLAY_PRICE, targetCurrency));
            } else {
                offer.setDisplayPrice(getUnitPrice(productPrices, DISPLAY_ITEM_PRICE, targetCurrency));
            }
        }
    }

    private void throwExceptionIfTrue(final boolean empty, final String s) {
        if (empty) {
            throw new IllegalArgumentException(s);
        }
    }

    private void overrideDisplayCartTotal(final Cart shoppingCart, final Program program) {
        // If promo/campaign pricing exists, override display cart total
        if (CollectionUtils.isNotEmpty(program.getPricingModels()) &&
                CollectionUtils.isNotEmpty(shoppingCart.getCartItems())) {
            final CartTotal promoCartTotal = getPromoCartTotal(shoppingCart, program);
            if (promoCartTotal != null) {
                shoppingCart.setDisplayCartTotal(promoCartTotal);
            }
        }
    }

    private void setPayPeriodInfo(final Program program, final CartTotal cartTotal, final Double totalCost) {
        if (totalCost != null && program.getConfig().containsKey(PAY_PERIODS) &&
                Objects.nonNull(program.getConfig().get(PAY_PERIODS))) {
            final int payPeriods =
                    Integer.parseInt(program.getConfig().get(PAY_PERIODS).toString());
            if (payPeriods > 0) {
                cartTotal.setPayPerPeriod(ProductMapper.getPayPerPeriodPrice(program, totalCost, payPeriods));
                cartTotal.setPayPeriods(payPeriods);
            }
        }
        //below block is for program IDs with discount applied and no PPP eligible
        else {
            cartTotal.setPayPerPeriod(0.0D);
            cartTotal.setPayPeriods(0);
        }
    }

    private Double calculateEstimatedDownPayment(final Optional<PaymentOption> paymentOption, final Double totalCost) {
        Double estimatedDownPayment = 0.00;

        // Calculate estimatedDownPayment based on available spending limit
        if (paymentOption.isPresent()) {
            final Double paymentMaxLimit = paymentOption.get().getPaymentMaxLimit();
            if (paymentMaxLimit != null && paymentMaxLimit > 0 && totalCost > paymentMaxLimit) {
                estimatedDownPayment =
                        BigDecimal.valueOf(totalCost - paymentMaxLimit).setScale(2, RoundingMode.DOWN).doubleValue();
            }
        }
        return estimatedDownPayment;
    }

    private Price getCartTotalPrice(final CartTotal cartTotal) {
        Price price = cartTotal.getDiscountedPrice();
        if (cartTotal.getDiscountedPrice() == null && !cartTotal.isDiscountApplied()) {
            price = cartTotal.getPrice();
        }
        return price;
    }

    private void setCartTotalDiscountInfo(final Cart shoppingCart, final Program program,
        final CurrencyUnit targetCurrency, final Map<String, CartPriceResultsDTO> cartPrices,
        final CartTotal cartTotal) {
        if (CollectionUtils.isNotEmpty(shoppingCart.getDiscounts())) {
            cartTotal.setDiscountedPrice(
                    getDiscountedPrice(getPrice(cartPrices, DISPLAY_PRICE, targetCurrency),
                            shoppingCart.getDiscounts()));
            cartTotal.setDiscountedItemsSubtotalPrice(
                    getDiscountedPrice(getPrice(cartPrices, DISPLAY_ITEM_PRICE, targetCurrency),
                            shoppingCart.getDiscounts()));
            cartTotal.setIsDiscountApplied(true);
            cartTotal.setDiscountAmount(shoppingCart.getTotalDiscountAmount());
        }
        if (program.getConfig().containsKey(DISCOUNTCODE_PER_ORDER) &&
                Objects.nonNull(program.getConfig().get(DISCOUNTCODE_PER_ORDER))) {
            cartTotal.setDiscountCodePerOrder(
                    Integer.parseInt(program.getConfig().get(DISCOUNTCODE_PER_ORDER).toString()));
        } else {
            cartTotal.setDiscountCodePerOrder(
                    Integer.parseInt(applicationProperties.getProperty("discountcode" + ".perOrder")));
        }
    }

    private void populateServiceFee(final CurrencyUnit targetCurrency, final ProductResponseDTO productResponseDTO,
                                    final Offer offer) {
        final BigDecimal b2sServiceFeeRate = productResponseDTO.getPricingParameters().
                getB2sServiceFeeRate().orElse(null);
        if (Objects.nonNull(b2sServiceFeeRate)) {
            LOGGER.debug("Value of b2sServiceFeeRate is {}", b2sServiceFeeRate);
            offer.setB2sServiceFeeRateBps(convertToBasisPoints(b2sServiceFeeRate));
        }
        offer.setB2sServiceFee(getUnitPrice(productResponseDTO.getPrices(), BRIDGE2_FEE, targetCurrency));
    }

    private Integer convertToBasisPoints(final BigDecimal rate) {
        return rate.multiply(BASIS_POINTS_MULTIPLIER).intValue();
    }

    private void populateTaxes(final ProductResponseDTO productResponseDTO, final CurrencyUnit targetCurrency,
                               final Offer offer, final String country) {
        if (productResponseDTO.getPrices().containsKey(DISPLAY_TAXES)) {
            offer.setSupplierTaxPrice(getUnitPrice(productResponseDTO.getPrices(), DISPLAY_TAXES, targetCurrency));
        }
        final Map<String, Tax> taxes = new HashMap<>();
        if (StringUtils.isNotBlank(country)) {
            for (final TaxValue taxValue : TaxValue.values()) {
                if (productResponseDTO.getPrices().containsKey(taxValue.displayName)) {
                    final Price price =
                            getUnitPrice(productResponseDTO.getPrices(), taxValue.displayName, targetCurrency);
                    final Tax tax = new Tax(taxValue.name(), price);
                    taxes.put(taxValue.name(), tax);
                }
            }
        }
        LOGGER.debug("Value of b2sServiceFeeRate is {}", productResponseDTO.getPricingParameters());
        offer.setTax(taxes);
    }

    private void populateUnpromotedTaxes(final ProductResponseDTO productResponseDTO, final CurrencyUnit targetCurrency,
                                         final Offer offer, final String country) {
        if (productResponseDTO.getPrices().containsKey(DISPLAY_TAXES)) {
            offer.setUnpromotedSupplierTaxPrice(
                    getUndiscountedUnitPrice(productResponseDTO.getPrices(), DISPLAY_TAXES, targetCurrency));
        }
        final Map<String, Tax> taxes = new HashMap<>();
        if (StringUtils.isNotBlank(country)) {
            for (final TaxValue taxValue : TaxValue.values()) {
                if (productResponseDTO.getPrices().containsKey(taxValue.displayName)) {
                    final Price price =
                            getUndiscountedUnitPrice(productResponseDTO.getPrices(), taxValue.displayName, targetCurrency);
                    final Tax tax = new Tax(taxValue.name(), price);
                    taxes.put(taxValue.name(), tax);
                }
            }
        }
        offer.setUnPromotedTax(taxes);
    }

    private void populateFees(final ProductResponseDTO productResponseDTO, final CurrencyUnit targetCurrency,
                              final Offer offer) {
        final Map<String, Fee> fees = new HashMap<>();
        for (final FeeValue feeValue : FeeValue.values()) {
            if (productResponseDTO.getPrices().containsKey(feeValue.displayName)) {
                final Price price = getUnitPrice(productResponseDTO.getPrices(), feeValue.displayName, targetCurrency);
                final Fee fee = new Fee(feeValue.name(), price);
                fees.put(feeValue.name(), fee);
            }
        }
        offer.setFees(fees);
    }

    private void populateUnpromotedFees(final ProductResponseDTO productResponseDTO, final CurrencyUnit targetCurrency,
                                        final Offer offer) {
        final Map<String, Fee> fees = new HashMap<>();
        for (final FeeValue feeComponent : FeeValue.values()) {
            if (productResponseDTO.getPrices().containsKey(feeComponent.displayName)) {
                final Price price =
                        getUndiscountedUnitPrice(productResponseDTO.getPrices(), feeComponent.displayName, targetCurrency);
                final Fee fee = new Fee(feeComponent.name(), price);
                fees.put(feeComponent.name(), fee);
            }
        }
        offer.setUnPromotedFees(fees);
    }

    private void populateRetailPriceInfo(final PricingParametersDTO pricingParametersDTO, final Offer offer) {
        offer.setRetailUnitBasePrice(pricingParametersDTO.getRetailUnitBasePrice().orElse(null));
        offer.setRetailUnitTaxPrice(pricingParametersDTO.getRetailUnitTaxPrice().orElse(null));
    }

    private Price getDiscountedPrice(final Price price, final List<DiscountCode> discounts) {
        Price newPrice = price;
        if (CollectionUtils.isNotEmpty(discounts)) {
            for (final DiscountCode discountCode : discounts) {
                if (discountCode != null && discountCode.getDiscountType().equalsIgnoreCase(DISCOUNT_TYPE_DOLLAR)) {
                    Double discountedAmount = BigDecimal.valueOf(newPrice.getAmount()).subtract(
                            BigDecimal.valueOf(discountCode.getDiscountAmount())).setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    final Double discountAmtInPoints =
                            discountCode.getDiscountAmount() * 100; // dollar amount to point/cents
                    int discountedPoints = new BigDecimal(newPrice.getPoints()).subtract(
                            BigDecimal.valueOf(discountAmtInPoints)).intValue();
                    if (discountedAmount < 0) {
                        discountedAmount = 0.0d;
                        discountedPoints = 0;
                    }
                    newPrice = new Price(discountedAmount, "", discountedPoints);
                }
            }
        }
        return newPrice;
    }

    private CartTotal getPromoCartTotal(final Cart shoppingCart, final Program program) {
        CartTotal promoCartTotal = null;
        // find the first cart item. This condition is application only for campaign/promo orders and campaign/promo
        // orders are single item orders
        final CartItem firstCartItem = shoppingCart.getCartItems().get(0);
        final Optional<PricingModel>
                pricingModelOptional =
                PricingUtil.getPricingModel(firstCartItem.getProductDetail(), program.getPricingModels());
        if (pricingModelOptional.isPresent()) {
            final PricingModel pricingModel = pricingModelOptional.get();
            promoCartTotal = new CartTotal();
            promoCartTotal.setItemsSubtotalPrice(
                    new Price(pricingModel.getPaymentValue(), program.getTargetCurrency().getCode(),
                            pricingModel.getPaymentValuePoints()));
            promoCartTotal.setPrice(
                    new Price(pricingModel.getPaymentValue(), program.getTargetCurrency().getCode(),
                            pricingModel.getPaymentValuePoints()));
            promoCartTotal.setShippingPrice(shoppingCart.getCartTotal().getShippingPrice());
        }
        return promoCartTotal;
    }

    private Optional<ProductResponseDTO> findProductByIndex(final Integer index, final List<ProductResponseDTO> products) {
        return products.stream().filter(p -> p.getIndex() == index).findFirst();
    }

    private Product findProductByPsid(final String sku, final Cart shoppingCart) {
        Product product = null;
        for (final CartItem shoppingCartItem : shoppingCart.getCartItems()) {
            if (sku.equals(shoppingCartItem.getProductDetail().getSku())) {
                product = shoppingCartItem.getProductDetail();
            }
        }
        return product;
    }
}