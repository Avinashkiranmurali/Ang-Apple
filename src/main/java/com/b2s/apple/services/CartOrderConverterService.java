package com.b2s.apple.services;

import com.b2s.db.model.*;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.model.NotificationType;
import com.b2s.rewards.model.OrderSource;
import com.b2s.rewards.model.ShippingMethod;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.CreditTransactionManager;
import com.b2s.shop.common.order.DiscountCodeTransactionManager;
import com.b2s.shop.common.order.SubscriptionManager;
import com.b2s.shop.common.order.var.UserChase;
import com.b2s.shop.util.USER_MSG;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.b2s.rewards.common.util.CommonConstants.*;

@Service
public class CartOrderConverterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartOrderConverterService.class);

    //engraving attributes
    public static final String ENGRAVING_CODE   = "engravingCode";
    public static final String ENGRAVING_LINE_1 = "engravingLine1";
    public static final String ENGRAVING_LINE_2 = "engravingLine2";
    //gift message attributes
    public static final String GIFT_MESSAGE_1 = "giftMessage1";
    public static final String GIFT_MESSAGE_2 = "giftMessage2";
    public static final String GIFT_MESSAGE_3 = "giftMessage3";
    public static final String GIFT_MESSAGE_4 = "giftMessage4";
    public static final String GIFT_MESSAGE_5 = "giftMessage5";

    //post order constants
    private static final String SUPPLIER_ID = "supplierID";
    private static final String PRODUCT_ID = "productID";
    private static final String ACTIVATION_FEE = "activationFee";
    private static final String UPGRADE_FEE = "upgradeFee";
    private static final String SUBSIDY_AMOUNT = "subsidyAmount";
    private static final String SUBSIDY_TYPE = "subsidyType";
    private static final String MAX_REPAY = "maxRepay";
    private static final String MAXIMUM_MONTHLY_REPAY = "maximumMonthlyRepay";
    private static final String DURATION = "duration";
    private static final String DURATION_TYPE = "durationType";
    private static final String PURCHASE_REFERENCE_TYPE = "purchaseReferenceType";
    private static final String PURCHASE_REF_TYPE = "purchaseRefType";
    private static final String TENANT_ID = "tenantId";
    private static final String TOTAL_FINANCE_AMOUNT = "totalFinanceAmount";
    private static final String DISCOUNT_TIER1 = "discountTier1";
    private static final String DISCOUNT_TIER2 = "discountTier2";
    private static final String DISCOUNT_TIER3 = "discountTier3";

    private static final int CONVERT_DOLLAR_TO_CENTS = 100;


    @Autowired
    private MessageService messageService;

    @Autowired
    private SubscriptionManager subscriptionManager;

    public Order convert(final Cart cart, final User user, final Properties applicationProperties, final Program program) {
        if (LOGGER.isDebugEnabled()) { LOGGER.debug("Converting ShoppingCart to Order"); }

        final List<CartItem> cartItems = cart.getCartItems();

        UserVarProgramCreditAdds userVarProgramCreditAdds = null;
        if (Objects.nonNull(cart.getCreditLineItem())) {
            cartItems.add(cart.getCreditLineItem());

            final CreditItem creditItem = cart.getCreditItem();
            if (Objects.nonNull(creditItem)) {
                userVarProgramCreditAdds = UserVarProgramCreditAdds.builder()
                        .withCcVarMargin(creditItem.getCcVarMargin().floatValue())
                        .withCcVarPrice(creditItem.getVarPrice())
                        .withCcVarProfit(Money.of(program.getTargetCurrency(), creditItem.getVarProfit()))
                        .withPointsPurchased(creditItem.getPointsPurchased())
                        .withEffectiveConversionRate(Money.of(program.getTargetCurrency(), creditItem.getEffectiveConversionRate(), RoundingMode.HALF_UP))
                        .build();
            }
        }

        final String shopExperience = (String) program.getConfig().get(CommonConstants.SHOP_EXPERIENCE);
        final Order order = CommonConstants.EXPERIENCE_DRP.equalsIgnoreCase(shopExperience) ? new OrderAWP() : new Order();

        order.setGstAmount(cart.getGstAmount());
        setEarnPoints(order, cart, program);

        final Price establishmentFees = cart.getCartTotal().getEstablishmentFees();
        if (Objects.nonNull(establishmentFees)) {
            order.setEstablishmentFeesPoints(establishmentFees.getPoints());
            order.setEstablishmentFeesPrice(establishmentFees.getAmount());
        }

        order.setOrderDate(new Date());
        order.setIgnoreSuggestedAddress(cart.getIgnoreSuggestedAddress());
        order.setProgramId(user.getProgramId());
        order.setSupplierId(String.valueOf(user.getSupplierId()));

        setUserId(order, cart, user);
        order.setProxyUserId(user.getProxyUserId());
        order.setUserPoints(user.getBalance());
        order.setVarId(user.getVarId());
        order.setIpAddress(user.getIPAddress());

        final Address shippingAddress = cart.getShippingAddress();
        order.setFirstname(AppleUtil.decodeSpecialChar(shippingAddress.getFirstName()));
        order.setLastname(AppleUtil.decodeSpecialChar(shippingAddress.getLastName()));
        order.setAddr1(AppleUtil.decodeSpecialChar(shippingAddress.getAddress1()));
        order.setAddr2(AppleUtil.decodeSpecialChar(shippingAddress.getAddress2()));
        setAddr3(order, shippingAddress);

        order.setBusinessName(AppleUtil.decodeSpecialChar(shippingAddress.getBusinessName()));
        order.setCity(AppleUtil.decodeSpecialChar(shippingAddress.getCity()));
        order.setState(shippingAddress.getState());
        order.setCountry(shippingAddress.getCountry());
        setZipCode(order, shippingAddress);

        order.setPhone(PhoneNumberUtil.normalizeDiallableCharsOnly(shippingAddress.getPhoneNumber()));
        order.setEmail(shippingAddress.getEmail());
        order.setShipDesc(shippingAddress.getBusinessName());
        order.setIsApplySuperSaverShipping(Boolean.FALSE.toString());
        order.setOrderSource(user.isProxyUser() ? OrderSource.WEB_AGENT.name() : OrderSource.WEB.name());
        order.setIsEmailChanged(cart.getIsEmailChanged());
        order.setIsAddressChanged(cart.getIsAddressChanged());

        //TODO: set other notifications. Defaulted to EMAIL for now
        order.setNotificationType(NotificationType.EMAIL.name());

        // BRPRJ-1074 - Populate user language/country from locale at creation of Order
        final Locale userLocale = user.getLocale();
        order.setLanguageCode(userLocale.getLanguage());
        order.setCountryCode(userLocale.getCountry());
        order.setCurrencyCode(cart.getCartTotal().getCurrency().getCurrencyCode());

        setSelectedAddressId(order, cart, user);

        // use order_line.supplier_id to get the gift message since it may be an viator order using amazon cart.
        if (!cartItems.isEmpty()) {
            order.setGiftMessage(messageService.getMessage(user.getVarId(),user.getProgramId(), user.getSupplierId(), USER_MSG.GIFT_MESSAGE));
        }

        order.setOrderLines(createOrderLines(cart, user, applicationProperties, program, cartItems, order));

        if(Objects.nonNull(cart.getPromotionalSubscription()) && Objects.nonNull(user.getAdditionalInfo())){
            user.getAdditionalInfo().put(CommonConstants.UA_SERVICE_SUBSCRIPTION_DISPLAY_CHECKBOX,String.valueOf
                    (cart.getPromotionalSubscription().isDisplayCheckbox()));
            user.getAdditionalInfo().put(CommonConstants.UA_SERVICE_SUBSCRIPTION_IS_CHECKED,String.valueOf
                    (cart.getPromotionalSubscription().isChecked()));
        }

        if (Objects.nonNull(userVarProgramCreditAdds)) {
            order.setUserVarProgramCreditAdds(userVarProgramCreditAdds);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Done converting ShoppingCart to Order");
        }
        return order;
    }

    /**
     * Create Orider Lines
     *
     * @param cart
     * @param user
     * @param applicationProperties
     * @param program
     * @param cartItems
     * @param order
     * @return
     */
    private List<OrderLine> createOrderLines(final Cart cart, final User user, final Properties applicationProperties,
                                             final Program program, final List<CartItem> cartItems, final Order order) {
        final List<OrderLine> orderLineList = new ArrayList<OrderLine>();
        final AtomicInteger atomicLineNum = new AtomicInteger(0);
        cartItems.forEach(cartItem -> {
            // create order line for a regular product
            final OrderLine orderLine =
                    buildOrderLine(cart, user, applicationProperties, program, order, atomicLineNum.incrementAndGet(),
                            cartItem, false, null);
            orderLineList.add(orderLine);

            buildSelectAddonsOrderLines(cart, user, applicationProperties, program, order, orderLineList, atomicLineNum,
                    cartItem, orderLine.getLineNum());

        });

        //If coupon code applied then create it as new orderline entry.
        if(CollectionUtils.isNotEmpty(cart.getDiscounts())){
            for(final DiscountCode code:cart.getDiscounts()){
                final OrderLine orderLine = DiscountCodeTransactionManager.addDiscountCodeOrderLine(user, code, program);
                orderLine.setLineNum(atomicLineNum.incrementAndGet());
                orderLineList.add(orderLine);
            }
        }

        //Persist Cart AMP subscription in Order Line
        if (CollectionUtils.isNotEmpty(cart.getSubscriptions())) {
            final Stream<OrderLine> orderLineStream = cart.getSubscriptions().stream()
                .filter(Subscription::isAddedToCart)
                .map(subscription -> {
                    final OrderLine orderLine =
                        subscriptionManager.addSubscriptionOrderLine(user, subscription, program);
                    orderLine.setLineNum(atomicLineNum.incrementAndGet());
                    return orderLine;
                });
            orderLineList.addAll(orderLineStream.collect(Collectors.toList()));
        }

        //Add order related attribute
        order.setOrderAttributeValues(getOrderAttributes(user, program, cart));

        order.setSelectedPaymentOption(cart.getSelectedPaymentOption());
        if(CommonConstants.PaymentOption.PAYROLL_DEDUCTION.name().equals(cart.getSelectedPaymentOption())) {
            final OrderLine orderLine = getOrderLineForPayrollDeduction(user, cart, program);
            orderLine.setLineNum(atomicLineNum.getAndIncrement());
            if(order.getOrderAttributeValues() != null) {
                order.getOrderAttributeValues().addAll(getPayrollRelatedOrderAttributes(program));
            } else {
                order.setOrderAttributeValues(getPayrollRelatedOrderAttributes(program));
            }
            orderLineList.add(orderLine);
        }
        return orderLineList;
    }

    private void buildSelectAddonsOrderLines(Cart cart, User user, Properties applicationProperties, Program program,
                                             Order order, List<OrderLine> orderLineList, AtomicInteger atomicLineNum,
                                             CartItem cartItem, Integer qualifyingLineNum) {
        if (Objects.nonNull(cartItem.getSelectedAddOns())) {
            if (Objects.nonNull(cartItem.getSelectedAddOns().getServicePlan())) {
                // create order line for product's service plan
                final OrderLine servicePlanOrderLine =
                        buildOrderLine(cart, user, applicationProperties, program, order, atomicLineNum.incrementAndGet(),
                                cartItem.getSelectedAddOns().getServicePlan(), true, qualifyingLineNum);
                orderLineList.add(servicePlanOrderLine);
            }

            if (Objects.nonNull(cartItem.getSelectedAddOns().getGiftItem())) {
                // create order line for a GWP
                final OrderLine giftOrderLine =
                        buildOrderLine(cart, user, applicationProperties, program, order, atomicLineNum.incrementAndGet(),
                                cartItem.getSelectedAddOns().getGiftItem(), true, qualifyingLineNum);
                orderLineList.add(giftOrderLine);

                if (Objects.nonNull(cartItem.getSelectedAddOns().getGiftItem().getSelectedAddOns()) &&
                        Objects.nonNull(cartItem.getSelectedAddOns().getGiftItem().getSelectedAddOns().getServicePlan())) {
                    // create order line for gift's service plan
                    final OrderLine giftServicePlanOrderLine =
                            buildOrderLine(cart, user, applicationProperties, program, order, atomicLineNum.incrementAndGet(),
                                    cartItem.getSelectedAddOns().getGiftItem().getSelectedAddOns().getServicePlan(),
                                    true, giftOrderLine.getLineNum());
                    orderLineList.add(giftServicePlanOrderLine);
                }
            }
        }
    }

    /**
     * Build Order Line
     *
     * @param cart
     * @param user
     * @param applicationProperties
     * @param program
     * @param order
     * @param lineNum
     * @param cartItem
     * @return
     */
    private OrderLine buildOrderLine(final Cart cart, final User user, final Properties applicationProperties,
                                     final Program program, final Order order, int lineNum, final CartItem cartItem, final boolean isGiftItem, final Integer qualifyingLineNum) {
        OrderLine orderLine = new OrderLine();
        orderLine.setProductGroupId(cartItem.getProductGroupId());
        if(CommonConstants.EXPERIENCE_DRP.equalsIgnoreCase((String)program.getConfig().get(CommonConstants.SHOP_EXPERIENCE))){
            orderLine.setPaymentFrequency((String)program.getConfig().get(CommonConstants.PAY_FREQUENCY_VALUE));
            final String payPeriods = (String)program.getConfig().get(CommonConstants.PAY_PERIODS);
            if(StringUtils.isNotEmpty(payPeriods) && Integer.parseInt(payPeriods) > 0){
                orderLine.setPaymentDuration(Integer.parseInt(payPeriods));
                orderLine.setPayrollAmount(
                        BigDecimal.valueOf(cartItem.getProductDetail().getDefaultOffer().getPayPerPeriodPrice()).setScale(2).doubleValue());
                orderLine.setPayrollTotalAmount(BigDecimal.valueOf(cartItem.getProductDetail().getDefaultOffer().getPayPerPeriodTotalPrice()).setScale(2).doubleValue());
            }
        }
        if(cartItem.getSupplierId() == CommonConstants.SUPPLIER_TYPE_CREDIT){
            LOGGER.info("Adding CC Line item...");
            orderLine = CreditTransactionManager.addCreditOrderLine(user,cartItem.getProductDetail(), program);
            orderLine.setSupplierId(CommonConstants.SUPPLIER_TYPE_CREDIT_S);
            orderLine.setCategory(CommonConstants.PRODUCT_CATEGORY_CREDIT_CARD);
        }
        orderLine.setLineNum(lineNum);
        final Product appleProduct = cartItem.getProductDetail();
        final Offer appleOffer = appleProduct.getDefaultOffer();

        try {
            BeanUtils.copyProperties(orderLine, cartItem);
        } catch (final Exception e) {
            LOGGER.error("Exception while copying Cart Item to Order Line: ", e);
        }
        orderLine.setOrderSource(OrderSource.WEB.name());
        orderLine.setSupplierId(String.valueOf(cartItem.getSupplierId())) ;
        orderLine.setMerchantId(cartItem.getMerchantId());
        applyNameImageAndCategory(orderLine, appleProduct);
        orderLine.setItemId(cartItem.getProductId());

        //
        // Set pricing using the new pricing service - begin
        //
        // base item costs in points and money(no shipping, fees, taxes or margins)
        // TODO - Clean this up to set the Money in offer.
        orderLine.setSupplierItemPrice(getAmountInCents(appleOffer.getUnpromotedSupplierItemPrice().getAmount()));
        orderLine.setSupplierTaxPrice(getAmountInCents(appleOffer.getUnpromotedSupplierTaxPrice().getAmount()));
        orderLine.setVarOrderLinePrice(getAmountInCents(appleOffer.getUnpromotedVarPrice().getAmount()));

        // unpromoted fees costs in points and money
        applyFees(orderLine, appleOffer.getUnPromotedFees(), order);

        // unpromoted taxes costs in points and money
        applyTaxes(orderLine,appleOffer.getUnPromotedTax(),order);

        applyPromotionAndDiscount(isGiftItem, qualifyingLineNum, orderLine, appleProduct, appleOffer);

        applyPriceAndPoints(cartItem, orderLine, appleOffer);

        orderLine.setOrderId(order.getOrderId());
        orderLine.setQuantity(cartItem.getQuantity());

        orderLine.setSupplierShippingUnit("");  //TODO : for apple
//            orderLine.setSupplierShippingUnit(cartItem.getProductDetail().getDefaultOffer().getSupplierShippingUnit()); //TODO : for apple

        // Adding conv rate from cart since it is the correct conv rate from pricing service
        Double convRate = Objects.nonNull(appleOffer.getConvRate()) ?
                (new BigDecimal(appleOffer.getConvRate()).divide(new BigDecimal(100)).doubleValue()) :
                program.getConvRate();
        orderLine.setConvRate(convRate);

        setPointsRoundingIncrement(orderLine, cart, appleOffer.getRoundingIncrement());

        // TODO. Need to find proper tax rate
        orderLine.setTaxRate(0);

        orderLine.setB2sTaxRate(appleOffer.getB2sServiceFeeRateBps());
        orderLine.setB2sItemMargin(swiftTwoDecimalPlaces(appleOffer.getB2sItemMargin()));
        orderLine.setVarItemMargin(swiftTwoDecimalPlaces(appleOffer.getVarItemMargin()));
        orderLine.setB2sShippingMargin(swiftTwoDecimalPlaces(appleOffer.getB2sShippingMargin()));
        orderLine.setVarShippingMargin(swiftTwoDecimalPlaces(appleOffer.getVarShippingMargin()));

        orderLine.setCreateDate(new Date());
        orderLine.setBrand(appleProduct.getBrand());
        orderLine.setManufacturer(appleProduct.getManufacturer());

        applyShipmentInformation(program, cartItem, orderLine);

        orderLine.setSku(appleOffer.getSku());
        orderLine.setAppleSku(appleProduct.getAppleSku());//TODO: apple  this value is not set
//            orderLine.setSellerId(String.valueOf( cartItem.getProductDetail().getDefaultOffer().getSellerId() ));  //TODO : for apple:  Potential candidate to delete in DB
//            orderLine.setListingId(cartItem.getProductDetail().getDefaultOffer().getOfferListingId());   //TODO : for apple:  Potential candidate to delete in DB
        orderLine.setVarId(order.getVarId());
        orderLine.setProgramId(order.getProgramId());
        orderLine.setOrderStatus(CommonConstants.ORDER_STATUS_STARTED);

        //Add line attributes for gift/engrave details  //TODO: rename this to orderline attribute
        orderLine.setOrderAttributes(getOrderLineAttributes(cartItem, user, applicationProperties));

        // if the cart item is a service plan ==> bundle id is the qualifying hardware product
        // if the cart item is a hardware product and have an associated service plan ==> bundle id is the product line num
        if(CommonConstants.SUPPLIER_TYPE_SERVICE_PLAN_S.equals(orderLine.getSupplierId())) {
            orderLine.setBundleId(String.valueOf(qualifyingLineNum));
        } else if(Objects.nonNull(cartItem.getSelectedAddOns()) && Objects.nonNull(cartItem.getSelectedAddOns().getServicePlan())) {
            orderLine.setBundleId(String.valueOf(lineNum));
        }
        return orderLine;
    }

    private void applyNameImageAndCategory(final OrderLine orderLine, final Product appleProduct) {
        final String productName = appleProduct.getName();
        // Adding this change to remove new line characters being sent to DB or VIS
        if(StringUtils.isNotBlank(productName)) {
            orderLine.setName(productName.replaceAll(CommonConstants.NEW_LINE_REGEX, ""));
        }

        final ImageURLs imageURLs = appleProduct.getImages();
        if (Objects.nonNull(imageURLs) && Objects.nonNull(imageURLs.getSmall())) {
            orderLine.setImageUrl(imageURLs.getSmall());
        } else {
            orderLine.setImageUrl("");
        }

        if (StringUtils.isBlank(orderLine.getCategory())) {
            if (CollectionUtils.isNotEmpty(appleProduct.getCategories())) {
                orderLine.setCategory(appleProduct.getCategories().get(0).getName());
            } else {
                LOGGER.warn("CartOrderConverter: OrderLine category not set for psid {}", appleProduct.getPsid());
            }
        }
    }

    private void applyShipmentInformation(final Program program, final CartItem cartItem, final OrderLine orderLine) {
        if (ShippingMethod.ELECTRONIC.getLabel().equalsIgnoreCase(cartItem.getShippingMethod())) {
            orderLine.setShippingMethod(ShippingMethod.ELECTRONIC.getLabel());

        } else {
            orderLine.setShippingMethod(ShippingMethod.STANDARD.getLabel());
        }

        String orderDelayTime;
        if (cartItem.getSupplierId() == SUPPLIER_TYPE_MERC || cartItem.getSupplierId() == SUPPLIER_TYPE_SERVICE_PLAN) {
            orderDelayTime = (String) program.getConfig().get(CommonConstants.ORDERHOLDTIME);
            if(StringUtils.isNotBlank(orderDelayTime)){
                orderLine.setOrderDelay(orderDelayTime);
                LOGGER.info("Setting MERC Order Delay time: {}", orderDelayTime);
            }
        } else if (cartItem.getSupplierId() == SUPPLIER_TYPE_GIFTCARD) {
            orderDelayTime = (String) program.getConfig().get(CommonConstants.ORDERHOLDTIME_GC);
            if(StringUtils.isNotBlank(orderDelayTime)){
                orderLine.setOrderDelay(orderDelayTime);
                LOGGER.info("Setting GC Order Delay time: {}", orderDelayTime);
            }
        }
    }

    private void applyPriceAndPoints(final CartItem cartItem, final OrderLine orderLine, final Offer appleOffer) {
        orderLine.setSupplierShippingPrice(getAmountInCents(appleOffer.getOrgShippingPrice()));
        orderLine.setItemPoints(Double.valueOf(appleOffer.getB2sItemPrice().getPoints()));
        orderLine.setShippingPoints(Double.valueOf(appleOffer.getB2sShippingPrice().getPoints()));
        orderLine.setTaxPoints(Double.valueOf(appleOffer.getSupplierTaxPrice().getPoints()));

        // points including taxes and fees for a single unit
        orderLine.setOrderLinePoints(appleOffer.getTotalPrice().getPoints()/cartItem.getQuantity());

        // B2S Tax Affects Reports
        orderLine.setB2sTaxPrice(getAmountInCents(appleOffer.getB2sServiceFee().getAmount()));
        orderLine.setB2sTaxPoints((double) appleOffer.getB2sServiceFee().getPoints());

        // B2S item profit in cents. Calculated in pricing service based on B2S_Margin
        if(appleOffer.getB2sItemProfitPrice() != null) {
            orderLine.setB2sItemProfitPrice(getAmountInCents(appleOffer.getB2sItemProfitPrice().getAmount()));
        } else {
            orderLine.setB2sItemProfitPrice(0);
        }

        // B2S Shipping profit in cents.
        orderLine.setB2sShippingProfitPrice(getAmountInCents(appleOffer.getB2sShippingProfitPrice()));
        orderLine.setB2sTaxProfitPrice(0);

        // VAR item profit in cents. Calculated in pricing service based on VAR_Margin
        if(appleOffer.getVarItemProfitPrice() != null) {
            orderLine.setVarItemProfitPrice(getAmountInCents(appleOffer.getVarItemProfitPrice().getAmount()));
        } else {
            orderLine.setVarItemProfitPrice(0);
        }

        // VAR Shipping profit in cents.
        orderLine.setVarShippingProfitPrice(getAmountInCents(appleOffer.getVarShippingProfitPrice()));
        orderLine.setVarTaxProfitPrice(0);
        orderLine.setSupplierPerShipmentPrice(0);
//            orderLine.setSupplierPerShipmentPrice(!CommonConstants.CAT_CREDIT_STR.equals(cartItem.getProductId() ) ? offer.getSupplierPerShipmentPrice():0); //TODO : for apple
        orderLine.setSupplierShippingUnitPrice(0);
//            orderLine.setSupplierShippingUnitPrice(!CommonConstants.CAT_CREDIT_STR.equals(cartItem.getProductId() ) ? offer.getSupplierShippingUnitPrice() :0);  //TODO : for apple
        orderLine.setSupplierSingleItemShippingPrice(0);
    }

    private void applyPromotionAndDiscount(final boolean isGiftItem, final Integer qualifyingLineNum,
                                           final OrderLine orderLine, final Product appleProduct, final Offer appleOffer) {
        //D-08216 - Store Full price instead of promotional price
        final Optional<Promotion> applePromotion = appleProduct.getPromotion();
        if (applePromotion.isPresent()) {
            if (isGiftItem) {

                //if promotion is configured in terms of percentage, corresponding configs need to be set in orderLine
                setDiscountPercentageInOrderLine(orderLine, applePromotion);

                final Optional<Money> fixedPointPrice = applePromotion.get().getFixedPointPrice();
                if (fixedPointPrice.isPresent()) {
                    orderLine.setAttr1(DISCOUNTED_GIFT_POINTS);
                    orderLine.setAttr2(String.valueOf(fixedPointPrice.get().getAmountMajorInt()));
                }
                // the qualifying line number is the previous line number
                orderLine.setAttr3(String.valueOf(qualifyingLineNum));
            } else {
                orderLine.setAttr1(DISCOUNT_APPLIED);
                final Optional<BigDecimal> percentage = applePromotion.get().getDiscountPercentage();
                if (percentage.isPresent()) {
                    orderLine.setAttr2(percentage.get().toString());
                }
            }
            LOGGER.info("Product has promotion. Hence setting discounted price!!!");
            LOGGER.info("Promotion Type: {}, Value: {}.", orderLine.getAttr1(), orderLine.getAttr2());
            setDiscountedSupplierItemPrice(isGiftItem, orderLine, appleOffer);
            orderLine.setDiscountedVarOrderLinePrice(getAmountInCents(appleOffer.getVarPrice().getAmount()));
            orderLine.setDiscountedFees(calculateDiscountedFees(appleOffer.getFees()));
            orderLine.setDiscountedTaxes(calculateDiscountedTaxes(appleOffer.getTax()));
        }
    }

    private void setDiscountPercentageInOrderLine(final OrderLine orderLine, final Optional<Promotion> applePromotion) {
        final Optional<BigDecimal> percentage = applePromotion.get().getDiscountPercentage();
        if (percentage.isPresent()) {
            final BigDecimal discountPercentage = percentage.get();
            // in case of gift item (discount == 100%)
            if(discountPercentage.intValue() == 100){
                orderLine.setAttr1(GIFT_ITEM);
            }else{
                orderLine.setAttr1(DISCOUNTED_GIFT_PERCENTAGE);
            }
            orderLine.setAttr2(percentage.get().toString());
        }
    }

    private void setDiscountedSupplierItemPrice(final boolean isGiftItem, final OrderLine orderLine, final Offer appleOffer) {
        // store discounted price
        final int supplierItemPrice;
        if (isGiftItem &&
            (DISCOUNTED_GIFT_PERCENTAGE.equals(orderLine.getAttr1()) || GIFT_ITEM.equals(orderLine.getAttr1()))
        ) {
            // This can be applied only in case of percentage discount
            supplierItemPrice = getDiscountedAmountInCents(appleOffer.getBasePrice().getAmount(),
                Double.valueOf(orderLine.getAttr2()));
        } else {
            supplierItemPrice = getAmountInCents(appleOffer.getBasePrice().getAmount());
        }
        orderLine.setDiscountedSupplierItemPrice(supplierItemPrice);
    }

    /**
     *
     * @param program
     * @return
     */
    private static List<OrderAttributeValue> getPayrollRelatedOrderAttributes(Program program) {
        List<OrderAttributeValue> orderAttributeValues = new ArrayList<>();
        if(program != null && MapUtils.isNotEmpty(program.getConfig())) {
            if(Objects.nonNull(program.getConfig().get(CommonConstants.PAY_PERIODS))) {
                orderAttributeValues.add(buildOrderAttribute(CommonConstants.PAY_PERIODS, program.getConfig().get(CommonConstants.PAY_PERIODS).toString()));
            }
            if(Objects.nonNull(program.getConfig().get(CommonConstants.PAY_DURATION))) {
                orderAttributeValues.add(buildOrderAttribute(CommonConstants.PAY_DURATION, program.getConfig().get(CommonConstants.PAY_DURATION).toString()));
            }
        }
        return orderAttributeValues;
    }


    private static OrderLine getOrderLineForPayrollDeduction(final User user, final Cart cart, final Program program) {
        final OrderLine orderLine = new OrderLine();
        orderLine.setCreateDate(new Date());
        orderLine.setAttr1(String.valueOf(getAmountInCents(cart.getCartTotal().getPayPerPeriod())));
        orderLine.setAttr2("");
        orderLine.setAttr3("");
        orderLine.setB2sItemMargin(0.0d);
        orderLine.setB2sShippingMargin(0.0d);
        orderLine.setB2sTaxProfitPrice(0);
        orderLine.setB2sShippingProfitPrice(0);
        orderLine.setColor(" ");
        orderLine.setComment(CommonConstants.CAT_PAYROLLDEDUCTION_STR);
        orderLine.setConvRate(program.getConvRate());
        orderLine.setIsEligibleForSuperSaverShipping("N");
        orderLine.setItemId(CommonConstants.CAT_PAYROLLDEDUCTION_STR);
        orderLine.setSupplierId(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S);
        orderLine.setLineNum(2);
        orderLine.setName(CommonConstants.CAT_PAYROLLDEDUCTION_STR);
        orderLine.setOrderStatus(CommonConstants.ORDER_STATUS_STARTED);
        orderLine.setCategory(CommonConstants.CAT_PAYROLLDEDUCTION_STR);
        orderLine.setProgramId(user.getProgramId());
        orderLine.setQuantity(1);
        orderLine.setShippingPoints(0.0d);
        orderLine.setSize(" ");
        orderLine.setSupplierPerShipmentPrice(0);
        orderLine.setSupplierShippingPrice(0);
        orderLine.setSupplierShippingUnit("");
        orderLine.setSupplierShippingUnitPrice(0);
        orderLine.setSupplierSingleItemShippingPrice(0);
        orderLine.setSupplierTaxPrice(0);
        orderLine.setTaxPoints(Double.valueOf(0));
        orderLine.setTaxRate(0);
        orderLine.setB2sItemProfitPrice(0);
        orderLine.setB2sTaxPrice(0);
        orderLine.setB2sTaxPoints(Double.valueOf(0));
        orderLine.setB2sTaxRate(0);
        orderLine.setVarId(user.getVarId());
        orderLine.setVarItemMargin(0.0d);
        orderLine.setVarItemProfitPrice(0);
        orderLine.setVarShippingMargin(0.0d);
        orderLine.setVarTaxProfitPrice(0);
        orderLine.setVarShippingProfitPrice(0);
        if(cart.getCartTotal().isDiscountApplied() && cart.getCartTotal().getDiscountedPrice() != null) {
            orderLine.setVarOrderLinePrice(-(getAmountInCents(cart.getCartTotal().getDiscountedPrice().getAmount())) - getAmountInCents(cart.getCost()));
            orderLine.setSupplierItemPrice(-(getAmountInCents(cart.getCartTotal().getDiscountedPrice().getAmount())) - getAmountInCents(cart.getCost()));
            orderLine.setOrderLinePoints(-(cart.getCartTotal().getDiscountedPrice().getPoints()) - getAmountInCents(cart.getCost()));
            orderLine.setItemPoints(-(new Double(cart.getCartTotal().getDiscountedPrice().getPoints()) - getAmountInCents(cart.getCost())));
        } else {
            orderLine.setVarOrderLinePrice(-(getAmountInCents(cart.getCartTotal().getPrice().getAmount())) - getAmountInCents(cart.getCost()));
            orderLine.setSupplierItemPrice(-(getAmountInCents(cart.getCartTotal().getPrice().getAmount())) - getAmountInCents(cart.getCost()));
            orderLine.setOrderLinePoints(-(cart.getCartTotal().getPrice().getPoints()) - getAmountInCents(cart.getCost()));
            orderLine.setItemPoints(-(new Double(cart.getCartTotal().getPrice().getPoints()) - getAmountInCents(cart.getCost())));
        }
        orderLine.setWeight(0);
        orderLine.setIsQuantityUsed(false);
        return orderLine;
    }

    public static int getAmountInCents(Double amount) {
        if(amount != null) {
            return BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(CONVERT_DOLLAR_TO_CENTS)).round(MathContext.UNLIMITED).intValue();
        } else {
            return 0;
        }
    }

    public static int getDiscountedAmountInCents(Double amount, Double discount) {
        if(amount != null) {
            return (BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(CONVERT_DOLLAR_TO_CENTS - discount))).setScale(0,RoundingMode.HALF_UP).intValue();
        } else {
            return 0;
        }
    }

    public static double swiftTwoDecimalPlaces(final Double amount) {
        if(amount != null) {
            return BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(CONVERT_DOLLAR_TO_CENTS)).round(MathContext
                    .UNLIMITED).doubleValue();
        } else {
            return 0.0d;
        }
    }


    //Order related attributes
    private static List<OrderAttributeValue> getOrderAttributes(User user, final Program program,final Cart cart) {


        //TODO : Move order related entries from orderline attribute to orderAttribute
        return addOrderAttributesFromAdditionalInfo(user, program,cart);

    }
    /**
     *
     * @param cartItem
     * @return
     */
    private static List<OrderLineAttribute> getOrderLineAttributes(CartItem cartItem, User user, final Properties applicationProperties) {
        List<OrderLineAttribute> orderLineAttributes = new ArrayList<>();
        getEngraveAttributes(cartItem, orderLineAttributes);

        if (CollectionUtils.isNotEmpty(cartItem.getProductDetail().getOptions())) {
            final String optionString = cartItem.getProductDetail().getOptions().stream()
                .map(Option::getValue)
                .collect(Collectors.joining(CommonConstants.OPTIONS_JOIN_SEPARATOR));
            orderLineAttributes.add(buildOrderLineAtribute(PRODUCT_OPTIONS, optionString));
        }

        getGiftMessageAttributes(cartItem, orderLineAttributes);
        orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.SHIPPING_AVAILABILITY, cartItem.getProductDetail
                ().getShippingAvailabilityMessage()));
        //Adding Shipment Quote Date
        if(StringUtils.isNotBlank(cartItem.getProductDetail().getShipmentQuoteDate())){
            orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.SHIPMENT_QUOTE_DATE,cartItem
                    .getProductDetail().getShipmentQuoteDate()));
        }
        // Adding currency to order line attribute.
        if(user.getAdditionalInfo() != null && StringUtils.isNotEmpty(user.getAdditionalInfo().get(CommonConstants.VIS_CURRENCY))) {
            orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.VIS_CURRENCY, user.getAdditionalInfo().get(CommonConstants.VIS_CURRENCY)));
        }

        boolean sendVarAnalyticData = new Boolean(applicationProperties.getProperty(user.getVarId().toLowerCase()+"."+CommonConstants.SEND_VAR_ANALYTIC_DATA));
        if(sendVarAnalyticData && MapUtils.isNotEmpty(cartItem.getProductDetail().getAdditionalInfo())) {
            Object analyticCodeObj = cartItem.getProductDetail().getAdditionalInfo().get(CommonConstants.VAR_ANALYTIC_CODE);
            if(analyticCodeObj != null) {
                orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.VAR_ANALYTIC_CODE, analyticCodeObj.toString()));
            } else {
                orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.VAR_ANALYTIC_CODE, CommonConstants.DEFAULT_VAR_ANALYTIC_CODE));
            }

            Object analyticKeyObj = cartItem.getProductDetail().getAdditionalInfo().get(CommonConstants.VAR_ANALYTIC_KEY);
            if(analyticKeyObj != null) {
                orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.VAR_ANALYTIC_KEY, analyticKeyObj.toString()));
            } else {
                orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.VAR_ANALYTIC_KEY, CommonConstants.DEFAULT_VAR_ANALYTIC_KEY));
            }
        }

        //Add pricingParameters such as bridge2UnitBasePrice, retailUnitBasePrice and retailUnitTaxPrice to orderLineAttributes
        addOrderLineAttributesFromPricingParameters(cartItem,orderLineAttributes);

        addOrderLineAttributesFromAdditionalInfo(cartItem, orderLineAttributes, user, applicationProperties);
        // add unpromoted display price as order line attribute, if it differs from display price
        orderLineAttributes.addAll(getOrderLineAttributesFromOffer(cartItem));
        return orderLineAttributes;
    }

    private static void getGiftMessageAttributes(final CartItem cartItem,
                                                 final List<OrderLineAttribute> orderLineAttributes) {
        if (cartItem.hasGiftMessage()) {
            Gift gift = cartItem.getGift();
            orderLineAttributes.add(buildOrderLineAtribute(GIFT_MESSAGE_1, gift.getMessage1()));
            orderLineAttributes.add(buildOrderLineAtribute(GIFT_MESSAGE_2, gift.getMessage2()));
            orderLineAttributes.add(buildOrderLineAtribute(GIFT_MESSAGE_3, gift.getMessage3()));
            orderLineAttributes.add(buildOrderLineAtribute(GIFT_MESSAGE_4, gift.getMessage4()));
            orderLineAttributes.add(buildOrderLineAtribute(GIFT_MESSAGE_5, gift.getMessage5()));
        }
    }

    private static void getEngraveAttributes(final CartItem cartItem,
                                             final List<OrderLineAttribute> orderLineAttributes) {
        if (cartItem.hasEngraveMessage()) {
            final Engrave engrave = cartItem.getEngrave();
            if(StringUtils.isNotBlank(engrave.getLine1())|| StringUtils.isNotBlank(engrave.getLine2())) {
                orderLineAttributes.add(buildOrderLineAtribute(ENGRAVING_CODE, engrave.getFontCode()));
                orderLineAttributes.add(buildOrderLineAtribute(ENGRAVING_LINE_1, engrave.getLine1()));
                orderLineAttributes.add(buildOrderLineAtribute(ENGRAVING_LINE_2, engrave.getLine2()));
            }
        }
    }

    /**
     * Get unpromoted display price as order line attribute, if it differs from display price
     *
     * @param cartItem
     * @return
     */
    private static List<OrderLineAttribute> getOrderLineAttributesFromOffer(final CartItem cartItem) {
        final List<OrderLineAttribute> orderLineAttributes = new ArrayList<>();
        if (cartItem != null && cartItem.getSupplierId() != CommonConstants.SUPPLIER_TYPE_CREDIT) {
            populateOrderLineAttributesFromOffer(orderLineAttributes, cartItem.getProductDetail().getDefaultOffer());
        }
        return orderLineAttributes;
    }

    private static void populateOrderLineAttributesFromOffer(final List<OrderLineAttribute> orderLineAttributes, final Offer offer) {
        if (Objects.nonNull(offer)) {
            if (Objects.nonNull(offer.getDisplayPrice())) {
                //Persisting display Price attributes for Order History pages
                orderLineAttributes.add(
                    buildOrderLineAtribute(CommonConstants.ORDER_ATTR_KEY_DISPLAY_PRICE_AMOUNT, String.valueOf(
                        offer.getDisplayPrice().getAmount())));
                orderLineAttributes.add(
                    buildOrderLineAtribute(CommonConstants.ORDER_ATTR_KEY_DISPLAY_PRICE_POINTS, String.valueOf(
                        offer.getDisplayPrice().getPoints())));

                if (Objects.nonNull(offer.getUnpromotedDisplayPrice()) &&
                    !offer.getUnpromotedDisplayPrice().getAmount().equals(offer.getDisplayPrice().getAmount())) {
                    orderLineAttributes.add(
                        buildOrderLineAtribute(CommonConstants.ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_AMOUNT,
                            String.valueOf(
                                offer.getUnpromotedDisplayPrice().getAmount())));
                    orderLineAttributes.add(
                        buildOrderLineAtribute(CommonConstants.ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_POINTS,
                            String.valueOf(
                                offer.getUnpromotedDisplayPrice().getPoints())));
                }
            }
            if (Objects.nonNull(offer.getTotalPrice())) {
                orderLineAttributes.add(
                    buildOrderLineAtribute(CommonConstants.ORDER_ATTR_KEY_DISPLAY_TOTAL_PRICE_AMOUNT,
                        String.valueOf(
                            offer.getTotalPrice().getAmount())));
            }
            if (offer.getInverseRate() != null) {
                orderLineAttributes.add(
                    buildOrderLineAtribute(CommonConstants.ORDER_LINE_ATTR_KEY_INVERSE_RATE, String.valueOf(
                        offer.getInverseRate())));
            }
        }
    }

    //OrderAttribute
    //TODO: This is intentionally created similar to orderlineatribute, as order related entries from orderline attribute need to be moved to orderAttribute
    private static List<OrderAttributeValue> addOrderAttributesFromAdditionalInfo(User user, final Program program,
                                                                                  final Cart cart) {
        List<OrderAttributeValue> orderAttributeValues = new ArrayList<>();
        final String shippingEmail;

        if(user != null && MapUtils.isNotEmpty(user.getAdditionalInfo())) {
            user.getAdditionalInfo().forEach((k,v) ->{
                if(!k.equalsIgnoreCase(CommonConstants.ORDER_ATTIBUTE_EXCLUDE)){
                    orderAttributeValues.add(buildOrderAttribute(k,v));
                }
            });

        }

        if (Objects.nonNull(user) && program.getConfig().get(user.getEmployerId() + '_' + CommonConstants.PAYER_ID) != null) {
            orderAttributeValues.add(buildOrderAttribute(CommonConstants.PAYER_ID, program.getConfig().get(user
                    .getEmployerId() + '_' + CommonConstants.PAYER_ID).toString()));
        } else if (program.getConfig().get(CommonConstants.PAYER_ID) != null) {
            orderAttributeValues.add(buildOrderAttribute(CommonConstants.PAYER_ID, program.getConfig()
                    .get(CommonConstants.PAYER_ID).toString()));
        }


        //  Set profile email and shipping email address.
        shippingEmail=cart.getShippingAddress().getEmail();

        if(Objects.nonNull(user)  && program.getConfig().get(CommonConstants.PROFILE_EMAIL_NOTIFICATION)!=null &&
                Boolean.valueOf(program.getConfig().get(CommonConstants.PROFILE_EMAIL_NOTIFICATION).toString())){

            orderAttributeValues.add(buildOrderAttribute(CommonConstants.EmailType.PROFILE.getValue(), user.getEmail()));
            if (!user.getEmail().equalsIgnoreCase(shippingEmail)){
                orderAttributeValues.add(buildOrderAttribute(CommonConstants.EmailType.SHIPPING.getValue(),
                        shippingEmail));
            }
        }

        if(StringUtils.isNotBlank(cart.getTimeZoneId())){
            orderAttributeValues.add(buildOrderAttribute(CommonConstants
                    .TIME_ZONE_ID, cart.getTimeZoneId()));
        }

        // Adding currency to order line attribute.
        if(Objects.nonNull(user)  && Objects.nonNull(user.getAdditionalInfo()) && StringUtils.isNotEmpty(user.getAdditionalInfo().get(CommonConstants.VIS_CURRENCY))) {
            orderAttributeValues.add(buildOrderAttribute(CommonConstants.VIS_CURRENCY, user.getAdditionalInfo().get(CommonConstants.VIS_CURRENCY)));
        }

        return orderAttributeValues;

    }

    /**
     * Method to Add pricingParameters such as bridge2UnitBasePrice, retailUnitBasePrice and retailUnitTaxPrice to orderLineAttributes
     *
     * @param cartItem
     * @param orderLineAttributes
     * @return
     */
    private static void addOrderLineAttributesFromPricingParameters(final CartItem cartItem,
                                                                    final List<OrderLineAttribute> orderLineAttributes){
        if(Objects.nonNull(cartItem.getProductDetail()) && Objects.nonNull(cartItem.getProductDetail().getDefaultOffer())){
            final Offer appleOffer = cartItem.getProductDetail().getDefaultOffer();
            if(Objects.nonNull(appleOffer.getRetailUnitBasePrice())){
                orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.RETAIL_UNIT_BASE_PRICE,
                        appleOffer.getRetailUnitBasePrice().getAmount().toString()));
            }
            if(Objects.nonNull(appleOffer.getRetailUnitTaxPrice())){
                orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.RETAIL_UNIT_TAX_PRICE,
                        appleOffer.getRetailUnitTaxPrice().getAmount().toString()));
            }
            //Set Bridge2 Unit Base Price for DAC pricing
            if(Objects.nonNull(appleOffer.getBridge2UnitBasePrice())){
                orderLineAttributes.add(buildOrderLineAtribute(CommonConstants.BRIDGE2_UNIT_BASE_PRICE,
                    appleOffer.getBridge2UnitBasePrice().toString()));
            }
        }
    }

    //OrderLineAttribute
    private static void addOrderLineAttributesFromAdditionalInfo(CartItem cartItem, List<OrderLineAttribute> orderLineAttributes, User user, final Properties applicationProperties) {
        if(cartItem.getProductDetail() != null) {
            if(MapUtils.isNotEmpty(cartItem.getProductDetail().getAdditionalInfo()) && cartItem.getProductDetail().getAdditionalInfo().get(CommonConstants.PRICING_MODEL) != null) {
                PricingModel pricingModel = (PricingModel)cartItem.getProductDetail().getAdditionalInfo().get(CommonConstants.PRICING_MODEL);
                String supplierId = applicationProperties.getProperty(user.getVarId().toLowerCase() + ".supplier.id");
                addAttribute(orderLineAttributes, SUPPLIER_ID, supplierId);

                orderLineAttributes.add(buildOrderLineAtribute(PRODUCT_ID, CommonConstants.APPLEWATCH));
                addAttribute(orderLineAttributes, ACTIVATION_FEE, pricingModel.getActivationFee());
                addAttribute(orderLineAttributes, UPGRADE_FEE, pricingModel.getUpgradeCost());

                BigDecimal subsidyAmount = new BigDecimal(pricingModel.getPaymentValue()).setScale(2, RoundingMode.FLOOR).multiply(new BigDecimal(pricingModel.getMonthsSubsidized()));
                orderLineAttributes.add(buildOrderLineAtribute(SUBSIDY_AMOUNT, subsidyAmount.toString()));
                orderLineAttributes.add(buildOrderLineAtribute(SUBSIDY_TYPE, CommonConstants.EMPLOYER));
                if(pricingModel.getUpgradeCost() != null && pricingModel.getActivationFee() != null && cartItem.getProductDetail().getDefaultOffer() != null && cartItem.getProductDetail().getDefaultOffer().getB2sItemPrice() != null) {
                    BigDecimal totalFinanceAmount = new BigDecimal(cartItem.getProductDetail().getDefaultOffer().getB2sItemPrice().getAmount())
                            .subtract(new BigDecimal(pricingModel.getUpgradeCost())
                                    .add(new BigDecimal(pricingModel.getActivationFee())).add(subsidyAmount));
                    orderLineAttributes.add(buildOrderLineAtribute(TOTAL_FINANCE_AMOUNT, totalFinanceAmount.toString()));
                }
                addDiscountTier(orderLineAttributes, pricingModel.getDiscountTier1(), DISCOUNT_TIER1);
                addDiscountTier(orderLineAttributes, pricingModel.getDiscountTier2(), DISCOUNT_TIER2);
                addDiscountTier(orderLineAttributes, pricingModel.getDiscountTier3(), DISCOUNT_TIER3);
                orderLineAttributes.add(buildOrderLineAtribute(MAX_REPAY, new BigDecimal(pricingModel.getPaymentValue()).setScale(2, RoundingMode.FLOOR).multiply(new BigDecimal(pricingModel.getRepaymentTerm())).toString()));

                addAttribute(orderLineAttributes, MAXIMUM_MONTHLY_REPAY, pricingModel.getPaymentValue());
                addAttribute(orderLineAttributes, DURATION, pricingModel.getRepaymentTerm());

                orderLineAttributes.add(buildOrderLineAtribute(DURATION_TYPE, CommonConstants.MONTHS));
                orderLineAttributes.add(buildOrderLineAtribute(PURCHASE_REFERENCE_TYPE, CommonConstants.ORDER_REFERENCE));
                orderLineAttributes.add(buildOrderLineAtribute(PURCHASE_REF_TYPE, CommonConstants.SKU));
                if(user != null && MapUtils.isNotEmpty(user.getAdditionalInfo())) {
                    orderLineAttributes.add(buildOrderLineAtribute(TENANT_ID, user.getAdditionalInfo().get(TENANT_ID)));
                }
            }

            String seaNumber = applicationProperties.getProperty(user.getVarId().toLowerCase() + "."+ CommonConstants.SEA_NUMBER);
            addAttribute(orderLineAttributes, CommonConstants.SEA_NUMBER, seaNumber);

            String priceOverrideCode = applicationProperties.getProperty(user.getVarId().toLowerCase() + "."+ CommonConstants.PRICE_OVERRIDE_CODE);
            addAttribute(orderLineAttributes, CommonConstants.PRICE_OVERRIDE_CODE, priceOverrideCode);

            String promotionalOffer = applicationProperties.getProperty(user.getVarId().toLowerCase() + "."+ CommonConstants.PROMOTIONAL_OFFER);
            addAttribute(orderLineAttributes, CommonConstants.PROMOTIONAL_OFFER, promotionalOffer);
        }
    }

    private static void addAttribute(List<OrderLineAttribute> orderLineAttributes, final String key, final String value) {
        if(StringUtils.isNotBlank(value)) {
            orderLineAttributes.add(buildOrderLineAtribute(key, value));
        }
    }

    private static void addAttribute(List<OrderLineAttribute> orderLineAttributes, final String key, final Double value) {
        if(null != value) {
            addAttribute(orderLineAttributes, key, value.toString());
        }
    }

    private static void addAttribute(List<OrderLineAttribute> orderLineAttributes, final String key, final Integer value) {
        if(null != value) {
            addAttribute(orderLineAttributes, key, value.toString());
        }
    }

    private static void addDiscountTier(final List<OrderLineAttribute> orderLineAttributes, final Double discountTierValue, final String discountTier) {
        if(Objects.nonNull(discountTierValue)) {
            orderLineAttributes.add(buildOrderLineAtribute(discountTier, Double.toString(discountTierValue)));
        }
    }

    public static OrderLineAttribute buildOrderLineAtribute(String name, String value) {
        OrderLineAttribute orderLineAttribute =  new OrderLineAttribute();
        orderLineAttribute.setName(name);
        orderLineAttribute.setValue(value);
        return  orderLineAttribute;
    }

    public static OrderLineItemAttribute buildOrderLineAtribute(OrderLine orderLine, String name, String value) {
        OrderLineItemAttribute orderLineAttribute = null;
        if(orderLine != null && org.apache.commons.lang.StringUtils.isNotBlank(name) && org.apache.commons.lang.StringUtils.isNotBlank(value)) {
            orderLineAttribute =  new OrderLineItemAttribute();
            orderLineAttribute.setName(name);
            orderLineAttribute.setValue(value);
            orderLineAttribute.setLineNum(orderLine.getLineNum());
            orderLineAttribute.setOrderId(orderLine.getOrderId());
        }
        return  orderLineAttribute;
    }

    public static OrderAttributeValue buildOrderAttribute(String name, String value) {
        OrderAttributeValue orderAttributeValue =  new OrderAttributeValue();
        orderAttributeValue.setName(name);
        orderAttributeValue.setValue(value);
        return orderAttributeValue;
    }

    public static OrderAttributeValue buildOrderAttribute(String name, String value, Long orderId) {
        OrderAttributeValue orderAttributeValue = null;
        if (StringUtils.isNotBlank(value)) {
            orderAttributeValue = new OrderAttributeValue();
            orderAttributeValue.setName(name);
            orderAttributeValue.setValue(value);
            orderAttributeValue.setOrderId(orderId);
        }
        return orderAttributeValue;
    }

    private static void applyFees(OrderLine orderLine,Map<String, Fee> appleOfferFees,Order order){
        // fees costs in points and money
        if (appleOfferFees != null) {
            final List<OrderLineFee> fees = new ArrayList<>();
            for (final Fee fee : appleOfferFees.values()) {
                final OrderLineFee olFee = new OrderLineFee();
                olFee.setOrderID(order.getOrderId());
                olFee.setOrderLine(orderLine.getLineNum());
                olFee.setName(fee.getFeeId());
                olFee.setAmount(new BigDecimal(fee.getAmount().getAmount()).setScale(2, RoundingMode.HALF_UP));
                olFee.setPoints(fee.getAmount().getPoints());
                olFee.setCreateTime(new Date());
                LOGGER.debug("Setting... fee....");
                fees.add(olFee);
            }
            orderLine.setFees(fees);
        }

    }

    private static Integer calculateDiscountedFees(Map<String, Fee> appleOfferFees){
        Integer total = 0;
        if(null != appleOfferFees){
            for(final Fee fee : appleOfferFees.values()) {
                total+=getAmountInCents(fee.getAmount().getAmount());
            }
        }
        return total;
    }

    private static Integer calculateDiscountedTaxes(Map<String, Tax> appleOfferTaxes){
        Integer total = 0;
        if (null != appleOfferTaxes) {
            for (final Tax tax : appleOfferTaxes.values()) {
                total+=getAmountInCents(tax.getAmount().getAmount());
            }
        }
        return total;
    }

    private static void applyTaxes(OrderLine orderLine,Map<String, Tax> appleOfferTaxes,Order order){
        // taxes costs in points and money
        if (appleOfferTaxes != null) {
            final List<OrderLineTax> taxes = new ArrayList<>();
            for (final Tax tax : appleOfferTaxes.values()) {
                final OrderLineTax olTax = new OrderLineTax();
                olTax.setOrderID(order.getOrderId());
                olTax.setOrderLine(orderLine.getLineNum());
                olTax.setName(tax.getTaxId());
                olTax.setAmount(new BigDecimal(tax.getAmount().getAmount()).setScale(2, RoundingMode.HALF_UP));
                olTax.setPoints(tax.getAmount().getPoints());
                olTax.setCreateTime(new Date());
                LOGGER.debug("Setting... tax....");
                taxes.add(olTax);
            }
            orderLine.setTaxes(taxes);
        }

    }

    /**
     * Set Earn Points
     *
     * @param order
     * @param cart
     * @param program
     */
    private void setEarnPoints(final Order order, final Cart cart, final Program program){
        final boolean showEarnPoints = (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_EARN_POINTS, Boolean.FALSE);
        if(showEarnPoints){
            if(Objects.nonNull(cart.getEarnPoints())){
                order.setEarnedPoints(cart.getEarnPoints());
            }else{
                order.setEarnedPoints(0);
                LOGGER.warn("Earn points is persisted as zero");
            }
        }
    }

    /**
     * Set Used ID
     *
     * @param order
     * @param cart
     * @param user
     */
    private void setUserId(final Order order, final Cart cart, final User user){
        if(user.getUserId().contains(CommonConstants.ANONYMOUS_USER_ID)){
            order.setUserId(cart.getShippingAddress().getEmail());
            user.setUserId(cart.getShippingAddress().getEmail());
        }else{
            order.setUserId(user.getUserId());
        }
    }

    /**
     * Set Address Line 3
     *
     * @param order
     * @param shippingAddress
     */
    private void setAddr3(final Order order, final Address shippingAddress){
        final String shippingAddress3 = shippingAddress.getAddress3();
        if(CommonConstants.COUNTRY_CODE_GB.equals(shippingAddress.getCountry())) {
            order.setAddr3(AppleUtil.decodeSpecialChar(shippingAddress3));
        } else {
            final StringBuilder address3 = new StringBuilder();
            // For some countries, we have a sub locality/dependent locality. It is mapped to address.subCity
            // For now, we append this new field to address3 in orders DB table
            if (StringUtils.isNotBlank(shippingAddress3)) {
                address3.append(shippingAddress3);
            }
            final String shippingSubCity = shippingAddress.getSubCity();
            if (StringUtils.isNotBlank(shippingSubCity)) {
                if (StringUtils.isNotBlank(address3)) {
                    address3.append(", ");
                }
                address3.append(shippingSubCity);
            }
            order.setAddr3(AppleUtil.decodeSpecialChar(address3.toString()));
        }
    }

    /**
     * Set Zip Code
     *
     * @param order
     * @param shippingAddress
     */
    private void setZipCode(final Order order, final Address shippingAddress){
        String zipCode = shippingAddress.getZip5();
        final String zipCode4 = shippingAddress.getZip4();
        if (!StringUtils.isEmpty(zipCode4)) {
            zipCode = zipCode + "-" + zipCode4;
        }
        // Setting the Canadian postal code in proper format
        // that is 7 character, trimmed of spaces on front and back, and one space in the middle.
        // This change has been made for fixing APPLE-1983
        if (COUNTRY_CODE_CA.equals(order.getCountry()) && StringUtils.isNotBlank(zipCode)) {
            zipCode = zipCode.trim().replaceAll("\\s+","");
            if(zipCode.length() == 6) {
                zipCode = zipCode.substring(0,3) + " "+zipCode.substring(3,6);
            }
        }
        order.setZip(zipCode);
    }

    /**
     * Set Selected Address ID
     *
     * @param order
     * @param cart
     * @param user
     */
    private void setSelectedAddressId(final Order order, final Cart cart, final User user){
        if(user instanceof UserChase) {
            order.setSelectedAddressId(cart.getShippingAddress().getSelectedAddressId());
        }
    }

    /**
     * Set Points Rounding Increment
     *
     * @param orderLine
     * @param cart
     * @param roundingIncrement
     */
    private void setPointsRoundingIncrement(final OrderLine orderLine, final Cart cart, final Money roundingIncrement){
        // Add effective conv rate
        // Now pricing service returns rounding increment as 1 as default increment.
        // We need to find the effective conv rate only if the rounding increment is more than 1
        if(roundingIncrement != null
                && roundingIncrement.getAmountMajorInt() > 1
                && cart.getCartTotal().getPrice() != null) {
            // Get the effective conv rate using below formula.
            // points used (Total cart points - supplemental payment points) divided by price equivalent of points used (total cart amount - supplemental payment amount)
            int pointsUsed = cart.getCartTotal().getPrice().getPoints() - cart.getAddPoints();
            int cashValueOfPointsUsed = getAmountInCents(cart.getCartTotal().getPrice().getAmount() - cart.getCost());
            // Add check to avoid division by zero
            if(pointsUsed > 0 && cashValueOfPointsUsed > 0) {
                final Double effConvRate = new BigDecimal(pointsUsed)
                        .divide(new BigDecimal(cashValueOfPointsUsed), 15, RoundingMode.HALF_UP)
                        .doubleValue();
                orderLine.setEffConvRate(effConvRate);
            }
            orderLine.setPointsRoundingIncrement(roundingIncrement.getAmountMajorInt());
        }
    }
}
