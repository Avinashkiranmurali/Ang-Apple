package com.b2s.apple.services;

import com.b2s.apple.entity.PaymentEntity;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.db.model.BundledPricingOption;
import com.b2s.rewards.apple.dao.OrderHistoryDao;
import com.b2s.rewards.apple.dao.OrderLineStatusHistoryDao;
import com.b2s.rewards.apple.dao.PaymentDao;
import com.b2s.rewards.apple.integration.model.DelayedShippingInfo;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.merchandise.action.CartCalculationUtil;
import com.b2s.shop.common.OrderHistory;
import com.b2s.shop.common.RefundSummary;
import com.b2s.shop.common.ReturnLineItem;
import com.b2s.shop.common.User;
import com.b2s.spark.api.apple.to.impl.Address;
import com.b2s.spark.api.apple.to.impl.ContactInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by vmurugesan on 10/3/2016.
 */
@Service
public class OrderHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(OrderHistoryService.class);

    @Autowired
    private OrderHistoryDao orderHistoryDao;

    @Autowired
    private OrderLineStatusHistoryDao orderLineStatusHistoryDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private VarProgramMessageService varProgramMessageService;

    public static final String OS_ORDER_PLACED = "orderPlaced";
    public static final String OS_PROCESSING = "processing";
    public static final String OS_AWAITING_SHIPMENT = "awaitingShipment";
    public static final String OS_SHIPPED = "shipped";
    public static final String OS_CANCELLED = "cancelled";
    public static final String OS_RETURNED = "returned";

    public List<OrderHistory> getOrderHistory(final User user, final Program program, final Integer days)
            throws ServiceException {
        List<OrderHistory> orderHistoryList = new ArrayList<>();
        boolean unMappedStatus;
        try {
            final boolean allPrograms = (Boolean) program.getConfig()
                    .getOrDefault(CommonConstants.ORDER_HISTORY_ALL_PROGRAMS_PROGRAM_CONFIG_KEY, Boolean.FALSE);
            List<Orders> orderList = orderHistoryDao.getOrderHistory(user, days, allPrograms);
            for (Orders order : orderList) {
                unMappedStatus = false;
                OrderHistory orderHistory = new OrderHistory();
                List<OrderHistory.OrderLineInfo> orderLines = new ArrayList<>();

                for (OrderLineItem lineItem : order.getLineItemList()) {
                    unMappedStatus = updateOrderLines(user, program, order, orderHistory, orderLines, lineItem);

                    //Not to display the order, if any line item with Status ID (-2,21,24,25,98,11 or 99)
                    if (unMappedStatus) {
                        break;
                    }
                }
                final Locale locale = new Locale(order.getLanguageCode(), order.getCountryCode());
                if (!unMappedStatus) {
                    populateOrderTotals(orderLines, locale, orderHistory, order, program.getBundledPricingOption());
                    fetchOrderHistoryFromOrders(program, orderHistoryList, order, orderHistory);
                }
                logger.debug("Loading Order... {} ", order.getOrderId());
            }
        } catch (Exception ex) {
            logger.error("getOrder: Failed to load the Orders for the user: {} ", user.getUserId(), ex);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
        return orderHistoryList;
    }

    private boolean updateOrderLines(User user, Program program, Orders order, OrderHistory orderHistory,
                                     List<OrderHistory.OrderLineInfo> orderLines, OrderLineItem lineItem) {
        try {
            OrderHistory.OrderLineInfo orderLineInfo = orderHistory.new OrderLineInfo();
            orderLineInfo.setIsGift(isGiftItem(lineItem));
            populateFirstItemImageURL(orderHistory, lineItem);

            //Not to display the order, if any line item with Status ID (-2,21,24,25,98,11 or 99)
            if (!isUnMappedStatus(lineItem.getStatus().getStatusId())) {
                getOrderHistoryFromOrderLineItem(orderHistory, lineItem);

                if (!CommonConstants.SUPPLIER_TYPE_CREDIT_S.equals(lineItem.getSupplierId()) &&
                        !CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S.equals(lineItem.getSupplierId())) {
                    setLineItemPrice(orderLineInfo, lineItem, program, user.getLocale(),
                            order.getCurrencyCode());
                    orderLines.add(orderLineInfo);
                }
            } else {
                return true;
            }
        } catch (RuntimeException ex) {
            logger.warn("getOrder: Failed to load the Order {} for the user: {}", order.getOrderId(),
                    user.getUserId(), ex);
        }
        return false;
    }

    /**
     * Not to display the order, if any line item with Status ID (-2,21,24,25,98,11 or 99)
     *
     * @param statusId - Line item status ID
     * @return true/false
     */
    private boolean isUnMappedStatus(final Integer statusId) {
        return statusId == CommonConstants.ORDER_STATUS_STARTED ||
                statusId == CommonConstants.ORDER_LINE_ADJUSTMENT ||
                statusId == CommonConstants.ORDER_STATUS_ORDERED_INSTORE_PICKUP_AVAILABLE ||
                statusId == CommonConstants.ORDER_STATUS_COMPLETED_RETURN_STR_CRDT ||
                statusId == CommonConstants.ORDER_STATUS_FRAUD ||
                statusId == CommonConstants.ORDER_STATUS_DEMO ||
                statusId == CommonConstants.ORDER_STATUS_FAILED;
    }

    private boolean isGiftItem(final OrderLineItem lineItem) {
        final String giftAttribute = lineItem.getAttr1();
        return (StringUtils.isNotBlank(giftAttribute) &&
                (giftAttribute.equalsIgnoreCase(CommonConstants.GIFT_ITEM) ||
                        giftAttribute.equalsIgnoreCase(CommonConstants.DISCOUNTED_GIFT_PERCENTAGE) ||
                        giftAttribute.equalsIgnoreCase(CommonConstants.DISCOUNTED_GIFT_POINTS)));
    }

    private void populateFirstItemImageURL(final OrderHistory orderHistory, final OrderLineItem lineItem) {
        if (StringUtils.isBlank(orderHistory.getImageURL())) {
            orderHistory.setImageURL(lineItem.getImageURL());
        }
    }

    private void fetchOrderHistoryFromOrders(Program program, List<OrderHistory> orderHistoryList, Orders order, OrderHistory orderHistory) {
        orderHistory.setOrderId(order.getOrderId());
        orderHistory.setShowVarOrderId((Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_VAR_ORDER_ID, Boolean.FALSE));
        orderHistory.setDisplayOrderId(String.valueOf(order.getOrderId()));
        if(orderHistory.isShowVarOrderId()) {
            orderHistory.setDisplayOrderId(order.getVarOrderId());
        }
        Optional<OrderAttributeValue> orderAttibute =order.getOrderAttributeValueList().stream().filter
                (orderAttributeValue -> orderAttributeValue
                        .getName()
                        .equalsIgnoreCase(CommonConstants.TIME_ZONE_ID)).findFirst();

        if (orderAttibute.isPresent()) {
            orderHistory.setOrderDate(CartCalculationUtil.convertTimeZone(order.getOrderDate(), TimeZone.getTimeZone(orderAttibute.get().getValue())));
        } else {
            orderHistory.setOrderDate(order.getOrderDate());
        }

        final PaymentInfo paymentInfo = orderHistory.getPaymentInfo();
        paymentInfo.setAwardsUsed(order.getOrderTotalInPoints() - order.getOrderTotalCashBuyInPoints());
        paymentInfo.setAwardsPurchased(order.getOrderTotalCashBuyInPoints());
        orderHistory
                .setPurchasedPriceCurrency(getPriceinCurrency(order.getLanguageCode(), order.getCountryCode(),
                    paymentInfo.getAwardsPurchasedPrice()));
        orderHistory
            .setRefundTotal(new Price(order.getRefundedPrice(), order.getCurrencyCode(), order.getRefundedPoint()));
        orderHistoryList.add(orderHistory);
    }

    private void getOrderHistoryFromOrderLineItem(OrderHistory orderHistory, OrderLineItem lineItem) {
        if (lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.APPLE_SUPPLIER_ID_STRING) ||
                lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_SERVICE_PLAN_S) ||
                lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_GIFTCARD_S)) {
            orderHistory.setShipments(orderHistory.getShipments() + 1);
            orderHistory.setItems(orderHistory.getItems() + lineItem.getQuantity());
        } else if(lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_CREDIT_S)){
            double cashPaid = lineItem.getSupplierItemPrice()/100d;
            cashPaid+=Math.abs(lineItem.getB2sItemProfitPrice()/100d);
            cashPaid+=Math.abs(lineItem.getVarItemProfitPrice()/100d);
            orderHistory.getPaymentInfo().setAwardsPurchasedPrice(cashPaid);
        }
    }

    public OrderHistory getOrderHistoryDetails(final User user, final String orderId, final Program program)
        throws ServiceException {
        final boolean allPrograms = (Boolean) program.getConfig()
            .getOrDefault(CommonConstants.ORDER_HISTORY_ALL_PROGRAMS_PROGRAM_CONFIG_KEY, Boolean.FALSE);
        final boolean showVarOrderId =
            (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_VAR_ORDER_ID, Boolean.FALSE);

        final Orders order;
        if (showVarOrderId) {
            order = orderHistoryDao.getOrderHistoryDetails(user, orderId, allPrograms);
        } else {
            order = orderHistoryDao.getOrderHistoryDetails(user, Integer.parseInt(orderId), allPrograms);
        }
        return getOrderHistory(user, showVarOrderId, order, program);
    }

    public boolean updateUserIdIfOrderExist(final String orderId, final String email, final Program program, final Locale locale, User user)
            throws RuntimeException {
        final Orders orderDetails = orderHistoryDao.getOrderHistoryDetails(orderId, email, program.getVarId(), program.getProgramId(), locale);
        if (Objects.isNull(orderDetails)) {
            logger.error("getOrderHistoryDetails: B2S Order ID {} not found / does not belongs to email {}",orderId, email);
            return false;
        }
        user.setUserId(orderDetails.getUserId());

        return true;
    }

    public OrderHistory getOrderHistory(User user, boolean showVarOrderId, Orders order, final Program program) throws ServiceException {
        final boolean showGST = (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_GST, Boolean.FALSE);
        final boolean showEarnPoints = (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_EARN_POINTS, Boolean.FALSE);

        OrderHistory orderHistory = null;
        try {
            if (order != null) {

                orderHistory = new OrderHistory();
                final Address address = new Address();
                final ContactInfo contactInfo = new ContactInfo();

                //Address info
                getAddressInfo(order, address);

                // Contact info
                getContactInfo(order, contactInfo);

                //Order History Info
                getOrderHistoryInfo(showVarOrderId, order, program, showGST, showEarnPoints,
                        orderHistory, address, contactInfo, getOrderHistoryDateFormat(user.getVarId()), user.getLocale());

            }
        }catch(Exception ex){
            logger.error("getOrder: Failed to load Order for OrderId: {}..Exception: {} ", order.getOrderId() ,ex);
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION, ex);
        }
        return orderHistory;
    }

    private void getOrderHistoryInfo(boolean showVarOrderId, Orders order, Program program, boolean showGST,
                                     boolean showEarnPoints, OrderHistory orderHistory,
                                     Address address, ContactInfo contactInfo,
                                     String dateFormat, Locale locale) {
        List<OrderHistory.OrderLineInfo> lineItems = new ArrayList<>();
        List<ReturnLineItem> returnLineItemsList = new ArrayList<>();

        Properties properties = varProgramMessageService.getMessages(Optional.of(program.getVarId()),
                Optional.of(program.getProgramId()), locale.toString());

        orderHistory.setOrderId(order.getOrderId());
        orderHistory.setShowVarOrderId(showVarOrderId);
        orderHistory.setDisplayOrderId(String.valueOf(order.getOrderId()));
        if(orderHistory.isShowVarOrderId()) {
            orderHistory.setDisplayOrderId(order.getVarOrderId());
        }
        orderHistory.setDeliveryAddress(address);

        setBillToInformation(orderHistory);

        orderHistory.setContactInfo(contactInfo);
        for (OrderLineItem lineItem : order.getLineItemList()) {
            if (lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.APPLE_SUPPLIER_ID_STRING) ||
                    lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_SERVICE_PLAN_S) ||
                    lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_GIFTCARD_S)) {

                populateFirstItemImageURL(orderHistory, lineItem);
                OrderHistory.OrderLineInfo orderLineInfo =
                        getBasicOrderInfo(order, program, orderHistory, lineItem);

                orderLineInfo.setIsGift(isGiftItem(lineItem));

                //Sets Promoted, UnPromoted, Unit & Line Item Price
                setLineItemPrice(orderLineInfo, lineItem, program, locale, order.getCurrencyCode());

                getEngraveInfo(lineItem, orderLineInfo);
                getShipmentInfo(lineItem, orderLineInfo, properties, locale);
                getDelayedShipmentInfo(order, dateFormat, lineItem, orderLineInfo);

                //set refund if order status is Cancelled / invalid address / item not available / returned
                setRefund(order, lineItem, orderLineInfo, locale, returnLineItemsList );

                lineItems.add(orderLineInfo);

            } else if(lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_CREDIT_S)){
                double cashPaid = lineItem.getSupplierItemPrice()/100d;
                cashPaid+=Math.abs(lineItem.getB2sItemProfitPrice()/100d);
                cashPaid+=Math.abs(lineItem.getVarItemProfitPrice()/100d);

                orderHistory.getPaymentInfo().setAwardsPurchasedPrice(cashPaid);
            }
        }
        populateOrderTotals(lineItems, locale, orderHistory, order, program.getBundledPricingOption());
        getAdditionalOrderHistoryInfo(order, showGST, showEarnPoints, orderHistory, lineItems);
        processOrderAttributeValues(order, orderHistory);
        populateRefundSummary(order, orderHistory, returnLineItemsList, program.getBundledPricingOption());
    }

    /**
     * Populates Order Sub-Total and Total Price
     *
     * @param lineItems
     * @param locale
     * @param orderHistory
     * @param order
     * @param bundledPricingOption
     */
    private void populateOrderTotals(List<OrderHistory.OrderLineInfo> lineItems, Locale locale,
        OrderHistory orderHistory, Orders order, BundledPricingOption bundledPricingOption) {
        final BigDecimal subTotal =
            lineItems.stream().map(lineItem -> BigDecimal.valueOf(lineItem.getPrice().getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        orderHistory
            .setOrderSubTotal(new Price(Money.of(CurrencyUnit.of(locale), subTotal), order.getOrderSubTotalInPoints()));

        orderHistory.setOrderTotal(
            new Price(order.getOrderTotalInMoney(subTotal, bundledPricingOption), order.getOrderTotalInPoints()));
    }

    private void populateRefundSummary(final Orders order, final OrderHistory orderHistory,
        final List<ReturnLineItem> returnLineItemList, final BundledPricingOption bundledPricingOption) {
        if (CollectionUtils.isNotEmpty(returnLineItemList)) {
            RefundSummary refundSummary = new RefundSummary();
            refundSummary.setLineItems(returnLineItemList);
            refundSummary.setRefunds(new Price(order.getRefundedPrice(), order.getCurrencyCode(),
                order.getRefundedPoint()));
            calculateSubTotalAndTaxFees(refundSummary, order.getCurrencyCode());
            calculateTotal(refundSummary, order.getCurrencyCode(), bundledPricingOption);
            orderHistory.setRefundSummary(refundSummary);
        }
    }

    /*
     * Intention of this method is to calculate the subtotal & taxes of refund by adding all the amounts & points in
     *  returnLineItem
     * */
    private void calculateSubTotalAndTaxFees(final RefundSummary refundSummary, final String currencyCode) {

        if (CollectionUtils.isNotEmpty(refundSummary.getLineItems())) {
            Double totalAmount = 0.0;
            int totalPoints = 0;

            double taxAndFeeAmount = 0.0;
            int taxAndFeePoints = 0;
            for (ReturnLineItem lineItem : refundSummary.getLineItems()) {
                final Price totalPriceAndPoints = lineItem.getItemPrice();
                totalAmount += totalPriceAndPoints.getAmount();
                totalPoints += totalPriceAndPoints.getPoints();

                final Price taxes = lineItem.getTaxPrice();
                final Price fees = lineItem.getFeesPrice();
                taxAndFeeAmount += taxes.getAmount() + fees.getAmount();
                taxAndFeePoints += taxes.getPoints() + fees.getPoints();

            }
            refundSummary.setSubTotal(new Price(totalAmount, currencyCode, totalPoints));
            refundSummary.setTaxesAndFees(new Price(taxAndFeeAmount, currencyCode, taxAndFeePoints));
        }
    }

    private void calculateTotal(final RefundSummary refundSummary, final String currencyCode,
        final BundledPricingOption bundledPricingOption) {
        final Price subTotal = refundSummary.getSubTotal();
        if (BundledPricingOption.BUNDLED != bundledPricingOption) {
            final Price taxesAndFees = refundSummary.getTaxesAndFees();
            if (Objects.nonNull(subTotal) && Objects.nonNull(taxesAndFees)) {
                final double refundLineAmount =
                    BigDecimal.valueOf(subTotal.getAmount()).add(BigDecimal.valueOf(taxesAndFees.getAmount()))
                        .doubleValue();
                refundSummary.setTotal(
                    new Price(refundLineAmount, currencyCode, subTotal.getPoints() + taxesAndFees.getPoints()));
            }
        } else {
            refundSummary.setTotal(subTotal);
        }
    }

    private void setBillToInformation(final OrderHistory order){
        final List<PaymentEntity> paymentEntities = paymentDao.getSaleDetails(Long.valueOf(order.getOrderId()));
        if(CollectionUtils.isNotEmpty(paymentEntities)){
            final BillTo billTo = new BillTo();
            final PaymentEntity payment = paymentEntities.get(0);
            billTo.setFirstName(payment.getFirstName());
            billTo.setLastName(payment.getLastName());
            billTo.setAddressLine(payment.getAddress1());
            billTo.setCity(payment.getCity());
            billTo.setState(payment.getState());
            billTo.setCountry(payment.getCountry());
            billTo.setZip(payment.getZip());
            order.setBillTo(billTo);
        }
    }

    private void getAdditionalOrderHistoryInfo(Orders order, boolean showGST, boolean showEarnPoints,
        OrderHistory orderHistory, List<OrderHistory.OrderLineInfo> lineItems) {
        final PaymentInfo paymentInfo = orderHistory.getPaymentInfo();
        paymentInfo.setAwardsUsed(order.getOrderTotalInPoints() - order.getOrderTotalCashBuyInPoints());
        paymentInfo.setAwardsPurchased(order.getOrderTotalCashBuyInPoints());

        orderHistory.setTotalTax(new Price(order.getOrderTotalTaxesInMoney(), order.getOrderTotalTaxesInPoints()));

        processGST(order, showGST, orderHistory);
        getEarnedPoints(order, showEarnPoints, orderHistory);

        orderHistory.setShipments(lineItems.size());
        orderHistory.setLineItems(lineItems);
        orderHistory.setRemainingBalance(order.getUserPoints() - paymentInfo.getAwardsUsed());
        orderHistory
                .setPurchasedPriceCurrency(getPriceinCurrency(order.getLanguageCode(), order.getCountryCode(),
                        order.getRefundedPrice()));
        orderHistory.setRefundTotal(new Price(order.getRefundedPrice(),order.getCurrencyCode(),order
            .getRefundedPoint
                ()));
        orderHistory.setTotalFee(new Price(order.getOrderTotalFeesInMoney(),order.getOrderTotalFeesInPoints()));
        orderHistory.setTotalDiscount(new Price(order.getTotalDiscountInMoney(), order.getTotalDiscountInPoints()));
        orderHistory.setShippingCost(new Price(order.getOrderTotalShippingInMoney(), order
                .getOrderTotalShippingInPoints()));
    }

    private OrderLineProgress getProgress(final Long orderId, final Integer lineNum, final OrderLineStatus status) {
        final OrderLineProgress orderStatus = new OrderLineProgress();
        final int statusId = status.getStatusId();
        final OrderStatus orderStatusByStatusId = getOrderStatusByStatusId(statusId);
        orderStatus.setStatus(orderStatusByStatusId.getStatusName());
        orderStatus.setProgressValue(orderStatusByStatusId.getProgressValue());
        orderStatus.setProgressBarText(orderStatusByStatusId.getStatusSteps());

        if (OrderStatus.CANCELLED.equals(orderStatusByStatusId) || OrderStatus.RETURNED.equals(orderStatusByStatusId)) {
            //Set Modified Date only for Cancelled/Returned Status
            orderLineStatusHistoryDao
                .loadStatusHistoryLatestFirstByOrderIdLineNumStatusId(orderId, lineNum, statusId).stream()
                .findFirst()
                .ifPresent(history -> orderStatus.setModifiedDate(history.getId().getModifiedDate()));
            logger.info("Cancelled/Returned Order: {}, LineNum: {}, StatusId: {}:: {}", orderId, lineNum, statusId,
                orderStatus);
        }
        return orderStatus;
    }

    /**
     * Giving first precedence to displayPrice.amount in order_line_attribute, otherwise calculating the price
     *
     * @param orderLineInfo
     * @param orderLineItem
     * @param program
     * @param locale
     * @param currencyCode
     */
    private void setLineItemPrice(final OrderHistory.OrderLineInfo orderLineInfo, final OrderLineItem orderLineItem,
        final Program program, final Locale locale, final String currencyCode) {
        BigDecimal priceAmount;
        final Optional<OrderLineItemAttribute> displayPriceAmountOpt = orderLineItem.getOrderLineAttributes()
            .stream()
            .filter(orderLineItemAttribute -> CommonConstants.ORDER_ATTR_KEY_DISPLAY_PRICE_AMOUNT
                .equals(orderLineItemAttribute.getName()))
            .findFirst();

        priceAmount = displayPriceAmountOpt
                .map(orderLineItemAttribute -> BigDecimal.valueOf(Double.parseDouble(orderLineItemAttribute.getValue())))
                .orElseGet(() -> getCalculatedItemPrice(orderLineItem, program));

        Integer pricePoints;
        if (BundledPricingOption.BUNDLED == program.getBundledPricingOption()) {
            pricePoints = orderLineItem.getOrderLinePoints();
        } else {
            pricePoints = orderLineItem.getItemPoints().intValue();
        }

        orderLineInfo.setUnitPrice(new Price(Money.of(CurrencyUnit.of(locale), priceAmount), pricePoints));

        priceAmount = priceAmount.multiply(BigDecimal.valueOf(orderLineItem.getQuantity()));
        pricePoints =
            BigDecimal.valueOf(pricePoints).multiply(BigDecimal.valueOf(orderLineItem.getQuantity())).intValue();
        orderLineInfo.setPrice(new Price(Money.of(CurrencyUnit.of(locale), priceAmount), pricePoints));

        // Add unpromoted price, if exists
        getUnpromotedPrice(currencyCode, orderLineItem, orderLineInfo);
    }

    /**
     * Set Item Price using below logic
     * For Discounted - Bundled use case - discounted_var_order_line_price/100
     * For Discounted - UnBundled use case - (discounted_var_order_line_price - discounted_taxes - discounted_fees)/100
     *
     * @param orderLineItem
     * @param program
     * @return
     */
    private BigDecimal getCalculatedItemPrice(final OrderLineItem orderLineItem, final Program program) {
        BigDecimal priceAmount;
        //Applicable only when promotion is applied to a line item
        if (Objects.nonNull(orderLineItem.getDiscountedVarOrderLinePrice())) {
            priceAmount = BigDecimal.valueOf(orderLineItem.getDiscountedVarOrderLinePrice());
            if (BundledPricingOption.BUNDLED != program.getBundledPricingOption()) {
                priceAmount = priceAmount.subtract(BigDecimal.valueOf(orderLineItem.getDiscountedTaxes()))
                    .subtract(BigDecimal.valueOf(orderLineItem.getDiscountedFees()));
            }
            priceAmount = priceAmount
                .divide(BigDecimal.valueOf(CommonConstants.CENTS_TO_DOLLARS_DIVISOR), 2, RoundingMode.HALF_UP);
        } else {
            priceAmount = getUndiscountedItemPrice(orderLineItem, program);
        }
        return priceAmount;
    }

    /**
     * Set Undiscounted Item Price using below logic
     * For UnBundled use case
     * --> Giving first precedence to retailUnitBasePrice in order_line_attribute, otherwise supplier_item_price/100
     *
     * For Bundled use case - var_order_line_price/100
     *
     * @param orderLineItem
     * @param program
     * @return
     */
    private BigDecimal getUndiscountedItemPrice(final OrderLineItem orderLineItem, final Program program) {
        final BigDecimal priceAmount;
        if (BundledPricingOption.BUNDLED != program.getBundledPricingOption()) {
            final Optional<OrderLineItemAttribute> retailUnitBasePriceOpt =
                orderLineItem.getOrderLineAttributes()
                    .stream()
                    .filter(orderLineItemAttribute -> CommonConstants.RETAIL_UNIT_BASE_PRICE
                        .equals(orderLineItemAttribute.getName()))
                    .findFirst();

            if (retailUnitBasePriceOpt.isPresent()) {
                priceAmount = BigDecimal.valueOf(Double.valueOf(retailUnitBasePriceOpt.get().getValue()));
            } else {
                priceAmount = BigDecimal.valueOf(orderLineItem.getSupplierItemPrice())
                    .divide(BigDecimal.valueOf(CommonConstants.CENTS_TO_DOLLARS_DIVISOR), 2, RoundingMode.HALF_UP);
            }
        } else {
            priceAmount = BigDecimal.valueOf(orderLineItem.getVarOrderLinePrice())
                .divide(BigDecimal.valueOf(CommonConstants.CENTS_TO_DOLLARS_DIVISOR), 2, RoundingMode.HALF_UP);
        }
        return priceAmount;
    }


    private OrderHistory.OrderLineInfo getBasicOrderInfo(Orders order, Program program, OrderHistory orderHistory, OrderLineItem lineItem) {
        final Locale locale =new Locale(order.getLanguageCode(), order.getCountryCode());
        orderHistory.setItems(orderHistory.getItems() + lineItem.getQuantity());
        OrderHistory.OrderLineInfo orderLineInfo = orderHistory.new OrderLineInfo();

        final Long orderId = Long.valueOf(order.getOrderId());
        final Integer lineNum = lineItem.getId().getLineNum();
        orderLineInfo.setOrderLineID(lineNum);
        orderLineInfo.setSku(lineItem.getSku());
        orderLineInfo.setItemName(lineItem.getName());
        orderLineInfo.setItemImageURL(lineItem.getImageURL());
        orderLineInfo.setQuantity(lineItem.getQuantity());
        orderLineInfo.setCurrencyType(order.getCurrencyCode());

        String orderStatus = null;
        if(lineItem.getStatus().getStatusId().equals(CommonConstants.ORDER_STATUS_LOST_STOLEN)){
            orderStatus = getLostStolenPreviousStatus(orderId, lineNum);
        }else{
            orderStatus = lineItem.getStatus().getStatusId().toString();
        }

        orderLineInfo.setOrderLineProgress(getProgress(orderId, lineNum, lineItem.getStatus()));
        //Need below status for Old Flow
        orderLineInfo.setStatus( applicationProperties.getProperty(CommonConstants.UA_STATUS+(orderStatus)));
        orderLineInfo.setShippingMethod(lineItem.getShippingMethod());
        return orderLineInfo;
    }

    private void processOrderAttributeValues(Orders order, OrderHistory orderHistory) {
        List<OrderAttributeValue> orderAttributeValueList = order.getOrderAttributeValueList();
        for(OrderAttributeValue orderAttibute: orderAttributeValueList){
            if(orderAttibute.getName().equalsIgnoreCase(CommonConstants.CREDIT_CARD_LAST_FOUR_DIGIT)){
                orderHistory.getPaymentInfo().setCcLast4(orderAttibute.getValue());
            }
            if(orderAttibute.getName().equalsIgnoreCase(CommonConstants.TIME_ZONE_ID)){
                orderHistory.setOrderDate(CartCalculationUtil.convertTimeZone(order.getOrderDate(), TimeZone.getTimeZone(orderAttibute.getValue())));
            }
        }
        if(orderHistory.getOrderDate()==null) {
            orderHistory.setOrderDate(order.getOrderDate());
        }
    }

    private void getEarnedPoints(Orders order, boolean showEarnPoints, OrderHistory orderHistory) {
        if(showEarnPoints){
            orderHistory.setEarnedPoints(Objects.nonNull(order.getEarnedPoints())?order.getEarnedPoints():0);
        }
    }

    private void processGST(Orders order, boolean showGST, OrderHistory orderHistory) {
        if(showGST){
            orderHistory.setGstAmount(getPriceinCurrency(order.getLanguageCode(), order.getCountryCode(),
                    Objects.nonNull(order.getGstAmount())?order.getGstAmount():0));
        }
    }

    private void getUnpromotedPrice(String currencyCode, OrderLineItem lineItem, OrderHistory.OrderLineInfo orderLineInfo) {
        final Optional<OrderLineItemAttribute> unpromotedPriceAmount = lineItem.getOrderLineAttributes()
                .stream()
                .filter(orderLineItemAttribute -> CommonConstants.ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_AMOUNT.equals(orderLineItemAttribute.getName()))
                .findFirst();
        final Optional<OrderLineItemAttribute> unpromotedPricePoints = lineItem.getOrderLineAttributes()
                .stream()
                .filter(orderLineItemAttribute -> CommonConstants.ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_POINTS.equals(orderLineItemAttribute.getName()))
                .findFirst();
        if(unpromotedPriceAmount.isPresent() && unpromotedPricePoints.isPresent()) {
            final double unitUnpromotedAmount = Double.parseDouble(unpromotedPriceAmount.get().getValue());
            final int unitUnpromotedPoints = Integer.parseInt(unpromotedPricePoints.get().getValue());
            if(unitUnpromotedAmount > orderLineInfo.getUnitPrice().getAmount() && unitUnpromotedPoints > orderLineInfo.getUnitPrice().getPoints()) {
                final double unpromotedAmount = BigDecimal.valueOf(unitUnpromotedAmount).multiply(new BigDecimal(lineItem.getQuantity())).doubleValue();
                final int unpromotedPoints = new BigDecimal(unitUnpromotedPoints).multiply(new BigDecimal(lineItem.getQuantity())).intValue();
                orderLineInfo.setUnpromotedUnitPrice( new Price(unitUnpromotedAmount, currencyCode, unitUnpromotedPoints));
                orderLineInfo.setUnpromotedPrice( new Price(unpromotedAmount, currencyCode, unpromotedPoints));
            }
        }
    }

    private void getDelayedShipmentInfo(Orders order, String dateFormat, OrderLineItem lineItem, OrderHistory.OrderLineInfo orderLineInfo) {
        if (CommonConstants.ORDER_STATUS_BACK_ORDERED == lineItem.getStatus().getStatusId()) {
            final DelayedShippingInfo delayedShippingInfo = buildDelayedShippingInfo(
                    order.getOrderId(), dateFormat, lineItem);
            orderLineInfo.setDelayedShippingInfo(delayedShippingInfo);
        }
    }

    private void getShipmentInfo(OrderLineItem lineItem, OrderHistory.OrderLineInfo orderLineInfo,
                                 Properties properties, Locale locale) {
        final List<OrderLineShipmentNotification> orderLineShipmentNotifications =
                lineItem.getOrderLineShipmentNotifications();
        if (CollectionUtils.isNotEmpty(orderLineShipmentNotifications)) {
            OrderLineShipmentNotification orderLineShipmentNotification = null;
            int orderLineShipmentNotificationSize = orderLineShipmentNotifications.size();
            if (orderLineShipmentNotificationSize == 1) {
                orderLineShipmentNotification = orderLineShipmentNotifications.get(0);
            } else {
                logger.info("Multiple Shipment details persisted for the Order:{}, Line Num:{} -> Count:{}",
                        lineItem.getId().getOrderId(), lineItem.getId().getLineNum(), orderLineShipmentNotificationSize);

                Optional<OrderLineShipmentNotification> orderLineShipmentNotificationOpt =
                        orderLineShipmentNotifications.stream().max(Comparator.comparingInt(
                                OrderLineShipmentNotification::getId));
                if (orderLineShipmentNotificationOpt.isPresent()) {
                    orderLineShipmentNotification = orderLineShipmentNotificationOpt.get();
                }
            }

            if (Objects.nonNull(orderLineShipmentNotification)) {
                OrderHistory.ShipmentDeliveryInfo shipmentDeliveryInfo = new OrderHistory().new ShipmentDeliveryInfo();
                if (Objects.nonNull(orderLineShipmentNotification.getShipmentDate())) {
                    shipmentDeliveryInfo.setShipmentDate(orderLineShipmentNotification.getShipmentDate());
                }

                shipmentDeliveryInfo.setTrackingID(getTrackingLink(orderLineShipmentNotification, properties, locale));

                if (Objects.nonNull(orderLineShipmentNotification.getDeliveryDate())) {
                    shipmentDeliveryInfo.setDeliveryDate(orderLineShipmentNotification.getDeliveryDate());
                }
                shipmentDeliveryInfo.setCarrierName(orderLineShipmentNotification.getShippingCarrier());
                orderLineInfo.setShipmentInfo(shipmentDeliveryInfo);
            }
        }
    }

    public enum OrderStatus {
        ORDER_PLACED(OS_ORDER_PLACED, 1, List.of(OS_ORDER_PLACED, OS_AWAITING_SHIPMENT, OS_SHIPPED)),
        PROCESSING(OS_PROCESSING, 2, List.of(OS_ORDER_PLACED, OS_AWAITING_SHIPMENT, OS_SHIPPED)),
        SHIPPED(OS_SHIPPED, 3, List.of(OS_ORDER_PLACED, OS_AWAITING_SHIPMENT, OS_SHIPPED)),
        CANCELLED(OS_CANCELLED, 0, List.of(OS_CANCELLED)),
        RETURNED(OS_RETURNED, 0, List.of(OS_RETURNED));

        private final String statusName;
        private final int progressValue;
        private final List<String> statusSteps;

        OrderStatus(String statusName, int progressValue, List<String> statusSteps) {
            this.statusName = statusName;
            this.progressValue = progressValue;
            this.statusSteps = statusSteps;
        }

        public String getStatusName() {
            return statusName;
        }

        public int getProgressValue() {
            return progressValue;
        }

        public List<String> getStatusSteps() {
            return statusSteps;
        }
    }

    private OrderStatus getOrderStatusByStatusId(int statusId) {
        //Link:https://sherwoodforest.atlassian.net/wiki/spaces/LOY/pages/2324530937/Order+History+API+new+changes
        switch (statusId) {
            case -2: //Started
            case -1: //Submitted to Order Service
            case 10: //OrderPending
            case 11: //Demo
            case 12: //Reprocess - Full
            case 16: //Reprocess - Partial
            case 42: //On Hold
            case 44: //LostStolen
                return OrderStatus.ORDER_PLACED;
            case 0:  //Processing
            case 1:  //Ordered
            case 8:  //Processed
            case 18: //BackOrder
            case 20: //Delayed Delivery
            case 23: //Backorder Review
            case 98: //Fraud - Alert
                return OrderStatus.PROCESSING;
            case 2:  //Shipped
            case 3:  //Completed
            case 26: //RSL Requested
            case 27: //RSL Forwarded
            case 28: //RSL Used
            case 29: //RSL Vdr Recd Item
            case 30: //RSL Pnd Vdr Rfnd
            case 31: //Cmplt-RSL Expired
            case 32: //Cmplt-RTNW Closed
            case 38: //Cmplt-GC Reship Req
            case 39: //Cmplt-PofP Fwrd
            case 43: //Delivered
                return OrderStatus.SHIPPED;
            case 5:  //ReturnRequested
            case 6:  //Cmplt-Returned
            case 15: //Cmplt-Refused
            case 25: //Completed-ReturnStrCrdt
            case 33: //Cmplt-Conven Rtn
            case 35: //Cmplt-Rtn Damaged
            case 36: //Cmplt-Rtn Wrong Item
                return OrderStatus.RETURNED;
            case 4:  //Refund Pending
            case 7:  //Cmplt-Cancelled
            case 9:  //Cmplt-NLA
            case 14: //Cmplt-BadAddr
            case 19: //ReProcessPending
            case 21: //Order Line Adjustment
            case 22: //Cmplt-Unshippable
            case 34: //Cmplt-Not Recd
            case 37: //Cmplt-GC Not Recd
            case 97: //Killed
            case 99: //Failed
                return OrderStatus.CANCELLED;
            default:
                logger.warn("Order Status Enum is not configured for the ID: {}", statusId);
                return OrderStatus.ORDER_PLACED;
        }
    }

    private void getEngraveInfo(OrderLineItem lineItem, OrderHistory.OrderLineInfo orderLineInfo) {
        Engrave engrave;
        if (CollectionUtils.isNotEmpty(lineItem.getOrderLineAttributes())) {
            engrave = new Engrave();
            String shippingAvailability = null;
            boolean isShippingAvailabilityUpdated = false;
            for (OrderLineItemAttribute orderAttribute : lineItem.getOrderLineAttributes()) {

                switch (orderAttribute.getName()) {
                    case CommonConstants.PRODUCT_OPTIONS:
                        orderLineInfo.setProductOptions(
                                List.of(orderAttribute.getValue().split(CommonConstants.ORDER_PRODUCT_OPTIONS_SPLIT_SEPARATOR)));
                        break;

                    case CommonConstants.ENGRAVING_LINE_1:
                        engrave.setLine1(orderAttribute.getValue());
                        break;

                    case CommonConstants.ENGRAVING_LINE_2:
                        engrave.setLine2(orderAttribute.getValue());
                        break;

                    case CommonConstants.SHIPPING_AVAILABILITY:
                        shippingAvailability = orderAttribute.getValue();
                        orderLineInfo.setShippingAvailability(shippingAvailability);
                        break;

                    case CommonConstants.SHIPPING_AVAILABILITY_OLD:
                        isShippingAvailabilityUpdated = true;
                        break;

                    default:
                        break;
                }
            }

            if (isShippingAvailabilityUpdated) {
                updateShippingAvailability(orderLineInfo, shippingAvailability);
            }
            orderLineInfo.setEngrave(engrave);
        }
    }

    /**
     *
     * @param orderLineInfo
     * @param shippingAvailability
     */
    private void updateShippingAvailability(OrderHistory.OrderLineInfo orderLineInfo, String shippingAvailability) {
        try {
            final Date estimatedShipmentDate =
                new SimpleDateFormat("MM/dd/yyyy").parse(shippingAvailability);
            orderLineInfo.setShippingAvailabilityDate(estimatedShipmentDate);
        } catch (ParseException ex) {
            logger.warn("Unable to parse shipping availability date {}", shippingAvailability);
        }
    }

    //set refund if order status is Cancelled / invalid address / item not available / returned
    private void setRefund(Orders order, OrderLineItem lineItem, OrderHistory.OrderLineInfo orderLineInfo,
        Locale locale, List<ReturnLineItem> returnLineItemList) {
        if (isRefund(lineItem, orderLineInfo)) {
            populateReturnLineItem(lineItem, locale, orderLineInfo, returnLineItemList);

            for (OrderLineAdjustment orderLineAdjustment : lineItem.getOrderLineAdjustmentList()) {
                if (orderLineAdjustment.getAdjustmentType().equalsIgnoreCase(CommonConstants.ORDER_ADJUSTMENT_TYPE_P)) {
                    orderLineInfo.setRefund(new Price(orderLineAdjustment.getPriceAmount(), order.getCurrencyCode(),
                        orderLineAdjustment.getPointAmount()));
                    orderLineInfo.setRefundStatus(BooleanUtils.toBoolean(orderLineAdjustment.getStatusId()));
                }
            }
        }
    }

    private boolean isRefund(final OrderLineItem lineItem, final OrderHistory.OrderLineInfo orderLineInfo) {
        return (CommonConstants.ORDER_STATUS_UA_CANCELLED.equalsIgnoreCase(orderLineInfo.getStatus()) ||
            CommonConstants.ORDER_STATUS_UA_INVALID_ADDRESS.equalsIgnoreCase(orderLineInfo.getStatus()) ||
            CommonConstants.ORDER_STATUS_UA_ITEM_NOT_AVAILABLE.equalsIgnoreCase(orderLineInfo.getStatus()) ||
            CommonConstants.ORDER_STATUS_UA_RETURNED.equalsIgnoreCase(orderLineInfo.getStatus()) ||
            OS_CANCELLED.equalsIgnoreCase(orderLineInfo.getOrderLineProgress().getStatus())||
            OS_RETURNED.equalsIgnoreCase(orderLineInfo.getOrderLineProgress().getStatus())) &&
            CollectionUtils.isNotEmpty(lineItem.getOrderLineAdjustmentList());
    }

    private void populateReturnLineItem(final OrderLineItem lineItem, final Locale locale,
        final OrderHistory.OrderLineInfo orderLineInfo, final List<ReturnLineItem> returnLineItemList) {
        ReturnLineItem returnLineItem = new ReturnLineItem();
        returnLineItem.setQuantity(lineItem.getQuantity());
        final Integer quantity = returnLineItem.getQuantity();
        returnLineItem.setProductName(lineItem.getName());
        returnLineItem.setItemPrice(orderLineInfo.getPrice());

        populateTaxesAndFees(lineItem, locale, returnLineItem, quantity);
        returnLineItemList.add(returnLineItem);
    }

    private void populateTaxesAndFees(final OrderLineItem lineItem, final Locale locale,
        final ReturnLineItem returnLineItem, final Integer quantity) {

        BigDecimal taxMoney;
        if (Objects.nonNull(lineItem.getDiscountedTaxes()) && lineItem.getDiscountedTaxes() > 0) {
            taxMoney = BigDecimal.valueOf(lineItem.getDiscountedTaxes());
        } else {
            taxMoney = lineItem.getTotalTaxesInMoneyMinor();
        }

        int taxPoints;
        if (Objects.nonNull(lineItem.getTaxPoints()) && lineItem.getTaxPoints() > 0) {
            taxPoints = lineItem.getTaxPoints().intValue();
        } else {
            taxPoints = lineItem.getTotalTaxesInPoints();
        }

        returnLineItem.setTaxPrice(new Price(Money.ofMinor(CurrencyUnit.of(locale),
            taxMoney.longValue() * quantity),
            taxPoints * quantity));

        BigDecimal feeMoney;
        if (Objects.nonNull(lineItem.getDiscountedFees()) && lineItem.getDiscountedFees() > 0) {
            feeMoney = BigDecimal.valueOf(lineItem.getDiscountedFees());
        } else {
            feeMoney = lineItem.getTotalFeesInMoneyMinor();
        }
        returnLineItem.setFeesPrice(new Price(Money.ofMinor(CurrencyUnit.of(locale),
            feeMoney.longValue() * quantity), lineItem.getTotalFeesInPoints() * quantity));
    }

    private void getContactInfo(Orders order, ContactInfo contactInfo) {
        contactInfo.setEmail(order.getEmail());
        contactInfo.setPhone(order.getPhone());
    }

    private void getAddressInfo(Orders order, Address address) {
        address.setFirstName(order.getFirstName());
        address.setLastName(order.getLastName());
        address.setBusinessName(order.getBusinessName());
        address.setAddressLine1(order.getAddr1());
        address.setAddressLine2(order.getAddr2());
        address.setCity(order.getCity());
        address.setState(order.getState());
        address.setCountry(order.getCountry());
        address.setPostalCode(order.getZip());
    }

    /**
     * Retrieve Tracking URL from VPM instead of Hardcoded value
     * For Jan release - considering only FEDEX, TFORCE carriers
     *
     */
    private String getTrackingUrlBasedOnVPM(OrderLineShipmentNotification orderLineShipmentNotification,
                                           String shippingCarrier, Properties properties, Locale locale) {
        String trackingURL = "";
        if (shippingCarrier.toUpperCase().startsWith(CommonConstants.SHIPMENT_CARRIER_FEDEX)) {
            trackingURL = getTrackingUrl(orderLineShipmentNotification, properties, locale, CommonConstants.FED);
        } else if (shippingCarrier.toUpperCase().startsWith(CommonConstants.SHIPMENT_CARRIER_TFORCE)) {
            trackingURL = getTrackingUrl(orderLineShipmentNotification, properties, locale, CommonConstants.TFORCE);
        }
        return trackingURL;
    }

    private String getTrackingUrl(OrderLineShipmentNotification orderLineShipmentNotification, Properties properties,
                                  Locale locale, String messageCode) {
        String trackingURL;
        if (Objects.nonNull(properties.getProperty(messageCode))) {
            trackingURL = replaceUrlAttributes(orderLineShipmentNotification.getTrackingNumber(),
                    properties.getProperty(messageCode), locale);
        } else {
            trackingURL = orderLineShipmentNotification.getTrackingUrl();
        }
        return trackingURL;
    }

    private String replaceUrlAttributes(String trackingNumber, String url, Locale locale) {
        return url.replaceAll(CommonConstants.TRACK_NUM, trackingNumber)
                .replaceAll(CommonConstants.TRACK_COUNTRY, locale.getCountry().toLowerCase())
                .replaceAll(CommonConstants.TRACK_LOCALE, locale.toString().toLowerCase());
    }

    private String getTrackingLink(final OrderLineShipmentNotification orderLineShipmentNotification, Properties properties, Locale locale) {
        String link = "";
        if (orderLineShipmentNotification != null && orderLineShipmentNotification.getShippingCarrier() != null &&
                orderLineShipmentNotification.getShippingCarrier().length() > 2) {
            String shippingCarrier = orderLineShipmentNotification.getShippingCarrier().toUpperCase().trim();

            if (orderLineShipmentNotification.getTrackingNumber() != null) {
                link = getTrackingUrlBasedOnVPM(orderLineShipmentNotification, shippingCarrier, properties, locale);

                if (StringUtils.isBlank(link)) {
                    link = createLink(orderLineShipmentNotification, link, shippingCarrier);

                    // CUSTOMIZED TRACKING URL
                    link = getCustomURLTracking(orderLineShipmentNotification, link);
                }
            }
        }
        return link;
    }

    private String createLink(OrderLineShipmentNotification orderLineShipmentNotification, String link, String shippingCarrier) {
        if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_USPS) || shippingCarrier
                .startsWith(CommonConstants.SHIPMENT_CARRIER_US_P) ||
                shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_US_MAIL) || shippingCarrier
                .startsWith(CommonConstants.SHIPMENT_CARRIER_MAIL_EXPRESS)) {
            link = CommonConstants.USPS.replaceAll(CommonConstants.TRACK, orderLineShipmentNotification
                    .getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_UPS_MAIL_INNOVATIONS)) {
            link = CommonConstants.UPS_MI
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_UPS)) {
            link = CommonConstants.UPS
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_DHL)) {
            link = CommonConstants.DHL
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_AIR)) {
            link = CommonConstants.AIR
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_A1)) {
            link = CommonConstants.A1
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_ABF)) {
            link = CommonConstants.ABF
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else {
            link = populateLink(orderLineShipmentNotification, link, shippingCarrier);
        }
        return link;
    }

    private String getCustomURLTracking(OrderLineShipmentNotification orderLineShipmentNotification, String link) {
        //WE NEED TO USE ORIGINAL AUG GSI NASCAR TRACKING URL
        if (orderLineShipmentNotification.getTrackingNumber() != null &&
                (orderLineShipmentNotification.getTrackingNumber().startsWith(CommonConstants.SHIPMENT_CARRIER_HTTP_LOWER) ||
                        orderLineShipmentNotification.getTrackingNumber().startsWith(CommonConstants.SHIPMENT_CARRIER_HTTP_UPPER))) {
            link = orderLineShipmentNotification.getTrackingNumber();
        }
        return link;
    }

    private String populateLink(OrderLineShipmentNotification orderLineShipmentNotification, String link, String shippingCarrier) {
        if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_DYN)) {
            link = CommonConstants.DYN
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_PAR)) {
            link = CommonConstants.PAR
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_EGL) || shippingCarrier
                .startsWith(CommonConstants.SHIPMENT_CARRIER_EAG)) {
            link = CommonConstants.EGL
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_CEVA) || shippingCarrier
                .startsWith(CommonConstants.SHIPMENT_CARRIER_WWW_CEVA) ||
                shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_CEVA_START_WITH)) {
            link = CommonConstants.CEVA
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_ONTRAC)) {
            link = CommonConstants.ONTRAC
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_PUROLATOR)) {
            link = CommonConstants.PUROLATOR
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_DHLG)) {
            link = CommonConstants.DHL_G
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (StringUtils.startsWithIgnoreCase(shippingCarrier, CommonConstants.SHIPMENT_CARRIER_STREAM_LITE)) { //CSR-2082
            link = CommonConstants.STREAM_LITE
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else if (shippingCarrier.startsWith(CommonConstants.SHIPMENT_CARRIER_AIT)) {
            link = CommonConstants.AIT
                    .replaceAll(CommonConstants.TRACK, orderLineShipmentNotification.getTrackingNumber());
        } else {
            link = populateLinkFromShipmentNotification(orderLineShipmentNotification, link, shippingCarrier);
        }
        return link;
    }

    private String populateLinkFromShipmentNotification(OrderLineShipmentNotification orderLineShipmentNotification, String link, String shippingCarrier) {
        if ((shippingCarrier.toUpperCase().startsWith(CommonConstants.SHIPMENT_CARRIER_TNT) ||
                shippingCarrier.toUpperCase().startsWith(CommonConstants.SHIPMENT_CARRIER_YAMATO) ||
                shippingCarrier.toUpperCase().startsWith(CommonConstants.SHIPMENT_CARRIER_SCHENKER) ||
                shippingCarrier.toUpperCase().startsWith(CommonConstants.SHIPMENT_CARRIER_SHUN) ||
                shippingCarrier.toUpperCase().startsWith(CommonConstants.SHIPMENT_CARRIER_STARTRACK)) &&
                orderLineShipmentNotification.getTrackingUrl() != null &&
                orderLineShipmentNotification.getTrackingUrl().toLowerCase()
                        .startsWith(CommonConstants.TRACK_HTTP)) {
            link = orderLineShipmentNotification.getTrackingUrl();
        }
        return link;
    }

    private String getPriceinCurrency(String languageCode, String countryCode, double money  ) {
        final Locale orderLocale = new Locale(languageCode, countryCode);
        final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(orderLocale);
        return currencyFormat.format(money);
    }

    private String getOrderHistoryDateFormat(final String varId){
        String dateFormat =  applicationProperties.getProperty(varId.toLowerCase() +"."+ CommonConstants.DEFAULT_ORDER_HISTORY_DATE_FORMATE);
        if(StringUtils.isEmpty(dateFormat)){
            dateFormat = applicationProperties.getProperty(CommonConstants.DEFAULT_ORDER_HISTORY_DATE_FORMATE);
        }
        return dateFormat;
    }

    private DelayedShippingInfo buildDelayedShippingInfo(
            final Integer orderId,
            final String dateFormat,
            final OrderLineItem lineItem) {

        final Optional<String> shippingAvailability = lineItem.getOrderLineAttributes().stream()
                .filter(attr -> CommonConstants.ORDER_LINE_ATTRIBUTE_KEY_SHIPS_BY.equals(attr.getName()))
                .map(OrderLineItemAttribute::getValue)
                .findFirst();

        final Optional<String> asOfDate =
                orderLineStatusHistoryDao.loadStatusHistoryLatestFirstByOrderId(Long.valueOf(orderId)).stream()
                        .filter(statusHistory -> CommonConstants.ORDER_STATUS_BACK_ORDERED == statusHistory.getId().getStatus().getStatusId())
                        .findFirst()
                        .map(statusHistory -> statusHistory.getId().getModifiedDate())
                        .map(timestamp -> new Date(timestamp.getTime()))
                        .map(date -> new SimpleDateFormat(dateFormat).format(date));

        if (shippingAvailability.isPresent() && asOfDate.isPresent()) {
            return DelayedShippingInfo.builder()
                    .withShippingAvailability(shippingAvailability.get())
                    .withAsOfDate(asOfDate.get())
                    .build();
        }

        return null;
    }

    private void setBundledPrice(final OrderHistory.OrderLineInfo orderLineInfo, final OrderLineItem orderLineItem, final Program program, final Locale locale){
        if(BundledPricingOption.BUNDLED == program.getBundledPricingOption()){
            orderLineInfo.setPrice(new Price(orderLineItem.getOrderLineTotalPrice(locale), orderLineItem.getOrderLinePoints()));
        }else{
            orderLineInfo.setPrice(new Price(orderLineItem.getPriceForQuantity(locale), orderLineItem.getPointsForQuantity()));
        }
    }

    private void setPromotionAmount(final OrderHistory.OrderLineInfo orderLineInfo, final OrderLineItem orderLineItem,
        final Program program, final Locale locale) {
        if (Objects.nonNull(orderLineItem.getDiscountedSupplierItemPrice())) {
            Integer amount;
            if (BundledPricingOption.BUNDLED == program.getBundledPricingOption()) {
                amount = orderLineItem.getDiscountedVarOrderLinePrice();
            } else {
                amount = orderLineItem.getDiscountedSupplierItemPrice();
            }
            orderLineInfo.setPrice(
                new Price(Money.ofMinor(CurrencyUnit.of(locale), amount * orderLineItem.getQuantity().longValue()),
                    orderLineInfo.getPrice().getPoints()));
        }
    }

    /**
     * Method to return Lost Stolen order line previous order status
     *
     * @param orderId
     * @param lineNum
     * @return
     */
    public String getLostStolenPreviousStatus(final Long orderId, final Integer lineNum) {
        Optional<OrderLineStatusHistory> orderLineStatusHistory =
                orderLineStatusHistoryDao.loadStatusHistoryLatestFirstByOrderIdLineNum(orderId, lineNum)
                        .stream()
                        .filter(statusHistory ->
                                statusHistory.getId().getStatus().getStatusId() != CommonConstants.ORDER_STATUS_LOST_STOLEN)
                        .findFirst();

        return orderLineStatusHistory.isPresent() ?
                orderLineStatusHistory.get().getId().getStatus().getStatusId().toString() : null;
    }

}
