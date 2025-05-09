package com.b2s.apple.services;

import com.b2s.apple.mapper.ProductMapper;
import com.b2s.common.services.discountservice.CouponCodeValidator;
import com.b2s.common.services.exception.DataException;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.pricing.impl.LocalPricingServiceV2;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.dao.NaughtyWordDao;
import com.b2s.rewards.apple.dao.ShoppingCartDao;
import com.b2s.rewards.apple.dao.ShoppingCartItemDao;
import com.b2s.rewards.apple.dao.WhiteListWordDao;
import com.b2s.rewards.apple.integration.model.AddToCartResponse;
import com.b2s.rewards.apple.integration.model.PaymentOptions;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.apple.validator.AddressMapper;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.merchandise.action.CartCalculationUtil;
import com.b2s.rewards.model.ProductImage;
import com.b2s.rewards.model.ShippingMethod;
import com.b2s.service.product.client.exception.EntityNotFoundException;
import com.b2s.service.product.client.exception.RequestValidationException;
import com.b2s.shop.common.User;
import com.b2s.shop.util.VarProgramConfigHelper;
import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.b2s.rewards.apple.util.AppleUtil.getProgramConfigValueAsBoolean;
import static com.b2s.rewards.common.util.CommonConstants.DISABLE_AMP;
import static com.b2s.rewards.common.util.CommonConstants.ENABLE_APPLE_CARE_SERVICE_PLAN;
import static com.b2s.rewards.common.util.CommonConstants.ENABLE_SMART_PRICING;
import static org.apache.commons.beanutils.BeanUtils.populate;

/**
 * All Operations Related to User Apple Shopping Cart
 * Created by ssrinivasan on 3/30/2015.
 */
@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    public static final int DENOMINATION_CURRENCY_SYMBOL_POSITION = 1;

    public static final String DENOMINATION_KEY = "denomination";

    //Default quantity for Add To Cart
    private static final Integer DEFAULT_CART_QTY = 1;
    public static final String EXCEEDED_SLIDER_RANGE_VALUE_OF = " exceeded Slider Range value of ";
    public static final String EXCEPTION_WHILE_MODIFYING_CART = "Exception while modifying Cart";
    public static final String AS_SET_IN_ADMIN_PAGE = " as set in Admin page..";
    public static final String SPLIT_PAY_STR = "SplitPay: {}";

    @Autowired
    private DetailService detailService;

    @Autowired
    @Qualifier("productServiceV3Service")
    ProductServiceV3 productServiceV3;

    @Autowired
    @Qualifier("LocalPricingServiceV2")
    private LocalPricingServiceV2 pricingServiceV2;

    @Autowired
    private VarProgramConfigHelper varProgramConfigHelper;

    @Autowired
    private CategoryConfigurationService categoryConfigurationService;

    @Autowired
    private ShoppingCartDao appleShoppingCartDao;

    @Autowired
    private ShoppingCartItemDao appleShoppingCartItemDao;

    @Autowired
    private NaughtyWordDao naughtyWordDao;

    @Autowired
    private WhiteListWordDao whiteListWordDao;

    @Autowired
    private CouponCodeValidator couponCodeValidator;

    @Autowired
    private EngravingService engravingService;

    @Autowired
    private GiftPromoService giftPromoService;

    @Autowired
    private ProductMapper productMapper;
    public static final String NAUGHTYBADWORD = "NAUGHTYBADWORD";

    public boolean inCorrectSupplementalSplit(final Integer ccAddPoints, final Cart cart, final Program program,
        final StringBuilder logMessage) {

        if(ccAddPoints == 0){
            return false;
        }

        final List<VarProgramRedemptionOption> paymentOptions = new ArrayList<>();
        if (MapUtils.isNotEmpty(program.getRedemptionOptions())) {
            program.getRedemptionOptions().forEach((k, v) -> {
                paymentOptions.addAll(v);
            });
        }
        if (CollectionUtils.isNotEmpty(paymentOptions) &&
            validateRedemptionOption(ccAddPoints, cart, logMessage, paymentOptions)) {
            logger.warn("Not Satisfied CcSlider Supplemental verification...");
            return true;
        }
        logger.info("Satisfied CcSlider Supplemental verification...");
        return false;
    }


    private boolean validateRedemptionOption(Integer ccAddPoints, Cart cart, StringBuilder logMessage, List<VarProgramRedemptionOption> paymentOptions) {
        for(final VarProgramRedemptionOption paymentOption : paymentOptions) {
            if(paymentOption.getPaymentOption().equalsIgnoreCase(PaymentOptions.SPLITPAY.getPaymentOption())) {
                logger.info("CcSlider Supplemental is Active...");
                // TODO -- Need to validate for Full points scenario and add code changes if any
                if (Objects.nonNull(paymentOption.getLimitType())) {
                    //Check dollar value
                    if (validateDollarValue(cart, logMessage, paymentOption)){
                        return true;
                    }
                    if (validatePercentagePaymentOption(ccAddPoints, cart, logMessage, paymentOption)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean validatePercentagePaymentOption(Integer ccAddPoints, Cart cart, StringBuilder logMessage, VarProgramRedemptionOption paymentOption) {
        if (paymentOption.getLimitType().equalsIgnoreCase(CommonConstants.PERCENTAGE)) {
            //Points range
            logger.info("CcSlider Supplemental type is POINTS...");
            final int allowedPurchasePoints =
                cart.getRedemptionPaymentLimit().getCashMaxLimit().getPoints();
            if (ccAddPoints > allowedPurchasePoints) {
                logMessage.append(
                    "CcSlider Supplemental PERCENTAGE verification did not satisfy. Slider points ");
                logMessage.append(ccAddPoints);
                logMessage.append(EXCEEDED_SLIDER_RANGE_VALUE_OF);
                logMessage.append(allowedPurchasePoints);
                logMessage.append(AS_SET_IN_ADMIN_PAGE);
                logger.info(SPLIT_PAY_STR, logMessage);
                return true;
            }
        }
        return false;
    }

    private boolean validateDollarValue(Cart cart, StringBuilder logMessage, VarProgramRedemptionOption paymentOption) {
        if (paymentOption.getLimitType().equalsIgnoreCase(CommonConstants.DOLLAR) && cart.getCost() > paymentOption.getPaymentMaxLimit()) {
            logMessage.append(
                "CcSlider Supplemental DOLLAR verification did not satisfy. Slider amount ");
            logMessage.append(cart.getCost());
            logMessage.append(EXCEEDED_SLIDER_RANGE_VALUE_OF);
            logMessage.append(paymentOption.getPaymentMaxLimit());
            logMessage.append(AS_SET_IN_ADMIN_PAGE);
            logger.info(SPLIT_PAY_STR, logMessage);
            return true;
        }

        return false;
    }


    /**
     * Adds the product that corresponds to the psid along with the quantity of the product
     * This also updates the shopping cart object in User session, updates the shopping cart table
     * along with the updated pricing from pricing service.
     *
     * @param cart Current Cart object
     * @param user Current Logged in User, also present in session.
     * @param program Program for current user
     * @param newProduct   Core model product object.
     * @param servicePlanProduct, Service Plan Product
     */
    public Long addToCart(Cart cart, final User user, Program program, final Product newProduct, final Product servicePlanProduct) throws ServiceException {

        ShoppingCartItem shoppingCartItem=null;
        try {
            Map<String, String> optionMap = new HashMap<>();
            Gift giftMessage = new Gift();
            Gson gson = new Gson();

            //clear bill to
            user.setBillTo(null);

            //get current cart
            ShoppingCart shoppingCart = appleShoppingCartDao.get(user.getVarId(), user.getProgramId(), user.getUserId());
            if (shoppingCart == null) {
                //Create cart entry for this user
                shoppingCart = persistNewCart(user);
            }

            // Create CartItem
            shoppingCartItem =
                createShoppingCartItem(newProduct, servicePlanProduct, optionMap, giftMessage, gson, shoppingCart);

            CartItem cartItem = CartItem.transform(shoppingCartItem, user, program);

            updateCartItem(cartItem, user, program, cart);

            //Add Service Plan to Selected AddOns If exist
            final CartItem servicePlan = cartItem.getSelectedAddOns().getServicePlan();
            if (Objects.nonNull(servicePlanProduct) && Objects.nonNull(servicePlan)) {
                updateCartItem(servicePlan, user, program, cart);
            }

            //add item to cart to calculate pricing
            cart.getCartItems().add(cartItem);

            //Calculate cart total
            logger.info("P$ Request flow(addToCart) starts");
            callCalculateCartPrice(cart, user, program);
            if (!cart.isMaxCartTotalExceeded()) {
                appleShoppingCartItemDao.create(shoppingCartItem);
                cartItem.setId(shoppingCartItem.getId());

                if(cart.getDisplayCartTotal().isDiscountApplied() && Objects.nonNull(cart.getDisplayCartTotal().getDiscountedPrice())
                    && cart.getDisplayCartTotal().getDiscountedPrice().getPoints() >= user.getBalance()){
                    cart.setAddPoints(cart.getDisplayCartTotal().getDiscountedPrice().getPoints() - user.getBalance());
                }
                else if(cart.getDisplayCartTotal().getPrice().getPoints() > user.getBalance()){
                    final String maxPurchaseAmt = (String) program.getConfig().get(CommonConstants.MAX_PURCHASE_AMOUNT);
                    if (StringUtils.isBlank(maxPurchaseAmt)) {
                        cart.setAddPoints(cart.getDisplayCartTotal().getPrice().getPoints() - user.getBalance());
                    }
                }
            } else {
                cart.getCartItems().remove(cartItem);
            }
            return cartItem.getId();
        }
        catch ( Exception ex) {
            logger.error("Exception while search for products", ex);
            appleShoppingCartItemDao.delete(shoppingCartItem);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
    }

    /**
     * Persist a new Cart
     *
     * @param user
     * @return
     */
    private ShoppingCart persistNewCart(final User user) {
        final ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setVarId(user.getVarId());
        shoppingCart.setProgramId(user.getProgramId());
        shoppingCart.setUserId(user.getUserId());
        appleShoppingCartDao.save(shoppingCart);

        return shoppingCart;
    }

    /**
     * Create Shopping Cart Item
     *
     * @param newProduct
     * @param servicePlanProduct
     * @param optionMap
     * @param giftMessage
     * @param gson
     * @param shoppingCart
     * @return
     */
    private ShoppingCartItem createShoppingCartItem(final Product newProduct, final Product servicePlanProduct,
        final Map<String, String> optionMap, final Gift giftMessage, final Gson gson, final ShoppingCart shoppingCart) {
        final ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        shoppingCartItem.setShoppingCart(shoppingCart);
        shoppingCartItem.setAddedDate(new Date());
        shoppingCartItem.setMerchantId(newProduct.getDefaultOffer().getMerchant().getMerchantId());
        shoppingCartItem.setProductId(newProduct.getProductId());
        shoppingCartItem.setProductName(productMapper.productName2CartNameForGiftCard(newProduct));
        //TODO: Once image proxy implementation is complete, replace this with imgeProxy URL
        if (Optional.ofNullable(newProduct.getProductImages()).isPresent() &&
            !newProduct.getProductImages().isEmpty()) {
            final ProductImage productImage =
                Optional.ofNullable(newProduct.getProductImages().get(0))
                    .flatMap(productImages -> Optional.ofNullable(productImages.get(0)))
                    .orElseGet(ProductImage::new);

            shoppingCartItem.setImageURL(productImage.getMediumImageURL());
        }
        shoppingCartItem.setSupplierId(newProduct.getSupplier().getSupplierId());
        //shoppingCartItem.setStoreId(newProduct.getStoreId()); //Value is always NULL
        shoppingCartItem.setParentProductId(newProduct.getParentProductId());

        //set gift wrap cost as points now, so that it will be available when user is on GiftWrap UI screen
        //TODO: update this with the REST service call once PricingService is ready & Apple supports gift wrap option
        giftMessage.setGiftWrapPoints(100);
        optionMap.put(CartItemOption.GIFT.getValue(), gson.toJson(giftMessage));
        if(Objects.nonNull(servicePlanProduct)){
            optionMap.put(CartItemOption.SERVICE_PLAN.getValue(), servicePlanProduct.getPsid());
        }

        shoppingCartItem.setOptionsXml(shoppingCartItem.convertToOptionsXml(optionMap));

        //NOTE: Quantity restriction is not needed as it will always be 1 for Add to Cart
        shoppingCartItem.setQuantity(DEFAULT_CART_QTY);

        return shoppingCartItem;
    }

    /**
     * Gets Current Cart for the logged in user
     *
     * @param user           Current Logged in User, also present in session.
     */
    public Cart getCart(final User user, Cart sessionCart, Program program) throws ServiceException {

        Cart cart;

        try {
            //get cart from session, if available
            if (sessionCart == null) {
                cart = loadCart(user, program, sessionCart);
                if (CollectionUtils.isNotEmpty(cart.getCartItems())) {
                    //Compute cart price
                    logger.info("P$ Request flow(getCart-Empty session) starts");
                    callCalculateCartPrice(cart, user, program);
                }
            } else {
                //unapply invalid discount code.
                sessionCart.setDiscounts(couponCodeValidator.removeInvalidDiscount(user, sessionCart.getDiscounts()));

                cart = getCartFromSessionCart(user, sessionCart, program);
            }

        } catch ( EntityNotFoundException ex) {
            return new Cart();
        }

        configPaymentMinMaxLimitToCart(cart,program);
        setPhysicalGiftcardMaxValue(program, cart);

        //Set Smart Pricing based on VPC - enableSmartPricing
        if (getProgramConfigValueAsBoolean(program, ENABLE_SMART_PRICING)) {
            final RedemptionPaymentLimit redemptionPaymentLimit = cart.getRedemptionPaymentLimit();

            if (Objects.nonNull(redemptionPaymentLimit)) {
                SmartPrice smartPrice = productMapper.getSmartPrice(redemptionPaymentLimit, program);
                cart.setSmartPrice(smartPrice);
            }
        }

        return cart;
    }

    /**
     *
     * @param user
     * @param sessionCart
     * @param program
     * @return
     */
    private Cart getCartFromSessionCart(User user, Cart sessionCart, Program program) {
        Cart cart;
        if (hasCartChangedSinceLoaded(user, sessionCart)) {
            cart = loadCart(user, program, sessionCart);
            cart.setDiscounts(sessionCart.getDiscounts());
            cart.setCartModifiedByUser(sessionCart.getCartModifiedByUser());
            cart.setCartModifiedBySystem(sessionCart.getCartModifiedBySystem());
            cart.setCartTotalModified(sessionCart.isCartTotalModified());
            cart.setShippingAddress(sessionCart.getShippingAddress());
            //Compute cart price with the loaded items. Since calculateCartPrice handles empty address, user shipping address null check is removed.
            if (CollectionUtils.isNotEmpty(cart.getCartItems())) {
                //Compute cart price. Address validation is done inside calculateCartPrice method
                logger.info("P$ Request flow(getCart-In Session Cart changed) starts");
                callCalculateCartPrice(cart, user, program);
            }
            if(cart != null && cart.getCartTotal() != null) {
                cart.getCartTotal().setDiscountAmount(cart.getTotalDiscountAmount());
                cart.getDisplayCartTotal().setDiscountAmount(cart.getTotalDiscountAmount());
            }

        } else {
            //get b2s cart
            verifyIsCartItemsStillAvailable(getShoppingCart(user).getShoppingCartItems(), sessionCart, user, program);
            addSubscriptions(program, sessionCart, user);

            //Compute cart price. Since calculateCartPrice handles empty address, user shipping address null check is removed.
            computeCartPrice(user, sessionCart, program);
            cart = sessionCart;
        }
        return cart;
    }

    /**
     * Add the subscriptions in cart response object, based on the AMP config present in the program object
     *
     * @param program
     * @param cart
     */
    public void addSubscriptions(final Program program, final Cart cart, final User user) {
        final Set<Subscription> subscriptions = new TreeSet<>();
        final Set<AMPConfig> ampSubscriptionConfigs = program.getAmpSubscriptionConfig();
        final List<CartItem> cartItems = cart.getCartItems();
        final boolean ampDisabled = getProgramConfigValueAsBoolean(program, DISABLE_AMP);

        if (!ampDisabled && CollectionUtils.isNotEmpty(ampSubscriptionConfigs) && CollectionUtils.isNotEmpty(cartItems)) {
            for(CartItem cartItem : cartItems) {
                getSubscriptions(subscriptions, ampSubscriptionConfigs, cartItem);
            }

            /*Update addedToCart flag in subscription attribute of cart response to true in Subscriptions property of
            cart response if already added to cart*/
            processSubscriptions(subscriptions, user, program);
            cart.setSubscriptions(subscriptions);
        }
    }

    /**
     *
     * @param subscriptions
     * @param ampSubscriptionConfigs
     * @param cartItem
     */
    private void getSubscriptions(Set<Subscription> subscriptions, Set<AMPConfig> ampSubscriptionConfigs, CartItem cartItem) {
        if(Objects.nonNull(cartItem.getProductDetail())
            && CollectionUtils.isNotEmpty(cartItem.getProductDetail().getCategories())) {
            for (AMPConfig ampConfig : ampSubscriptionConfigs) {
                final Set<String> ampsFromPS = cartItem.getProductDetail().getAmpSubscriptionConfig();
                if(CollectionUtils.isNotEmpty(ampsFromPS) && ampsFromPS.contains(ampConfig.getItemId())) {
                    getSubscriptionFromAllCategory(cartItem.getProductDetail().getCategories(),
                        ampConfig, subscriptions);
                }
            }
        }
    }

    /**
     * Updates flag to true in Subscriptions property of cart response if already added to cart and
     *        also removes subscription from cart , if item that has AMP is removed
     * @param subscriptions
     * @param user
     * @param program
     */
    private void processSubscriptions(final Set<Subscription> subscriptions, final User user, final Program program) {
        final List<String> invalidAMPProductIdsInCart = new ArrayList<>();

        setAddedToCartFlag(user, subscriptions, invalidAMPProductIdsInCart);

        //Remove subscriptions from cart if an item that has  AMP Services is removed from cart
        deleteInvalidSubscriptionFromCartDB(user.getUserId(), invalidAMPProductIdsInCart, program);
    }

    /**
     * Update the AddedTOCart to true if subscription is already added to cart
     *
     * @param user
     * @param subscriptions
     * @param invalidAMPProductIdsInCart
     */
    private void setAddedToCartFlag(final User user, final Set<Subscription> subscriptions,
        final List<String> invalidAMPProductIdsInCart) {

        final ShoppingCart shoppingCart = getShoppingCart(user);
        final List<ShoppingCartItem> ampShoppingCartItems = shoppingCart.getShoppingCartItems().stream()
            .filter(ampCartItem -> ampCartItem.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_AMP))
            .collect(Collectors.toList());

        for (ShoppingCartItem ampShoppingCartItem : ampShoppingCartItems) {
            boolean invalidCartItem = true;
            for (Subscription sub : subscriptions) {
                if (sub.getItemId().equalsIgnoreCase(ampShoppingCartItem.getProductId())) {
                    sub.setAddedToCart(true);
                    invalidCartItem = false;
                    logger.info("Subscription flag for {} is set to true ", sub.getItemId());
                }
            }

            if (invalidCartItem) {
                invalidAMPProductIdsInCart.add(ampShoppingCartItem.getProductId());
            }
        }
    }

    /**
     * Remove subscriptions from cart if an item that has an AMP Service is removed from cart
     * @param userId
     * @param productIdsToRemove
     * @param program
     */
    private void deleteInvalidSubscriptionFromCartDB(final String userId, final List<String> productIdsToRemove,
        final Program program) {
        if (CollectionUtils.isNotEmpty(productIdsToRemove)) {
            try {
                appleShoppingCartItemDao.deleteByProductId(productIdsToRemove, program.getVarId(),
                    program.getProgramId(), userId);
                logger.info("Invalid AMP items {} are successfully removed from the shopping cart of the user",
                    productIdsToRemove);
            } catch (Exception e) {
                logger.error("Exception occurred while trying to remove invalid AMP item from cart : {}",
                    e.getStackTrace());
            }
        }

    }

    /**
     * To get the subscription for the product based on their categories
     * Subscription will be available only for hero categories
     * So we avoid adding subscriptions for non hero categories here
     *
     * @param categoryList
     * @param ampConfig
     * @param subscriptions
     * @return
     */
    protected Set<Subscription> getSubscriptionFromAllCategory(final List<Category> categoryList,
        final AMPConfig ampConfig, final Set<Subscription> subscriptions) {
        for (Category category : categoryList) {
            if (!AppleUtil.isAccessories(category.getSlug())) {
                if (category.getSlug().equalsIgnoreCase(ampConfig.getCategory())) {
                    setUniqueSubscription(subscriptions, ampConfig);
                } else if (CollectionUtils.isNotEmpty(category.getParents())) {
                    getSubscriptionFromAllCategory(category.getParents(), ampConfig, subscriptions);
                }
            }
        }
        return subscriptions;
    }

    /**
     * Sets Subscription uniquely based on AMP Item ID
     * -> Selects Maximum duration record if duration present
     * -> Otherwise selects any one of the record
     */
    private void setUniqueSubscription(final Set<Subscription> subscriptions, final AMPConfig ampConfig) {
        Optional<Subscription> existingSubscription = subscriptions.stream()
            .filter(subscription -> (subscription.getItemId().equalsIgnoreCase(ampConfig.getItemId()) &&
                Objects.nonNull(subscription.getDuration())))
            .findAny();

        if (existingSubscription.isPresent() && Objects.nonNull(ampConfig.getDuration()) &&
            ampConfig.getDuration() > existingSubscription.get().getDuration()) {

            //This logic will override the selected Subscription with Maximum duration available in the ampConfig Table
            existingSubscription.get().setDuration(ampConfig.getDuration());
            logger.debug("Updated AMP Subscription {} based on duration {} for the Category {}.", ampConfig.getItemId(),
                ampConfig.getDuration(), ampConfig.getCategory());
        } else {
            subscriptions.add(new Subscription(ampConfig.getItemId(), ampConfig.getDuration(), false));
            logger.info("AMP Subscription {} added for the Category {}.", ampConfig.getItemId(),
                ampConfig.getCategory());
        }
    }

    private void computeCartPrice(User user, Cart sessionCart, Program program) {
        logger.info("P$ Request flow(computeCartPrice) starts");
        callCalculateCartPrice(sessionCart, user, program);
        if(sessionCart != null && sessionCart.getCartTotal() != null) {
            sessionCart.getCartTotal().setDiscountAmount(sessionCart.getTotalDiscountAmount());
            sessionCart.getDisplayCartTotal().setDiscountAmount(sessionCart.getTotalDiscountAmount());
            final Double currentCartTotal = CartCalculationUtil.getCartTotalAmount(sessionCart);
            if(user.isAmexInstallment()) {
                sessionCart.setInstallment(calculatePriceForAllInstallment(1, program, currentCartTotal));
            }
        }
    }

    private void setPhysicalGiftcardMaxValue(Program program, Cart cart) {
        if(program.getConfig().get(CommonConstants.PHYSICALGIFTCARDMAXVALUE) != null){
            cart.setPhysicalGiftcardMaxValue(Integer.parseInt(program.getConfig().get(CommonConstants.PHYSICALGIFTCARDMAXVALUE).toString()));
        }
    }

    private void configPaymentMinMaxLimitToCart(final Cart cart,final Program program){
        if(cart != null && cart.getCartTotal() != null &&  program.getPayments().size() == 1) {
            cart.setIsPayrollOnly(true);
            final Optional<PaymentOption> payrollOption = program.getPayments().stream()
                .filter(payment -> CommonConstants.CAT_PAYROLLDEDUCTION_STR.equals(payment.getPaymentOption()))
                .findFirst();

            if (payrollOption.isPresent()) {
                final PaymentLimit paymentLimit = getPaymentLimit(cart, payrollOption);
                cart.setPaymentLimit(paymentLimit);
            }
        }
    }

    private PaymentLimit getPaymentLimit(Cart cart, Optional<PaymentOption> payrollOption) {
        final PaymentLimit paymentLimit = new PaymentLimit();
        payrollOption.ifPresent(paymentOption -> {
            paymentLimit.setPaymentMinLimit(paymentOption.getPaymentMinLimit());
            paymentLimit.setPaymentMaxLimit(paymentOption.getPaymentMaxLimit());
        });
        final Optional<Double> paymentMinLimit = paymentLimit.getPaymentMinLimit();
        final Optional<Double> paymentMaxLimit = paymentLimit.getPaymentMaxLimit();

        if (Optional.ofNullable(cart.getCartTotal().getDiscountedPrice()).isPresent()) {
            if (paymentMinLimit.isPresent() && cart.getCartTotal().getDiscountedPrice().getAmount() < paymentMinLimit.get()) {
                paymentLimit.setMinNotMet(true);
            }
            if (paymentMaxLimit.isPresent() && cart.getCartTotal().getDiscountedPrice().getAmount() > paymentMaxLimit.get()) {
                paymentLimit.setMaxExceed(true);
            }
        } else if (paymentMinLimit.isPresent() && Optional.ofNullable(cart.getCartTotal().getPrice().getAmount()).isPresent()) {
            if (cart.getCartTotal().getPrice().getAmount() < paymentMinLimit.get()) {
                paymentLimit.setMinNotMet(true);
            }
            if (paymentMaxLimit.isPresent() && cart.getCartTotal().getPrice().getAmount() > paymentMaxLimit.get()) {
                paymentLimit.setMaxExceed(true);
            }
        }
        return paymentLimit;
    }

    private Cart loadCart(User user, Program program, final Cart sessionCart) {
        //Get cart from DB
        ShoppingCart shoppingCart = getShoppingCart(user);
        if (CollectionUtils.isNotEmpty(shoppingCart.getShoppingCartItems())) {
            verifyIfAddOnsPromotionItemStillValid(shoppingCart.getShoppingCartItems(), user, program);
        }
        //convert to apple cart
        Cart cart = Cart.transform(shoppingCart, user, program);
        if(Objects.nonNull(sessionCart) && Objects.nonNull(sessionCart.getDiscounts())){
            cart.setDiscounts(sessionCart.getDiscounts());
        }
        if (shoppingCart.getShoppingCartItems()!=null) {
            //check if item is still available since it was added to cart
            verifyIsCartItemsStillAvailable(shoppingCart.getShoppingCartItems(), cart, user, program);
        }

        setShippingAddress(user, program, sessionCart, cart);

        if (CollectionUtils.isNotEmpty(cart.getCartItems())) {
            loadCartItems(user, program, sessionCart, cart);
        }
        addSubscriptions(program, cart, user);
        return cart;
    }

    /**
     *
     * @param user
     * @param program
     * @param sessionCart
     * @param cart
     */
    private void loadCartItems(User user, Program program, Cart sessionCart, Cart cart) {
        for (CartItem cartItem : cart.getCartItems()) {
            if (!Objects.equals(cartItem.getSupplierId(), CommonConstants.AMP_SUPPLIER_ID)) {
                updateCartItem(cartItem, user, program, sessionCart);
                boolean isAppleCareServiceEnabled =
                    getProgramConfigValueAsBoolean(program, ENABLE_APPLE_CARE_SERVICE_PLAN);
                final CartItem giftItem = cartItem.getSelectedAddOns().getGiftItem();
                if (Objects.nonNull(giftItem)) {
                    updateCartItem(giftItem, user, program, sessionCart);
                    final CartItem giftItemServicePlan = giftItem.getSelectedAddOns().getServicePlan();
                    if (isAppleCareServiceEnabled && Objects.nonNull(giftItemServicePlan)) {
                        /*This is intended to update productDetails via PS call for ServicePlan that is
                         applicable for giftItem in cart*/
                        updateCartItem(giftItemServicePlan, user, program, sessionCart);
                    }
                }
                final CartItem servicePlan = cartItem.getSelectedAddOns().getServicePlan();
                if (isAppleCareServiceEnabled && Objects.nonNull(servicePlan)) {
                    updateCartItem(servicePlan, user, program, sessionCart);
                }
            }
        }
    }

    private void setShippingAddress(User user, Program program, Cart sessionCart, Cart cart) {
        // add user address as shipping address if exists
        if(cart.getShippingAddress() == null || !cart.getShippingAddress().canCalculateWithFeesAndTaxes(AddressMapper.isAddressCheckNeeded(program))) {
            if(Objects.nonNull(sessionCart) && Objects.nonNull(sessionCart.getShippingAddress())){
                cart.setShippingAddress(sessionCart.getShippingAddress());
            } else{
                cart.setShippingAddress(AddressMapper.getAddress(user, program));
            }
        }
    }

    /**
     * Update Cart Item
     *
     * @param cartItem
     * @param user
     * @param program
     * @param sessionCart
     */
    private void updateCartItem(CartItem cartItem, final User user, final Program program, final Cart sessionCart) {
        setGiftCardMaxQuantity(cartItem, program);
        setCartItemProductDetail(cartItem, user, program, sessionCart);
        setCartItemEngrave(cartItem, user);
    }

    /**
     * Set Cart Item Engrave Data
     *
     * @param cartItem
     * @param user
     */
    private void setCartItemEngrave(CartItem cartItem, final User user) {
        final String productSlug = AppleUtil.getCategorySlug(cartItem.getProductDetail());
        final String psid =
            Objects.nonNull(cartItem.getProductId()) ? cartItem.getProductId() : cartItem.getProductDetail().getPsid();

        if (cartItem.getEngrave() != null && StringUtils.isBlank(cartItem.getEngrave().getMaxCharsPerLine()) &&
            productSlug != null) {
            cartItem.setEngrave(engravingService.getEngravingConfiguration(user, productSlug, psid, null));
        }
    }

    /**
     * Set Cart Item Product Details
     *
     * @param cartItem
     * @param user
     * @param program
     * @param sessionCart
     */
    private void setCartItemProductDetail(CartItem cartItem, final User user, final Program program,
        final Cart sessionCart) {

        final Product appleProductDetail = productServiceV3
            .getAppleProductDetail(cartItem.getProductId(), program, false, user,
                isDiscount(sessionCart), true, false, false);

        //Set Cart Item from Core Product Detail
        setCartItemFromCoreProductDetail(cartItem, appleProductDetail);

        //all platform should be the concatenating name. ( cart, saved in DB, checkout)
        productMapper.productName2CartName(appleProductDetail);
        cartItem.setProductDetail(appleProductDetail);
        calculatePayPeriodPrice(cartItem.getQuantity(), appleProductDetail);
        /*changes for AMEX*/
        validateAmexInstallment(user, program, cartItem, appleProductDetail);
    }

    /**
     * Set Cart Item From Core Product Details call
     * Use productServiceV3.getAppleProductDetail instead of detailService.getCoreProductDetail
     *
     * @param cartItem
     * @param appleProductDetail
     */
    public void setCartItemFromCoreProductDetail(CartItem cartItem, final Product appleProductDetail) {
        cartItem.setShippingMethod(appleProductDetail.getFormat());
        cartItem.setGiftCardDenomination(
            buildDenominationMoney(appleProductDetail.getVariationDimensionNameValues(), CurrencyUnit.USD));
        setCartItemAdditionalData(cartItem, appleProductDetail);
    }

    /**
     * Set Cart Item Additional Data
     *
     * @param cartItem
     * @param product
     */
    private void setCartItemAdditionalData(CartItem cartItem, final Product product) {
        cartItem.setMerchantId(product.getDefaultOffer().getMerchant().getMerchantId());
        cartItem.setProductId(product.getProductId());
        cartItem.setProductName(productMapper.productName2CartNameForGiftCard(product));
        //TODO: Once image proxy implementation is complete, replace this with imgeProxy URL
        if (Optional.ofNullable(product.getProductImages()).isPresent() && !product.getProductImages().isEmpty()) {
            ProductImage productImage =
                (product.getProductImages().get(0)) != null ? product.getProductImages().get(0).get(0) :
                    new ProductImage();
            cartItem.setImageURL(productImage.getMediumImageURL());
        }
        cartItem.setSupplierId(product.getSupplier().getSupplierId());
        cartItem.setParentProductId(product.getParentProductId());
        cartItem.setShippingMethod(product.getFormat());
    }

    /**
     * Empty all items in Cart
     * @param user {}
     */
    public void emptyCart(final User user) {
        //Get cart from DB
        ShoppingCart shoppingCart = appleShoppingCartDao.get(user.getVarId(), user.getProgramId(), user.getUserId());
        if (shoppingCart != null) {
            appleShoppingCartDao.delete(shoppingCart);
        }

    }

    /**
     * Updates the shopping cart table
     * along with the updated pricing from pricing service.
     *
     * @param cart
     * @param user      Current Logged in User, also present in session.
     * @param itemId    Id of the item to be modified
     * @param giftEngraveMessage
     * @return
     * @throws ServiceException
     */
    public Cart modifyCart(final Cart cart, final User user, Long itemId, Map giftEngraveMessage, Program program)
            throws ServiceException, DataException {

        try {
            ShoppingCart shoppingCart = getShoppingCart(user);
            final double preModifiedCartTotal = CartCalculationUtil.getCartTotalAmount(cart);

            //S-20398 - Add / Modify AMP Services to Cart
            if (modifyAMPSubscriptionDetails(giftEngraveMessage, shoppingCart)) {
                return getUpdatedCart(cart, user, giftEngraveMessage, program, false, preModifiedCartTotal);
            }

            boolean maxCartTotalExceeded = isMaxCartTotalExceeded(cart, user, itemId, giftEngraveMessage, program, shoppingCart);
            //get Updated Cart
            return getUpdatedCart(cart, user, giftEngraveMessage, program, maxCartTotalExceeded, preModifiedCartTotal);

        } catch (NoSuchElementException nse) {
            logger.error(EXCEPTION_WHILE_MODIFYING_CART, nse);
            throw new NoSuchElementException();
        } catch (ServiceException se) {
            logger.error("Invalid data in modify cart...", se);
            throw se;
        } catch (IllegalAccessException e) {
            logger.error(EXCEPTION_WHILE_MODIFYING_CART, e);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, e);
        } catch (InvocationTargetException e) {
            logger.error(EXCEPTION_WHILE_MODIFYING_CART, e);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, e);
        } catch (DataException ex) {
            logger.error("Engrave Text Contains Naughty/Bad word(s) for User Locale: {}", user.getLocale());
            throw ex;
        } catch (Exception ex) {
            logger.error("General Exception while modifying Cart", ex);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
    }

    private boolean isMaxCartTotalExceeded(Cart cart, User user, Long itemId, Map modifyMessage, Program program, ShoppingCart shoppingCart)
            throws ServiceException, IllegalAccessException, InvocationTargetException, DataException {
        boolean maxCartTotalExceeded = false;
        Gson gson = new Gson();
        int quantity = 0;

        for (ShoppingCartItem shoppingCartItem : shoppingCart.getShoppingCartItems()) {
            if (Objects.equals(shoppingCartItem.getSupplierId(), CommonConstants.APPLE_SUPPLIER_ID)) {
                final Product appleProductDetail = productServiceV3
                        .getAppleProductDetail(shoppingCartItem.getProductId(), program, false, user,
                                isDiscount(cart), true, false, false);

                //This is required to calculate total cost
                CartItem cartItem = CartItem.transform(shoppingCartItem, user, program);
                cartItem.setShippingMethod(appleProductDetail.getFormat());

                cartItem.setProductDetail(appleProductDetail);
                validateAmexInstallment(user, program, cartItem, appleProductDetail);
                calculatePayPeriodPrice(cartItem.getQuantity(), appleProductDetail);
                //Get product details only for the items to be modified
                if (cartItem.getId().longValue() == itemId.longValue()) {
                    Integer newQuantity = getNewQuantity(user, modifyMessage, program, shoppingCartItem, appleProductDetail, cartItem);
                    if (Objects.nonNull(newQuantity)) {
                        if (newQuantity == 0) {
                            break;//Item removed from the shopping cart, no further action required
                        }
                        quantity = newQuantity;
                    }

                    //Modify AddOn Details
                    Map<String, String> optionMap = modifyAddOnDetails(cart, user, modifyMessage, program, gson, shoppingCartItem, cartItem);

                    //Modify Engrave Details
                    modifyEngraveDetails(user, modifyMessage, gson, cartItem, optionMap);

                    // Modify Gift Message
                    modifyGiftMessage(modifyMessage, gson, optionMap);

                    validateEngraveItemsQuantity(gson, quantity, optionMap);

                    //set updated Engrave Message & Gift Message information
                    shoppingCartItem.setOptionsXml(shoppingCartItem.convertToOptionsXml(optionMap));

                    setCartItemQuantity(cart, shoppingCartItem);

                    callCartPriceIfNotEngraveMsg(cart, user, program, modifyMessage);
                }
            }
            //Update the shopping_cart_items table if Maximum Cart Limit is not exceeded
            maxCartTotalExceeded = validateMaxCartLimit(cart, maxCartTotalExceeded, shoppingCartItem);

        }
        return maxCartTotalExceeded;
    }

    private void callCartPriceIfNotEngraveMsg(Cart cart, User user, Program program, Map modifyMessage) {
        final boolean isModifyOnlyEngraveMsg =
                MapUtils.isNotEmpty(modifyMessage) && modifyMessage.size() == 1 &&
                        Objects.nonNull(modifyMessage.get(CartItemOption.ENGRAVE.getValue()));

        //Added condition to skip below P$ call, if request is to modify only Engrave message
        if (!isModifyOnlyEngraveMsg) {
            // check pricing
            logger.info("P$ Request flow(processModifyCart) starts");
            callCalculateCartPrice(cart, user, program);
        }
    }

    private Integer getNewQuantity(User user, Map modifyMessage, Program program, ShoppingCartItem shoppingCartItem, Product appleProductDetail, CartItem cartItem) throws ServiceException {
        Integer newQuantity = (Integer) modifyMessage.get(CartItemOption.QUANTITY.getValue());
        // Modify Quantity. Check for max quantity limitation
        if (newQuantity != null) {
            if (newQuantity < 0) {
                throw new ServiceException(ServiceExceptionEnums.QUANTITY_INVALID_EXCEPTION);
            } else if (newQuantity == 0) {
                //just delete the current item
                appleShoppingCartItemDao.delete(shoppingCartItem);
                logger.info("{} is successfully removed from the shopping cart of the user {}", cartItem.getProductDetail().getPsid() + " " + cartItem.getProductDetail().getName(), user.getUserId());
            } else {
                processMaxQuantityRestriction(user, program, newQuantity, shoppingCartItem, appleProductDetail,
                        cartItem);
            }
        }
        return newQuantity;
    }

    private boolean modifyAMPSubscriptionDetails(final Map modifyMessage, final ShoppingCart shoppingCart) {
        final Map ampSubRequestMap;

        if (MapUtils.isNotEmpty(modifyMessage) &&
            Objects.nonNull(modifyMessage.get(CartItemOption.SUBSCRIPTIONS.getValue()))) {
            ampSubRequestMap = (Map) modifyMessage.get(CartItemOption.SUBSCRIPTIONS.getValue());
            final Integer newAMPQuantity = (Integer) ampSubRequestMap.get(CommonConstants.QUANTITY);
            if (newAMPQuantity == 1) {
                //Add Subscription into cart
                final ShoppingCartItem ampItemToAdd = getAMPShoppingCartItem(shoppingCart,ampSubRequestMap);
                appleShoppingCartItemDao.update(ampItemToAdd);
                logger.info("Adding {} into shopping cart id {} has been completed", ampItemToAdd.getProductId(),
                    shoppingCart.getId());
            } else if (newAMPQuantity == 0) {
                //Remove Subscription from cart
                final String ampIdToRemove = String.valueOf(ampSubRequestMap.get(CommonConstants.ITEM_ID));
                final ShoppingCartItem itemsToDelete = shoppingCart.getShoppingCartItems().stream()
                    .filter(shoppingCartItem ->
                        shoppingCartItem.getProductId().equalsIgnoreCase(ampIdToRemove)).findFirst().orElse(null);
                appleShoppingCartItemDao.delete(itemsToDelete);
                logger.info("Removing {} from shopping cart id {} has been completed", itemsToDelete.getProductId(),
                    shoppingCart.getId());
            }
            return true;

        }
        return false;
    }

    private boolean validateMaxCartLimit(Cart cart, boolean maxCartTotalExceeded, ShoppingCartItem shoppingCartItem) {
        if (cart.isMaxCartTotalExceeded()) {
            maxCartTotalExceeded = true;
        } else {
            // Update Cart
            appleShoppingCartItemDao.update(shoppingCartItem);
        }
        return maxCartTotalExceeded;
    }

    private void validateAmexInstallment(User user, Program program, CartItem cartItem, Product appleProductDetail) {
        if (user.isAmexInstallment()) {
            cartItem.setInstallment(calculatePriceForAllInstallment(cartItem.getQuantity(), program, appleProductDetail.getDefaultOffer().getB2sItemPrice().getAmount()));
        }
    }

    private Cart getUpdatedCart(Cart cart, User user, Map giftEngraveMessage, Program program, boolean maxCartTotalExceeded, double preModifiedCartTotal) throws ServiceException {
        //Reloads the cart.
        Cart updatedCart = getCart(user, cart, program);
        final double currentCartTotal = CartCalculationUtil.getCartTotalAmount(updatedCart);

        //Process Finance / Installment / Cash
        processPayment(user, giftEngraveMessage, program, updatedCart);

        final Object disableCartTotalModifiedPopUp = program.getConfig().get(CommonConstants.DISABLE_CART_TOTAL_MODIFIED_POP_UP);
        if(disableCartTotalModifiedPopUp==null || !(boolean)disableCartTotalModifiedPopUp) {
            updatedCart.setCartTotalModified(
                CartCalculationUtil.isCartTotalModified(preModifiedCartTotal, currentCartTotal));
        }
        if (cart.getShippingAddress() != null) {
            updatedCart.setShippingAddress(cart.getShippingAddress());
            updatedCart.getShippingAddress().setCartTotalModified(false);
        }
        if (cart.getBillingAddress() != null) {
            updatedCart.setBillingAddress(cart.getBillingAddress());
        }
        updatedCart.setMaxCartTotalExceeded(maxCartTotalExceeded);
        return updatedCart;
    }

    private void processPayment(User user, Map giftEngraveMessage, Program program, Cart updatedCart) {
        if (PaymentOptions.FINANCE.name().equalsIgnoreCase(
            (String) giftEngraveMessage.get(CartItemOption.SELECTED_REDEMPTION_OPTION.getValue())) &&
            (Integer) giftEngraveMessage.get(CartItemOption.PAYMENT_INSTALMENT.getValue()) != 0) {
            final InstallmentOption totalInstallment =
                getpayPeriod((Integer) giftEngraveMessage.get(CartItemOption.PAYMENT_INSTALMENT.getValue()),
                    updatedCart.getInstallment());
            if(Objects.nonNull(totalInstallment)) {
                updatedCart.getInstallment().setSelectedInstallement(totalInstallment);
                program.getConfig().put(CommonConstants.PAY_PERIODS, totalInstallment.getPayPeriods());
            }
            updatedCart.setPaymentType(PaymentOptions.FINANCE.name());
        } else {
            logger.info("P$ Request flow(modifyCart) starts");
            callCalculateCartPrice(updatedCart, user, program, getAddPoints(updatedCart, user, program));
            updatedCart
                .setPaymentType((String) giftEngraveMessage.get(CartItemOption.SELECTED_REDEMPTION_OPTION.getValue()));
        }
    }

    private Map<String, String> modifyAddOnDetails(Cart cart, User user, Map addOnMessage, Program program, Gson gson, ShoppingCartItem shoppingCartItem, CartItem cartItem) throws IllegalAccessException, InvocationTargetException, ServiceException, DataException {
        final Map<String, String> optionsMap = shoppingCartItem.convertToMap(shoppingCartItem.getOptionsXml());
        modifyGiftItem(cart, user, addOnMessage, program, gson, cartItem, optionsMap);
        modifyAppleCareServicePlans(addOnMessage, optionsMap);
        return optionsMap;
    }

    private void modifyGiftItem(final Cart cart, final User user, final Map addOnMessage, final Program program,
        final Gson gson, final CartItem cartItem, final Map<String, String> optionsMap)
        throws IllegalAccessException, InvocationTargetException, ServiceException, DataException {
        if (Objects.nonNull(addOnMessage.get(CartItemOption.GIFT_ITEM.getValue()))) {
            final GiftItemRequest giftItemRequest = new GiftItemRequest();
            populate(giftItemRequest, (Map<String, ? extends Object>) addOnMessage
                .get(CartItemOption.GIFT_ITEM.getValue()));

            if (StringUtils.isNotBlank(giftItemRequest.getProductId())) {
                final Optional<GiftItem> giftItemConfig =
                    giftPromoService.getGiftItem(user, cartItem.getProductId(), giftItemRequest.getProductId(), program);

                if (giftItemConfig.isPresent()) {
                    processGiftItemConfig(cart, user, program, gson, cartItem, optionsMap, giftItemRequest, giftItemConfig);
                } else {
                    optionsMap.remove(CartItemOption.GIFT_ITEM.getValue());
                }
            } else {
                optionsMap.remove(CartItemOption.GIFT_ITEM.getValue());
            }
        }
    }

    /**
     *
     * @param cart
     * @param user
     * @param program
     * @param gson
     * @param cartItem
     * @param optionsMap
     * @param giftItemRequest
     * @param giftItemConfig
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws DataException
     */
    private void processGiftItemConfig(Cart cart, User user, Program program, Gson gson, CartItem cartItem, Map<String, String> optionsMap, GiftItemRequest giftItemRequest, Optional<GiftItem> giftItemConfig) throws IllegalAccessException, InvocationTargetException, DataException {
        CartItem giftItem = cartItem.getSelectedAddOns().getGiftItem();
        if (Objects.isNull(giftItem)) {
            giftItem = new CartItem();
            giftItem.setQuantity(1);
            cartItem.getSelectedAddOns().setGiftItem(giftItem);
        }
        logger.info("DGwP --> Qualifying Product: {}, Selected Gift details: {}",
            giftItemRequest.getProductId(), giftItemConfig.get());
        giftItem.setProductId(giftItemRequest.getProductId());
        updateCartItem(giftItem, user, program, cart);

        Engrave giftEngraveObj = null;
        if (MapUtils.isNotEmpty(giftItemRequest.getEngrave())) {
            giftEngraveObj = getEngraveInfo(giftItemRequest.getEngrave(), user, giftItem);
        }

        final String servicePlanPsId = giftItemRequest.getServicePlan();
        optionsMap.put(CartItemOption.GIFT_ITEM.getValue(), getOptionsXmlGiftItemObject(
            getExistingGiftItemFromOptionsXML(gson, optionsMap, giftItemConfig.get()), gson, giftEngraveObj,
            servicePlanPsId));
    }

    private GiftItem getExistingGiftItemFromOptionsXML(final Gson gson, final Map<String, String> optionMap,
        final GiftItem giftItemConfig) {
    /*Override existing GiftItem config with latest available giftItem Config...
    This is helpful to handle discrepancies that occur when customer adds a giftItem to cart
    & later giftConfigurations(discount percentage/discount type) are altered
    but the OptionsXml still contains the old config*/
        GiftItem existingGiftItem = new GiftItem();
        if (Objects.nonNull(optionMap.get(CartItemOption.GIFT_ITEM.getValue()))) {
            existingGiftItem = gson.fromJson(optionMap.get(CartItemOption.GIFT_ITEM.getValue()), GiftItem.class);
        }

        // Remove existing Gift Item from DB, if Gift Item ID in request is not matching with DB
        if (!giftItemConfig.getProductId().equalsIgnoreCase(existingGiftItem.getProductId())) {
            optionMap.remove(CartItemOption.GIFT_ITEM.getValue());
            existingGiftItem = new GiftItem();
        }

        existingGiftItem.setProductId(giftItemConfig.getProductId());
        existingGiftItem.setDiscount(giftItemConfig.getDiscount());
        existingGiftItem.setDiscountType(giftItemConfig.getDiscountType());
        return existingGiftItem;
    }

    private void modifyAppleCareServicePlans(final Map addOnMessage, final Map<String, String> optionMap) {
        if (Objects.nonNull(addOnMessage.get(CartItemOption.SERVICE_PLAN.getValue()))) {
            final String servicePlanRequestPsId = (String) addOnMessage.get(CartItemOption.SERVICE_PLAN.getValue());
            if (StringUtils.isNotBlank(servicePlanRequestPsId)) {
                optionMap.put(CartItemOption.SERVICE_PLAN.getValue(), servicePlanRequestPsId);
            } else {
                optionMap.remove(CartItemOption.SERVICE_PLAN.getValue());
            }
        }
    }

    private void setCartItemQuantity(Cart cart, ShoppingCartItem shoppingCartItem) {
        final CartItem sessionCartItem = cart.getCartItems().stream().filter(ci -> ci.getId().equals(shoppingCartItem.getId()))
            .findFirst().orElse(null);
        if(Objects.nonNull(sessionCartItem)) {
            sessionCartItem.setQuantity(shoppingCartItem.getQuantity());
        }
    }

    private void modifyEngraveDetails(User user, Map giftEngraveMessage, Gson gson, CartItem cartItem, Map<String, String> optionMap) throws IllegalAccessException, InvocationTargetException, DataException {
        if (giftEngraveMessage.get(CartItemOption.ENGRAVE.getValue()) != null) {
            Engrave engrave = getEngraveInfo(
                (Map<String, ? extends Object>) giftEngraveMessage.get(CartItemOption.ENGRAVE.getValue()),
                user, cartItem);

            //In DB it is stored in OptionXML
            if (StringUtils.isBlank(engrave.getLine1()) && StringUtils.isBlank(engrave.getLine2())) {
                optionMap.remove(CartItemOption.ENGRAVE.getValue());
            } else {
                optionMap.put(CartItemOption.ENGRAVE.getValue(), getOptionsXmlEngraveObject(engrave, gson));
            }
        }
    }

    private void validateEngraveItemsQuantity(Gson gson, int quantity, Map<String, String> optionMap) throws ServiceException {
        //Quantity can not be more than 1, for items that has engrave message
        if (quantity > 1) {
            String engraveMessageJson = optionMap.get(CartItemOption.ENGRAVE.getValue());
            if (!StringUtils.isEmpty(engraveMessageJson)) {
                Engrave engraveMessage = gson.fromJson(engraveMessageJson, Engrave.class);
                if (engraveMessage.hasEngraveMessage()) {
                    throw new ServiceException(ServiceExceptionEnums.ENGRAVED_ITEMS_QUANTITY_RESTRICTION_EXCEPTION);
                }
            }
        }
    }

    private void modifyGiftMessage(Map giftEngraveMessage, Gson gson, Map<String, String> optionMap) throws IllegalAccessException, InvocationTargetException {
        if (giftEngraveMessage.get(CartItemOption.GIFT.getValue()) != null) {
            Gift giftMessage = new Gift();
            populate(giftMessage, (Map<String, ? extends Object>) giftEngraveMessage.get(CartItemOption.GIFT.getValue()));
            if (giftMessage != null) {
                if (giftMessage.hasMessageLengthExceededLimit()) {
                    throw new RequestValidationException(ServiceExceptionEnums.GIFT_MESSAGE_LENGTH_EXCEEDED_EXCEPTION.getErrorMessage());
                }
                //get giftwrap points cost
                //TODO: update this with the REST service call once PricingService is ready
                giftMessage.setGiftWrapPoints(100);
                optionMap.put(CartItemOption.GIFT.getValue(), gson.toJson(giftMessage));
            }
        }
    }

    private void processMaxQuantityRestriction(User user, Program program, int quantity, ShoppingCartItem shoppingCartItem, Product product, CartItem cartItem) throws ServiceException {
        Boolean singleItemPurchase = Boolean.valueOf(varProgramConfigHelper
            .getValue(user.getVarId(), user.getProgramId(), CommonConstants.SINGLE_ITEM_PURCHASE));

        if (CommonConstants.EXPERIENCE_DRP.equalsIgnoreCase((String)program.getConfig().get(CommonConstants.SHOP_EXPERIENCE))){
            if( Objects.nonNull(cartItem.getMaxQuantity() ) && quantity > cartItem.getMaxQuantity() ){
                throw new ServiceException(
                    ServiceExceptionEnums.QUANTITY_RESTRICTION_EXCEEDED_EXCEPTION);

            } else {
                // update cart item
                cartItem.setQuantity(quantity);
                shoppingCartItem.setQuantity(quantity);
                logger.info("{} quantity is set to {} in the shopping cart of the user {}",
                    cartItem.getProductDetail().getPsid() + " " +
                        cartItem.getProductDetail().getName(), quantity, user.getUserId());
            }
        } else {
            // Program level max quantity restriction takes precedence
            //BR-6126 qty restriction enforced
            processQuantityRestriction(user, program, quantity, shoppingCartItem,
                cartItem,product.getDefaultOffer().getQuantityRestrictionLimit(), singleItemPurchase);
        }
    }

    private void processQuantityRestriction(User user, Program program, int quantity, ShoppingCartItem shoppingCartItem, CartItem cartItem, Integer maxQuantityLimitFromProduct, Boolean singleItemPurchase) throws ServiceException {
        if ((maxQuantityLimitFromProduct == null && (singleItemPurchase && quantity > 1 &&
            quantity > Integer.parseInt(program.getConfig().getOrDefault(CommonConstants.LIMIT_MAX_QUANTITY,1).toString()))
            || (maxQuantityLimitFromProduct != null && quantity > maxQuantityLimitFromProduct)
            || (maxQuantityLimitFromProduct == null &&
            quantity > CommonConstants.MERCHANDISE_QUANTITY_RESTRICTION))) {
            throw new ServiceException(
                ServiceExceptionEnums.QUANTITY_RESTRICTION_EXCEEDED_EXCEPTION);
        } else {
            // update cart item
            cartItem.setQuantity(quantity);
            shoppingCartItem.setQuantity(quantity);
            logger.info("{} quantity is set to {} in the shopping cart of the user {}",
                cartItem.getProductDetail().getPsid() + " " +
                    cartItem.getProductDetail().getName(), quantity, user.getUserId());
        }
    }

    /**
     * Method to get Engrave Information
     *
     * @param giftEngraveMessage
     * @param user
     * @param cartItem
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws DataException
     */
    private Engrave getEngraveInfo(final Map giftEngraveMessage, final User user, final CartItem cartItem)
        throws IllegalAccessException, InvocationTargetException, DataException {

        Engrave engrave = new Engrave();
        if (MapUtils.isNotEmpty(giftEngraveMessage)) {
            populate(engrave, giftEngraveMessage);
        }

        if (Objects.nonNull(engrave)) {
            List<DataException.EngraveData> errors = new ArrayList<>();
            if (StringUtils.isNotBlank(engrave.getLine1())) {
                validateEngraveText(user, engrave.getLine1(), CommonConstants.LINE_1, errors);
            }

            if (StringUtils.isNotBlank(engrave.getLine2())) {
                validateEngraveText(user, engrave.getLine2(), CommonConstants.LINE_2, errors);
            }

            if (errors.size() > 0) {
                throw new DataException(errors);
            }
        }

        final String slugName = AppleUtil.getCategorySlug(cartItem.getProductDetail());
        final String psid = cartItem.getProductId();

        //set engrave configuration details
        engravingService.getEngravingConfiguration(user, slugName, psid, engrave);
        return engrave;
    }

    /**
     * Engrave Text Validation
     *
     * @param user
     * @param engraveLine
     * @param engraveLineNo
     * @param errors
     */
    private void validateEngraveText(final User user, final String engraveLine, final String engraveLineNo,
        final List<DataException.EngraveData> errors) {

        String containsNaughtyWord = hasNaughtyWord(user, engraveLine);
        if (null != containsNaughtyWord) {
            final String userLocale = user.getLocale().toString();
            if (containsNaughtyWord.equalsIgnoreCase(NAUGHTYBADWORD)) {
                logger.info("Engrave {} Text Contains Naughty/Bad word(s) for User Locale : {} ", engraveLineNo,
                    userLocale);
                errors.add(new DataException.EngraveData(engraveLineNo,
                    ServiceExceptionEnums.NAUGHTY_WORD_FOUND_EXCEPTION.getErrorMessage()));
            } else {
                logger.info("Engrave {} Text Contains Invalid Characters {} for User Locale : {} ", engraveLineNo,
                    containsNaughtyWord, userLocale);
                errors.add(new DataException.EngraveData(engraveLineNo, containsNaughtyWord));
            }
        }
    }

    /**
     * Exposing this method to controller to avoid Pricing Service directly accessed by the controller
     *
     * @param cart
     * @param user
     * @param program
     * @param ccAddPoints
     */
    public void callPricingServiceOnCreditCardCostForPoints(final Cart cart, final User user, final Program program,
        final Integer ccAddPoints){
        logger.info("P$ Request flow starts with addPoints: {}",ccAddPoints);
        callCalculateCartPrice(cart, user, program, ccAddPoints);
    }


    /**
     * Call Pricing service based on Cart, User and Program
     *
     * @param cart
     * @param user
     * @param program
     */
    private void callCalculateCartPrice(final Cart cart, final User user, final Program program){
        //Gift Items if available ,are added into cart as main cart item to get its pricing details
        List<CartItem> addOnItemsInCart = addAddOnItemsWithProductDetailsToCart(cart);

        pricingServiceV2.calculateCartPrice(cart, user, program);
        processCartItems(cart, addOnItemsInCart);
        logger.info("P$ call is completed");
    }

    /**
     * Creates Cart and call P$
     *
     * @param user information
     * @param program to get redemption configuration
     * @param product to convert to cart
     * @return cart object
     */
    public Cart generateCartWithPriceInfo(final User user, final Program program, final Product product) {
        Cart cart = new Cart();
        cart.setUserId(user.getUserId());

        List<CartItem> cartItems = new ArrayList<>();
        CartItem cartItem = createCartItem(product);
        cartItems.add(cartItem);
        cart.setShippingAddress(AddressMapper.getAddress(user, program));
        cart.setCartItems(cartItems);

        pricingServiceV2.calculateCartPrice(cart, user, program);
        return cart;
    }

    /**
     * Creates Cart item based on product
     *
     * @param product to get cart item
     * @return cart object
     */
    private CartItem createCartItem(final Product product) {
        final CartItem cartItem = new CartItem();
        cartItem.setAddedDate(new Date());
        cartItem.setProductId(product.getProductId());
        cartItem.setSupplierId(product.getSupplier().getSupplierId());
        cartItem.setParentProductId(product.getParentProductId());
        cartItem.setProductDetail(product);

        //NOTE: Quantity restriction is not needed as it will always be 1 for Detail Page
        cartItem.setQuantity(DEFAULT_CART_QTY);
        return cartItem;
    }

    /**
     * Call Pricing service based on Cart, User, Program and Add points
     *
     * @param cart
     * @param user
     * @param program
     * @param addPoints
     */
    private void callCalculateCartPrice(final Cart cart, final User user, final Program program,
        final Integer addPoints){
        //Gift Items if available ,are added into cart as main cart item to get its pricing details
        List<CartItem> addOnItemsInCart = addAddOnItemsWithProductDetailsToCart(cart);
        pricingServiceV2.calculateCartPrice(cart, user, program, addPoints);
        processCartItems(cart, addOnItemsInCart);
        logger.info("P$ call with AddPoints is completed");
    }

    /**
     * AddOn Items if available are added into cart as main line item to get its pricing details
     * @param cart
     * @return
     */
    private List<CartItem> addAddOnItemsWithProductDetailsToCart(final Cart cart) {
        final List<CartItem> addOnsInCart = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(cart.getCartItems())) {
            for (final CartItem cartItem : cart.getCartItems()) {
                populateGiftItemAddOnToCart(addOnsInCart, cartItem.getSelectedAddOns().getGiftItem());
                populateServicePlanAddOnToCart(addOnsInCart, cartItem.getSelectedAddOns().getServicePlan());
            }
            cart.getCartItems().addAll(addOnsInCart);
        }
        return addOnsInCart;
    }

    private void populateGiftItemAddOnToCart(final List<CartItem> addOnsInCart, final CartItem addOnItem) {
        if (Objects.nonNull(addOnItem)) {
            addOnItem.applyDiscountedGwpPromotion();
            addOnsInCart.add(addOnItem);
            populateServicePlanAddOnToCart(addOnsInCart, addOnItem.getSelectedAddOns().getServicePlan());
        }
    }

    private void populateServicePlanAddOnToCart(final List<CartItem> addOnsInCart, final CartItem addOnItem) {
        if (Objects.nonNull(addOnItem)) {
            addOnsInCart.add(addOnItem);
        }
    }

    /**
     * Gift Items that are added as main cart items in cart for pricing call must be removed
     * @param cart
     * @param giftItemsInCart
     */
    private void processCartItems(final Cart cart, final List<CartItem> giftItemsInCart) {
        if (CollectionUtils.isNotEmpty(cart.getCartItems())) {
            cart.getCartItems().removeAll(giftItemsInCart);
        }
    }

    protected int getAddPoints(final Cart updatedCart, final User user, final Program program) {
        if (Optional.ofNullable(updatedCart.getDisplayCartTotal()).isPresent() &&
            Optional.ofNullable(updatedCart.getDisplayCartTotal().getPrice()).isPresent()) {
            if (MapUtils.isNotEmpty(program.getRedemptionOptions()) && (
                program.getRedemptionOptions().containsKey(PaymentOptions.POINTSFIXED.getPaymentOption())
                    || program.getRedemptionOptions().containsKey(PaymentOptions.POINTSONLY.getPaymentOption()))) {
                if (updatedCart.getDisplayCartTotal().getPrice().getPoints() > user.getBalance()) {
                    return updatedCart.getDisplayCartTotal().getPrice().getPoints() - user.getBalance();
                }
                return 0;
            }
            return updatedCart.getDisplayCartTotal().getPrice().getPoints();
        }
        return 0;
    }

    private InstallmentOption getpayPeriod(final Integer noOfInstalment, final Installment updatedCart) {
        return  updatedCart.getInstallmentOption()
            .stream()
            .filter(installmentOption -> installmentOption.getPayPeriods()==noOfInstalment)
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks whether Product is Available along with Gift Product
     *
     * @param cartItem
     * @param user
     * @param program
     * @return
     */
    private boolean isProductAvailable(final ShoppingCartItem cartItem, final User user, final Program program) {
        final Product product = productServiceV3
            .getAppleProductDetail(cartItem.getProductId(), program, false, user,
                    true, true, false, false);
        boolean isAvailable = Objects.nonNull(product) && product.isAvailable();
        if (isAvailable) {
            final CartItem giftCartItem =
                getGiftCartItemFromOptionsXml(cartItem.convertToMap(cartItem.getOptionsXml()));
            if (Objects.nonNull(giftCartItem)) {
                final Product giftProduct = productServiceV3
                    .getAppleProductDetail(cartItem.getProductId(), program, false, user, true, true, false, false);
                isAvailable = Objects.nonNull(giftProduct) && giftProduct.isAvailable();
            }
        }
        return isAvailable;
    }

    public void verifyIsCartItemsStillAvailable(List<ShoppingCartItem> dbShoppingCartItems, Cart appleCart, User user, Program program)  {
        try {
            List<CartItem> itemsNoLongerAvailable = new ArrayList<>();
            for (ShoppingCartItem cartItem : dbShoppingCartItems ) {
                // This is a rare scenario. Item is no longer available since it was added to Cart
                // we're checking the gift item availability as well
                if (Objects.equals(cartItem.getSupplierId(), CommonConstants.APPLE_SUPPLIER_ID)
                    && !isProductAvailable(cartItem, user, program)) {
                    final CartItem appleCartItem = appleCart.getCartItems().stream().filter(ci -> ci.getId().equals
                        (cartItem.getId()))
                        .findFirst().orElse(null);
                    if(Objects.nonNull(appleCartItem)) {

                        appleCart.getCartItems().remove(appleCartItem);
                        appleCart.setCartModifiedBySystem(true);
                        //remove from DB
                        ShoppingCartItem itemToDelete = appleShoppingCartItemDao.get(cartItem.getId());
                        itemsNoLongerAvailable.add(appleCartItem);
                        appleShoppingCartItemDao.delete(itemToDelete);
                    }
                }
            }
            appleCart.setItemsNoLongerAvailable(itemsNoLongerAvailable);
        }catch (Exception ex) {
            logger.error("Exception while checking for Item Availability since it was added to Cart", ex);
        }
    }

    /**
     * verify if AddOn Promotions (Gift Item & ServicePlan) Still Valid
     *
     * @param cartItems
     * @param user
     */
    public void verifyIfAddOnsPromotionItemStillValid(final List<ShoppingCartItem> cartItems, final User user,
        final Program program) {
        Gson gson = new Gson();
        if (Optional.ofNullable(cartItems).isPresent()) {
            cartItems.forEach(shoppingCartItem -> {

                //Verify if GiftItems are still valid
                final Map<String, String> optionMap = shoppingCartItem.convertToMap(shoppingCartItem.getOptionsXml());
                verifyIfGiftItemPromotionsStillValid(user, gson, shoppingCartItem, optionMap, program);

                //Verify if Service Plans for CartItems are still valid
                verifyIfServicePlansStillValid(user, program, shoppingCartItem.getProductId(), optionMap);
                shoppingCartItem.setOptionsXml(shoppingCartItem.convertToOptionsXml(optionMap));
            });
        }
    }

    private void verifyIfServicePlansStillValid(final User user, final Program program, final String productId,
        final Map<String, String> optionMap) {
        final String selectedServicePlan = optionMap.get(CartItemOption.SERVICE_PLAN.getValue());
        if (StringUtils.isNotEmpty(selectedServicePlan) &&
            !isServicePlanStillValid(user, program, productId, selectedServicePlan)) {
            optionMap.remove(CartItemOption.SERVICE_PLAN.getValue());
            logger.info("Service Plan {} is no longer valid & it is removed from cart...", selectedServicePlan);
        }
    }

    private boolean isServicePlanStillValid(final User user, final Program program, final String productId,
        final String servicePlanSelected) {
        boolean isServicePlanStillValid = false;
        if (getProgramConfigValueAsBoolean(program, ENABLE_APPLE_CARE_SERVICE_PLAN)) {
            // this PS Call will simultaneously verify if Service Plan is currently eligible for cart Item & also
            // productId is currently available
            final Product product = productServiceV3
                .getAppleProductDetail(productId, program, false, user, true, true, false, false);

            if (Objects.nonNull(product) && product.isAvailable()) {
                List<Product> servicePlansList = product.getAddOns().getServicePlans();

                if (CollectionUtils.isNotEmpty(servicePlansList)) {
                    isServicePlanStillValid = servicePlansList.stream()
                        .anyMatch(servicePlanItem -> servicePlanItem.getPsid().equalsIgnoreCase(servicePlanSelected));
                }
            }
        }
        return isServicePlanStillValid;
    }

    private void verifyIfGiftItemPromotionsStillValid(final User user, final Gson gson,
        final ShoppingCartItem shoppingCartItem, final Map<String, String> optionMap, final Program program) {
        final CartItem giftCartItem = getGiftCartItemFromOptionsXml(optionMap);
        if (Objects.nonNull(giftCartItem)) {
            final List<GiftItem> giftItemsFromDB =
                giftPromoService.getGiftItemList(user, shoppingCartItem.getProductId(), program);

            final Optional<GiftItem> eligibleGiftItemOptional = giftItemsFromDB.stream()
                .filter(availableGiftItem ->
                    availableGiftItem.getProductId().equalsIgnoreCase(giftCartItem.getProductId()))
                .findFirst();

            if (eligibleGiftItemOptional.isPresent()) {
                try {
                    final GiftItem eligibleGiftItem = eligibleGiftItemOptional.get();
                    final GiftItem existingGiftItem =
                        gson.fromJson(optionMap.get(CartItemOption.GIFT_ITEM.getValue()), GiftItem.class);

                    existingGiftItem.setProductId(eligibleGiftItem.getProductId());
                    existingGiftItem.setDiscount(eligibleGiftItem.getDiscount());
                    existingGiftItem.setDiscountType(eligibleGiftItem.getDiscountType());

                    //Verify if added servicePlan for the giftItem is still available
                    final String servicePlanSelected = existingGiftItem.getServicePlan();
                    if (StringUtils.isNotEmpty(servicePlanSelected) &&
                        !isServicePlanStillValid(user, program, eligibleGiftItem.getProductId(), servicePlanSelected)) {
                        existingGiftItem.setServicePlan(null);
                        logger.info("Service Plan {} is no longer valid & it is removed from cart...",
                            servicePlanSelected);
                    }

                    optionMap.put(CartItemOption.GIFT_ITEM.getValue(),
                        getOptionsXmlGiftItemObject(existingGiftItem, gson, null, null));

                    shoppingCartItem.setOptionsXml(shoppingCartItem.convertToOptionsXml(optionMap));
                } catch (Exception e) {
                    logger.error(
                        "Exception occurred while validating GiftItem Promotions. Qualifying Item:{} - Gift Item: {}",
                        shoppingCartItem.getProductId(), giftCartItem.getProductId(), e);
                }
            } else {
                // Gift promotion is no longer available
                // update OptionXml in DB
                optionMap.remove(CartItemOption.GIFT_ITEM.getValue());
                shoppingCartItem.setOptionsXml(shoppingCartItem.convertToOptionsXml(optionMap));
            }
        }
    }

    private CartItem getGiftCartItemFromOptionsXml(final Map<String, String> optionMap) {
        String freeGiftItemMessageJson = optionMap.get(CartItemOption.GIFT_ITEM.getValue());
        Gson gson = new Gson();

        return gson.fromJson(freeGiftItemMessageJson, CartItem.class);
    }

    private boolean hasCartChangedSinceLoaded(User user, Cart sessionCart) {

        ShoppingCart shoppingCart = appleShoppingCartDao.get(user.getVarId(), user.getProgramId(), user.getUserId());
        if (shoppingCart == null) {
            return true;
        }
        List<ShoppingCartItem> dbShoppingCartItems = appleShoppingCartItemDao.getShoppingCartItems(shoppingCart);
        //db cart & session cart is empty
        if (dbShoppingCartItems == null && sessionCart.getCartItems() == null) {
            return false;
        }

        //cart has been modified
        if ((dbShoppingCartItems == null && sessionCart.getCartItems() != null) ||
            (dbShoppingCartItems != null && sessionCart.getCartItems() == null)) {
            sessionCart.setCartModifiedByUser(true);
            return true;
        }

        List<ShoppingCartItem> dbShoppingCartItemsWithoutAMP = dbShoppingCartItems.stream()
            .filter(dbCartItem -> !dbCartItem.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_AMP))
            .collect(Collectors.toList());

        if (dbShoppingCartItemsWithoutAMP != null && sessionCart.getCartItems() != null && dbShoppingCartItemsWithoutAMP.size() != sessionCart.getCartItems().size() ) {
            sessionCart.setCartModifiedByUser(true);
            return true;
        }

        //quantity check & timestamp check
        return validateQuantityAndTimeStamp(sessionCart, dbShoppingCartItemsWithoutAMP) ;
    }

    private boolean validateQuantityAndTimeStamp(Cart sessionCart, List<ShoppingCartItem> dbShoppingCartItems) {
        if (dbShoppingCartItems != null && dbShoppingCartItems.size() > 0 && sessionCart.getCartItems() != null) {
            //quantity check
            if (dbShoppingCartItems.size() != sessionCart.getCartItems().size()) {
                sessionCart.setCartModifiedByUser(true);
                return true;
            }

            //get the first item, that has the latest timestamp
            ShoppingCartItem dbShoppingCartItem = dbShoppingCartItems.get(0);
            CartItem sesssionCartItemForSpecificId = sessionCart.getCartItems().stream().filter(ci -> ci.getId().equals
                (dbShoppingCartItem.getId()))
                .findFirst().orElse(null);

            //Timestamp check
            if (sesssionCartItemForSpecificId == null || !(sesssionCartItemForSpecificId.getAddedDate().getTime() == (dbShoppingCartItem.getAddedDate().getTime())) ) {
                sessionCart.setCartModifiedByUser(true);
                return true;
            }
        }
        return false;
    }

    private boolean isEngraveTextWhiteList(User user, String engraveText){

        final List<WhiteListWord> whiteListWords = whiteListWordDao.getWhitelistWords(user.getLocale(),user.getLocale().getLanguage());

        //match whole word
        if(Objects.nonNull(whiteListWords) && whiteListWords.size() > 0){
            List<String> matchFullWhiteListWords = whiteListWords.stream()
                .filter(ci -> ci.getMatchWholeWord() == 1)
                .map(w -> w.getPattern())
                .collect(Collectors.toList());


            //Words that should start with
            List<String> matchStartWhiteListWords = whiteListWords.stream()
                .filter(ci -> ci.getMatchWholeWord() == 2)
                .map(w -> w.getPattern())
                .collect(Collectors.toList());

            Pattern wholeWordWhiteListPattern = buildRegExFromList(matchFullWhiteListWords, 1);
            Pattern startWhiteListPattern = buildRegExFromList(matchStartWhiteListWords, 2);

            if ((Objects.nonNull(wholeWordWhiteListPattern) && wholeWordWhiteListPattern.matcher(engraveText).find())
                ||(Objects.nonNull(startWhiteListPattern)&& startWhiteListPattern.matcher(engraveText).find())){
                logger.info("Engrave Text Contains WhiteList word for User Locale : {}", user.getLocale());
                return true;
            }
        }
        return false;
    }

    /**
     * Restrict engrave process if engrave message has any bad, restricted word(s)
     * @param user
     * @param engraveText
     * @return
     */
    public String hasNaughtyWord(User user, String engraveText) {
        //If engraved text is whitelist then no naughty word, allow the word
        if(isEngraveTextWhiteList(user,engraveText)){
            return null;
        }

        List<NaughtyWord> naughtyWords = new ArrayList<>();

        logger.info("Loading Naughty Word list from DB...");
        naughtyWords = naughtyWordDao.getByLocaleOrLanguage(user.getLocale(), user.getLocale().getLanguage());

        if (naughtyWords != null && naughtyWords.size() > 0) {
            //Words that need to be searched in any where in the search string
            List<String> anyPartOfWords = naughtyWords.stream()
                .filter(ci -> ci.getMatchWholeWord() == 0)
                .map(w -> w.getPattern())
                .collect(Collectors.toList());

            //Words that need to be searched as a single word by itself
            List<String> matchFullWord = naughtyWords.stream()
                .filter(ci -> ci.getMatchWholeWord() == 1)
                .map(w -> w.getPattern())
                .collect(Collectors.toList());

            //Words that should start with
            List<String> startWord = naughtyWords.stream()
                .filter(ci -> ci.getMatchWholeWord() == 2)
                .map(w -> w.getPattern())
                .collect(Collectors.toList());

            //Words that should end with
            List<String> endWord = naughtyWords.stream()
                .filter(ci -> ci.getMatchWholeWord() == 3)
                .map(w -> w.getPattern())
                .collect(Collectors.toList());

            logger.info("Building RegEx for naughty word list...");
            if (isNaughtyBadWord(engraveText, anyPartOfWords, matchFullWord, startWord, endWord)) {
                logger.info("Engrave Text Contains Naughty/Bad word(s) for User Locale : {}", user.getLocale());
                return NAUGHTYBADWORD;
            }else {
                logger.info("Engrave Text has NO  Naughty/Bad word(s) for User Locale : {} ", user.getLocale());
                return processEngraveText(user, engraveText);
            }
        }
        else {
            logger.info("Naughty word list is Empty in DB for this User Locale: {}", user.getLocale());
            return null;
        }
    }

    private String processEngraveText(User user, String engraveText) {
        String invalidCharset = validateCharset(user, engraveText);
        String disallowedChars = validateDisallowedCharacters(user, engraveText);
        String invalidChars = validateCharacters(user, engraveText);
        String finalStr = "";
        finalStr = finalStr + (invalidCharset == null? "" : invalidCharset);
        finalStr = finalStr + (disallowedChars == null? "" : disallowedChars);
        finalStr = finalStr + (invalidChars == null? "" : invalidChars);
        if(!finalStr.equals("")){
            finalStr = Stream.of(finalStr.split(""))
                .distinct()
                .collect(Collectors.joining());
            logger.info("These characters cannot be engraved: {} .",finalStr);
            return finalStr;
        }
        return null;
    }

    private boolean isNaughtyBadWord(String engraveText, List<String> anyPartOfWords,
        List<String> matchFullWord, List<String> startWord, List<String> endWord) {
        Pattern regExPattern1 = buildRegExFromList(anyPartOfWords, 0); //no word boundaries needed
        Pattern regExPattern2 = buildRegExFromList(matchFullWord, 1); // need word boundary
        Pattern regExPattern3 = buildRegExFromList(startWord, 2); //should not start
        Pattern regExPattern4 = buildRegExFromList(endWord, 3); //should not end

        return (regExPattern1 != null && regExPattern1.matcher(engraveText).find())
            || (regExPattern2 != null && regExPattern2.matcher(engraveText).find())
            || (regExPattern3 != null && regExPattern3.matcher(engraveText).find())
            || (regExPattern4 != null && regExPattern4.matcher(engraveText).find());
    }

    private String validateCharset(User user, String engraveText) {
        String invalidCharset = checkForCharset(engraveText,user.getLocale().toString());
        if(invalidCharset != null){
            logger.info("Engrave Text contains Invalid charset for User Locale: {} ", user.getLocale());
        }
        return invalidCharset;
    }

    private String validateDisallowedCharacters(User user, String engraveText) {
        String regExForGlobalDisallowedChars = CommonConstants.disallowedCharsRegexPattern.get(String.valueOf("-1")) != null ?
            (CommonConstants.disallowedCharsRegexPattern.get(String.valueOf("-1"))) : null;
        String regExForDisallowedChars = CommonConstants.disallowedCharsRegexPattern.get(String.valueOf(user.getLocale())) != null ?
            (CommonConstants.disallowedCharsRegexPattern.get(String.valueOf(user.getLocale()))) : null;
        StringBuilder stringBuilder = new StringBuilder();
        if(regExForDisallowedChars != null){
            stringBuilder.append("[")
                .append(regExForGlobalDisallowedChars)
                .append(regExForDisallowedChars)
                .append("]");
        }else {
            stringBuilder.append("[")
                .append(regExForGlobalDisallowedChars)
                .append("]");
        }
        regExForDisallowedChars = stringBuilder.toString();

        String disallowedChars = null;
        if(regExForDisallowedChars != null) {

            String allowedChars = engraveText.replaceAll(regExForDisallowedChars, "");
            if (!engraveText.equals(allowedChars)) {
                logger.info("Engrave Text contains disallowed character for User Locale : {}", user.getLocale());
                if(StringUtils.isNotBlank(allowedChars)){
                    allowedChars = "["+allowedChars+"]";
                    disallowedChars = engraveText.replaceAll(allowedChars, "");
                }else{
                    disallowedChars = engraveText;
                }

            }
        }
        return disallowedChars;
    }

    private String validateCharacters(User user, String engraveText) {
        String regExForSpecialChars = CommonConstants.regexPattern.get(String.valueOf(user.getLocale())) != null ?
            (CommonConstants.regexPattern.get(String.valueOf(user.getLocale()))) : null;
        String invalidChars = null;
        if (regExForSpecialChars != null) {  //Using replaceAll to find invalid characters
            invalidChars = engraveText.replaceAll(regExForSpecialChars, "");
            if (invalidChars != null && invalidChars.length() > 0) {
                logger.info("Engrave Text contains Special character for User Locale : {} ", user.getLocale());

            } else {
                logger.info("Parsed special char {}", invalidChars);
            }
        } else {
            logger.warn("RegEx Pattern is not found for User Locale : {}", user.getLocale());
        }
        return invalidChars;
    }

    public String checkForCharset(String engraveText, String locale){
        Charset charset = null;
        switch(locale){
            case "zh_HK":
                charset = Charset.forName("Big5-HKSCS");
                break;
            case "zh_TW":
                charset = Charset.forName("Big5");
                break;
            case "th_TH":
                charset = Charset.forName("TIS-620");
                break;
            default:
                break;
        }
        if(charset != null) {
            final Charset characterSet = charset;
            String invalidCharacters = Stream.of(engraveText.split(""))
                .filter(character -> !characterSet.newEncoder().canEncode(character))
                .collect(Collectors.joining());
            if(invalidCharacters != null && !invalidCharacters.equals("")){
                return invalidCharacters;
            }
        }
        return null;
    }

    /**
     * Build/Construct regex for the naughty word list
     * @param offensiveKeywords
     * @return
     */
    private Pattern buildRegExFromList(List<String> offensiveKeywords, Integer useWordBoundaries) {

        StringBuffer pipeDelimitted = new StringBuffer();
        if (offensiveKeywords != null && offensiveKeywords.size() > 0) {
            logger.info("Constructing RegEx pattern from loaded Naughty Word list...");
            if (useWordBoundaries == 1) {
                pipeDelimitted.append("\\b");
                pipeDelimitted.append(StringUtils.join(offensiveKeywords.toArray(), "\\b|\\b"));
                pipeDelimitted.append("\\b");
            }
            else if(useWordBoundaries == 2){
                pipeDelimitted.append("\\b");
                pipeDelimitted.append(StringUtils.join(offensiveKeywords.toArray(), "|\\b"));
            }
            else if(useWordBoundaries == 3){
                pipeDelimitted.append(StringUtils.join(offensiveKeywords.toArray(), "\\b|"));
                pipeDelimitted.append("\\b");
            }
            else {
                pipeDelimitted.append(StringUtils.join(offensiveKeywords.toArray(), "|"));
            }

            logger.info("RegEx pattern for Naughty words : {} ", pipeDelimitted);
            return Pattern.compile(pipeDelimitted.toString(), Pattern.CASE_INSENSITIVE);

        }

        return null;
    }

    public ShoppingCart getShoppingCart(User user) {
        try {
            ShoppingCart shoppingCart = appleShoppingCartDao.get(user.getVarId(), user.getProgramId(), user.getUserId());
            if (shoppingCart != null) {
                List<ShoppingCartItem> shoppingCartItems = appleShoppingCartItemDao.getShoppingCartItems(shoppingCart);
                shoppingCart.setShoppingCartItems(shoppingCartItems);
                return shoppingCart;
            }
            else {
                return new ShoppingCart();
            }
        }
        catch (Exception ex) {
            logger.error("Error loading Apple Shopping Cart  ", ex);
            throw ex;
        }
    }

    public boolean canAddToCart(final Cart cart, final Program program, AddToCartResponse addToCartResponse,
        Product product) {
        final Optional<Object> cartSizeObj =
            Optional.ofNullable(program.getConfig().get(CommonConstants.MAX_CART_SIZE_CONFIG_KEY));
        final int cartSize =
            cartSizeObj.isPresent() ? Integer.parseInt(cartSizeObj.get().toString()) : CommonConstants.CART_SIZE;
        if (cart.getCartItems().size() >= cartSize) {
            logger.info("Cart Full!");
            return false;
        }
        return isGiftCardValidationsSuccessful(cart, program, addToCartResponse, product);
    }

    private boolean isGiftCardValidationsSuccessful(final Cart cart, final Program program,
        final AddToCartResponse addToCartResponse, final Product product) {
        // Quantity restriction for Giftcards
        if (product.getSupplier().getSupplierId() == CommonConstants.SUPPLIER_TYPE_GIFTCARD &&
            CollectionUtils.isNotEmpty(cart.getCartItems()) &&
            cart.getCartItems()
                .stream()
                .filter(item -> item.getProductDetail().getPsid().equals(product.getPsid()))
                .count() > 0) {
            addToCartResponse.setGiftcardMaxQuantity(true);
            return false;
        }

        // total value restriction for physical Giftcards
        if (program.getConfig().get(CommonConstants.PHYSICALGIFTCARDMAXVALUE) != null &&
            product.getSupplier().getSupplierId().intValue() == CommonConstants.SUPPLIER_TYPE_GIFTCARD &&
            !ShippingMethod.ELECTRONIC.getLabel().equals(product.getFormat())) {

            int totalValue = cart.getCartItems()
                .stream()
                .filter(item -> CommonConstants.GIFTCARDS_PHYSICAL.equals(item.getProductDetail().getOptionValue(CommonConstants.GIFTCARDS_DELIVERYMETHOD)))
                .mapToInt(item -> {
                    if( !StringUtils.isEmpty(item.getProductDetail().getOptionValue(CommonConstants.GIFTCARDS_DENOMINATION)) ) {
                        return Integer.parseInt( item.getProductDetail().getOptionValue(CommonConstants.GIFTCARDS_DENOMINATION).substring(1) ) * item.getQuantity();
                    }
                    return 0;
                }).sum();

            if(product.getVariationDimensionNameValues().get(CommonConstants.GIFTCARDS_DENOMINATION) != null){
                totalValue += Integer.parseInt(product.getVariationDimensionNameValues().get(CommonConstants.GIFTCARDS_DENOMINATION).substring(1));
                int physicalGiftcardMaxValue = Integer.parseInt(program.getConfig().get(CommonConstants.PHYSICALGIFTCARDMAXVALUE).toString());
                if (totalValue > physicalGiftcardMaxValue) {
                    addToCartResponse.setPhysicalGiftcardTotalValueFull(true);
                    addToCartResponse.setPhysicalGiftcardMaxValue(physicalGiftcardMaxValue);
                    return false;
                }
            }
        }
        return true;
    }

    private void calculatePayPeriodPrice(final int qty, final Product product1) {
        final Offer offer = product1.getDefaultOffer();
        if (Objects.nonNull(offer) && offer.getPayPeriods() > 0) {
            offer.setPayPerPeriodTotalPrice(BigDecimal.valueOf(offer.getB2sItemPrice().getAmount())
                .multiply(BigDecimal.valueOf(qty))
                .divide(BigDecimal.valueOf(offer.getPayPeriods()), 2, RoundingMode.CEILING)
                .doubleValue());
        }

    }

    private void setGiftCardMaxQuantity(CartItem cartItem, Program program){
        if(Integer.valueOf(CommonConstants.SUPPLIER_TYPE_GIFTCARD).equals(cartItem.getSupplierId())){
            if(ShippingMethod.ELECTRONIC.getLabel().equals(cartItem.getShippingMethod())){
                if(Objects.nonNull(program.getConfig().get(CommonConstants.MAXPURCHASEQUANTITY_E_GC))){
                    cartItem.setGiftCardMaxQuantity(Integer.parseInt(program.getConfig().get(CommonConstants.MAXPURCHASEQUANTITY_E_GC).toString()));
                }
            } else {
                if(Objects.nonNull(program.getConfig().get(CommonConstants.MAXPURCHASEQUANTITY_P_GC))){
                    cartItem.setGiftCardMaxQuantity(Integer.parseInt(program.getConfig().get(CommonConstants.MAXPURCHASEQUANTITY_P_GC).toString()));
                }
            }
        }
    }

    private Money buildDenominationMoney(final Map<String, String> denominationMap, final CurrencyUnit currency) {
        if (denominationMap == null || StringUtils.isEmpty(denominationMap.get(DENOMINATION_KEY))) {
            return Money.zero(currency);
        }
        return Money.of(currency, new BigDecimal(denominationMap.get(DENOMINATION_KEY).substring(DENOMINATION_CURRENCY_SYMBOL_POSITION)));
    }

    public Installment calculatePriceForAllInstallment(final Integer quantity, final Program program, final Double price) {
        final Installment totalInstallments = new Installment();
        final List<InstallmentOption> installmentOptions = new ArrayList<>();
        if (StringUtils.isNotBlank((String) program.getConfig().get(CommonConstants.INSTALLMENT))) {
            final String[] payPeriods = program.getConfig().get(CommonConstants.INSTALLMENT).toString().split(",");
            Arrays.stream(payPeriods).forEach(payPeriod -> {
                final InstallmentOption totalInstallment = new InstallmentOption();
                totalInstallment.setPayPeriods(Integer.parseInt(payPeriod));
                Double payPerPeriod = 0.0;
                if(Objects.nonNull(program) && Objects.nonNull(price)) {
                    // This will show exact payPerPeriod including decimal places
                    payPerPeriod = new BigDecimal(price)
                        .multiply(new BigDecimal(quantity))
                        .divide(new BigDecimal(payPeriod), CommonConstants.TWO, RoundingMode.CEILING)
                        .doubleValue();
                }
                totalInstallment.setPayPerPeriod(payPerPeriod);
                installmentOptions.add(totalInstallment);
            });
            totalInstallments.setInstallmentOption(installmentOptions);
            totalInstallments.setSelectedInstallement(getpayPeriod(Integer.parseInt(program.getConfig().get(CommonConstants.PAY_PERIODS).toString()),totalInstallments));

        }
        return totalInstallments;
    }

    private boolean isDiscount(final Cart cart) {
        if(Objects.isNull(cart)) {
            return false;
        }
        return CollectionUtils.isNotEmpty(cart.getDiscounts());
    }

    /**
     * Set Correct Font code before persisting the engrave object in shopping_cart_items table(options_xml)
     * Removes Engrave attributes that are not needed to persist in DB
     *
     * @param engrave
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Engrave trimEngraveObject(final Engrave engrave) throws IllegalAccessException,
        InvocationTargetException{
        final String exactFontCode = getFontCode(engrave);

        final Engrave engraveOptionsXml = new Engrave();
        BeanUtils.copyProperties(engraveOptionsXml, engrave);
        engraveOptionsXml.setFontCode(exactFontCode);
        //Nullify FontConfigurations as it is not needed to persist in DB and to Overcome DataTruncation Exception
        engraveOptionsXml.setEngraveFontConfigurations(null);
        return engraveOptionsXml;
    }

    /**
     * Converts Engrave object to String
     *
     * @param engrave Model object
     * @param gson converter to Json
     * @return engrave object in string
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private String getOptionsXmlEngraveObject(final Engrave engrave, Gson gson) throws IllegalAccessException,
        InvocationTargetException{
        return gson.toJson(trimEngraveObject(engrave));
    }

    /**
     * Get Font code based on Engrave Character length
     * @param engrave Model object
     * @return fontCode String
     */
    private String getFontCode(final Engrave engrave){
        String engraveFontCode = engrave.getFontCode();

        if(CollectionUtils.isNotEmpty(engrave.getEngraveFontConfigurations())){
            final int lineLength = StringUtils.isNotBlank(engrave.getLine1()) ?
                engrave.getLine1().length(): engrave.getLine2().length();

            Optional<EngraveFontConfiguration> engraveFont = engrave.getEngraveFontConfigurations().stream()
                .filter(engraveFontConfiguration -> engraveFontConfiguration.getCharLengthFrom()<=lineLength)
                .max(Comparator.comparing(EngraveFontConfiguration::getCharLengthFrom));

            if(engraveFont.isPresent()){
                engraveFontCode = engraveFont.get().getFontCode();
            }
        }

        return engraveFontCode;
    }

    /**
     * Get GiftItemOptionsXML to be persisted from Gift Item object
     *
     * @param giftItem
     * @param gson
     * @param giftEngraveObj
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private String getOptionsXmlGiftItemObject(final GiftItem giftItem, Gson gson, final Engrave giftEngraveObj,
        final String servicePlanPsId)
        throws IllegalAccessException, InvocationTargetException {
        if (Objects.nonNull(giftEngraveObj)) {
            if (StringUtils.isBlank(giftEngraveObj.getLine1()) && StringUtils.isBlank(giftEngraveObj.getLine2())) {
                giftItem.setEngrave(null);
            } else {
                giftItem.setEngrave(trimEngraveObject(giftEngraveObj));
            }
        }

        //Empty but not null "ServicePlan" object handles Delete ServicePlan request
        if (Objects.nonNull(servicePlanPsId)) {
            if (StringUtils.isNotBlank(servicePlanPsId)) {
                giftItem.setServicePlan(servicePlanPsId);
            } else {
                giftItem.setServicePlan(null);
            }
        }
        return gson.toJson(giftItem);
    }

    private ShoppingCartItem getAMPShoppingCartItem(final ShoppingCart shoppingCart, final Map modifyMessage) {
        ShoppingCartItem ampSubscriptionItem=new ShoppingCartItem();
        String productId = String.valueOf(modifyMessage.get(CommonConstants.ITEM_ID));
        ampSubscriptionItem.setAddedDate(new Date());
        ampSubscriptionItem.setMerchantId(CommonConstants.APPLE_MERCHANT_ID);
        ampSubscriptionItem.setProductId(productId);
        ampSubscriptionItem.setProductName(productId);
        ampSubscriptionItem.setQuantity((Integer)modifyMessage.get(CommonConstants.QUANTITY));
        ampSubscriptionItem.setSupplierId(CommonConstants.AMP_SUPPLIER_ID);
        ampSubscriptionItem.setShoppingCart(shoppingCart);
        return ampSubscriptionItem;
    }
}
