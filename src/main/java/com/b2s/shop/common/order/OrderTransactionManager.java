package com.b2s.shop.common.order;

import com.b2r.service.payroll.ppc.model.CartResponse;
import com.b2r.service.payroll.ppc.model.PayrollConstants;
import com.b2r.service.payroll.ppc.model.TransactionInfoResponse;
import com.b2r.service.payroll.ppc.service.PayrollService;
import com.b2s.apple.entity.PricingLogEntity;
import com.b2s.apple.mapper.CartRequestMapper;
import com.b2s.apple.mapper.ProgramMapper;
import com.b2s.apple.mapper.ToStringConvertor;
import com.b2s.apple.model.CartPricingRequestDTO;
import com.b2s.apple.model.CartPricingResponseDTO;
import com.b2s.apple.services.*;
import com.b2s.common.services.discountservice.CouponDetails;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.dao.OrderAttributeValueDao;
import com.b2s.rewards.apple.dao.PricingLogDao;
import com.b2s.rewards.apple.dao.VarProgramDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.common.util.KountIdologyErrorsEnum;
import com.b2s.rewards.merchandise.action.CartCalculationUtil;
import com.b2s.security.idology.IDologyContextProviderImpl;
import com.b2s.security.idology.IDologyStatusConsumer;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.b2r.B2ROrderManager;
import com.b2s.shop.common.order.msg.Message;
import com.b2s.shop.common.order.var.OrderCodeStatus;
import com.b2s.shop.common.order.var.VAROrderManagerIF;
import com.b2s.shop.common.order.var.VarOrderManagerHolder;
import com.b2s.shop.util.VarProgramConfigHelper;
import com.client.kount.model.KountDecision;
import com.client.kount.model.KountSession;
import com.google.gson.Gson;
import com.paypal.soap.api.AckCodeType;
import com.paypal.soap.api.DoCaptureResponseType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static com.b2s.rewards.apple.model.PaymentOption.PaymentProvider.PPC;
import static com.b2s.rewards.common.util.CommonConstants.PaymentOption.PAYROLL_DEDUCTION;
import static com.b2s.apple.services.AnalyticsService.*;

@Service
public class OrderTransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(OrderTransactionManager.class);

    boolean orderCompleted = false;
    private DoCaptureResponseType resp = null;

    @Autowired
    private B2ROrderManager b2rOrderManager;


    @Autowired
    private OrderAttributeValueDao orderAttributeValueDao;

    @Autowired
    private VarProgramDao varProgramDao;

    @Autowired
    private VarProgramConfigHelper varProgramConfigHelper;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private VarOrderManagerHolder varOrderManagerHolder;


    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private DiscountCodeTransactionManager discountCodeTransactionManager;

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private CartRequestMapper cartRequestMapper;

    @Autowired
    private ProgramService programService;

    @Autowired
    private VarProgramNotificationService varProgramNotificationService;

    @Autowired
    private AppleKountService appleKountService;

    @Autowired
    private IDologyContextProviderImpl iDologyContextProvider;

    @Autowired
    private IDologyStatusConsumer iDologyStatusConsumer;

    @Autowired
    private CartService cartService;

    @Autowired
    private CreditTransactionManager creditManager;

    @Autowired
    private OrderCommitStatusService orderCommitStatusService;

    private Message b2rMessage;
    private Message varMessage;


    @Autowired
    private MessageService messageService;

    @Autowired
    private CartOrderConverterService cartOrderConverterService;

    @Autowired
    private PricingLogDao pricingLogDao;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AnalyticsService analyticsService;

    public Message getB2rMessage() {
        if (b2rMessage == null) {
            b2rMessage = new Message();
        }
        return b2rMessage;
    }

    public Message getVarMessage() {
        if (varMessage == null) {
            varMessage = new Message();
        }
        return varMessage;
    }

    public String getVAROrderId() {
        return getVarMessage().getVAROrderId();
    }

    public Order getOrder(Long orderId)
            throws ServiceException {
        try {
            logger.info("Loading order from database for OrderId: {}", orderId);
            //TODO: get orderattribute
            Order order = b2rOrderManager.selectOrderById(orderId);
            if (order != null) {
                List<OrderAttributeValue> orderAttributeValues = orderAttributeValueDao.getByOrder(order.getOrderId());
                order.setOrderAttributeValues(orderAttributeValues);
            }
            return order;
        } catch (final Exception ex) {
            logger.error("getOrder: Failed to load Order for OrderId: {} ", orderId);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
    }

    //Apple Order
    public OrderStatus placeMyOrder(Cart cart, User user, Program program, final HttpServletRequest servletRequest,
        final String transactionId) {

        Order order = getOrderFromCart(cart, user, program);

        VAROrderManagerIF varOrderManager = null;
        boolean isVarOrderSuccess = false;
        try {

            // Check user points
            varOrderManager = varOrderManagerHolder.getVarOrderManager(user.getVarId());

            if (validateUserPoints(cart, user, program, order, varOrderManager)) {
                //Fail the order.
                return getOrderStatus(order, false, program);
            }

            order = validateDemoProgram(program, order);

            final OrderStatus orderStatus = validateIdologyKountFailure(cart, user, program, servletRequest, order);
            if (orderStatus != null) {
                return orderStatus;
            }


            // set discountCode line status as started only for payroll deduction orders.
            // Status will be changed to completed when orders gets confirmed from payroll deduction provider
            setDiscountCodeLineStatus(order);

            boolean isB2sOrderSuccess = placeB2sOrder(user, program, order);

            if (!isB2sOrderSuccess) {
                logger.warn("Order Placement: B2S Order placement failed and DB entries rolled back ");
                orderCompleted = false;
                orderCommitStatusService.endPlacingOrder(user.getUserId(), user.getVarId(), user.getProgramId(), order);
                return getOrderStatus(order, orderCompleted, program);
            } else {
                logger.info("Inserting PricingLog to database");
                persistPricingLog(order);

                //Insert orderAttribute into DB
                insertOrderAttributes(cart, user, order);

                logger.info("Order Placement: B2S Order created in DB successfully with orderid {}",
                    order.getOrderId());

                //  To mark discount code as redeemed for non payroll deduction orders
                if (updateDiscountCodeStatus(cart, user, order)) {
                    return getOrderStatus(order, orderCompleted, true, program);
                }

                logger.info("Order Placement: VarOrderPlacement: Starting VAR order placement");

                isVarOrderSuccess = varOrderManager.placeOrder(order, user, program);
                if (!isVarOrderSuccess) {
                    analyticsService.trackEvent(order, program, user, AnalyticsEventName.ORDER_CANCELLATION, AnalyticsType.HEAP);
                    return processVarOrderFailure(cart, user, program, order, varOrderManager);
                } else{
                    if (processVarOrderSuccess(cart, user, program, transactionId, order, varOrderManager)){
                        return getOrderStatus(order, false, program);
                    }
                }
            }

            // get all messages
            varMessage = varOrderManager.getMessage();

        } catch (B2RException b2re) {
            analyticsService.trackEvent(order, program, user, AnalyticsEventName.ORDER_CANCELLATION, AnalyticsType.HEAP);
            b2RExceptionHandler(user, program, order, varOrderManager, b2re);
        } catch (Exception e) {
            analyticsService.trackEvent(order, program, user, AnalyticsEventName.ORDER_CANCELLATION, AnalyticsType.HEAP);
            placeOrderExceptionHandler(cart, user, program, order, varOrderManager, e);

        }

        OrderStatus orderStatus = getOrderStatus(order, orderCompleted, program);
        resetMessages();
        return orderStatus;
    }

    private void placeOrderExceptionHandler(Cart cart, User user, Program program, Order order, VAROrderManagerIF varOrderManager, Exception e) {
        orderCompleted = false;
        if (b2rOrderManager != null && order.getOrderId()!=null) {
            b2rOrderManager.rollBackOrder(order, user);
        }
        if (varOrderManager != null && order != null && StringUtils.isNotBlank(order.getVarOrderId())) {
            varOrderManager.cancelOrder(order, user, program);
        }
        // Rollback / un-apply discount code usage
        if (CollectionUtils.isNotEmpty(cart.getDiscounts()) && Objects.nonNull(discountCodeTransactionManager)) {
            discountCodeTransactionManager.rollbackDiscountCoderedeemption(cart.getDiscounts(), user);
        }
        logger.error("Order Placement: Exception while placing an order: ", e);
        messageService.insertMessageException(user, this.getClass().getName(),
                "ERROR IN ORDER TRANSACTION MANAGER", e);
    }

    private void b2RExceptionHandler(User user, Program program, Order order, VAROrderManagerIF varOrderManager, B2RException b2re) {
        orderCompleted = false;
        logger.error("B2RException occured while placing order... {}", b2re.getMessage());
        if (b2re != null
                && StringUtils.isNotBlank(b2re.getMessage())
                && b2re.getMessage().contains(CommonConstants.UA_TIMED_OUT_ERROR_TEXT)) {
            getVarMessage().setTimedOut(true);
        }
        if (order != null && order.getOrderId() != null && order.getOrderId().intValue() > 0) {
            if (b2rOrderManager != null) {
                b2rOrderManager.rollBackOrder(order, user);
            }
            if (varOrderManager != null && StringUtils.isNotBlank(order.getVarOrderId())) {
                varOrderManager.cancelOrder(order, user, program);
            }
        }
    }

    private boolean processVarOrderSuccess(Cart cart, User user, Program program, String transactionId, Order order, VAROrderManagerIF varOrderManager) {
        logger.info("Order Placement: VarOrderPlacement: VAR order placed successfully with VarOrderId " +
                "{}", order.getVarOrderId() );
        /**
         * To capture credit card transaction, checking whether the order has credit order lines instead
         * of cart.getAddPoints > 0 condition
         * Checking credit order lines is more stable and consistent because VIS redemption is based on
         * order line data.
         */
        if (processSplitTenderOrders(cart, user, program, transactionId, order, varOrderManager)) {
            return true;
        }
        logger.info("Order Placement: Updating order with VarOrderId received from VIS ");
        b2rOrderManager.updateOrderVAROrderId(order, order.getVarOrderId());
        cart.setPurchased(true);
        orderCommitStatusService.endPlacingOrder(user.getUserId(), user.getVarId(),
                user.getProgramId(), order);

        orderCompleted = true;

        //For Vitality, Order status to PROCESSING & email notification will be sent from confirmPurchase
        // REST call, invoked externally by Vitality
        if (varOrderManager.isOrderReadyForProcessing() &&
                !PayrollConstants.PAYROLL_DEDUCTION_PAYMENT_OPTION.equals(order.getSelectedPaymentOption())) {
            updateOrderLineStatus(cart, program, order, varOrderManager);
        } else {
            final boolean sendCfmEmailforStartedStatus =
                    (Boolean) program.getConfig().getOrDefault(CommonConstants.SEND_CFM_EMAIL_FOR_STARTED_STATUS,
                            Boolean.FALSE);
            if(!varOrderManager.isOrderReadyForProcessing() && sendCfmEmailforStartedStatus){
                logger.info("Order Placement: Send Confirmation Email for Order not ready for Processing - in Started" +
                    " status");
                notificationService.sendAsyncNotification(order, program);
            }

            if (PayrollConstants.PAYROLL_DEDUCTION_PAYMENT_OPTION
                    .equals(order.getSelectedPaymentOption()) && program.getPayments().stream()
                    .anyMatch(paymentOption -> paymentOption.getPaymentProvider() == PPC)) {

                sendCartDataToPPCAndSaveResponseData(order, user, program);
            }
        }
        updateUserPoints(user);
        return false;
    }

    private void updateUserPoints(User user) {
        // some vars such as razr have negative point values and this is to prevent the user from
        // displaying negative points
        logger.info("Order Placement: updating user points ... ");
        if (user.getPoints() == null || user.getPoints() < 0) {
            user.setPoints(0);
        }
    }

    //For Vitality, Order status to PROCESSING & email notification will be sent from confirmPurchase
    // REST call, invoked externally by Vitality
    private void updateOrderLineStatus(Cart cart, Program program, Order order, VAROrderManagerIF varOrderManager) {
        varMessage = varOrderManager.getMessage();
        if (varMessage != null && varMessage.isOrderOnHold()) {
            b2rOrderManager.updateProductOrderLineStatus(order, CommonConstants.ORDER_STATUS_ON_HOLD);
            // In On Hold flow, we only hold product order lines. So completing non-product order lines.
            b2rOrderManager
                    .updateNonProductOrderLineStatus(order, CommonConstants.ORDER_STATUS_COMPLETED);
            String holdQueueDurationInMinutesStr = varProgramConfigHelper
                    .getValue(order.getVarId(), order.getProgramId(),
                            CommonConstants.ORDER_HOLD_DURATION_IN_MINUTES_KEY);
            if (StringUtils.isNotBlank(holdQueueDurationInMinutesStr)) {
                logger.info("Order Placement: Hold duration (in minutes): {}", holdQueueDurationInMinutesStr);
                final OrderAttributeValue orderAttributeValue =
                    createAndPersistOrderAttribute(CommonConstants.ORDER_HOLD_DURATION_IN_MINUTES_KEY, holdQueueDurationInMinutesStr,
                        order.getOrderId());
                if (Objects.nonNull(orderAttributeValue)) {
                    order.getOrderAttributeValues().add(orderAttributeValue);
                }
            }
        } else if (enableOrderProcessing(program)){
            logger.info("Order Placement: Changing the order line -> order status to 'Processing'");
            b2rOrderManager
                    .updateProductOrderLineStatus(order, CommonConstants.ORDER_STATUS_PROCESSING);
            // Since OrderService does not consider non-product lines, we complete it
            b2rOrderManager
                    .updateNonProductOrderLineStatus(order, CommonConstants.ORDER_STATUS_COMPLETED);
        }

        // Send order confirmation email
        sendEmailConfirmation(cart, program, order, varOrderManager);
    }

    private void sendEmailConfirmation(Cart cart, Program program, Order order, VAROrderManagerIF varOrderManager) {
        logger.info("Order Placement: Sending email notification about successful order completion to email id: {} ",
            order.getEmail());
        if (varOrderManager.isSendOrderConfirmationEmailToUser()) {
            notificationService.sendAsyncNotification(order, program);
        }
    }

    private boolean processSplitTenderOrders(Cart cart, User user, Program program, String transactionId, Order order, VAROrderManagerIF varOrderManager) {
        /**
         * To capture credit card transaction, checking whether the order has credit order lines instead
         * of cart.getAddPoints > 0 condition
         * Checking credit order lines is more stable and consistent because VIS redemption is based on
         * order line data.
         */
        if (order.isSplitTenderOrder()) {
            logger.info("Order Placement: CC payment - Starting CC Capture");
            if (!creditManager.placeCreditPayment(order, user, cart, transactionId)) {
                logger.warn("Order Placement: CC payment - Failed to process Credit payment process ");
                handleFailedCreditPayment(user, order);
                //
                updateOrderLines(b2rOrderManager, order);
                //refund points
                if (order != null && StringUtils.isNotBlank(order.getVarOrderId())) {
                    analyticsService.trackEvent(order, program, user, AnalyticsEventName.ORDER_CANCELLATION, AnalyticsType.HEAP);
                    varOrderManager.cancelOrder(order, user, program);
                    logger.info("Order Placement: CC payment Failed - Cancel Order completed");
                }
                //rollback order related entries
                b2rOrderManager.rollBackOrder(order, user);
                // Rollback / un-apply discount code usage
                if (CollectionUtils.isNotEmpty(cart.getDiscounts())) {
                    discountCodeTransactionManager
                            .rollbackDiscountCoderedeemption(cart.getDiscounts(), user);
                }
                return true;
            }
            resp = new DoCaptureResponseType();
            resp.setAck(AckCodeType.Success);
            logger.info("Order Placement: CC payment - End CC Capture - Payment successful");
        }
        return false;
    }

    private OrderStatus processVarOrderFailure(Cart cart, User user, Program program, Order order, VAROrderManagerIF varOrderManager) {
        logger.warn(
                "Order Placement: VarOrderPlacement: Points redemption in VIS did not go through. Removing pending order created" +
                        " and aborting the process ...");
        orderCompleted = false;
        varMessage = varOrderManager.getMessage();
        //VIS cancel redeemed points
        if (order != null && StringUtils.isNotBlank(order.getVarOrderId())) {
            varOrderManager.cancelOrder(order, user, program);
            logger.debug("Order Placement: Cancel Order completed");
        }
        b2rOrderManager.rollBackOrder(order, user);
        // Rollback / un-apply discount code usage
        if (CollectionUtils.isNotEmpty(cart.getDiscounts())) {
            discountCodeTransactionManager.rollbackDiscountCoderedeemption(cart.getDiscounts(), user);
        }

        orderCommitStatusService.endPlacingOrder(user.getUserId(), user.getVarId(),
                user.getProgramId(), order);
        return getOrderStatus(order, orderCompleted, program);
    }

    private boolean updateDiscountCodeStatus(Cart cart, User user, Order order) {
        //  To mark discount code as redeemed for non payroll deduction orders
        if (CollectionUtils.isNotEmpty(cart.getDiscounts()) && !isOrderTypePayrollDeduction(order)) {
            logger.info("Order Placement: DiscountCode: Starting discount code redemption");
            User tmpUser = getUserObjectForDiscountCodeRedemption(order);
            for (final DiscountCode discountCode : cart.getDiscounts()) {
                final CouponDetails couponDetails =
                        discountCodeTransactionManager.redeemDiscountCode(discountCode, tmpUser);

                if (couponDetails.isValid()) {
                    logger.info("Order Placement: DiscountCode: Applied discount code # {}", discountCode.getDiscountCode());
                } else {
                    logger.error("Order Placement: DiscountCode: Error applying discount code # {}", discountCode.getDiscountCode());
                    orderCompleted = false;
                    b2rOrderManager.rollBackOrder(order, user);
                    orderCommitStatusService.endPlacingOrder(user.getUserId(), user.getVarId(),
                            user.getProgramId(), order);
                    return true;
                }

            }
        }
        return false;
    }

    private void insertOrderAttributes(Cart cart, User user, Order order) {
        List<OrderAttributeValue> orderAttributeValues = order.getOrderAttributeValues();

        if (cart.getAddPoints() > 0 && Objects.nonNull(cart.getCreditItem())) {
            OrderAttributeValue cclast4 = CartOrderConverterService.buildOrderAttribute
                (CommonConstants.CREDIT_CARD_LAST_FOUR_DIGIT, cart.getCreditItem().getCcLast4());
            OrderAttributeValue cctype = CartOrderConverterService.buildOrderAttribute
                (CommonConstants.CREDIT_CARD_TYPE, cart.getCreditItem().getCreditCardType());

            orderAttributeValues.add(cclast4);
            orderAttributeValues.add(cctype);
        }

        if (StringUtils.isNotEmpty(user.getEmployerId())) {
            OrderAttributeValue employerId = CartOrderConverterService.buildOrderAttribute
                (CommonConstants.EMPLOYER_ID, user.getEmployerId());
            orderAttributeValues.add(employerId);
        }

        //Persist WF SAML Authorized User Information
        if (Objects.nonNull(user.getSessionUserInfo())) {
            OrderAttributeValue authFirstName = CartOrderConverterService.buildOrderAttribute(CommonConstants.VIS_AUTHORIZED_USER_FIRST_NAME, user.getSessionUserInfo().getFirstName());
            orderAttributeValues.add(authFirstName);
            OrderAttributeValue authLastName = CartOrderConverterService.buildOrderAttribute(CommonConstants.VIS_AUTHORIZED_USER_LAST_NAME, user.getSessionUserInfo().getLastName());
            orderAttributeValues.add(authLastName);
        }

        for (OrderAttributeValue orderAttribute : orderAttributeValues) {
            orderAttribute.setOrderId(order.getOrderId());
            if (StringUtils.isNotBlank(orderAttribute.getValue())) {
                orderAttributeValueDao.insert(orderAttribute);
            }
        }
    }

    private boolean placeB2sOrder(User user, Program program, Order order) {
        boolean isB2sOrderSuccess=false;
        try{
            isB2sOrderSuccess = b2rOrderManager.placeOrder(order, user, program);

        }catch (final Exception e){
            logger.error("Order Placement: B2R failed to persist order with exception: ", e);
            final Message msg = new Message();
            msg.setSuccess(false);
            msg.setContentText("B2R failed to place order!");
            msg.setCode(B2ROrderManager.FAIL);
            b2rMessage = msg;
        }
        return isB2sOrderSuccess;
    }

    private void setDiscountCodeLineStatus(Order order) {
        // Status will be changed to completed when orders gets confirmed from payroll deduction provider
        if (isOrderTypePayrollDeduction(order)) {
            order.getOrderLines().forEach(o -> {
                if (((OrderLine) o).getSupplierId()
                        .equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S)) {
                    ((OrderLine) o).setOrderStatus(CommonConstants.ORDER_STATUS_STARTED);
                }
            });
        }
    }

    private OrderStatus validateIdologyKountFailure(Cart cart, User user, Program program, HttpServletRequest servletRequest, Order order) {
        boolean idologyFailed = iDologyContextProvider.isIdologyEnabled(user.getVarId(), user.getProgramId()) && !iDologyStatusConsumer.isTransactionSuccess();
        boolean kountFailed = false;

        if(appleKountService.isKountEnabled(program)) {
            final KountDecision kountDecision = appleKountService.getDecision(cart, user, program, (KountSession)servletRequest.getSession().getAttribute(CommonConstants.KOUNT_SESSION_OBJECT));
            if(!KountDecision.APPROVED.equals(kountDecision) && !KountDecision.REVIEW.equals(kountDecision)) {
                kountFailed = true;
                logger.warn("Kount Failed for the user");
            }
        }
        if (idologyFailed || kountFailed) {
            logger.warn("Idology or Kount Failed for the user");
            return getErrorCode(user, program, order, idologyFailed, kountFailed);
        }
        return null;
    }

    private OrderStatus getErrorCode(final User user, final Program program, final Order order,
                                     final boolean idologyFailed, final boolean kountFailed) {
        KountIdologyErrorsEnum errorCode = null;
        if (idologyFailed && kountFailed) {
            errorCode = KountIdologyErrorsEnum.KOUNT_IDOLOGY_ERROR_CODE;
        } else if (!idologyFailed && kountFailed) {
            errorCode = KountIdologyErrorsEnum.KOUNT_ERROR_CODE;
        } else if (idologyFailed && !kountFailed) {
            errorCode = KountIdologyErrorsEnum.IDOLOGY_ERROR_CODE;
        }


        orderCommitStatusService.endPlacingOrder(user.getUserId(), user.getVarId(),
                user.getProgramId(), order);
        final OrderStatus orderStatus = getOrderStatus(order, false, program);
        if (Objects.nonNull(errorCode)) {
            orderStatus.setErrorCode(errorCode.getValue());
        }
        return orderStatus;
    }

    private Order validateDemoProgram(Program program, Order order) {
        if (program.getIsDemo()) {
            logger.info("Order Placement: Setting order line status as DEMO user order...");
            order = setOrderDemoStatus(order);
        }
        return order;
    }

    private boolean validateUserPoints(Cart cart, User user, Program program, Order order,
        VAROrderManagerIF varOrderManager)
        throws IOException, B2RException {

        return !doesUserHaveEnoughPoints(user, order, varOrderManager, program) ||
            inCorrectSupplementalSplit(order, cart, program) ||
            doesPendingOrderExistAlready(user, order) ||
            paymentLimitNotSatisfied(cart, user, program);
    }

    private boolean paymentLimitNotSatisfied(Cart cart, User user, Program program){
        final boolean paymentLimitNotSatisfied =
            program.getPayments().size() == 1 && minMaxPaymentLimitNotSatisfied(cart, program, user);
        if(paymentLimitNotSatisfied){
            logger.warn("Order Placement: Payment Limit not satisfied");
        }
        return paymentLimitNotSatisfied;
    }

    private Order getOrderFromCart(Cart cart, User user, Program program) {
        logger.info("Order Placement: Converting shopping cart to order object...");
        //Convert cart to Order
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program);
        logger.info("Order Placement: Shopping Cart to Order object conversion completed successfully...");
        return order;
    }

    private boolean inCorrectSupplementalSplit(final Order order, final Cart cart, final Program program) {
        return cartService.inCorrectSupplementalSplit(new BigDecimal(order.getOrderTotalCashBuyInPoints()).negate().intValue(), cart, program, new StringBuilder());
    }

    private void sendCartDataToPPCAndSaveResponseData(
            final Order order,
            final User user,
            final Program program) {

        final CartResponse response = payrollService.sendCartRequest(cartRequestMapper.from(order, program, user));
        if (response == null) {
            b2rOrderManager.rollBackOrder(order, user);
            orderCompleted = false;
        } else {
            createAndPersistOrderAttribute(CommonConstants.PPC_CART_ID_ORDER_ATTRIBUTE_KEY, response.getCartId(), order.getOrderId());
            createAndPersistOrderAttribute(CommonConstants.PPC_ACCESS_TOKEN_ORDER_ATTRIBUTE_KEY, response.getAccessToken(), order.getOrderId());
        }
    }

    private OrderAttributeValue createAndPersistOrderAttribute(final String key, final String value, final Long orderId) {
        final OrderAttributeValue orderAttributeValue = CartOrderConverterService.buildOrderAttribute(key, value, orderId);
        if (Objects.nonNull(orderAttributeValue)) {
            orderAttributeValueDao.insert(orderAttributeValue);
        }

        return orderAttributeValue;
    }

    private void resetMessages() {
        b2rMessage = null;
        varMessage = null;
    }

    /**
     * Block user if the order is in pending status. This typically happens when order was placed thru another session
     *
     * @param user
     * @param order
     * @return
     */
    private boolean doesPendingOrderExistAlready(User user, Order order) {
        if (!orderCommitStatusService.startPlacingOrder(user.getUserId(), user.getVarId(),
                user.getProgramId(), order)) {
            Message msg = new Message();
            msg.setSuccess(false);
            msg.setContentText("PENDING ORDER FOUND: " + user.getUserid());
            msg.setCode(OrderCodeStatus.HAS_PENDING_ORDER.getValue());
            orderCompleted = false;
            varMessage = msg;
            Log.error(msg.getContentText());

            logger.error("Order Placement: Aborting, as user has pending orders...");

            return true;
        }
        return false;
    }

    private boolean minMaxPaymentLimitNotSatisfied(final Cart cart, final Program program, final User user) {
        final Optional<PaymentOption> payrollOption = program.getPayments().stream()
                .filter(payment -> CommonConstants.CAT_PAYROLLDEDUCTION_STR.equals(payment.getPaymentOption()))
                .findFirst();
        if (payrollOption.isPresent()) {
            final Message msg = new Message();
            msg.setSuccess(false);
            msg.setCode(OrderCodeStatus.FAIL.getValue());
            final PaymentOption paymentOption = payrollOption.get();
            if (validateMinimumPaymentLimit(cart, paymentOption)) {
                return setStatusMessageForPaymentLimit(msg, user, "Payment Min Limit not met for User ID: ");
            }
            if (validateMaximumPaymentLimit(cart, paymentOption))
                return setStatusMessageForPaymentLimit(msg, user, "Payment Max Limit exceeded for User ID: ");

        }
        return false;
    }

    private boolean validateMaximumPaymentLimit(Cart cart, PaymentOption paymentOption) {
        if (Optional.ofNullable(paymentOption.getPaymentMaxLimit()).isPresent()) {
            if (Optional.ofNullable(cart.getCartTotal().getDiscountedPrice()).isPresent()) {
                if (paymentOption.getPaymentMaxLimit() < cart
                        .getCartTotal().getDiscountedPrice().getAmount()) {
                    return true;
                }
            } else if (paymentOption.getPaymentMaxLimit() < cart.getCartTotal().getPrice().getAmount()) {

                return true;
            }
        }
        return false;
    }

    private boolean validateMinimumPaymentLimit(Cart cart, PaymentOption paymentOption) {
        if (OptionalDouble.of(paymentOption.getPaymentMinLimit()).isPresent()) {
            if (Optional.ofNullable(cart.getCartTotal().getDiscountedPrice()).isPresent()) {
                if (paymentOption.getPaymentMinLimit() > cart
                        .getCartTotal().getDiscountedPrice().getAmount()) {
                    return true;
                }
            } else if (paymentOption.getPaymentMinLimit() > cart.getCartTotal().getPrice().getAmount()) {
                return true;
            }
        }
        return false;
    }

    private boolean setStatusMessageForPaymentLimit(final Message msg, final User user, final String content) {
        msg.setContentText(content + user.getUserid());
        orderCompleted = false;
        varMessage = msg;
        Log.error(msg.getContentText());
        return true;
    }

    private boolean doesUserHaveEnoughPoints(User user, Order order, VAROrderManagerIF varOrderManager, Program program)
            throws IOException, B2RException {
        int balance;

        if(user.isAnonymous()){
            logger.info("Order Placement: Anonymous User flow");
            return doesAnonymousUserHasPurchasePower(order);
        }
        try {
            balance = varOrderManager.getUserPoints(user, program);
            user.setInitialUserBalance(balance);
        } catch (Exception e) {
            String message = "exception getUserPoints " + user.getVarId() + "-" + user.getProgramId() + "-"
                    + user.getUserId();
            logger.error(message, e);
            orderCommitStatusService.endPlacingOrder(user.getUserId(), user.getVarId(),
                    user.getProgramId(), order);
            throw e;
        }

        //Check for user points
        if (balance < order.getOrderTotalPointsPaid()) {
            Message msg = new Message();
            msg.setSuccess(false);
            msg.setContentText("NOT ENOUGH POINTS: " + user.getUserid());
            msg.setCode(OrderCodeStatus.NOT_ENOUGH_POINTS.getValue());
            orderCompleted = false;
            varMessage = msg;

            logger.warn("Order Placement: Aborting, as user has insufficient points...");

            return false;
        }
        return true;
    }

    private void handleFailedCreditPayment(User user, Order order) {
        final String reason = "CREDIT TRANSACTION FAILED";
        resp = new DoCaptureResponseType();
        resp.setAck(AckCodeType.Failure);
        orderCommitStatusService.endPlacingOrder(user.getUserId(), user.getVarId(),
                user.getProgramId(), order);
        List lines = order.getOrderLines();
        for (int i = 0; i < lines.size(); i++) {
            OrderLine line = (OrderLine) lines.get(i);
            line.setOrderStatus(CommonConstants.ORDER_STATUS_FAILED);
            if (CommonConstants.SUPPLIER_TYPE_CREDIT_S.equals(line.getSupplierId())) {
                line.setComment((StringUtils.isEmpty(line.getComment()) ? "" : line.getComment() + " - ") + reason);
            }
        }

        logFailedOrder(user, order, reason, null);
    }

    private void logFailedOrder(User user, Order order, String reason, Message message) {
        String messageString = message == null ? "" : message.getCode() + "-" + message.getContentText();
        logger.error("Error placing order: userid={};varId={};programId={};orderId={};reason={};message={}",
                user.getUserId(), user.getVarId(), user.getProgramId(), order.getOrderId(), reason, messageString);
    }

    private void updateOrderLines(B2ROrderManager manager, Order order) {
        List lines = order.getOrderLines();
        for (int i = 0; i < lines.size(); i++) {
            manager.updateOrderLine((OrderLine) lines.get(i));

        }
    }

    /**
     * Update orderlines with the status
     *
     * @param order         Order to be updated
     * @param paymentStatus true/false
     * @return
     * @throws ServiceException
     */
    public boolean updateOrderLines(final Order order, final boolean paymentStatus)
            throws ServiceException {
        try {
            if (order != null) {

                if (paymentStatus) {
                    logger.info("Confirm Purchase: Updating OrderLines with status {}, for Order: {}",
                            CommonConstants.ORDER_STATUS_PROCESSING, order.getOrderId());
                    b2rOrderManager.updateProductOrderLineStatus(order, CommonConstants.ORDER_STATUS_PROCESSING);
                    // Since OrderService does not consider non-product lines, we complete it
                    b2rOrderManager.updateNonProductOrderLineStatus(order, CommonConstants.ORDER_STATUS_COMPLETED);
                    //email notification
                    sendEmailNotification(order);

                } else {
                    logger.info("Confirm Purchase: Updating OrderLines with status {}, for Order: {}",
                            CommonConstants.ORDER_STATUS_FAILED, order.getOrderId());
                    b2rOrderManager.updateProductOrderLineStatus(order, CommonConstants.ORDER_STATUS_FAILED);
                    b2rOrderManager.updateNonProductOrderLineStatus(order, CommonConstants.ORDER_STATUS_FAILED);
                    //No email notification for failure
                }


                //send confirmation email
                return true;
            } else {
                logger.info("Confirm Purchase: Failed to Update OrderLines with status. Order is NULL ");
                return false;
            }
        } catch (final Exception ex) {
            if (Objects.nonNull(order)) {
                logger.error("Confirm Purchase: Update OrderLines: Failed to update OrderLines  for B2S OrderId: {} ",
                        order.getOrderId());
            }
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
    }

    private void sendEmailNotification(Order order) {
        final VarProgram varProgram =
                varProgramDao.getActiveVarProgram(order.getVarId(), order.getProgramId());
        final Set<ProgramConfig> programConfigs =
                programService.getProgramConfigs(order.getVarId(), order.getProgramId());
        if (varProgram != null) {
            final Locale locale = new Locale(order.getLanguageCode(), order.getCountryCode());
            final Program program = programMapper.from(varProgram, new ArrayList<>(programConfigs), locale);
            if (program != null) {
                final List<VarProgramNotification> varProgramNotifications =
                        varProgramNotificationService.getActiveEmailNotifications(varProgram.getVarId(),
                            varProgram.getProgramId(), locale.toString());

                // send email will fail if template mapping not available. Template will not be available
                // if we dont wish to send confirmation email to user
                if (varProgramNotifications.size() > 0) {
                    program.setNotifications(programMapper.from(varProgramNotifications));
                    notificationService.sendAsyncNotification(order, program);
                }
            }
        }
    }

    private Order setOrderDemoStatus(Order order) {
        List lines = order.getOrderLines();
        for (int u = 0; u < lines.size(); u++) {
            OrderLine line = (OrderLine) lines.get(u);
            if (line.getOrderStatus() != CommonConstants.ORDER_STATUS_FAILED) {
                line.setOrderStatus(CommonConstants.ORDER_STATUS_DEMO);
            }
        }

        return order;
    }

    /**
     * Order placement status details
     *
     * @param order
     * @param orderCompleted
     * @return
     */
    private OrderStatus getOrderStatus(Order order, boolean orderCompleted, final Program program) {
        final boolean showEarnPoints = (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_EARN_POINTS, Boolean.FALSE);
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setVarOrderId(order.getVarOrderId());
        orderStatus.setB2sOrderId(order.getOrderId());

        if(showEarnPoints){
            orderStatus.setEarnedPoints(order.getEarnedPoints());
        }
        getOrderStatusFromAttributeValue(order, orderStatus);
        orderStatus.setB2rMessage(b2rMessage != null ? b2rMessage.getContentText() : "");
        orderStatus.setVarMessage(varMessage != null ? varMessage.getContentText() : "");
        orderStatus.setOrderCompleted(orderCompleted);
        validatePayrollDeduction(order, orderStatus);
        getOrderHoldDuration(order, orderStatus);
        if (varMessage != null) {
            validatePromotionUseExceeded(orderStatus);
            validateTimedOut(orderStatus);
        }
        return orderStatus;
    }

    private void validateTimedOut(OrderStatus orderStatus) {
        if (varMessage.isTimedOut()) {
            orderStatus.setTimedOut(varMessage.isTimedOut());
        }
    }

    private void validatePromotionUseExceeded(OrderStatus orderStatus) {
        if (varMessage.isPromotionUseExceeded()) {
            orderStatus.setPromotionUseExceeded(varMessage.isPromotionUseExceeded());
        }
    }

    private void getOrderHoldDuration(Order order, OrderStatus orderStatus) {
        if (order.getOrderAttributeValues() != null) {
            List<OrderAttributeValue> orderAttributeValues = order.getOrderAttributeValues();
            for (OrderAttributeValue orderAttributeValue : orderAttributeValues) {
                if (CommonConstants.ORDER_HOLD_DURATION_IN_MINUTES_KEY.equals(orderAttributeValue.getName())) {
                    Float orderDurationInMinutes = Float.valueOf(orderAttributeValue.getValue());
                    final Integer orderDurationInDays = (orderDurationInMinutes <= (24 * 60)) ? 1 :
                        ((int) Math.ceil(orderDurationInMinutes / (24 * 60)));
                    orderStatus.setOrderHoldDurationInDays(orderDurationInDays);
                }
            }
        }
    }

    private void validatePayrollDeduction(Order order, OrderStatus orderStatus) {
        if (CommonConstants.CAT_PAYROLLDEDUCTION_STR.equals(order.getSelectedPaymentOption())) {
            orderStatus.setEmptyCart(false);
        }
    }

    private void getOrderStatusFromAttributeValue(Order order, OrderStatus orderStatus) {
        List<OrderAttributeValue> orderAttributeValueList = orderAttributeValueDao.getByOrder(order.getOrderId());
        if (orderAttributeValueList != null) {
            Optional<OrderAttributeValue> orderAttibute = orderAttributeValueList.stream()
                    .filter(orderAttributeValue -> orderAttributeValue
                            .getName()
                            .equalsIgnoreCase(CommonConstants.TIME_ZONE_ID)).findFirst();
            if (orderAttibute.isPresent()) {
                orderStatus.setOrderDate(CartCalculationUtil.convertTimeZone(order.getOrderDate(), TimeZone.getTimeZone
                        (orderAttibute.get().getValue())));
            } else {
                //TODO need to refactor to reduce the IFs
                orderStatus.setOrderDate(order.getOrderDate());
            }
        } else {
            orderStatus.setOrderDate(order.getOrderDate());
        }
    }

    private OrderStatus getOrderStatus(final Order order, final boolean orderCompleteflag,
                                       final boolean discountCodeError, final Program program) {
        final OrderStatus orderStatus = getOrderStatus(order, orderCompleteflag, program);
        orderStatus.setDiscountCodeError(discountCodeError);
        return orderStatus;
    }

    @Transactional
    public void updatePDOrderLine(OrderStatusUpdate status, String orderId)
            throws Exception {

        final int orderStatusItemLine;
        final int orderStatusOtherLines;
        int pdAmountInCent = 0;
        Order order = null;
        if (status.getPdAmount() != null) {
            pdAmountInCent = getAmountInCents(Double.parseDouble(status.getPdAmount()));
        }
        int ccAmountInCent = 0;
        if (status.getCcAmount() != null) {
            ccAmountInCent = getAmountInCents(Double.parseDouble(status.getCcAmount()));
        }

        if (status != null && StringUtils.isNotBlank(orderId)) {
            try {
                if (status.getStatus().equalsIgnoreCase(OrderStatusUpdate.Status.SUBMITTED.toString())) {
                    orderStatusItemLine = CommonConstants.ORDER_STATUS_PROCESSING;
                    orderStatusOtherLines = CommonConstants.ORDER_STATUS_COMPLETED;
                } else {
                    orderStatusItemLine = CommonConstants.ORDER_STATUS_FAILED;
                    orderStatusOtherLines = CommonConstants.ORDER_STATUS_FAILED;
                }

                // Get order from DB
                order = getOrder(Long.valueOf(orderId));
                if(Objects.isNull(order)){
                    throw new B2RException("Order ID not found in DB.");
                }

                // PD update and status update for other lines
                boolean creditLineExists = isCreditLineExists(status, orderStatusItemLine,
                        orderStatusOtherLines, pdAmountInCent, order, ccAmountInCent);

                if (!creditLineExists && ccAmountInCent != 0) {
                    final OrderLine orderLine = CreditTransactionManager.addCreditOrderLine(status, order.getVarId(),
                            order.getProgramId(), order.getOrderId(), order.getOrderLines().size() + 1);
                    // Insert into orderLine
                    b2rOrderManager.insertOrderLine(orderLine);
                    logger.info("PD confirmation: Inserted new credit entry for ");
                }
                insertOrderAttribute(status, orderId);

                // Get transaction details from PPC
                getTransactionDetailsFromPPC(status, orderId);
            } catch (Exception e) {
                logger.error(
                        "Error while updating order line in order status update. Request: {}, order id: {}, exception: {}",
                        new Gson().toJson(status), orderId, e);
                throw e;
            }
            // send order confirmation email after PPC order status update
            sendOrderConfirmation(status, orderId, order);
        }
    }

    private boolean isCreditLineExists(OrderStatusUpdate status, int orderStatusItemLine, int orderStatusOtherLines, int pdAmountInCent, Order order, int ccAmountInCent) throws Exception {
        boolean creditLineExists = false;
        if (CollectionUtils.isNotEmpty(order.getOrderLines())) {
            for (Object orderLineObj : order.getOrderLines()) {
                OrderLine orderLine = (OrderLine) orderLineObj;
                if (orderLine.getSupplierId()
                        .equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S)) {
                    populateOrderLineInfo(status, orderStatusOtherLines, pdAmountInCent, orderLine);
                    orderLine.setAttr2("partnerReferenceId :" + status.getPartnerReferenceId());
                    b2rOrderManager.updateOrderLine(orderLine);
                } else if (orderLine.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                    populateOrderLineInfo(status, orderStatusOtherLines, ccAmountInCent, orderLine);
                    orderLine.setAttr1("partnerId : " + status.getPartnerId());
                    orderLine.setAttr2("partnerReferenceId :" + status.getPartnerReferenceId());
                    b2rOrderManager.updateOrderLine(orderLine);
                    creditLineExists = true;
                } else if (orderLine.getSupplierId()
                        .equalsIgnoreCase(CommonConstants.APPLE_SUPPLIER_ID_STRING)) {
                    orderLine.setOrderStatus(orderStatusItemLine);
                    b2rOrderManager.updateOrderLine(orderLine);
                } else {
                    orderLine.setOrderStatus(orderStatusOtherLines);
                    b2rOrderManager.updateOrderLine(orderLine);
                    redeemDiscountCode(orderStatusOtherLines, order, orderLine);
                }
            }
        }
        return creditLineExists;
    }

    private void populateOrderLineInfo(OrderStatusUpdate status, int orderStatusOtherLines, int pdAmountInCent, OrderLine orderLine) {
        orderLine.setOrderStatus(orderStatusOtherLines);
        orderLine.setSupplierItemPrice(-pdAmountInCent);
        orderLine.setVarOrderLinePrice(-pdAmountInCent);
        orderLine.setItemPoints(-new Double(pdAmountInCent));
        orderLine.setOrderLinePoints(-pdAmountInCent);
        orderLine.setOrderStatus(orderStatusOtherLines);
        orderLine.setSupplierOrderId(status.getPartnerReferenceId());
    }

    private void sendOrderConfirmation(OrderStatusUpdate status, String orderId, Order order) {
        if (order != null && OrderStatusUpdate.Status.SUBMITTED.toString().equalsIgnoreCase(status.getStatus())) {
            try {
                // send order confirmation email after PPC order status update
                Program program = programService.getProgram(order.getVarId(), order.getProgramId(),
                        LocaleUtils.toLocale(order.getLanguageCode() + "_" + order.getCountryCode()));
                order = getOrder(Long.valueOf(orderId));
                if(Objects.isNull(order)){
                    throw new B2RException("Order ID not found in DB.");
                }
                notificationService.sendAsyncNotification(order, program);
            } catch (Exception e) {
                logger.error(
                        "Error while sending order confirmation email while order status update for order id : {}. " +
                                "Exception: {}",
                        orderId, e);
            }
        }
    }

    private void insertOrderAttribute(OrderStatusUpdate status, String orderId) {
        if (status.getTaxes() != null) {
            insertOrderAttribute(CommonConstants.PPC_TAX, status.getTaxes(), orderId);
        }
        if (status.getFees() != null) {
            insertOrderAttribute(CommonConstants.PPC_FEE, status.getFees(), orderId);
        }
        if (status.getFees() != null) {
            insertOrderAttribute(CommonConstants.PPC_CURRENCY_TYPE, status.getCurrencyType(), orderId);
        }
    }

    private void getTransactionDetailsFromPPC(OrderStatusUpdate status, String orderId) {
        try {
            TransactionInfoResponse response =
                    payrollService.getTransactionInfo(status.getPartnerReferenceId());
            if (response != null &&
                    response.getStatus().getCode().equalsIgnoreCase(String.valueOf(HttpStatus.OK.value())) &&
                    response.getDeduction() != null) {
                insertOrderAttribute(CommonConstants.PPC_PAY_PER_PERIOD,
                        response.getDeduction().getPricePerPayment(), orderId);
                insertOrderAttribute(CommonConstants.PPC_PAY_PERIODS,
                        response.getDeduction().getNumberOfPayment(), orderId);
                insertOrderAttribute(CommonConstants.PPC_PAY_DURATION, response.getDeduction().getTerm(),
                        orderId);
            }
        } catch (final Exception e) {
            logger.error("Error retrieving and updating payperperiod from PPC for order id # {}", orderId);
        }
    }

    private void redeemDiscountCode(int orderStatusOtherLines, Order order, OrderLine orderLine) throws Exception {
        if (orderStatusOtherLines == CommonConstants.ORDER_STATUS_COMPLETED &&
                orderLine.getSupplierId()
                        .equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S)) {
            List<DiscountCode> discountCodes = new ArrayList<>();
            DiscountCode discountCode = new DiscountCode();
            User tmpUser = getUserObjectForDiscountCodeRedemption(order);
            discountCode.setDiscountCode(orderLine.getItemId());
            discountCodes.add(discountCode);

            //Redeem discount code
            CouponDetails couponDetails =
                    discountCodeTransactionManager.redeemDiscountCode(discountCode, tmpUser);
            if (couponDetails == null || !couponDetails.isValid()) {
                User user = new User();
                user.setUserId(order.getUserId());
                user.setProgramId(order.getProgramId());
                user.setVarId(order.getVarId());
                // Mark order as failed since discount redemption failed
                b2rOrderManager.rollBackOrder(order, user);
                logger.error("Error redeeming coupon code {} for order id {}",
                        discountCode.getDiscountCode(), order.getOrderId());
                throw new Exception("Discount code redemption exception");
            }
        }
    }

    private static int getAmountInCents(final Double amount) {
        return BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(100)).round(MathContext.UNLIMITED).intValue();
    }

    private OrderAttributeValue insertOrderAttribute(final String key, final String value, final String orderId) {
        return createAndPersistOrderAttribute(key, value, Long.valueOf(orderId));
    }

    public boolean isStartedPayrollDeduction(final Order order) {
        Optional<OrderLine> orderLineOptional = order.getOrderLines().stream()
                .filter(orderLine -> orderLine instanceof OrderLine &&
                        PAYROLL_DEDUCTION.name().equalsIgnoreCase(((OrderLine) orderLine).getCategory()) &&
                        ((OrderLine) orderLine).getOrderStatus() == CommonConstants.ORDER_STATUS_STARTED).findFirst();
        return orderLineOptional.isPresent();
    }

    public OrderStatus updateOrderStatusFromDB(final OrderStatus orderStatus)
            throws ServiceException {

        Order order = getOrder(orderStatus.getB2sOrderId());

        if (Objects.isNull(order)) {
            logger.error("Order ID {} not found in DB.", orderStatus.getB2sOrderId());
            return null;
        }

        if (isStartedPayrollDeduction(order)) {
            return orderStatus;
        }

        // Get payroll deduction orderLine
        Optional<OrderLine> orderLine = order.getOrderLines().stream()
                .filter(
                        o -> ((OrderLine) o).getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S))
                .findAny();

        if (orderLine.isPresent()) {
            OrderLine orderLine1 = orderLine.get();
            PayrollDeduction payrollDeduction = new PayrollDeduction();

            orderStatus.setOrderCompleted(
                    orderLine1.getOrderStatus().equals(CommonConstants.ORDER_STATUS_FAILED) ? false : true);
            if (orderLine.get().getVarOrderLinePrice() != null) {
                payrollDeduction.setPdAmount(new BigDecimal(orderLine.get().getVarOrderLinePrice()).negate()
                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).doubleValue());
            }
            payrollDeduction.setPayPeriods(order.getOrderAttributeValues().stream()
                    .filter(attributeValue -> attributeValue.getName().equalsIgnoreCase(CommonConstants.PAY_PERIODS))
                    .mapToInt(value -> (Integer.parseInt(value.getValue())))
                    .findAny().getAsInt());

            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String payPerPeriod = decimalFormat.format(
                    (new BigDecimal(orderLine1.getItemPoints()).negate().divide(new BigDecimal("100")))
                            .divide(new BigDecimal(payrollDeduction.getPayPeriods()), 2, RoundingMode.DOWN));
            payrollDeduction.setPayPerPeriod(Double.valueOf(payPerPeriod));


            // Set pay per period and pay period retrived from PPC.
            setPayPerPeriodFromPPC(order, payrollDeduction);

            Optional<OrderLine> creditLine = order.getOrderLines().stream()
                    .filter(o -> ((OrderLine) o).getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_CREDIT_S))
                    .findAny();
            if (creditLine.isPresent()) {
                payrollDeduction.setCardPayment(Double.valueOf(decimalFormat.format(
                        new BigDecimal(creditLine.get().getOrderLinePoints()).negate()
                                .divide(new BigDecimal("100"), 2, RoundingMode.DOWN))));
            }

            orderStatus.setPayrollDeduction(payrollDeduction);

        }
        return orderStatus;
    }

    private void setPayPerPeriodFromPPC(Order order, PayrollDeduction payrollDeduction) {
        // Set pay per period and pay period retrived from PPC.
        // If we dont have PPC values then we will have above information which we caluclated manually
        order.getOrderAttributeValues().stream().forEach(attributeValue1 -> {
            if (attributeValue1.getName().equalsIgnoreCase(CommonConstants.PPC_PAY_PER_PERIOD)
                    && StringUtils.isNotBlank(attributeValue1.getValue())) {
                payrollDeduction.setPayPerPeriod(new BigDecimal(attributeValue1.getValue()).setScale(2)
                        .doubleValue());
            }
            if (attributeValue1.getName().equalsIgnoreCase(CommonConstants.PPC_PAY_PERIODS)
                    && StringUtils.isNotBlank(attributeValue1.getValue())) {
                payrollDeduction.setPayPeriods(Integer.valueOf(attributeValue1.getValue()));
            }
        });
    }

    public boolean isOrderTypePayrollDeduction(final Order order) {
        Optional<OrderLine> orderLineOptional = order.getOrderLines().stream()
                .filter(
                        o -> ((OrderLine) o).getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S))
                .findAny();

        return orderLineOptional.isPresent();

    }

    public Boolean enableOrderProcessing(final Program program){
        return !(Boolean)program.getConfig().getOrDefault(CommonConstants.ORDER_CONFIRMATION_FLAG,Boolean.FALSE);
    }

    private PricingLogEntity mapToPricingLogEntity(final Order order) {
        logger.debug("Creating PricingLogEntity model");

        final CartPricingRequestDTO cartPricingRequestDTO =
                (CartPricingRequestDTO) httpSession.getAttribute(CommonConstants.CART_REQUEST_SESSION_OBJECT);
        final CartPricingResponseDTO cartPricingResponseDTO =
                (CartPricingResponseDTO) httpSession.getAttribute(CommonConstants.CART_RESPONSE_SESSION_OBJECT);
        final PricingLogEntity pricingLogEntity = new PricingLogEntity();
        pricingLogEntity.setVarId(order.getVarId());
        pricingLogEntity.setProgramId(order.getProgramId());
        pricingLogEntity.setOrderId(order.getOrderId());
        pricingLogEntity.setUserId(order.getUserId());
        pricingLogEntity
                .setCartRequest(ToStringConvertor.createFor(cartPricingRequestDTO));
        pricingLogEntity
                .setCartResponse(ToStringConvertor.createFor(cartPricingResponseDTO));
        pricingLogEntity.setCreatedDate(new Date());

        return pricingLogEntity;
    }

    @Async
    public void persistPricingLog(final Order order){
        try {
            final PricingLogEntity pricingLogEntity = mapToPricingLogEntity(order);
            pricingLogDao.insert(pricingLogEntity);
            logger.debug("Persisted pricing_log entry for orderId {}",order.getOrderId());
        } catch (final RuntimeException e) {
            logger.error("B2R failed to persist orderId {} with exception: ", order.getOrderId(), e);
        }
    }

    //Allow Anonymous User to purchase only if the VarOrderPriceTotal is Zero or lesser
    private boolean doesAnonymousUserHasPurchasePower(final Order order) {
        if(Objects.nonNull(order.getVarOrderPriceTotalInMoney()) && order.getVarOrderPriceTotalInMoney().isNegativeOrZero()){
            return true;
        }
        logger.warn("Anonymous User doesn't have Purchasing Power");
        return false;
    }

    /**
     * This function was created to fix anonymous user issue who is redeeming a discount code.
     * When a user redeems a coupon a row is inserted with 'CLAIMED' status in table coupon_user.
     * Instead of inserting Anonymous+SessionId in the discount tables, we use the userId
     * from the order (which is the email address) to fix the issue.
     * When admin cancels the order, the discount coupon is made 'AVAILABLE' again with userId set from the order.
     * See D-13843
     * @param order Order
     * @return User
     */
    private User getUserObjectForDiscountCodeRedemption(Order order) {
        User tmpUser = new User();
        tmpUser.setVarId(order.getVarId());
        tmpUser.setProgramId(order.getProgramId());
        tmpUser.setUserId(order.getUserId());
        tmpUser.setIPAddress(order.getIpAddress());
        return tmpUser;
    }
}
