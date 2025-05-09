package com.b2s.common.services.pricing.impl;

import com.b2s.apple.mapper.PricingServiceRequestMapperV2;
import com.b2s.apple.mapper.PricingServiceResponseMapperV2;
import com.b2s.apple.model.CartPricingRequestDTO;
import com.b2s.apple.model.CartPricingResponseDTO;
import com.b2s.apple.model.ConversionRatesDTO;
import com.b2s.apple.model.SplitTenderResultDTO;
import com.b2s.apple.services.CartPricingRestApi;
import com.b2s.rewards.apple.integration.model.PaymentOptions;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.apple.validator.AddressMapper;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.b2s.common.services.exception.ServiceExceptionEnums.PRICING_SERVICE_ADDRESS_ERROR_STR;
import static com.b2s.service.product.common.domain.attributes.CommonAttributes.POINTS_CURRENCY_UNIT;

@Service("LocalPricingServiceV2")
public class LocalPricingServiceV2<P, C>{

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalPricingServiceV2.class);

    @Autowired
    protected CartPricingRestApi pricingClient;

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    private PricingServiceRequestMapperV2 pricingServiceRequestMapper;

    @Autowired
    private PricingServiceResponseMapperV2 pricingServiceResponseMapper;

    @Autowired
    private HttpSession httpSession;

    public void calculateCartPrice(final C cart, final User user, final Program program) {
        Integer addPoints = calculateAddPoints(cart, program);
        calculateCartPrice(cart, user, program, addPoints);
        if(cart != null && cart instanceof Cart && ((Cart) cart).getDisplayCartTotal()!=null && addPoints==null){
            //Updating the default addPoints based on Cart Total Difference.
            final Cart appleCart = ((Cart) cart);
            final CartTotal cartTotal = appleCart.getDisplayCartTotal();
            int currentCartTotalPoints = cartTotal.getPrice().getPoints();
            if(cartTotal.isDiscountApplied()){
                currentCartTotalPoints = cartTotal.getDiscountedPrice().getPoints();
            }
            //cash option use the totalpoints to newAddpoints
            //This should cover recalculation of the addPoints and Cost, which controlls amount to be paid by
            // creditcard.
            int newAddPoints = 0;
                newAddPoints = getRedemptionAddPoints(appleCart, currentCartTotalPoints, user, program);
            if (newAddPoints > 0) {
                calculateCartPrice(cart, user, program, newAddPoints);
            }
        }
    }

    private Integer calculateAddPoints(final C cart, final Program program) {
        Integer addPoints = null;
        if(cart != null && cart instanceof Cart && ((Cart) cart).getAddPoints() > 0) {
            // Adding this condition because only CC slider will have user selected add points
            // and need to get already selected from cart, if any. In CC Fixed, system generates add points
            // So this check will force system to recalculate add points, in case of any cart changes like address
            // change etc
                if(Objects.nonNull(program) && MapUtils.isNotEmpty(program.getRedemptionOptions()) &&
                    program.getRedemptionOptions().containsKey(PaymentOptions.SPLITPAY.getPaymentOption())){
                    addPoints = ((Cart) cart).getAddPoints();
                }
        }
        return addPoints;
    }

    public void calculateCartPrice(final C cart, final User user, final Program program, final Integer addPoints) {
        LOGGER.debug("Start calculateCartPrice.");
        final boolean showEarnPoints = (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_EARN_POINTS, Boolean.FALSE);

        if (isCartEmpty(cart)) {
            LOGGER.info("Price not Calculated. Cart is empty.");
        } else {
            final Cart appleCart = (Cart) cart;
            appleCart.setMaxCartTotalExceeded(false);
            Boolean hasShippingAddress = false;
            hasShippingAddress = setAppleCartShippingAddressIsValid(program, appleCart, hasShippingAddress);

            final CartPricingRequestDTO
                cartRequest = pricingServiceRequestMapper.from((Cart) cart, program, hasShippingAddress, user, addPoints);

            httpSession.setAttribute(CommonConstants.CART_REQUEST_SESSION_OBJECT, cartRequest);
            LOGGER.info("cartRequest: {}", cartRequest);

            CartPricingResponseDTO cartResponse =
                getCartPricingWithExceptionHandling((Cart) cart, user, program, addPoints, appleCart,
                    cartRequest);

            httpSession.setAttribute(CommonConstants.CART_RESPONSE_SESSION_OBJECT, cartResponse);
            LOGGER.debug("cartResponse: {}", cartResponse);

            if (cartResponse != null) {
                pricingServiceResponseMapper.populateCartPrices(cartResponse, user, (Cart) cart, program);

                addConversionRateToUserAdditionalInfo(user, cartResponse);

                setCartTotalIsActual(appleCart, hasShippingAddress);

                if (CollectionUtils.isNotEmpty(appleCart.getDiscounts())) {
                    appleCart.getCartTotal().setIsDiscountApplied(true);
                }

                cartResponse.getSplitTenderResult().ifPresent(splitTenderResultDTO -> {
                    appleCart.setPointPurchaseRate(splitTenderResultDTO.getPointPurchaseRate());
                    setAppleCartAddPoints(user, program, appleCart, splitTenderResultDTO);
                    setAppleCartEarnPoints(showEarnPoints, appleCart, splitTenderResultDTO);
                    final double gstAmount = splitTenderResultDTO.getCcTaxAmount().isPresent() ?
                            splitTenderResultDTO.getCcTaxAmount().get().getAmount().doubleValue() : 0;
                    appleCart.setGstAmount(gstAmount);

                    setAppleCartCost(appleCart, splitTenderResultDTO);

                    CreditItem creditItem = appleCart.getCreditItem();
                    if (Objects.isNull(creditItem)) {
                        creditItem = new CreditItem();
                        appleCart.setCreditItem(creditItem);
                    }

                    creditItem.setB2sProfit(asDouble(splitTenderResultDTO.getCcB2sProfit()));
                    creditItem.setVarProfit(asDouble(splitTenderResultDTO.getCcVarProfit()));
                    creditItem.setB2sMargin(splitTenderResultDTO.getCcB2sMargin().doubleValue());
                    creditItem.setVarMargin(splitTenderResultDTO.getCcVarMargin().doubleValue());
                    creditItem.setCurrency(program.getTargetCurrency());
                    creditItem.setBaseItemPrice(asDouble(splitTenderResultDTO.getPreCCMarkupAmount()));

                    creditItem.setCcVarMargin(splitTenderResultDTO.getCcVarMargin());
                    creditItem.setVarPrice(splitTenderResultDTO.getCcVarPrice());
                    creditItem.setPointsPurchased(splitTenderResultDTO.getCcBuyInPoints());
                    creditItem.setEffectiveConversionRate(splitTenderResultDTO.getEffectiveConversionRate());

                    appleCart.setSupplementaryPaymentLimit(
                            getSupplementaryPaymentLimit(appleCart, splitTenderResultDTO, program));

                    appleCart.setRedemptionPaymentLimit(
                            getRedemptionPaymentLimit(appleCart, splitTenderResultDTO, user, program));
                });
                setAppleCartPointsInformation(user, appleCart);
                appleCart.setCcPayment(appleCart.getCost());
                LOGGER.info("Price Calculated.");
            }
        }
        LOGGER.debug("Leaving calculateCartPrice.");
    }

    private Boolean setAppleCartShippingAddressIsValid(final Program program, final Cart appleCart,
        Boolean hasShippingAddress) {
        if (appleCart.getShippingAddress() != null) {
            hasShippingAddress = appleCart.getShippingAddress().canCalculateWithFeesAndTaxes(AddressMapper.isAddressCheckNeeded(program))
                && StringUtils.isNotEmpty(appleCart.getShippingAddress().getFirstName())
                && StringUtils.isNotEmpty(appleCart.getShippingAddress().getLastName());
            appleCart.getShippingAddress().setIsValidAddress(hasShippingAddress);
        }
        return hasShippingAddress;
    }

    private void addConversionRateToUserAdditionalInfo(final User user, final CartPricingResponseDTO cartResponse) {
        if (Objects.nonNull(cartResponse.getProducts()) && !cartResponse.getProducts().isEmpty()) {
            final ConversionRatesDTO conversionRatesDTO = cartResponse.getProducts().get(0).getConversionRates().orElse(null);
            if (Objects.nonNull(conversionRatesDTO) && MapUtils.isNotEmpty(conversionRatesDTO.getFxRates()) &&
                    conversionRatesDTO.getFxRates()
                            .containsKey(POINTS_CURRENCY_UNIT)) {
                user.getAdditionalInfo().put(CommonConstants.CONVERSION_RATE,
                        conversionRatesDTO.getFxRates().get(POINTS_CURRENCY_UNIT)
                                .getInverseRate().toString());
            }
        }
    }

    private void setAppleCartPointsInformation(final User user, final Cart appleCart) {
        if(appleCart.getCartTotal() != null) {
            if (appleCart.getAddPoints() > 0) {
                appleCart.setPointsPayment(appleCart.getCartTotal().getPrice().getPoints() - appleCart.getAddPoints());
            }
            else {
                appleCart.setPointsPayment(appleCart.getCartTotal().getPrice().getPoints());
            }
            if(user.getBalance() > appleCart.getPointsPayment()) {
                appleCart.setPointsBalance(user.getBalance() - appleCart.getPointsPayment());
            }
            else {
                appleCart.setPointsBalance(appleCart.getPointsPayment() - appleCart.getCartTotal().getPrice().getPoints());
            }
        }
    }

    private void setAppleCartCost(final Cart appleCart, final SplitTenderResultDTO splitTenderResultDTO) {
        if (splitTenderResultDTO.getCcBuyInAmount().isEqual(splitTenderResultDTO.getPreCCMarkupAmount())) {
            appleCart.setCost(asDouble(splitTenderResultDTO.getCcBuyInAmount()));
        } else {
            appleCart.setCost(splitTenderResultDTO.getPreCCMarkupAmount().getAmount()
                .add(splitTenderResultDTO.getCcB2sProfit().getAmount())
                .add(splitTenderResultDTO.getCcVarProfit().getAmount())
                .doubleValue());
        }
    }

    private void setAppleCartEarnPoints(final boolean showEarnPoints, final Cart appleCart,
        final SplitTenderResultDTO splitTenderResultDTO) {
        if(showEarnPoints){
            appleCart.setEarnPoints(splitTenderResultDTO.getEarnedPoints().orElse(0));
        }
    }

    private void setAppleCartAddPoints(final User user, final Program program, final Cart appleCart,
        final SplitTenderResultDTO splitTenderResultDTO) {
        if (MapUtils.isNotEmpty(program.getRedemptionOptions()) && program.getRedemptionOptions().containsKey(
            PaymentOptions.POINTSFIXED.getPaymentOption()) &&
            appleCart.getCartTotal().getPrice().getPoints() > user.getBalance()) {
            appleCart.setAddPoints(appleCart.getCartTotal().getPrice().getPoints() - user.getBalance());
        }
        else {
            appleCart.setAddPoints(splitTenderResultDTO.getCcBuyInPoints());
        }
    }

    private void setCartTotalIsActual(final Cart appleCart, final Boolean hasShippingAddress) {
        if (appleCart.getCartTotal() != null && hasShippingAddress) {
            appleCart.getCartTotal().setIsActual(true);
        }
    }

    private CartPricingResponseDTO getCartPricingWithExceptionHandling(final Cart cart, final User user,
        final Program program, final Integer addPoints, final Cart appleCart, final CartPricingRequestDTO cartRequest) {
        CartPricingResponseDTO cartResponse = null;
        try {
            cartResponse = pricingClient.getPrice(cartRequest);
        } catch ( final Exception ex) {
            // If Pricing Service Address Not Found Exception
            LOGGER.error("Get Price Failed! for the User: {} VarId: {} ProgramId: {}",user.getUserId(), user.getVarId(),user.getProgramId());
            if (StringUtils.containsIgnoreCase(ex.getMessage(), PRICING_SERVICE_ADDRESS_ERROR_STR)) {
                LOGGER.error("Cart Pricing failed with Invalid address error ! for the User: {} VarId: {} ProgramId: {}",user.getUserId(), user.getVarId(),user.getProgramId());
                appleCart.getShippingAddress().setIsValidAddress(false);
                appleCart.getShippingAddress().getErrorMessage().put(CartItemOption.SHIPPING_ADDRESS.getValue(), messageSource.getMessage(
                    CommonConstants.INVALID_ADDRESS_ERROR_MSG_KEY, null, CommonConstants.INVALID_ADDRESS_ERROR_DEFAULT_MSG, user.getLocale()));
                final CartPricingRequestDTO cRequest = pricingServiceRequestMapper.from(cart, program, false, user, addPoints);
                cartResponse = pricingClient.getPrice(cRequest);
            } else if (ex.getMessage().contains("is above the allowable maximum")) {
                LOGGER.info("The cart price is above the allowable maximum limit.");
                appleCart.setMaxCartTotalExceeded(true);
            } else {
                throw ex;
            }
        }
        return cartResponse;
    }

    private int getRedemptionAddPoints(final Cart cart, final int currentCartTotalPoints, final User user,
        final Program program) {
        int addPoints = 0;
        if(StringUtils.isNotBlank(cart.getSelectedRedemptionOption())) {
            if(PaymentOptions.CASHONLY.getPaymentOption().equalsIgnoreCase(cart.getSelectedRedemptionOption())) {
                addPoints = currentCartTotalPoints;
            } else if(PaymentOptions.POINTSONLY.getPaymentOption().equalsIgnoreCase(cart.getSelectedRedemptionOption())
                || PaymentOptions.POINTSFIXED.getPaymentOption().equalsIgnoreCase(cart.getSelectedRedemptionOption())
                || PaymentOptions.SPLITPAY.getPaymentOption().equalsIgnoreCase(cart.getSelectedRedemptionOption())
                || PaymentOptions.PAYROLL_DEDUCTION.getPaymentOption().equalsIgnoreCase(cart.getSelectedRedemptionOption())) {
                addPoints = (currentCartTotalPoints - user.getBalance());
            }
        }
        return addPoints;
    }

    private boolean isCartEmpty(final C cart) {
        if (Objects.isNull(cart)) {
            return true;
        }
        if(cart instanceof Cart){
            final Cart appleCart = (Cart) cart;
            return CollectionUtils.isEmpty(appleCart.getCartItems());
        }else{
            return true;
        }
    }

    private double asDouble(final Money amount) {
        return amount.getAmount().doubleValue();
    }

    private SupplementaryPaymentLimit getSupplementaryPaymentLimit(final Cart appleCart,
        final SplitTenderResultDTO splitTenderResultDTO, final Program program) {
        final SupplementaryPaymentLimit supplementaryPaymentLimitValue = new SupplementaryPaymentLimit();
        final Optional<Integer> ccBuyInPointsLimit = splitTenderResultDTO.getCcBuyInPointsLimit();
        final Optional<Money> ccBuyInAmountLimit = splitTenderResultDTO.getCcBuyInAmountLimit();

        if (ccBuyInPointsLimit.isPresent() && ccBuyInAmountLimit.isPresent()) {
            final Price cartTotalPrice = appleCart.getCartTotal().getPrice();
            final double ccBuyInAmountLimitValue = asDouble(ccBuyInAmountLimit.get());

            final Price paymentMaxLimitPrice =
                new Price(ccBuyInAmountLimitValue, cartTotalPrice.getCurrencyCode(), ccBuyInPointsLimit.get());
            supplementaryPaymentLimitValue.setPaymentMaxLimit(paymentMaxLimitPrice);
            Price paymentMinLimitPrice = null;
            if (Objects.nonNull(program.getConfig().get(CommonConstants.REWARD_MIN_LIMIT))) {
                final Double minPrice = new BigDecimal(
                    Integer.parseInt(program.getConfig().get(CommonConstants.REWARD_MIN_LIMIT).toString()))
                    .divide(BigDecimal.valueOf(appleCart.getConvRate())).doubleValue();
                paymentMinLimitPrice = new Price(minPrice, cartTotalPrice.getCurrencyCode(),
                    Integer.parseInt(program.getConfig().get(CommonConstants.REWARD_MIN_LIMIT).toString()));
            } else {
                paymentMinLimitPrice =
                    new Price(cartTotalPrice.getAmount() - ccBuyInAmountLimitValue, cartTotalPrice.getCurrencyCode(),
                        cartTotalPrice.getPoints() - ccBuyInPointsLimit.get());
            }

            supplementaryPaymentLimitValue.setRewardsMinLimit(paymentMinLimitPrice);
        } else if (onlyOneExistOfTwo(ccBuyInPointsLimit, ccBuyInAmountLimit)) {
            LOGGER.warn(
                "Only one one between ccBuyInPointsLimit - {} and ccBuyInAmountLimit - {} got in Pricing response.",
                ccBuyInPointsLimit, ccBuyInAmountLimit);
        }
        return supplementaryPaymentLimitValue;
    }

    private RedemptionPaymentLimit getRedemptionPaymentLimit(final Cart appleCart,
        final SplitTenderResultDTO splitTenderResultDTO, final User user, final Program program) {

        final Optional<Integer> ccBuyInPointsLimit = splitTenderResultDTO.getCcBuyInPointsLimit();
        final Optional<Money> ccBuyInAmountLimit = splitTenderResultDTO.getCcBuyInAmountLimit();

        Map<String, List<VarProgramRedemptionOption>> paymentOptions=program.getRedemptionOptions();
        RedemptionPaymentLimit.Builder builder=RedemptionPaymentLimit.builder();

        if (ccBuyInPointsLimit.isPresent() && ccBuyInAmountLimit.isPresent()) {
            final Price cartTotalPrice = appleCart.getCartTotal().getPrice();
            final double ccBuyInAmountLimitValue = asDouble(ccBuyInAmountLimit.get());

            VarProgramRedemptionOption option = getPaymentPointsOption(paymentOptions);
            // Points Maximum limit
            Price maxPointsLimit=getPointsMaxLimit(appleCart, user, ccBuyInPointsLimit.get(), option);

            // Points minimum limit
            int minPoint = cartTotalPrice.getPoints() - ccBuyInPointsLimit.get();
            if (Objects.nonNull(option) && PaymentOptions.POINTSFIXED.getPaymentOption().equals(option.getPaymentOption())) {
                if(cartTotalPrice.getPoints() >= user.getBalance() && user.getBalance() > minPoint) {
                    minPoint = user.getBalance();
                }
                else if(cartTotalPrice.getPoints() < user.getBalance()){
                    minPoint = cartTotalPrice.getPoints();
                }
            }
            Price minPointsLimit = new Price(minPoint / appleCart.getConvRate(),
                cartTotalPrice.getCurrencyCode(), minPoint);
            // Cash maximum limit
            Price maxCashLimit=new Price(ccBuyInAmountLimitValue, cartTotalPrice.getCurrencyCode(),
                ccBuyInPointsLimit.get());
            // Cash minimum limit
            Price minCashLimit=getCashMinLimit(paymentOptions, appleCart,splitTenderResultDTO);


            // useMaxPoints
            Price useMaxPoints=
                new Price(new BigDecimal(cartTotalPrice.getPoints()-maxPointsLimit.getPoints()).multiply(appleCart.getPointPurchaseRate()).doubleValue(),
                    cartTotalPrice.getCurrencyCode(),maxPointsLimit.getPoints());

            //useMinPoints
            Price useMinPoints=new Price(new BigDecimal(cartTotalPrice.getPoints()-minPoint).multiply(appleCart.getPointPurchaseRate()).doubleValue(),
                cartTotalPrice.getCurrencyCode(),minPoint);

            Price cartMaxLimit = getCartMaxLimit(paymentOptions, user, appleCart);

            builder.withPointsMaxLimit(maxPointsLimit)
                .withPointsMinLimit(minPointsLimit)
                .withCashMaxLimit(maxCashLimit)
                .withCashMinLimit(minCashLimit)
                .withUseMaxPoints(useMaxPoints)
                .withUseMinPoints(useMinPoints)
                .withCartMaxLimit(cartMaxLimit);

        } else if (onlyOneExistOfTwo(ccBuyInPointsLimit, ccBuyInAmountLimit)) {
            LOGGER.warn(
                "Only one one between ccBuyInPointsLimit - {} and ccBuyInAmountLimit - {} got in Pricing response.",
                ccBuyInPointsLimit, ccBuyInAmountLimit);
        }
        return builder.build();
    }

    private VarProgramRedemptionOption getPaymentPointsOption(
        final Map<String, List<VarProgramRedemptionOption>> paymentOptions) {
        VarProgramRedemptionOption option=null;
        // Points max restriction is not an existing functionality.
        // Hence we dot not support limit by percent / dollars during the initial migration
        if(MapUtils.isNotEmpty(paymentOptions)) {
            if (paymentOptions.containsKey(PaymentOptions.POINTSONLY.getPaymentOption()))
                option = paymentOptions.get(PaymentOptions.POINTSONLY.getPaymentOption()).get(0);
            if (paymentOptions.containsKey(PaymentOptions.POINTSFIXED.getPaymentOption()))
                option = paymentOptions.get(PaymentOptions.POINTSFIXED.getPaymentOption()).get(0);
        }
        return option;
    }

    private boolean onlyOneExistOfTwo(final Optional<Integer> ccBuyInPointsLimit,
        final Optional<Money> ccBuyInAmountLimit) {
        return (ccBuyInPointsLimit.isPresent() && !ccBuyInAmountLimit.isPresent()) ||
            (!ccBuyInPointsLimit.isPresent() && ccBuyInAmountLimit.isPresent());
    }

    private Price getCashMinLimit(Map<String, List<VarProgramRedemptionOption>> paymentOptions,
        final Cart appleCart, final SplitTenderResultDTO splitTenderResultDTO){

        Price cartTotalPrice=appleCart.getCartTotal().getPrice();
        final AtomicReference<Price> price = new AtomicReference<Price>();

        if(MapUtils.isNotEmpty(paymentOptions) && CollectionUtils.isNotEmpty(paymentOptions.get(PaymentOptions.SPLITPAY.getPaymentOption()))){
            paymentOptions.get(PaymentOptions.SPLITPAY.getPaymentOption()).stream().forEach(varProgramRedemptionOption -> {
                if(varProgramRedemptionOption.getPaymentMinLimit()>0) {
                    if (varProgramRedemptionOption.getLimitType().equalsIgnoreCase(CommonConstants.PERCENTAGE)) {
                        price.set(new Price(
                            cartTotalPrice.getAmount() * (varProgramRedemptionOption.getPaymentMinLimit().doubleValue() / 100),
                            cartTotalPrice.getCurrencyCode(),
                            cartTotalPrice.getPoints() * (varProgramRedemptionOption.getPaymentMinLimit() / 100)));
                    } else {
                        price.set(new Price(varProgramRedemptionOption.getPaymentMinLimit().doubleValue(),
                            cartTotalPrice.getCurrencyCode(),
                            varProgramRedemptionOption.getPaymentMinLimit().intValue() *
                                splitTenderResultDTO.getPointPurchaseRate().intValue()));
                    }
                }
            });

            if(Objects.isNull(price.get())){
                price.set(new Price(0d,cartTotalPrice.getCurrencyCode(),0));
            }

        }

        return price.get();
    }

    private Price getPointsMaxLimit(final Cart appleCart, final User user, final Integer ccBuyInPointsLimit, final VarProgramRedemptionOption option){
        int maxPoints=0;
        int minPoints=0;
        Price cartTotal=appleCart.getCartTotal().getPrice();

        if (Objects.nonNull(option)) {
            minPoints = cartTotal.getPoints() - ccBuyInPointsLimit;
            if (PaymentOptions.POINTSFIXED.getPaymentOption().equals(option.getPaymentOption())) {
                maxPoints = getMaxPointsInCaseOfPointsFixed(user, minPoints, cartTotal);
            } else {
                if (user.getBalance() >= cartTotal.getPoints()) {
                    maxPoints = cartTotal.getPoints();

                } else {
                    maxPoints = user.getBalance();
                }

                if (minPoints > maxPoints) {
                    maxPoints = minPoints;
                }
            }

        }
        return new Price((maxPoints/appleCart.getConvRate()),cartTotal.getCurrencyCode(),maxPoints);
    }

    private int getMaxPointsInCaseOfPointsFixed(final User user, final int minPoints, final Price cartTotal) {
        final int maxPoints;
        if(cartTotal.getPoints() >= user.getBalance() && user.getBalance() > minPoints) {
            maxPoints = user.getBalance();
        }
        else if(cartTotal.getPoints() > user.getBalance() && user.getBalance() < minPoints) {
            maxPoints = minPoints;
        }
        else {
            maxPoints = cartTotal.getPoints();
        }
        return maxPoints;
    }

    private Price getCartMaxLimit(final Map<String, List<VarProgramRedemptionOption>> paymentOptions, final User user,
        final Cart appleCart){
        final int userBalance = user.getBalance();
        final Price cartTotalPrice = appleCart.getCartTotal().getPrice();
        VarProgramRedemptionOption option=null;

        final AtomicReference<Price> price = new AtomicReference<Price>();
        if(MapUtils.isNotEmpty(paymentOptions) && (CollectionUtils.isNotEmpty(paymentOptions.get(PaymentOptions.POINTSONLY.getPaymentOption())) ||
            CollectionUtils.isNotEmpty(paymentOptions.get(PaymentOptions.POINTSFIXED.getPaymentOption())))){

            if(paymentOptions.containsKey(PaymentOptions.POINTSONLY.getPaymentOption())) {
                option = paymentOptions.get(PaymentOptions.POINTSONLY.getPaymentOption()).get(0);
            }
            if(paymentOptions.containsKey(PaymentOptions.POINTSFIXED.getPaymentOption())) {
                option = paymentOptions.get(PaymentOptions.POINTSFIXED.getPaymentOption()).get(0);
            }

            if(Objects.nonNull(option) && option.getPaymentMinLimit()>0 && cartTotalPrice.getPoints()>userBalance){
                final Integer cartMaxLimit = (userBalance* 100) / option.getPaymentMinLimit() ;
                price.set(new Price(0d, cartTotalPrice.getCurrencyCode(), cartMaxLimit));
                return price.get();
            }
        }
        return null;
    }

}
