package com.b2s.apple.mapper;

import com.b2s.apple.model.*;
import com.b2s.rewards.apple.model.Price;
import com.b2s.rewards.apple.model.Program;
import org.apache.axis.utils.StringUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.b2s.rewards.common.util.CommonConstants.*;

/**
 * @author rkumar 2019-09-11
 */
public class PricingMapperV2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(PricingMapperV2.class);
    public static final String PRODUCT_RESPONSE_DOES_NOT_HAVE_THE_PRICE_VALUE_FOR =
        "Product response does not have the price value for {}";

    //CartPricingResponseDTO
    protected Price getPrice(final Map<String, CartPriceResultsDTO> cartPrices, final String pricingType,
        final CurrencyUnit targetCurrency) {
        if (cartPrices.containsKey(pricingType)) {
            final CartPriceResultsDTO cartPriceResultsDTO = cartPrices.get(pricingType);
            final Map<CurrencyUnit, CartCurrencyAmountDTO> currencies = cartPriceResultsDTO.getCurrencies();
            if (currencies.containsKey(targetCurrency)) {
                return new Price(currencies.get(targetCurrency).getTotalPrice(),
                    cartPriceResultsDTO.getTotalPoints().orElse(null));
            }
        }
        LOGGER.info("Cart response does not have the price value for {}", pricingType);
        return new Price();
    }

    //ProductResponseDTO
    protected Price getUnitPrice(final Map<String, PriceResultsDTO> productPrices, final String pricingType,
        final CurrencyUnit targetCurrency) {
        if (productPrices.containsKey(pricingType)) {
            final PriceResultsDTO priceResultsDTO = productPrices.get(pricingType);
            final Map<CurrencyUnit, CurrencyAmountDTO> currencies = priceResultsDTO.getCurrencies();
            if (currencies.containsKey(targetCurrency)) {
                return new Price(currencies.get(targetCurrency).getUnitPrice(),
                        priceResultsDTO.getUnitPoints().orElse(null));
            }
        }
        LOGGER.info(PRODUCT_RESPONSE_DOES_NOT_HAVE_THE_PRICE_VALUE_FOR, pricingType);
        return new Price();
    }

    protected Price getProductPrice(final Map<String, PriceResultsDTO> productPrices, final String pricingType,
        final CurrencyUnit targetCurrency) {
        if (productPrices.containsKey(pricingType)) {
            final PriceResultsDTO priceResultsDTO = productPrices.get(pricingType);
            final Map<CurrencyUnit, CurrencyAmountDTO> currencies = priceResultsDTO.getCurrencies();
            if (currencies.containsKey(targetCurrency)) {
                return new Price(currencies.get(targetCurrency).getTotalPrice(),
                    priceResultsDTO.getTotalPoints().orElse(null));
            }
        }
        LOGGER.info(PRODUCT_RESPONSE_DOES_NOT_HAVE_THE_PRICE_VALUE_FOR, pricingType);
        return new Price();
    }

    //unpromotedUnitPrice
    protected Price getUndiscountedUnitPrice(final Map<String, PriceResultsDTO> productPrices, final String pricingType,
        final CurrencyUnit targetCurrency) {
        if (productPrices.containsKey(pricingType)) {
            final PriceResultsDTO priceResultsDTO = productPrices.get(pricingType);
            final Map<CurrencyUnit, CurrencyAmountDTO> currencies = priceResultsDTO.getCurrencies();
            if (currencies.containsKey(targetCurrency)) {
                return new Price(currencies.get(targetCurrency).getUnpromotedUnitPrice(),
                    priceResultsDTO.getUnpromotedUnitPoints().orElse(null));
            }
        }
        LOGGER.info(PRODUCT_RESPONSE_DOES_NOT_HAVE_THE_PRICE_VALUE_FOR, pricingType);
        return new Price();
    }

    //establishmentFees
    protected Price getEstablishmentPrice(final Program program, final CurrencyUnit targetCurrency) {
        final Optional<Object> establishmentFeesPrice =
            Optional.ofNullable(program.getConfig().get(ESTABLISHMENT_FEES_PRICE));
        final Optional<Object> establishmentFeesPoints =
            Optional.ofNullable(program.getConfig().get(ESTABLISHMENT_FEES_POINTS));

        if (establishmentFeesPrice.isPresent() && establishmentFeesPoints.isPresent()) {
            return new Price(Money.of(targetCurrency, Double.parseDouble(establishmentFeesPrice.get().toString())),
                Integer.parseInt(establishmentFeesPoints.get().toString()));
        }
        return establishmentFeesPrice
            .map(price -> new Price(Money.of(targetCurrency, Double.parseDouble(price.toString())), 0))
            .orElseGet(() -> establishmentFeesPoints
                .map(points -> new Price(Money.zero(targetCurrency), Integer.parseInt(points.toString())))
                .orElseGet(Price::new));
    }

    protected DeliveryMethodValue retrieveDeliveryMethod(final String shippingMethod) {
        if(StringUtils.isEmpty(shippingMethod)){
            return DeliveryMethodValue.DOMESTIC_SHIPPING;
        }
        switch (shippingMethod) {
            case GIFTCARDS_SHIPPINGMETHOD_TYPE:
                return DeliveryMethodValue.EMAIL;
            default:
                return DeliveryMethodValue.DOMESTIC_SHIPPING;
        }
    }
}
