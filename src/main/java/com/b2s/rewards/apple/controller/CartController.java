package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.CartService;
import com.b2s.apple.services.EngravingService;
import com.b2s.common.services.discountservice.CouponDetails;
import com.b2s.common.services.discountservice.CouponError;
import com.b2s.common.services.discountservice.DiscountServiceClient;
import com.b2s.common.services.exception.DataException;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.integration.model.AddToCartResponse;
import com.b2s.rewards.apple.integration.model.UA.PromotionalSubscription;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.ContextUtil;
import com.b2s.rewards.apple.validator.AddressMapper;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.merchandise.action.CartCalculationUtil;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.service.product.client.exception.EntityNotFoundException;
import com.b2s.service.product.client.exception.RequestValidationException;
import com.b2s.shop.common.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.b2s.rewards.apple.util.AppleUtil.getProgramConfigValueAsBoolean;
import static com.b2s.rewards.common.util.CommonConstants.ENABLE_APPLE_CARE_SERVICE_PLAN;

/**
 * All cart related calls reside in this controller
 *
 * @author Ssrinivasan
 */
@RestController
@RequestMapping(value="/cart", produces = "application/json;charset=UTF-8")
@ResponseBody
public class CartController {

    public static final String CACHE_CONTROL = "Cache-control";
    public static final String NO_CACHE_NO_STORE = "no-cache, no-store";
    public static final String PRAGMA = "Pragma";
    public static final String NO_CACHE = "no-cache";
    public static final String THERE_ARE_NO_ITEMS_IN_THE_CART = "There are no items in the Cart";
    public static final String THERE_ARE_NO_ITEMS_IN_THE_CART1 = "There are no items in the Cart ";
    public static final String FAILED_TO_LOAD_CART = "Failed to load cart...";
    public static final String EXCEPTION_IN_MODIFYING_CART_ITEM = "Exception in Modifying CartItem... ";

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductServiceV3 productServiceV3;

    @Autowired
    private ContextUtil contextUtil;

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    private DiscountServiceClient discountServiceClient;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private EngravingService engravingService;

    @Autowired
    private AppSessionInfo appSessionInfo;

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    //Filter out all categories and parents except the first one.
    private static CartItem filterCategories(final CartItem cartItem) {

        if(Objects.nonNull(cartItem.getProductDetail()) && CollectionUtils.isNotEmpty(cartItem.getProductDetail().getCategories())) {
            final Category category =
                    cartItem.getProductDetail().getCategories().get(0);
            if (!category.getParents().isEmpty()) {
                final Category parentCategory = category.getParents().get(0);
                category.setParents(Collections.singletonList(parentCategory));
            }
            cartItem.getProductDetail().setCategory(Collections.singletonList(category));
        }
        return cartItem;
    }

    /**
     * Get current cart content, if any. Otherwise returns '204-No Content' status code
     *
     * @return
     */
    @RequestMapping(value = "",method = RequestMethod.GET)
    public ResponseEntity<Object> getCart(final HttpServletRequest request,
                                          final HttpServletResponse response) {
        response.setHeader(CACHE_CONTROL, NO_CACHE_NO_STORE);
        response.setHeader(PRAGMA, NO_CACHE);
        final User user = appSessionInfo.currentUser();
        try {
            XSSRequestWrapper servletRequest = new XSSRequestWrapper(request);
            logger.info("Cart : getCart");
            final Cart sessionCart = getCart(user, servletRequest);
            final Cart clonedSessionCart = getModifiedCart(sessionCart);

            return ResponseEntity.ok(clonedSessionCart);
        } catch (EntityNotFoundException enfe) {
            logger.error(THERE_ARE_NO_ITEMS_IN_THE_CART);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(THERE_ARE_NO_ITEMS_IN_THE_CART1);
        } catch (ServiceException se) {
            logger.error(FAILED_TO_LOAD_CART, se.getMessage(), se);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(se.getMessage());
        } catch (final Exception se) {
            logger.error("Exception in Loading Cart... ", se);
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT.INTERNAL_SERVER_ERROR)
                    .body((!StringUtils.isEmpty(se.getMessage()) ? se.getMessage() + " " : "Exception  in Loading Cart... ")
                    );
        }
    }

    private Cart getModifiedCart(final Cart sessionCart) {
        // Cloning the Cart to a simplified cart with only 1 category and setting total cart items count
        final Cart clonedSessionCart = copy(sessionCart);
        clonedSessionCart.setCartItemsTotalCount(
            clonedSessionCart.getCartItems()
                .stream()
                .map(cartItem -> filterCategories(cartItem))
                .filter(cartItem -> Objects.nonNull(cartItem.getQuantity()))
                .collect(Collectors.summingInt(cartItem -> cartItem.getQuantity())));
        return clonedSessionCart;
    }

    private Cart getCart(
            final User user,
            final HttpServletRequest servletRequest) throws ServiceException {

        final HttpSession session = servletRequest.getSession();
        boolean sessionCartIsEmpty = false;
        //ShippingAddress : get cart from current session, as it is not stored in DB
        Cart sessionCart = appSessionInfo.getSessionCart();
        final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        if (sessionCart != null ) {
            //reset user & system modified flag to FALSE, as this need to be recalculated everytime
            sessionCart.setCartModifiedByUser(false);
            sessionCart.setCartModifiedBySystem(false);
            // Reset CartTotalModified
            sessionCart.setCartTotalModified(false);
            if(sessionCart.getShippingAddress() != null) {
                sessionCart.getShippingAddress().setCartTotalModified(false);
            }
        }
        else {
            sessionCartIsEmpty = true;
        }
        sessionCart = cartService.getCart(user, sessionCart, program);
        if (sessionCartIsEmpty) {
            sessionCart.setShippingAddress(AddressMapper.getAddress(user, program));
        }

        if(!user.getAdditionalInfo().isEmpty() &&
                StringUtils.isNotBlank(user.getAdditionalInfo().get(CommonConstants.UA_SERVICE_SUBSCRIPTION_DISPLAY_CHECKBOX))){
            PromotionalSubscription promo=new PromotionalSubscription();
            promo.setDisplayCheckbox(Boolean.valueOf(user.getAdditionalInfo().get(CommonConstants.UA_SERVICE_SUBSCRIPTION_DISPLAY_CHECKBOX)));
            promo.setChecked(Boolean.valueOf(user.getAdditionalInfo().get(CommonConstants.UA_SERVICE_SUBSCRIPTION_IS_CHECKED)));
            sessionCart.setPromotionalSubscription(promo);
        }

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, sessionCart);
        if(logger.isDebugEnabled()) {
            logger.debug("Printing cart response in getCart method: {}", toJson(sessionCart));
        }
        return sessionCart;
    }

    //Generate a new Cart object using groovy JSON Parser
    private Cart copy(final Cart cart){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsonNode.class, new InstanceCreator<JsonNode>(){
                    public JsonNode createInstance(Type type) {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.createObjectNode();
                    }
                }).create();
        return gson.fromJson(gson.toJson(cart), Cart.class);
    }

    private String toJson(Object pojos) {
        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return (fieldAttributes.getName().equals("categories")
                        ||fieldAttributes.getName().equals("additionalInfo"));
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        }).create();
        return gson.toJson(pojos);
    }

    /**
     * Modify the cart
     *
     * @param modifyCartRequest - @ModifyCartRequest object
     * @return - Updated Cart information
     * @throws Exception
     */
    @RequestMapping(value ="/{cartId}",  method = RequestMethod.PUT)
    public ResponseEntity<Object> modifyCart(
            @PathVariable final String cartId,
            @RequestBody final ModifyCartRequest modifyCartRequest,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws Exception {
        final User user = appSessionInfo.currentUser();
        try {
            XSSRequestWrapper servletRequest = new XSSRequestWrapper(request);
            logger.info("Cart : modifyCart - put");
            if(org.apache.commons.lang.StringUtils.isNotBlank(cartId) && modifyCartRequest != null) {
                return getModifiedCart(modifyCartRequest, user, servletRequest, response);
            }else {
                return ResponseEntity.badRequest().body("Bad request. Access the modify cart request with valid data");
            }

        } catch (EntityNotFoundException enfe) {
            logger.error(THERE_ARE_NO_ITEMS_IN_THE_CART);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(THERE_ARE_NO_ITEMS_IN_THE_CART1);
        } catch (ServiceException se) {
            logger.error(FAILED_TO_LOAD_CART, se.getMessage(), se);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(se.getMessage());
        }  catch (final Exception se) {
            logger.error("Exception in modifying cart for the user: {} having var id: {}, program id: {} ", user.getUserId(), user.getVarId(), user.getProgramId(),se);
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT.INTERNAL_SERVER_ERROR).body(
                    (!StringUtils.isEmpty(se.getMessage()) ? se.getMessage() + " " : "Exception in modifying cart... "));
        }
    }

    private ResponseEntity<Object> getModifiedCart(
            @RequestBody final ModifyCartRequest modifyCartRequest,
            final User user,
            final HttpServletRequest servletRequest, final HttpServletResponse response)
            throws ServiceException, IllegalAccessException, InvocationTargetException {
        response.setHeader(CACHE_CONTROL, NO_CACHE_NO_STORE);
        response.setHeader(PRAGMA, NO_CACHE);
        double preModifiedCartTotal = 0.0d;
        final Cart cart = getCart(user, servletRequest);
        if(cart != null) {
            preModifiedCartTotal = CartCalculationUtil.getCartTotalAmount(cart);
            BeanUtils.copyProperties(cart, modifyCartRequest);

            if (StringUtils.isNotBlank(modifyCartRequest.getTimeZoneId())) {
                cart.setTimeZoneId(modifyCartRequest.getTimeZoneId());
            } else {
                cart.setTimeZoneId(TimeZone.getDefault().getID());
            }
        }

        final double currentCartTotal = CartCalculationUtil.getCartTotalAmount(cart);
        final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        final Object disableCartTotalModifiedPopUp = program.getConfig().get(CommonConstants.DISABLE_CART_TOTAL_MODIFIED_POP_UP);
        if((disableCartTotalModifiedPopUp==null || !(boolean)disableCartTotalModifiedPopUp ) && Objects.nonNull(cart)) {
            cart.setCartTotalModified(
                    CartCalculationUtil.isCartTotalModified(preModifiedCartTotal, currentCartTotal));
        }
        return ResponseEntity.ok(cart);
    }

    /**
     * Add the given PSID item to cart. If the same item exist in the cart,
     * it will be added as a separate line item instead of increasing the cart count.
     * <p>
     * If the given PSID/ProductId is not found, then '204-No Content' status code will be returned.
     * NOTE: To increase the quantity of the same item, it has to go through ModifyCart service
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/add"}, method = RequestMethod.POST)
    //TODO: remove quantity from param
    public ResponseEntity<Object> addToCart(@RequestBody final AddToCartRequest cartRequest,
                                            final HttpServletRequest request)
            throws Exception {
        XSSRequestWrapper servletRequest = new XSSRequestWrapper(request);
        logger.info("Cart : addToCart with path {}", servletRequest.getPathInfo());
        final Gson gson = new Gson();
        final User user = appSessionInfo.currentUser();

        try {
            final HttpSession session = servletRequest.getSession();
            final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            final String psid = cartRequest.getPsId();
            final Product product = productServiceV3.getAppleProductDetail(psid, program, false, user,
                        true, true, false, false);
            if (Optional.ofNullable(product).isEmpty()) {
                logger.error("No such product found for this psid/productId: {} ", psid);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No such product found for this psid/productId: " + psid);
            }

            final Product servicePlanProduct =
                getServicePlan(product, user, program, psid, cartRequest.getServicePlanPsId(),
                    getProgramConfigValueAsBoolean(program, ENABLE_APPLE_CARE_SERVICE_PLAN));

            updateCartIfSingleItemPurchase(user, session, program);
            //get cart from session
            Cart cart = Optional.ofNullable((Cart) session.getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT)).orElseGet(Cart::new);

            final double preAddToCartTotal = CartCalculationUtil.getCartTotalAmount(cart);

            if(cart.getShippingAddress() == null || !cart.getShippingAddress().canCalculateWithFeesAndTaxes(AddressMapper.isAddressCheckNeeded(program))) {
                cart.setShippingAddress(AddressMapper.getAddress(user, program));
            }
            // This flag should be set to false in all other scenario other than Address modified and Total is changed.
            cart.getShippingAddress().setCartTotalModified(false);
            //add new item to cart
            AddToCartResponse addToCartResponse=new AddToCartResponse();
            if (cartService.canAddToCart(cart, program, addToCartResponse, product)) {
                return getAddToCartResponseIfCanAddToCart(user, gson, session, program, product, cart,
                    preAddToCartTotal, addToCartResponse, servicePlanProduct);
            } else {
                if(addToCartResponse.isQuantityLimitExceed()){//Seems quantityLimitExceed always false
                    return ResponseEntity.ok(addToCartResponse);
                } else if (addToCartResponse.isGiftcardMaxQuantity() || addToCartResponse.isPhysicalGiftcardTotalValueFull()){
                    return ResponseEntity.badRequest().body(addToCartResponse);
                }
                logger.error("Failed to add item to cart. Cart Full!");
                return ResponseEntity.badRequest().build();
            }
        } catch (final Exception se) {
            logger.error("Failed to add item to cart. See server log for exception trace", se);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    "Failed to add item to cart. Please contact System Administrator ");
        }
    }

    /**
     * Get Service Plan Product
     *
     * @param product
     * @param user
     * @param program
     * @param productPsId
     * @param servicePlanPsId
     * @param appleCareServiceEnabled
     * @return
     */
    private Product getServicePlan(final Product product, final User user, final Program program,
        final String productPsId, final String servicePlanPsId, final boolean appleCareServiceEnabled) {
        Product servicePlanProduct = null;
        if (appleCareServiceEnabled && StringUtils.isNotBlank(servicePlanPsId)) {
            if (product.getAddOns().getServicePlans().stream()
                .anyMatch(servicePlan -> servicePlan.getPsid().equalsIgnoreCase(servicePlanPsId))) {
                servicePlanProduct = productServiceV3.getAppleProductDetail(servicePlanPsId, program, false, user,
                        true, true, false, false);
                if (Optional.ofNullable(servicePlanProduct).isEmpty()) {
                    logger.error("Product{}: Service Plan {} details not found.", productPsId, servicePlanPsId);
                }
            } else {
                logger.error("Service Plan {} not mapped with the Product: {} ", servicePlanPsId, productPsId);
            }
        }
        return servicePlanProduct;
    }

    private ResponseEntity<Object> getAddToCartResponseIfCanAddToCart(final User user, final Gson gson,
        final HttpSession session, final Program program, final Product product, final Cart cart,
        final double preAddToCartTotal, final AddToCartResponse addToCartResponse, final Product servicePlanProduct)
        throws ServiceException {
        final Long cartItemId = cartService.addToCart(cart, user, program, product, servicePlanProduct);

        if (cart.isMaxCartTotalExceeded()) {
            logger.info("Cart : Failed price overflow.");
            addToCartResponse.setPricingFull(true);
            return ResponseEntity.badRequest().body(addToCartResponse);
        }
        logger.info("Cart : {} is added successfully to the shopping cart of the user {}",
            product.getPsid() + " " + product.getName(), user.getUserId());
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);
        if(logger.isDebugEnabled()) {
            logger.debug("Printing cart response in addToCart method: {}", gson.toJson(cart));
        }
        addToCartResponse.setCartItemId(cartItemId);

        final double currentCartTotal = CartCalculationUtil.getCartTotalAmount(cart);
        final Object disableCartTotalModifiedPopUp = program.getConfig().get(CommonConstants.DISABLE_CART_TOTAL_MODIFIED_POP_UP);
        if(disableCartTotalModifiedPopUp==null || !(boolean)disableCartTotalModifiedPopUp) {
            addToCartResponse.setCartTotalModified(
                    CartCalculationUtil.isCartTotalModified(preAddToCartTotal, currentCartTotal));
        }
        return ResponseEntity.ok(addToCartResponse);
    }

    /**
     * update Cart in case of Single Item Purchase
     *
     * @param user
     * @param session
     * @param program
     * @throws ServiceException
     */
    private void updateCartIfSingleItemPurchase(
            final User user,
            final HttpSession session, final Program program)
            throws ServiceException {
        // Empty the cart for singleItemPurchase scenario
        final Boolean singleItemPurchase =
                Optional.ofNullable((Boolean) program.getConfig().get(CommonConstants.SINGLE_ITEM_PURCHASE))
                        .orElse(false);
        if (singleItemPurchase) {
            logger.info(
                    "Add to Cart - Emptying cart, if any, as var/program: {}/{},  has SingleItemQuantity purchase restriction", user.getVarId(), user.getProgramId());
            cartService.emptyCart(user);
            final Cart cart = (Cart) session.getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
            // if the cart is not empty, get only the discount codes
            final List<DiscountCode> discounts = (cart != null)?cart.getDiscounts():null;
            final Address shippingAddressFromSession = cart != null ? cart.getShippingAddress(): null;
            //Emptying the cart in the DB and setting to session.
            try {
                final Cart cartFromDb = cartService.getCart(user, null, program);
                cartFromDb.setDiscounts(discounts);
                cartFromDb.setShippingAddress(shippingAddressFromSession);
                session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT,  cartFromDb);
            } catch (final EntityNotFoundException enfe) {
                session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, null);
            }

        }
    }

    /**
     * Apply the provided discount code
     *
     * @param discCode - Discount code to apply
     * @param request
     * @return - Updated Cart information
     * @throws Exception
     */
    @RequestMapping(value ="/discounts/{discountCode}",  method = RequestMethod.PUT)
    public ResponseEntity<Object> applyDiscount(
            @PathVariable final String discCode,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws Exception {
        // Set cache control headers
        logger.info("Cart : applyDiscount");
        final User user = appSessionInfo.currentUser();
        final String discountCode = HtmlUtils.htmlEscape(discCode);
        response.setHeader(CACHE_CONTROL, NO_CACHE_NO_STORE);
        response.setHeader(PRAGMA, NO_CACHE);
        try {
            XSSRequestWrapper servletRequest = new XSSRequestWrapper(request);
            if(StringUtils.isNotBlank(discountCode)) {
                if (!isDiscountCodeValid(discountCode)) {
                    return ResponseEntity.badRequest()
                            .body(createCouponErrorObject(discountCode, user.getLocale(), "CC_0001.header",
                                    "CC_0001.message"));
                }
                Cart cart = appSessionInfo.getSessionCart();
                Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
                return getCartResponse(discountCode, user, servletRequest, cart, program);

            }else {
                return ResponseEntity.badRequest().body("Bad request");
            }

        }  catch (final Exception se) {
            logger.error("Exception in applying discount code, {} ", discountCode,se);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(!StringUtils.isEmpty(se.getMessage()) ? se.getMessage() + " " :
                    EXCEPTION_IN_MODIFYING_CART_ITEM);
        }
    }

    private ResponseEntity<Object> getCartResponse(
            @PathVariable final String discountCode,
            final User user,
            final HttpServletRequest servletRequest, final Cart cart, final Program program)
            throws ServiceException {
        if(cart != null) {
            ResponseEntity<Object> errorDiscountCodePerOrder = errorIfDiscountCodeWithinNoOfAllowed(user, cart, program);
            if (errorDiscountCodePerOrder != null) {
                return errorDiscountCodePerOrder;
            }

            if(isDiscountListContainDiscountCode(discountCode, cart.getDiscounts())) {
                return ResponseEntity.badRequest().body(createCouponErrorObject(discountCode, user.getLocale(), "alreadyapplied.header","alreadyapplied.message"));
            } else {
                return applyDiscountAndGetCartResponse(discountCode, user, servletRequest, cart, program);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empty cart");
        }
    }

    private ResponseEntity<Object> applyDiscountAndGetCartResponse(
            @PathVariable final String discountCode,
            final User user,
            final HttpServletRequest servletRequest, Cart cart, final Program program)
            throws ServiceException {
        final CouponDetails couponDetails = discountServiceClient.getValidDiscountCode(discountCode, user);
        if (couponDetails != null) {
            if (couponDetails.isValid()) {
                final DiscountCode
                        discount = new DiscountCode(discountCode, couponDetails.getFriendlyNameDesc(), couponDetails.getCouponDesc(), couponDetails.getTypeOffer(), couponDetails.getAmountOff());
                cart.addDiscount(discount);
                //Resetting the add points when discount code is applied
                cart.setAddPoints(0);
                cart = cartService.getCart(user, cart, program);
                if (cart != null) {
                    servletRequest.getSession().setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);
                    return ResponseEntity.ok(cart);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empty cart");
                }
            } else {
                return ResponseEntity.badRequest().body(couponDetails.getCouponError());
            }
        } else {
            return ResponseEntity.badRequest().body("Empty Coupon Details");
        }
    }

    private boolean isDiscountListContainDiscountCode(
            @PathVariable final String discountCode, final List<DiscountCode> discounts) {
        return CollectionUtils.isNotEmpty(discounts) && discounts.stream()
                .filter(discountCode1 -> (discountCode1 != null && StringUtils.isNotBlank(discountCode1.getDiscountCode())))
                .anyMatch(discountCode1 -> discountCode1.getDiscountCode().equalsIgnoreCase(discountCode));
    }

    private ResponseEntity<Object> errorIfDiscountCodeWithinNoOfAllowed(
            final User user,
            final Cart cart, final Program program) {
        //Check if its within no of allowed discount code
        if(Objects.nonNull(program.getConfig().get(CommonConstants.DISCOUNTCODE_PER_ORDER))){
            int discountCodePerOrder=Integer.parseInt(program.getConfig().get(CommonConstants.DISCOUNTCODE_PER_ORDER).toString());
            if(discountCodePerOrder<=cart.getDiscounts().size()){
                // If discount code does not have min or max length, return invalid code message
                if(discountCodePerOrder==1){
                    return ResponseEntity.badRequest().body(createCouponErrorObject(String.valueOf(discountCodePerOrder), user.getLocale(), "CC_0006.header1","CC_0006.message1"));
                }else{
                    return ResponseEntity.badRequest().body(createCouponErrorObject(String.valueOf(discountCodePerOrder), user.getLocale(), "CC_0006.header", "CC_0006.message"));
                }
            }

        }
        return null;
    }

    private boolean isDiscountCodeValid(
            @PathVariable final String discountCode) {
        return discountCode.length() <=
                Integer.valueOf(applicationProperties.getProperty(CommonConstants.DISCOUNTCODE_LENGTH_MAX_LIMIT_KEY))
                && discountCode.length() >=
                Integer.valueOf(applicationProperties.getProperty(CommonConstants.DISCOUNTCODE_LENGTH_MIN_LIMIT_KEY));
    }

    /**
     * Apply the provided discount code
     *
     * @param discountCode - Discount code to delete
     * @param request
     * @return - Updated Cart information
     * @throws Exception
     */
    @RequestMapping(value ="/discounts/{discountCode}",  method = RequestMethod.DELETE)
    public ResponseEntity<Object> removeDiscount(
            @PathVariable final String discountCode,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws Exception {

        XSSRequestWrapper servletRequest = new XSSRequestWrapper(request);

        // Set cache control headers
        logger.info("Cart : removeDiscount");
        final User user = appSessionInfo.currentUser();
        response.setHeader(CACHE_CONTROL, NO_CACHE_NO_STORE);
        response.setHeader(PRAGMA, NO_CACHE);
        try {
            if(StringUtils.isNotBlank(discountCode)) {
                Cart cart = (Cart) servletRequest.getSession().getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
                if(cart != null && CollectionUtils.isNotEmpty(cart.getDiscounts())) {
                    cart.removeDiscount(discountCode);
                    final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
                    cart = cartService.getCart(user, cart, program);
                    return ResponseEntity.ok(cart);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Cart is empty or discount code does not exist");
                }
            }else {
                return ResponseEntity.badRequest().body("Bad request");
            }

        }  catch (final Exception se) {
            logger.error("Exception in removing discount code {} ", discountCode, se);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(!StringUtils.isEmpty(se.getMessage()) ? se.getMessage() + " " :
                            EXCEPTION_IN_MODIFYING_CART_ITEM);
        }
    }

    /**
     * Modifies Cart Item for the given itemId. Information to be modified will be sent as JSON
     *
     * @param cartItemId     - Unique Identifier of the item, to be modified
     * @param modifyMessages - Cart content to be modified
     * @param request
     * @return - Updated Cart information
     * @throws Exception
     */
    @RequestMapping(value = "/modify/{cartItemId}", method = RequestMethod.POST)
    public ResponseEntity<Object> modifyCart(
            @PathVariable final String cartItemId,
            @RequestBody final Map modifyMessages,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws Exception {

        XSSRequestWrapper servletRequest = new XSSRequestWrapper(request);

        // Set cache control headers
        response.setHeader(CACHE_CONTROL, NO_CACHE_NO_STORE);
        response.setHeader(PRAGMA, NO_CACHE);
        final User user = appSessionInfo.currentUser();
        final Gson gson = new Gson();
        try {
            logger.info("Cart : modifyCart - post");
            return applyCartModifiedByInformationAndGetResponse(cartItemId, modifyMessages, user, servletRequest, gson);
        } catch (final NoSuchElementException nse) {
            logger.error("Item not found in Cart anymore ");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Item not found in Cart anymore ");
        } //TODO: refactor exception handling
        catch (final JsonParseException jpe) {
            logger.error("Cannot Modify Cart. Invalid ModifyCart JSON : {}", jpe);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cannot Modify Cart. Invalid ModifyCart JSON :\n " + modifyMessages);
        } catch (final JSONException je) {
            logger.error("Cannot Modify Cart. JSON is empty : {}", je);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cannot Modify Cart. JSON is empty:\n " + modifyMessages);
        } catch (final RequestValidationException rve) {
            logger.error("Invalid data in modify cart...", rve);
            return ResponseEntity.badRequest().body(rve.getMessage());
        } catch (final EntityNotFoundException enfe) {
            logger.error(THERE_ARE_NO_ITEMS_IN_THE_CART, enfe);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(THERE_ARE_NO_ITEMS_IN_THE_CART1);
        } catch (final ServiceException se) {
            return handleServiceException(se);

        } catch (final DataException de) {
            return handleDataException(user, de);
        } catch (final Exception se) {
            logger.error("Exception in Modifying CartItem... {} ", cartItemId, se);
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT.INTERNAL_SERVER_ERROR).body((!StringUtils.isEmpty(se.getMessage()) ? se.getMessage() + " " :
                    EXCEPTION_IN_MODIFYING_CART_ITEM) + cartItemId);
        }

    }

    private ResponseEntity<Object> handleDataException(
            final User user,
            final DataException de) {
        List<Map<String, String>> errorListResponse = new ArrayList<>();
        de.getErrors().forEach(error -> {
            final Map<String, String> errorMapResponse = new HashMap<>();
            errorMapResponse.put(CommonConstants.INPUT_FIELD, error.getInputField());
            if (error.getMessage() != null &&
                    error.getMessage().equals(ServiceExceptionEnums.NAUGHTY_WORD_FOUND_EXCEPTION.getErrorMessage())) {
                logger.error("Engrave Text Contains Naughty/Bad word(s) for User Locale : {} ", user.getLocale());

                errorMapResponse.put(CommonConstants.MESSAGE, contextUtil.getMessageSource(user.getVarId())
                        .getMessage(CommonConstants.NAUGHTY_WORD_FOUND, null, CommonConstants.NAUGHTY_WORD_FOUND,
                                user.getLocale()));
            } else {
                logger.error("These characters cannot be engraved: {} for User Locale : {} ", error.getMessage(),
                        user.getLocale());
                errorMapResponse.put(CommonConstants.MESSAGE, contextUtil.getMessageSource(user.getVarId())
                        .getMessage(CommonConstants.INVALID_CHARACTER_FOUND, null, CommonConstants.INVALID_CHARACTER_FOUND,
                                user.getLocale()) + " " + error.getMessage());
            }
            errorListResponse.add(errorMapResponse);
        });
        return ResponseEntity.badRequest().body(errorListResponse);
    }

    private ResponseEntity<Object> handleServiceException(final ServiceException se) {
        if(se.getErrorCode() == ServiceExceptionEnums.QUANTITY_INVALID_EXCEPTION.getErrorCode()) {
            logger.error("Failed to modify cart because of invalid quantity...", se);
            return ResponseEntity.badRequest().body("Failed to modify cart because of invalid quantity...");
        } else {
            logger.error(FAILED_TO_LOAD_CART, se.getMessage(), se);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(FAILED_TO_LOAD_CART + "Please contact support.");
        }
    }

    private ResponseEntity<Object> applyCartModifiedByInformationAndGetResponse(
            @PathVariable final String cartItemId,
            @RequestBody final Map modifyMessages,
            final User user,
            final HttpServletRequest servletRequest, final Gson gson)
            throws ServiceException, DataException {
        logger.debug("modifyCart with cartItemId: {} and modifyMessages:{} - ENTRY", cartItemId, modifyMessages);
        final HttpSession session = servletRequest.getSession();
        final Cart cart = (Cart) session.getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
        final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        //reset user & system modified flag to FALSE, as this need to be recalculated everytime
        if (cart != null) {
            cart.setCartModifiedByUser(false);
            cart.setCartModifiedBySystem(false);
        }
        final Cart appleCart = cartService.modifyCart(cart, user, Long.parseLong(cartItemId), modifyMessages, program);

        if (appleCart.getCartItems().size() == 0) {
            logger.info("Cart is Empty now & removing shopping cart from DB");
            cartService.emptyCart(user);
        }
        //update cart to session
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, appleCart);
        if(logger.isDebugEnabled()) {
            logger.debug("Printing Cart after modification: {}", gson.toJson(appleCart));
        }

        final Cart clonedSessionCart = getModifiedCart(appleCart);
        logger.debug("modifyCart with cartItemId: {} and modifyMessages:{} - EXIT", cartItemId, modifyMessages);
        return ResponseEntity.ok(clonedSessionCart);
    }

    /**
     * Empty all items in Cart
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/emptyCart", method = RequestMethod.GET)
    public ResponseEntity<Object> emptyCart(
                                            final HttpServletRequest request,
                                            final HttpServletResponse response) {
        XSSRequestWrapper servletRequest = new XSSRequestWrapper(request);

        // Set cache control headers
        logger.info("Cart : emptyCart");
        response.setHeader(CACHE_CONTROL, NO_CACHE_NO_STORE);
        response.setHeader(PRAGMA, NO_CACHE);
        final User user = appSessionInfo.currentUser();
        try {
            cartService.emptyCart(user);
            //remove from session
            servletRequest.getSession().removeAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
            return ResponseEntity.ok().build();
        } catch (final EntityNotFoundException enfe) {
            logger.error("Exception while emptying Cart", enfe);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception while emptying Cart ");
        }
    }

    /**
     * Get Credit Card Cost for points
     * @param request
     * @return
     */
    @RequestMapping(value = "/ccDollarValue/{ccAddPoints}", method = RequestMethod.GET)
    public ResponseEntity<Object> getCreditCardCostForPoints(final HttpServletRequest request,
                                                             @PathVariable final Integer ccAddPoints) {
        XSSRequestWrapper servletRequest = new XSSRequestWrapper(request);

        final User user = appSessionInfo.currentUser();
        try {
            logger.info("Cart : getCreditCardCostForPoints");
            final StringBuilder logMessage = new StringBuilder();
            final CurrencyUnit currency = CurrencyUnit.of(user.getLocale());
            final Cart cart = (Cart) servletRequest.getSession().getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
            final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            final SplitTenderCart splitTenderCart = new SplitTenderCart();
            if ((cart == null || cart.getCartItems() == null || cart.getCartItems().size() == 0) && ccAddPoints > 0) {
                return ResponseEntity.badRequest().body("Cart Empty");
            }
            if (ccAddPoints == null || ccAddPoints < 0) {
                return ResponseEntity.badRequest().body("ccAddPoints must be a positive integer");
            }

            cartService.callPricingServiceOnCreditCardCostForPoints(cart, user, program, ccAddPoints);

            if(Objects.nonNull(cart)) {
                splitTenderCart.setEarnPoints(cart.getEarnPoints());
                // check for supplemental dollar/points restriction
                if (inCorrectSupplementalSplit(ccAddPoints, cart, program, logMessage) ) {
                    logger.error("SplitPay Log: {}", logMessage);
                    splitTenderCart.setCashAmount(Money.of(currency, cart.getCost(), RoundingMode.UNNECESSARY).getAmount());
                    return ResponseEntity.badRequest().body(splitTenderCart);
                }
                if(logger.isDebugEnabled()) {
                    logger.debug("Printing response in getCreditCardCostForPoints method: {}",
                            Money.of(currency, cart.getCost(), RoundingMode.UNNECESSARY).getAmount());
                }
                splitTenderCart.setCashAmount(Money.of(currency, cart.getCost(), RoundingMode.UNNECESSARY).getAmount());
            }

            return ResponseEntity.ok(splitTenderCart);
        } catch (final EntityNotFoundException enfe) {
            logger.error("Exception while applying CC cost for points in Cart : ", enfe);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Exception while applying CC cost for points in Cart ");
        }
    }

    @RequestMapping(value = "/ccDollarValueOnChange/{ccAddPoints}", method = RequestMethod.GET)
    public ResponseEntity<Object> getCreditCardCostForPointsOnChange(final HttpServletRequest request,
                                                                     @PathVariable final Integer ccAddPoints) {
        final User user = appSessionInfo.currentUser();
        try {
            XSSRequestWrapper servletRequest = new XSSRequestWrapper(request);
            logger.info("Cart : getCreditCardCostForPointsOnChange");
            final StringBuilder logMessage = new StringBuilder();
            final CurrencyUnit currency = CurrencyUnit.of(user.getLocale());
            final Cart cart = appSessionInfo.getSessionCart();
            //Object deep clone using JSON Serialization
            final Cart clonedCart = copyByModifier(cart);
            final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            final SplitTenderCart splitTenderCart = new SplitTenderCart();
            if ((clonedCart == null || clonedCart.getCartItems() == null || clonedCart.getCartItems().size() == 0) && ccAddPoints > 0) {
                return ResponseEntity.badRequest().body("Cart Empty");
            }
            if (ccAddPoints == null || ccAddPoints < 0) {
                return ResponseEntity.badRequest().body("ccAddPoints must be a positive integer");
            }

            cartService.callPricingServiceOnCreditCardCostForPoints(clonedCart, user, program, ccAddPoints);

            splitTenderCart.setEarnPoints(cart.getEarnPoints());
            // check for supplemental dollar/points restriction
            Double cost = null;
            if (Objects.nonNull(clonedCart)) {
                cost = clonedCart.getCost();
            }
            if (inCorrectSupplementalSplit(ccAddPoints, clonedCart, program, logMessage) ) {
                logger.error("SplitPay Log: {}", logMessage);

                splitTenderCart.setCashAmount(Money.of(currency, cost, RoundingMode.UNNECESSARY).getAmount());
                return ResponseEntity.badRequest().body(splitTenderCart);
            }
            if(logger.isDebugEnabled()) {
                logger.debug("Printing response in getCreditCardCostForPointsOnChange method: {}", Money.of(currency, cost, RoundingMode.UNNECESSARY).getAmount());
            }
            splitTenderCart.setCashAmount(Money.of(currency, cost, RoundingMode.UNNECESSARY).getAmount());
            return ResponseEntity.ok().body(splitTenderCart);
        } catch (final EntityNotFoundException enfe) {
            logger.error("Exception while onChanging CC cost for points in Cart : ", enfe);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Exception while onChanging CC cost for points in Cart ");
        }
    }



    /**
     * Verify if the slider value matches the rule set in Admin page
     * @param ccAddPoints
     * @return
     */
    private boolean inCorrectSupplementalSplit(final Integer ccAddPoints, final Cart cart, final Program program, final StringBuilder logMessage) {
        return cartService.inCorrectSupplementalSplit(ccAddPoints, cart, program, logMessage);
    }

    private CouponError createCouponErrorObject(final String placeHolder,final Locale locale,
                                                final String header,
                                                final String message){

        CouponError couponError = new CouponError(placeHolder, "",
                messageSource.getMessage(header, new String[]{placeHolder}, locale),
                messageSource.getMessage(message, new String[]{placeHolder}, locale));

        return couponError;
    }

    //Generate a new Cart object using groovy JSON Parser
    private Cart copyByModifier(final Cart cart){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsonNode.class, new InstanceCreator<JsonNode>(){
                    public JsonNode createInstance(Type type) {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.createObjectNode();
                    }
                }).excludeFieldsWithModifiers(Modifier.STATIC).create();

        return gson.fromJson(gson.toJson(cart), Cart.class);
    }



}
