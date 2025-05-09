package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.CartService;
import com.b2s.apple.services.NotificationService;
import com.b2s.apple.services.ProgramService;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.db.model.OrderLineAttribute;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.BasicAuthValidation;
import com.b2s.rewards.common.exception.RestException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.common.util.PropertiesUtils;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.security.idology.IDologyStatusConsumer;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.OrderTransactionManager;
import com.b2s.shop.common.order.var.VAROrderManagerIF;
import com.b2s.shop.common.order.var.VarOrderManagerHolder;
import com.b2s.spark.api.apple.to.EmailResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * All Order calls reside in this controller
 *
 * @author rperumal
 */
@RestController
@SessionAttributes(CommonConstants.APPLE_CART_SESSION_OBJECT)
@RequestMapping(value="/order", produces = "application/json;charset=UTF-8")
@ResponseBody
public class PlaceOrderController {

    @Value("${split.tender.order.error.generic.url}")
    private String orderErrorGenericUrl;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderTransactionManager manager;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private ProgramService programService;

    @Autowired
    private BasicAuthValidation basicAuthValidation;

    @Autowired
    private VarOrderManagerHolder varOrderManagerHolder;

    @Autowired
    private IDologyStatusConsumer iDologyStatusConsumer;

    @Autowired
    private AppSessionInfo appSessionInfo;

    @Autowired
    private NotificationService notificationService;

    private static final String ORDER_INFORMATION = "orderInformation";
    private static final Logger logger = LoggerFactory.getLogger(PlaceOrderController.class);

    private static final Map<String, String> VAR_WITH_INVISIBLE_CART = new HashMap<String, String>();

    static {

        VAR_WITH_INVISIBLE_CART.put(CommonConstants.VAR_VITALITYUS,CommonConstants.VAR_VITALITYUS);
        VAR_WITH_INVISIBLE_CART.put(CommonConstants.VAR_BSWIFT,CommonConstants.VAR_BSWIFT);

    }
    private static String getPurchasePostURL(final String varId, final String programId, final String locale) {
        return String.format("%s.%s.%s.%s", varId.toLowerCase(), programId.toLowerCase(), ExternalUrlConstants
            .PURCHASE_POST_URL, locale);
    }
    private static String getPurchasePostURL(final String varId, final String programId){
        return String.format("%s.%s.%s",varId.toLowerCase(),programId.toLowerCase(), ExternalUrlConstants.PURCHASE_POST_URL);
    }
    private static String getPurchasePostURL(final String varId){
        return String.format("%s.%s",varId.toLowerCase(),ExternalUrlConstants.PURCHASE_POST_URL);
    }

    /**
     * Place order
     * @return
     */
    @RequestMapping(value = "/orderInformation",method = RequestMethod.GET)
    public ResponseEntity currentOrderInformation(final HttpSession session,
                                                  @RequestParam(value = "orderId", required = false) String orderId,
                                                  final HttpServletResponse response) throws ServiceException
    {
        final User user = appSessionInfo.currentUser();
        // Set cache control headers
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        OrderStatus orderStatus = (OrderStatus)session.getAttribute(ORDER_INFORMATION);
        if(StringUtils.isNotBlank(orderId) && orderStatus!=null && !orderId.equalsIgnoreCase(orderStatus.getB2sOrderId().toString())){
            return new ResponseEntity("OrderId conflict", HttpStatus.CONFLICT);
        }
        //  Specific for payroll deduction
        if(orderStatus!=null && orderStatus.getB2sOrderId()!=null && !CommonConstants.AWP_PROGRAM_DRP.equals(user.getProgramId())){
                orderStatus = manager.updateOrderStatusFromDB(orderStatus);
        }
        if (orderStatus == null) {
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity(orderStatus, HttpStatus.OK);
        }
    }
    /**
     * Place order
     * @return
     */
    @RequestMapping(value = {"/placeOrder"}, method = RequestMethod.POST)
    public ResponseEntity placeOrder(@RequestBody final PlaceOrderRequest placeOrderRequest,
                                     final HttpServletRequest  servletRequest,
                                     final HttpServletResponse response,
                                     final ModelMap modelMap) {
        final User user = appSessionInfo.currentUser();
        final Cart cart = appSessionInfo.getSessionCart();

        final String ERROR_MSG = "Error placing the order. Please contact administrator.";

        // Set cache control headers
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        try {

            final Program program =
                (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
            final String transactionId =
                (String) servletRequest.getSession().getAttribute(CommonConstants.PAYMENT_TRANSACTION_ID);

            logger.info("BEGIN: Order Placement process ....");
            String last4 = null;
            String ccType = null;

            final String uiTransactionId = placeOrderRequest.getTransactionId();

            if (Objects.nonNull(cart.getCreditItem())) {
                last4 = cart.getCreditItem().getCcLast4();
                ccType = cart.getCreditItem().getCreditCardType();
            }

            if (StringUtils.isNotBlank(uiTransactionId) && StringUtils.isNotBlank(transactionId) &&
                !transactionId.equals(uiTransactionId)) {
                logger
                    .error("END: Order Placement aborted as the Transaction Id {} is incorrect....", transactionId);
                return ResponseEntity.badRequest().body(ERROR_MSG);
            }

            //Do not allow Checkout for BrowseOnly!
            if (null != user && user.isBrowseOnly()) {
                logger.error("Browse only user cannot submit the order.");
                return ResponseEntity.badRequest().body(ERROR_MSG);
            }

            //Place Order is restricted for Agent Browse User
            if (null != user && user.isAgentBrowse()) {
                logger.error("Agent Browse user cannot place the order.");
                return ResponseEntity.badRequest().body(ERROR_MSG);
            }

            //get DB cart
            ShoppingCart shoppingCart = cartService.getShoppingCart(user);
            // Load cartitems from DB & Make sure if cart is not emptied by different session
            final ResponseEntity<String> cartValidationError = validateCart(cart, shoppingCart, user, program);
            if (cartValidationError != null) {
                return cartValidationError;
            }
            return placeTheOrder(cart, placeOrderRequest, servletRequest, modelMap, program, transactionId, last4,
                ccType);

        } catch (final Exception enfe) {
            logger.error("END: Order Placement aborted with exception....", enfe);
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT.INTERNAL_SERVER_ERROR).body("Order Placement did not go through");
        }
    }

    private ResponseEntity placeTheOrder(
        final Cart cart,
        final PlaceOrderRequest placeOrderRequest,
        final HttpServletRequest servletRequest, final ModelMap modelMap, final Program program,
        final String transactionId, final String last4, final String ccType) {
        final User user = appSessionInfo.currentUser();
        //set supplier id
        if (user.getSupplierId() == 0) {
            user.setSupplierId(cart.getCartItems().get(0).getSupplierId());
        }
        if(cart.getCreditItem() != null && last4 != null ) {
            cart.getCreditItem().setCcLast4(last4);
        }else{
            // Use isPromotionChecked flag when its points only order
            // For cc order, cart it already populated with this flag
            if(Objects.nonNull(cart.getPromotionalSubscription())){
                cart.getPromotionalSubscription().setChecked(placeOrderRequest.getIsPromotionChecked());
            }
        }

        //place order
        OrderStatus orderStatus = manager.placeMyOrder(cart, user, program, servletRequest, transactionId);
        resetUserCreditAdds(cart);
        updateOrderStatus(cart, last4, ccType, orderStatus);
        if (orderStatus.isOrderCompleted()) {
            return processOrderCompleted(cart, servletRequest, modelMap, program, orderStatus);
        }
        else {
            logger.info("Order Placement: Did not go through. Please see error log above for more details...");
            servletRequest.getSession().setAttribute(ORDER_INFORMATION, orderStatus);
            logger.error("END: Order Placement process did not go through and process completed with failure ...." );
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT.INTERNAL_SERVER_ERROR).body(orderStatus);
        }
    }

    private void updateOrderStatus(
        final Cart cart, final String last4,
        final String ccType, final OrderStatus orderStatus) {
        if(StringUtils.isNotBlank(last4)){
            orderStatus.setLast4(last4);
        }
        if(StringUtils.isNotBlank(ccType)){
            orderStatus.setCcType(ccType);
        }
        if(CollectionUtils.isNotEmpty(cart.getDiscounts())) {
            orderStatus.setDiscountCodes(cart.getDiscounts());
        }
    }

    private ResponseEntity processOrderCompleted(
        final Cart cart,
        final HttpServletRequest servletRequest, final ModelMap modelMap, final Program program,
        final OrderStatus orderStatus) {
        final User user = appSessionInfo.currentUser();
        // Adding cart total in order status to pass discounted prices to order confirmation page
        orderStatus.setCartTotal(cart.getDisplayCartTotal());
        if(MapUtils.isNotEmpty(program.getConfig())) {
            final Optional<Boolean> showVarOrderIdOpt = Optional.ofNullable(program.getConfig().get(CommonConstants.SHOW_VAR_ORDER_ID))
                    .map(Object::toString).map(Boolean::parseBoolean);
            if(showVarOrderIdOpt.isPresent()) {
                orderStatus.setShowVarOrderId(showVarOrderIdOpt.get());
            }
        }
        cart.setPurchased(true);


        //Clear Idology values from session.
        iDologyStatusConsumer.clearFlag();
        //empty cart
        logger.info("Order Placement: Emptying cart items after successful order completion...");
        if(orderStatus.isEmptyCart()) {
            cartService.emptyCart(user);

            //clear discount code from user after order completed
            if(CollectionUtils.isNotEmpty(user.getDiscounts())){
                user.getDiscounts().clear();
            }


            //remove from session
            servletRequest.getSession().removeAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
        }

        logger.info("Order Placement: Removing cart from session after successful order completion...");
        modelMap.remove(CommonConstants.APPLE_CART_SESSION_OBJECT);
        servletRequest.getSession().setAttribute(CommonConstants.ORDER_ID_SESSION_OBJECT, orderStatus.getB2sOrderId());
        servletRequest.getSession().setAttribute(ORDER_INFORMATION, orderStatus);

        //Regenerate new XSRF-TOKEN after a successful order placement
        logger.info("Order Placement: adding CSRF_TOKEN...");
        servletRequest.getSession()
            .setAttribute(CommonConstants.XSRF_TOKEN_SESSION_KEY, UUID.randomUUID().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.add("XSRF-TOKEN", HtmlUtils.htmlEscape(servletRequest.getSession()
                .getAttribute(CommonConstants.XSRF_TOKEN_SESSION_KEY).toString()) );


        logger.info("END: Order Placement process successfully completed ....");
        return ResponseEntity.ok().headers(headers).body(orderStatus);
    }

    private ResponseEntity<String> validateCart(
        final Cart cart, final ShoppingCart shoppingCart,
        final User user, final Program program) {
        ResponseEntity<String> response = null;
        if (shoppingCart != null && shoppingCart.getShoppingCartItems() != null && shoppingCart.getShoppingCartItems().isEmpty() ) {
            logger.warn("END: Order Placement aborted as Cart is empty ...." );
            response = ResponseEntity.status(HttpStatus.CONFLICT).body(ServiceExceptionEnums.CART_TOTAL_MODIFIED_EXCEPTION.getErrorMessage());
        }

        //If this service is called from UI then, this validation is not required. As a REST call if invoked , this is required.
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().size() < 1) {
            logger.warn("END: Order Placement aborted as Session Cart is empty ...." );
            response = ResponseEntity.status(HttpStatus.NO_CONTENT).body("Cart is Empty now ");
        } else if (cart.getShippingAddress() == null || !(cart.getShippingAddress().isValidAddress()) ) {
            logger.warn("END: Order Placement process aborted for missing Shipping Address ...." );
            response = ResponseEntity.badRequest().body("Valid Shipping Address Required ");
        } else {
            //Confirm if cart items are still available.
            if (Objects.nonNull(shoppingCart)) {
                cartService.verifyIsCartItemsStillAvailable(shoppingCart.getShoppingCartItems(), cart, user, program);
            }

            //Notify user if cart item is either modified by user thru different session or no longer available
            if (cart.getCartModifiedBySystem()) {
                //Abort Order Placement
                logger.warn(
                    "END: Order Placement did not go through: Cart item(s) is either modified by user through different session or no longer available");
                response = ResponseEntity.status(HttpStatus.CONFLICT).body(
                    "Order Placement did not go through: Cart item is either modified by user through different session " +
                        "or no longer available");
            }
        }
        return response;
    }

    /**
     * This REST service will be exposed and authenticated through basicAuth.
     * After successful authentication, order status will be set to PROCESSING or FAILED based on the status
     * and email notification will be sent accordingly.
     *
     * @return
     */
    @RequestMapping(value = {"/status/{orderId}"}, method=RequestMethod.POST)
    public ResponseEntity updateStatus(@PathVariable("orderId") final String orderId,@RequestBody
    OrderStatusUpdate status)
        throws ServiceException {

        logger.info("PD confirmation: Order update started for orderId # {}. Received object is {}",orderId,status);

        //Validate b2s orderid
        final Order order = manager.getOrder(Long.valueOf(orderId));
        if ( order == null ) {
            logger.error("PD confirmation:  Order ID {} not found in B2S.", orderId);
            return new ResponseEntity("PD confirmation: Order ID not found in B2S.", HttpStatus.NOT_FOUND);
        }

        // compare basic auth role and varId to identify user access to given order id
        if(!basicAuthValidation.isUserHasAccessToOrder(order)){
            logger.error("PD confirmation: Order ID {} does not belongs to your entity.",orderId);
            return new ResponseEntity("PD confirmation: Order ID does not belongs to your entity.", HttpStatus.NOT_FOUND);
        }

        //get orderstatus
        final Integer orderStatus = ((com.b2s.db.model.OrderLine) order.getOrderLines().get(0)).getOrderStatus();

        // Check for conflict
        if (( orderStatus == CommonConstants.ORDER_STATUS_DEMO ) ) {
            logger.error("PD confirmation: This is a Demo order. Order cannot be confirmed. ");
            return new ResponseEntity("This is a Demo order. Order cannot be confirmed.", HttpStatus.BAD_REQUEST);
        }

        // Check for conflict
        if (( orderStatus == CommonConstants.ORDER_STATUS_FAILED ) ) {
            logger.error("PD confirmation: Order is in failed status. Invalid payment status : {}",orderStatus);
            return new ResponseEntity("Order is already in Failed status. No further change is possible. Please contact support.", HttpStatus.BAD_REQUEST);
        }

        // Check for conflict
        if (( orderStatus == CommonConstants.ORDER_STATUS_PROCESSING ) ) {
            logger.error("PD confirmation: Order is being processed. Invalid PaymentStatus value: {}",orderStatus);
            return new ResponseEntity("Order is being processed. Conflicting status.", HttpStatus.CONFLICT);
        }

        //Order must be in Pending status
        if (orderStatus == CommonConstants.ORDER_STATUS_STARTED ) {
            try{
                manager.updatePDOrderLine(status,orderId);
            }catch (final Exception e){
                logger.error("PD confirmation: Error updating order details for orderId # {}, partnerReferenceId #{}",orderId,status.getPartnerReferenceId());
                return new ResponseEntity("Error updating order details. Please contact administrator",HttpStatus.BAD_REQUEST);
            }

        } else {
            logger.error(
                "PD confirmation: Order is not in Pending status but in: {} status. Order is either being processed " +
                    "or crossed a point that change in status is not possible at this time. Payment Status received: " +
                    "{}",
                orderStatus, status.getStatus());
            return new ResponseEntity(
                "Order is either being processed or crossed a point that change in status is not possible at this " +
                    "time. Please contact support.",
                HttpStatus.CONFLICT);
        }

        return new ResponseEntity(HttpStatus.OK);
    }
    /**
     * This REST service will be exposed and authenticated through basicAuth.
     * After successful authentication, order status will be set to PROCESSING or FAILED based on the status
     * and email notification will be sent accordingly.
     *
     * @param orderId
     * @param paymentStatus  true/false
     * @return
     */
    @RequestMapping(value = {"/confirmPurchase/{orderId}"}, method=RequestMethod.PUT)
    public ResponseEntity<Void> confirmPurchase(@PathVariable final Long orderId,
        @RequestParam(required = true, value = "paymentStatus") final Boolean paymentStatus) throws RestException {
        try {
            logger.info("AUDIT Order Confirm purchase received for order id {}, payment status = {}", orderId, paymentStatus);

            // TODO : The following validation can be removed.
            //  From Spring 5.3, an empty string converted to other type is treated as missing.
            //  ie: with required=true, paymentStatus can never be null
            if (Objects.isNull(paymentStatus)) {
                logger.error(
                    "ConfirmPurchase: Failed to Confirm Order Purchase. Missing Payment Status for Order ID: {}.",
                    orderId);
                throw new RestException("ConfirmPurchase: Missing Payment Status.", HttpStatus.BAD_REQUEST);
            }

            //Validate b2s orderid
            final Order order = manager.getOrder(orderId);
            if ( order == null ) {
                logger.info("ConfirmPurchase:  Order ID {} not found in B2S.", orderId);
                throw new RestException("ConfirmPurchase: Order ID not found in B2S.", HttpStatus.NOT_FOUND);
            }


            // compare basic auth role and varId to identify user access to given order id
            if(!basicAuthValidation.isUserHasAccessToOrder(order)){
                logger.error("AUDIT ConfirmPurchase: Order ID {} does not belongs to your entity.",orderId);
                throw new RestException("ConfirmPurchase: Order ID does not belongs to your entity.",
                    HttpStatus.FORBIDDEN);
            }


            //get orderstatus
            final Integer orderStatus = ((com.b2s.db.model.OrderLine) order.getOrderLines().get(0)).getOrderStatus();

            // Check for conflict
            if (( orderStatus == CommonConstants.ORDER_STATUS_DEMO ) ) {
                logger.info("ConfirmPurchase: This is a Demo order. Order cannot be confirmed. ");
                throw new RestException("This is a Demo order. Order cannot be confirmed.", HttpStatus.BAD_REQUEST);
            }

            // Check for conflict
            if (( orderStatus == CommonConstants.ORDER_STATUS_FAILED ) ) {
                logger.info("ConfirmPurchase: Order is in failed status. Invalid payment status : {}", paymentStatus);
                throw new RestException("Order is already in Failed status. No further change is possible. Please " +
                    "contact support.", HttpStatus.BAD_REQUEST);
            }

            // Check for conflict
            if (( orderStatus == CommonConstants.ORDER_STATUS_PROCESSING ) ) {
                logger.info("ConfirmPurchase: Order is being processed. Invalid PaymentStatus value: {}", paymentStatus);
                throw new RestException("Order is being processed. Conflicting status.", HttpStatus.CONFLICT);
            }

            //Order must be in Pending status
            if (orderStatus == CommonConstants.ORDER_STATUS_STARTED ) {
                manager.updateOrderLines(order, paymentStatus);
            } else {
                logger.info("ConfirmPurchase: Order is not in Pending status but in: {} status. Order is either being processed or crossed a point that change in status is not possible at this time. Payment Status received: {}", orderStatus, paymentStatus);
                throw new RestException("Order is either being processed or crossed a point that change in status is not possible at this time. Please contact support.", HttpStatus.CONFLICT);
            }

            if(AppleUtil.isOrderTypePayrollDeduction(order)){
                cartService.emptyCart(createUserFromOrderDetails(order));
            }

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (final RestException re) {
            logger.error("ConfirmPurchase: RestException: Failed to Confirm Order Purchase...", re);
            throw re;
        } catch (final ServiceException se) {
            logger.error("ConfirmPurchase: ServiceException: Failed to Confirm Order Purchase...", se);
            throw new RestException("Failed to Confirm Order Purchase. Please notify B2S.",
            HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (final Exception se) {
            logger.error("ConfirmPurchase: Failed to Confirm Order Purchase...", se);
            throw new RestException("Failed to Confirm Order Purchase. Please notify B2S.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This REST end point will return information for posting purchase selection details to Vitality
     *
     * @param orderId
     * @return
     */
    @RequestMapping(value = {"/sendShipmentNotification"}, method=RequestMethod.GET)
    public ResponseEntity sendShipmentNotification(@RequestParam(required = true) final String orderId,
        @RequestParam(required = true) final Integer lineNum) {
        try {
            //mandatory field check
            if (StringUtils.isBlank(orderId)) {
                logger.error("sendShipmentNotification: Failed to send shipment notification. Missing OrderId: {}", orderId);
                return new ResponseEntity("Missing OrderId", HttpStatus.BAD_REQUEST);
            }

            try {
                //Validate b2s orderid
                final Order order = manager.getOrder(Long.valueOf(orderId));
                if ( order == null ) {
                    return new ResponseEntity("sendShipmentNotification: Order ID not found in B2S.", HttpStatus.NOT_FOUND);
                }
                final Program program = programService.getProgram(order.getVarId(), order.getProgramId(), LocaleUtils.toLocale(order.getLanguageCode() + "_" + order.getCountryCode()));
                ResponseEntity<EmailResponse> emailResponse = notificationService.sendDataEmail(order, program, CommonConstants.NotificationName.SHIPMENT, lineNum, null);

                if(emailResponse != null && emailResponse.getStatusCode() != null && emailResponse.getStatusCode().equals(HttpStatus.OK)) {
                    logger.info("Sucessfully send shipment notification email for order id: {} and line number: {}", orderId, lineNum);
                } else {
                    logger.info("Failed to send shipment notification email for order id: {} and line number: {}", orderId, lineNum);
                    return new ResponseEntity("Failed to send shipment notification email for order id: "+orderId+" and line number: "+lineNum, HttpStatus.NO_CONTENT);
                }
                return new ResponseEntity(emailResponse, HttpStatus.OK);
            } catch (final NumberFormatException nfe) {
                logger.error("sendShipmentNotification: The given order id, {}, is not numeric, Exception : {}", orderId, nfe);
                return new ResponseEntity("sendShipmentNotification: Order id should be numeric.", HttpStatus.BAD_REQUEST);
            }

        } catch (final ServiceException se) {
            logger.error("sendShipmentNotification: Failed to get order having order id {}. Exception: {}...", orderId, se);
            return new ResponseEntity("ServiceException while sending shipment notification", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (final Exception se) {
            logger.error("sendShipmentNotification: Failed to get purchase selection information...", se);
            return new ResponseEntity("Failed to send shipment notification for order id "+orderId+". Please notify B2S.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This REST end point will return information for posting purchase selection details to Vitality
     *
     * @param orderId
     * @return
     */
    @RequestMapping(value = {"/getPurchaseSelectionInfo/{orderId}"}, method=RequestMethod.GET)
    public ResponseEntity<Object> getPurchaseSelectionInformation(@PathVariable final String orderId,
        final HttpSession session) {
        final User user = appSessionInfo.currentUser();
        try {
            if (!validateRequest(orderId, session)) {
                return ResponseEntity.badRequest().body("Missing or Invalid OrderId");
            }

            try {
                //Validate b2s orderid
                final Order order = manager.getOrder(Long.valueOf(orderId));
                if (order == null || Objects.isNull(user) || !user.getVarId().equalsIgnoreCase(order.getVarId())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("PurchaseSelectionInfo: Order ID not found in B2S.");
                }

                return processPurchaseSelectionInformation(orderId, session, order);
            } catch (final NumberFormatException nfe) {
                logger.error("PurchaseSelectionInfo: The given order id {} is not numeric, Exception : ", orderId, nfe);
                return ResponseEntity.badRequest().body("PurchaseSelectionInfo: Order id should be numeric.");
            }

        } catch (final ServiceException se) {
            logger
                .error("PurchaseSelectionInfo: Failed to get order having order id {}. Exception: {}...", orderId, se);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("ServiceException in PurchaseSelectionInfo service");
        } catch (final Exception se) {
            logger.error("PurchaseSelectionInfo: Failed to get purchase selection information...", se);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to get purchase selection information. Please notify B2S.");
        }
    }

    private ResponseEntity<Object> processPurchaseSelectionInformation(
        @PathVariable final String orderId,
        final HttpSession session, final Order order) {
        final User user = appSessionInfo.currentUser();
        final Program program = (Program) session.getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        final PurchaseSelectionInfo
            purchaseSelectionInfo = populatePurchaseSelectionDetails(order, user, program, session);
        if(!(purchaseSelectionInfo instanceof PurchaseSelectionInfoPPC || purchaseSelectionInfo instanceof PurchaseSelectionInfoGrassRoots) ) {
            //remove from session for var redirect
            if(session.getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT) != null) {
                session.removeAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
                cartService.emptyCart(user);
            }
            session.removeAttribute(CommonConstants.ORDER_ID_SESSION_OBJECT);
        } else {
            final String gsonToJsonString = AppleUtil.gsonToJsonString(purchaseSelectionInfo);
            logger.info("Redirecting to PPC with values: {}", gsonToJsonString);
        }
        //Setting this value only for Automation Test Cases, Data Validation for regression testing
        if (order != null) {
            session.setAttribute(CommonConstants.ORDER_ID_FOR_AUTOMATION_TEST, order.getOrderId());
        }

        if(Objects.isNull(purchaseSelectionInfo)) {
            logger.error("Post back data is empty for order id # {}", orderId);
        }
        logger.info("Successfully obtained post back data");
        // Return map for dynamic behaviour of weightwatcher in query parameter
        if(purchaseSelectionInfo instanceof PurchaseSelectionInfoWeightWatchers){
            Map map=new HashMap();
            map.putAll(((PurchaseSelectionInfoWeightWatchers) purchaseSelectionInfo).getAdditionalInfo());
            map.put(CommonConstants.ORDER_ID, ((PurchaseSelectionInfoWeightWatchers) purchaseSelectionInfo).getOrderId());
            map.put(CommonConstants.PURCHASE_POST_URL,purchaseSelectionInfo.getPurchasePostUrl());
            map.put(CommonConstants.METHOD,purchaseSelectionInfo.getMethod());
            return ResponseEntity.ok(map);
        }

        // postback data to client via API
        if(program.getConfig().get(CommonConstants.POST_BACK_TYPE)!=null
                && program.getConfig().get(CommonConstants.POST_BACK_TYPE).toString().equalsIgnoreCase(CommonConstants.POST_BACK_TYPES.API.getValue())
                && AppleUtil.isPayrollDeductionCompleted(order))
        {
            final VAROrderManagerIF varOrderManager = varOrderManagerHolder.getVarOrderManager(order.getVarId());
            varOrderManager.performAPIPostBack(order,user,program);
        }

        return ResponseEntity.ok(purchaseSelectionInfo);
    }

    private boolean validateRequest(final String orderId, final HttpSession session) {
        boolean result;
        if (StringUtils.isBlank(orderId)) {
            logger.error("PurchaseSelectionInfo: Failed to get purchase selection info for posting. Missing OrderId: {}", orderId);
            result = false;
        } else if (session.getAttribute(CommonConstants.ORDER_ID_SESSION_OBJECT) != null &&
            !orderId.equals(session.getAttribute(CommonConstants.ORDER_ID_SESSION_OBJECT).toString())) {
            // Validation for PPC postback. Verifying whether PPC postback is sending back the correct order id
            logger.error("Invalid order ID");
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    private PurchaseSelectionInfo populatePurchaseSelectionDetails(
        final Order order, final User user, final Program program, final HttpSession session) {
        final String overridePurchasePostUrl;
        if (order.getOrderTotalInMoney().getAmount().doubleValue() <= 0.0d) {
            overridePurchasePostUrl = applicationProperties
                .getProperty(PlaceOrderController.getPurchasePostURL(user.getVarId(), user.getProgramId()));
            if(StringUtils.isNotBlank(overridePurchasePostUrl)){
                session.setAttribute(ExternalUrlConstants.PURCHASE_POST_URL,overridePurchasePostUrl);
            }
        }
        PurchaseSelectionInfo purchaseSelectionInfo = null;
        if (CommonConstants.VAR_VITALITYUS.equalsIgnoreCase(user.getVarId()) || CommonConstants.VAR_VITALITYCA.equalsIgnoreCase(user.getVarId())){
            purchaseSelectionInfo = populatePurchaseSelectionVitality(order, user, session);
        }

        return purchaseSelectionInfo;
    }

    private PurchaseSelectionInfo populatePurchaseSelectionVitality(
        final Order order, final User user, final HttpSession session) {

        final PurchaseSelectionInfoVitality purchaseSelectionInfo = new PurchaseSelectionInfoVitality();
        if(order != null && CollectionUtils.isNotEmpty(order.getOrderLines())) {
            final OrderLine orderLine = (OrderLine)order.getOrderLines().get(0);

            setPurchaseSelectionInfoName(purchaseSelectionInfo, orderLine);

            purchaseSelectionInfo.setMemberID(user.getUserId());
            purchaseSelectionInfo.setCurrencyType(order.getCurrencyCode());
            purchaseSelectionInfo.setTransactionDate(order.getOrderDate().toString());
            purchaseSelectionInfo.setPurchaseReference(String.valueOf(order.getOrderId()));
            purchaseSelectionInfo.setDeliveryFullName(new StringBuilder().append(order.getFirstname()).append(" ").append(order.getLastname()).toString());
            purchaseSelectionInfo.setDeliveryAddressLine1(order.getAddr1());
            purchaseSelectionInfo.setDeliveryAddressLine2(order.getAddr2());
            purchaseSelectionInfo.setDeliveryCity(order.getCity());
            purchaseSelectionInfo.setDeliveryState(order.getState());
            purchaseSelectionInfo.setDeliveryCountry(order.getCountry());
            purchaseSelectionInfo.setDeliveryZipCode(order.getZip());
            purchaseSelectionInfo.setEmail(order.getEmail());
            purchaseSelectionInfo.setPhoneNumber(order.getPhone());
            purchaseSelectionInfo.setPretaxtotal(order.getOrderSubTotalInMoney().getAmount().toString());
            purchaseSelectionInfo.setPosttaxtotal(order.getOrderTotalInMoney().getAmount().toString());
            purchaseSelectionInfo.setTaxAmount(order.getOrderTotalTaxesInMoney().getAmount().toString());
            purchaseSelectionInfo.setItemDescription(orderLine.getName());
            purchaseSelectionInfo.setPurchaseRef(orderLine.getSku());
            if (user.getAdditionalInfo() != null) {
                purchaseSelectionInfo.setEmployerId(user.getAdditionalInfo().get(CommonConstants.EMPLOYER_ID));
                purchaseSelectionInfo.setTenantId(user.getAdditionalInfo().get(CommonConstants.TENANT_ID));
            }
        }
        purchaseSelectionInfo.setPurchasePostUrl(purchasePostURL(session, user));
        return purchaseSelectionInfo;
    }

    private void setPurchaseSelectionInfoName(final PurchaseSelectionInfoVitality purchaseSelectionInfo,
        final OrderLine orderLine) {
        if(orderLine != null && CollectionUtils.isNotEmpty(orderLine.getOrderAttributes())) {
            for (final OrderLineAttribute orderLineAttribute : orderLine.getOrderAttributes()) {
                try {
                    BeanUtils.setProperty(purchaseSelectionInfo, orderLineAttribute.getName(), orderLineAttribute.getValue());
                } catch (IllegalAccessException e) {
                    logger.error("Access not allowed error while Setting the values on the PurchaseSelectionInfo object", e);
                } catch (InvocationTargetException e) {
                    logger.error("Could not invoke setter error while Setting the values on the PurchaseSelectionInfo object", e);
                }
            }
        }
    }

    /**
     * Gets purchase post url from environment.properties files.
     */
    private String purchasePostURL(final HttpSession session, final User user){
        String purchasePostUrl = null;

        // Read the post url from session
        if ( session.getAttribute(ExternalUrlConstants.PURCHASE_POST_URL) != null ) {
            purchasePostUrl = (String) session.getAttribute(ExternalUrlConstants.PURCHASE_POST_URL);
        }
        if ( StringUtils.isEmpty(purchasePostUrl) ) {
            if (user != null && StringUtils.isNotBlank(user.getVarId()) &&
                StringUtils.isNotBlank(user.getProgramId()) && user.getLocale() != null) {
                purchasePostUrl = applicationProperties
                    .getProperty(PlaceOrderController.getPurchasePostURL(user.getVarId(), user.getProgramId(), user
                        .getLocale().toString()));
            }
            if (StringUtils.isEmpty(purchasePostUrl) && user != null && StringUtils.isNotBlank(user.getVarId()) && StringUtils.isNotBlank(user.getProgramId())) {
                purchasePostUrl = applicationProperties.getProperty(PlaceOrderController.getPurchasePostURL(user.getVarId(), user.getProgramId()));
            }
            if(StringUtils.isEmpty(purchasePostUrl) && Objects.nonNull(user)) {
                purchasePostUrl = applicationProperties.getProperty(PlaceOrderController.getPurchasePostURL(user.getVarId()));
            }
        }
        return purchasePostUrl;
    }

    private String getRedirectURL(final ResponseEntity response,final User user, final Cart cart, final Program program){
        String redirectUrl = null;

        // Error redirect for non cart var's. Basically for Vitality and BSWIFT as of now.   It is for single item purchase
        if(!HttpStatus.OK.toString().equals(response.getStatusCode().toString()) && VAR_WITH_INVISIBLE_CART.containsKey(user.getVarId())) {
            if(CollectionUtils.isNotEmpty(cart.getCartItems()) && CollectionUtils.isNotEmpty(cart.getCartItems().get(0).getProductDetail().getCategories())){
                redirectUrl=PropertiesUtils.getInstance("application.properties").getValue("split.tender.order.error." + cart.getCartItems().get(0).getProductDetail().getCategories().get(0).getSlug() + ".url");
            }
            if(StringUtils.isBlank(redirectUrl)){
                redirectUrl=PropertiesUtils.getInstance("application.properties").getValue("split.tender.order.error.home.url"); // get default url if slug does not match.
            }
            return redirectUrl;
        }

        redirectUrl = (String)program.getConfig().get("split.tender.order." + response.getStatusCode().value() + ".url");
        if(StringUtils.isBlank(redirectUrl)) {
            // This block of code for var's with cart functionality. Refer map {VAR_WITH_INVISIBLE_CART} for non cart var
            final String redirectURLProperty = "split.tender.order." + response.getStatusCode().value() + ".url";
            redirectUrl = CommonConstants.getApplicationProperty( redirectURLProperty, user.getVarId(), user.getProgramId(), applicationProperties );
        }


        return redirectUrl;
    }


    /**
     * Resets user program credit adds
     * @param cart
     */
    private void resetUserCreditAdds(Cart cart) {
        if(cart != null) {
            cart.setCost(0d);
            cart.setAddPoints(0);
            cart.setPointPurchaseRate(null);
        }

        if(cart != null && CollectionUtils.isNotEmpty(cart.getCartItems())) {
            cart.getCartItems().removeIf(cartItem -> cartItem.getSupplierId() == CommonConstants.SUPPLIER_TYPE_CREDIT);
            cart.getCartItems().removeIf(cartItem -> cartItem.getSupplierId() == CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE);
        }
    }

    private User createUserFromOrderDetails(final Order order){
        final User user=new User();
        user.setVarId(order.getVarId());
        user.setProgramId(order.getProgramId());
        user.setUserId(order.getUserId());
        return user;
    }
}
