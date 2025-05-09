package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.OrderHistoryService;
import com.b2s.apple.services.ProgramService;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.db.model.BundledPricingOption;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.dao.OrderStatusDescDao;
import com.b2s.rewards.apple.dao.ShipmentNotificationDao;
import com.b2s.rewards.apple.model.OrderInfoAPI;
import com.b2s.rewards.apple.model.OrderLineShipmentNotification;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.BasicAuthValidation;
import com.b2s.rewards.apple.util.ContextUtil;
import com.b2s.rewards.common.exception.RestException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.SessionUtil;
import com.b2s.shop.common.OrderHistory;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.OrderTransactionManager;
import com.b2s.spark.api.apple.to.impl.ContactInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/***
 * Created by ssrinivasan on 9/15/2016.
 */
@RestController
@RequestMapping(value = "", produces = "application/json;charset=UTF-8")
@ResponseBody
public class OrdersController {

    public static final String ERROR_RETRIEVING_ORDER_DETAILS_FROM_DB =
        "getOrder: Error retrieving order details from DB for order ID # {} ";
    public static final String UNEXPECTED_ERROR = "Unexpected error";

    @Autowired
    private OrderTransactionManager orderManager;

    @Autowired
    private ShipmentNotificationDao shipmentNotificationDao;

    @Autowired
    private OrderStatusDescDao orderStatusDescDao;

    @Autowired
    private OrderHistoryService orderHistoryService;

    @Autowired
    private BasicAuthValidation basicAuthValidation;

    @Autowired
    private ContextUtil contextUtil;

    @Autowired
    private ProgramService programService;

    @Autowired
    private AppSessionInfo appSessionInfo;

    private static final Logger LOG = LoggerFactory.getLogger(OrdersController.class);

    @RequestMapping(value = {"/orders/{orderId}"}, method = RequestMethod.GET)
    public ResponseEntity<OrderInfoAPI> getOrder(@PathVariable final Long orderId) throws RestException{

        LOG.info("AUDIT Order received for order id {}", orderId);
        final Order order;

        if (Objects.isNull(orderId) || orderId == 0) {
            LOG.error("getOrder: invalid order ID # {}", orderId);
            throw new RestException("Not a valid orderId", HttpStatus.BAD_REQUEST);
        }

        try {
            order = orderManager.getOrder(orderId);
        } catch (final ServiceException e) {
            LOG.error(ERROR_RETRIEVING_ORDER_DETAILS_FROM_DB, orderId);
            throw new RestException(UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (Objects.isNull(order)) {
            LOG.error(ERROR_RETRIEVING_ORDER_DETAILS_FROM_DB, orderId);
            throw new RestException("B2S Order ID not found", HttpStatus.NOT_FOUND);
        }

        // compare basic auth role and varId to identify user access to given order id.
        if(!basicAuthValidation.isUserHasAccessToOrder(order)){
            throw new RestException("Order ID does not belongs to your entity.", HttpStatus.FORBIDDEN);
        }

        // Populate order details
        final Program program = programService.getProgram(order.getVarId(), order.getProgramId(), LocaleUtils.toLocale(order.getLanguageCode() + "_" + order.getCountryCode()));
        final String showExperience = (String)program.getConfig().getOrDefault(CommonConstants.SHOP_EXPERIENCE, "");
        final OrderInfoAPI orderInfo = populateOrderDetails(order,showExperience,program);

        if (Objects.isNull(orderInfo.getOrderID())) {
            LOG.error(ERROR_RETRIEVING_ORDER_DETAILS_FROM_DB, orderId);
            throw new RestException(UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(orderInfo, HttpStatus.OK);
    }

    @RequestMapping(value = {"/order/orderHistoryDetails/{orderId}"}, method = RequestMethod.GET)
    public ResponseEntity getOrderHistoryDetails(@PathVariable final String orderId, final HttpServletRequest servletRequest) {

        OrderHistory orderHistory= null;
        final User user = appSessionInfo.currentUser();

        if(Objects.isNull(user) || StringUtils.isEmpty(user.getUserId())){
            LOG.error("getOrderHistoryDetails: Invalid user session for order # {}",orderId);
            return new ResponseEntity("Invalid user session", HttpStatus.NOT_FOUND);
        }

        if (Objects.nonNull(orderId)) {
            try {
                final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
                orderHistory = orderHistoryService.getOrderHistoryDetails(user, orderId, program);
                if (Objects.isNull(orderHistory)) {
                    LOG.error("getOrderHistoryDetails: B2S Order ID not found / does not belongs to user # {}",user.getUserId());
                    return new ResponseEntity("B2S Order ID not found", HttpStatus.NOT_FOUND);
                }

                // clear session if anonymous
                if (AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.VIEW_ANONYMOUS_ORDER_DETAIL)) {
                    SessionUtil.restartSession(servletRequest);
                }
            } catch (ServiceException e) {
                LOG.error("getOrderHistoryDetails: Error retrieving order details from DB for order ID # {} ", orderId,
                    e);
                return new ResponseEntity(UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity(orderHistory, HttpStatus.OK);
        }

        LOG.error("getOrder: invalid order ID # {}", orderId);
        return new ResponseEntity("Not a valid orderId", HttpStatus.BAD_REQUEST);


    }

    @RequestMapping(value = {"/order/orderHistory"}, method = RequestMethod.GET)
    public ResponseEntity getOrderHistory(@RequestParam(required = false) final Integer days, final
    HttpServletRequest servletRequest) {

        List<OrderHistory> orderHistoryList = null ;
        final User user = appSessionInfo.currentUser();
        final Program program = (Program) servletRequest.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        if (!Objects.isNull(user) && !Objects.isNull(user.getUserId())) {
            try {
                orderHistoryList = orderHistoryService.getOrderHistory(user, program, days);
                if (Objects.isNull(orderHistoryList)) {
                    LOG.error("getOrderHistory:No orders found for the user ID # {}",user.getUserId());
                    return new ResponseEntity("Orders not found", HttpStatus.NOT_FOUND);
                }
            } catch (ServiceException e) {
                LOG.error("getOrderHistory: Error retrieving orders from DB for the user ID # {} ", user.getUserId());
                return new ResponseEntity(UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity(orderHistoryList, HttpStatus.OK);
        }
        LOG.error("orderHistoryDetails: Invalid user details ");
        return new ResponseEntity("User ID not found", HttpStatus.NOT_FOUND);

    }


    private OrderInfoAPI populateOrderDetails(final Order order, final String showExperience, final Program program) {

        final OrderInfoAPI orderInfo = new OrderInfoAPI();
        final List<OrderInfoAPI.OrderLineInfo> orderLineList = new ArrayList();

        order.getOrderLines().stream()
            .filter(
                line -> ((OrderLine) line).getSupplierId().equalsIgnoreCase(CommonConstants.APPLE_SUPPLIER_ID_STRING))
            .forEach(line -> populateOrderLine(order, showExperience, program, orderLineList, (OrderLine) line));

        // Delivery details
        final OrderInfoAPI.DeliveryAddress address = new OrderInfoAPI.DeliveryAddress();
        address.setFirstName(order.getFirstname());
        address.setLastName(order.getLastname());
        address.setAddressLine1(order.getAddr1());
        address.setAddressLine2(order.getAddr2());
        address.setAddressLine3(order.getAddr3());
        // After the changes done to add addr4 in Order.java to get the value from DB, and Address line 4 needs to be set in address object.
        address.setCity(order.getCity());
        address.setState(order.getState());
        address.setCountry(order.getCountry());
        address.setPostalCode(order.getZip());

        //Order Attributes
        final OrderInfoAPI.OrderAttributes orderAttributes = new OrderInfoAPI.OrderAttributes();
        orderAttributes.setLocale(new Locale(order.getLanguageCode(), order.getCountryCode()).toString());
        orderAttributes.setCurrencyCode(order.getCurrencyCode());
        orderAttributes.setProgramType(CommonConstants.ORDER_API_PROGRAM_TYPE_PERSONAL);
        if(CollectionUtils.isNotEmpty(order.getOrderAttributeValues())) {
            order.getOrderAttributeValues().stream()
                    .filter(orderAttributeValue -> CommonConstants.EMPLOYER_ID.equalsIgnoreCase(orderAttributeValue.getName()))
                    .findFirst()
                    .map(orderAttributeValue1 -> orderAttributeValue1.getValue())
                    .ifPresent(employerId -> orderAttributes.setEmployerID(employerId));
        }

        //Order Totals
        final OrderInfoAPI.OrderTotals orderTotals = new OrderInfoAPI.OrderTotals();
        orderTotals.setTaxTotal(String.valueOf(order.getOrderTotalTaxesInMoney().getAmount()));
        orderTotals.setFeeTotal(String.valueOf(order.getOrderTotalFeesInMoney().getAmount()));

        if(BundledPricingOption.BUNDLED.equals(program.getBundledPricingOption())){
            orderTotals.setOrderTotal(String.valueOf(order.getOrderTotalInMoney().plus(order.getSupplierTaxPriceInMoney()).getAmount()));
            orderTotals.setSupplierItemTotal(String.valueOf(order.getOrderTotalInMoney().plus(order.getSupplierTaxPriceInMoney()).getAmount()));
            orderTotals.setItemTotal(String.valueOf(order.getOrderTotalInMoney().plus(order.getSupplierTaxPriceInMoney()).getAmount()));
        }else{
            orderTotals.setOrderTotal(String.valueOf(order.getOrderTotalInMoney().getAmount()));
            orderTotals.setSupplierItemTotal(String.valueOf(order.getOrderSubTotalInMoney().getAmount()));
            orderTotals.setItemTotal(String.valueOf(order.getOrderSubTotalWithVarAndB2SMarginInMoney().getAmount()));
        }

        //Payment Info
        final OrderInfoAPI.PaymentInfo paymentInfo = new OrderInfoAPI.PaymentInfo();
        if(BundledPricingOption.BUNDLED.equals(program.getBundledPricingOption())){
            paymentInfo.setPaymentTotal(String.valueOf(order.getOrderTotalInMoney().plus(order.getSupplierTaxPriceInMoney()).getAmount()));
        }else{
            paymentInfo.setPaymentTotal(String.valueOf(order.getOrderTotalInMoney().getAmount()));
        }

        List<OrderInfoAPI.PaymentInfo.PaymentTender> paymentTenders = populatePaymentTender(order);
        paymentInfo.setPaymentTenders(paymentTenders);


        // Contact info
        final ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail(order.getEmail());
        contactInfo.setPhone(order.getPhone());

        // Populate orderInfo
        orderInfo.setOrderID(order.getOrderId());
        orderInfo.setUserID(order.getUserId());
        orderInfo.setEntityID(order.getVarId());
        orderInfo.setProgramID(order.getProgramId());
        orderInfo.setOrderDate(order.getOrderDate().toString());
        orderInfo.setOrderItems(orderLineList);
        orderInfo.setDeliveryAddress(address);
        orderInfo.setContactInfo(contactInfo);
        orderInfo.setOrderAttributes(orderAttributes);
        orderInfo.setOrderTotals(orderTotals);
        orderInfo.setPaymentInfo(paymentInfo);

        return orderInfo;
    }

    private void populateOrderLine(final Order order, final String showExperience, final Program program,
        final List<OrderInfoAPI.OrderLineInfo> orderLineList, final OrderLine line) {
        final OrderLine orderLine = line;
        final OrderInfoAPI.OrderLineInfo lineInfo = new OrderInfoAPI.OrderLineInfo();

        // Order Line info
        lineInfo.setOrderLineID(orderLine.getLineNum());
        lineInfo.setSku(orderLine.getSku());
        lineInfo.setItemName(orderLine.getName());
        lineInfo.setItemImageURL(orderLine.getImageUrl());
        lineInfo.setCurrencyType(order.getCurrencyCode());
        lineInfo.setQuantity(orderLine.getQuantity());

        if(BundledPricingOption.BUNDLED.equals(program.getBundledPricingOption())){
            lineInfo.setSupplierItemPrice(AppleUtil.getAmountInDollarsString(orderLine.getDisplayItemPrice()));
            lineInfo.setItemPrice(AppleUtil.getAmountInDollarsString(orderLine.getDisplayItemPrice()));
        }else{
            lineInfo.setSupplierItemPrice(AppleUtil.getAmountInDollarsString(orderLine.getSupplierItemPrice()));
            lineInfo.setItemPrice(AppleUtil.getAmountInDollarsString(orderLine.getSupplierItemPrice()));
        }

        lineInfo.setItemTax(orderLine.getTotalTaxesInMoneyMinor().setScale(2).toString());
        lineInfo.setItemFee(orderLine.getTotalFeesInMoneyMinor().setScale(2).toString());
        lineInfo.setStatus(orderStatusDescDao.getDescByStatusId(orderLine.getOrderStatus()));
        lineInfo.setCategory(orderLine.getCategory());
        if(CommonConstants.EXPERIENCE_DRP.equalsIgnoreCase(showExperience) && order.getPayrollFrequency() > 0){
            final Double itemPriceWithQuantityWithDuration =  (Double.valueOf(lineInfo.getSupplierItemPrice())
                    *orderLine.getQuantity())/order.getPayrollFrequency();
            lineInfo.setItemPaymentPerPeriod(BigDecimal.valueOf(itemPriceWithQuantityWithDuration).setScale(2,
                    RoundingMode.CEILING).toString());
        }
        // Shipment info
        try {
            final OrderLineShipmentNotification shipment;
            shipment =
                shipmentNotificationDao.getShipmentNotification(orderLine.getOrderId(), orderLine.getLineNum());
            OrderInfoAPI.ShipmentDeliveryInfo shipInfo = new OrderInfoAPI.ShipmentDeliveryInfo();
            if (Objects.nonNull(shipment)) {

                shipInfo.setShipmentDate(shipment.getShipmentDate().toString());
                shipInfo.setTrackingID(shipment.getTrackingNumber());
                shipInfo.setCarrierName(shipment.getShippingCarrier());
            }
            lineInfo.setShipmentInfo(shipInfo);
        } catch (final Exception e) {
            LOG.error("Error while retrieving shipment details for order Id # {}, lineNum #{}",
                orderLine.getOrderId(), orderLine.getLineNum());
        }
        orderLineList.add(lineInfo);
    }

    private List<OrderInfoAPI.PaymentInfo.PaymentTender> populatePaymentTender(Order order) {
        List<OrderInfoAPI.PaymentInfo.PaymentTender> paymentTenders = new ArrayList();
        final Locale orderLocale = getLocaleFromOrder(order);

        // Points Payment
        if(order.getOrderTotalPointsPaid() > 0) {
            final MessageSource messageSource = contextUtil.getMessageSource(order.getVarId());
            final Program program = programService.getProgram(order.getVarId(), order.getProgramId(), LocaleUtils.toLocale(order.getLanguageCode() + "_" + order.getCountryCode()));
            final String pointName = messageSource.getMessage(program.getPointName(), null, program.getPointName(), orderLocale);

            final OrderInfoAPI.PaymentInfo.PaymentTender paymentTenderPoints = new OrderInfoAPI.PaymentInfo.PaymentTender();
            paymentTenderPoints.setPaymentTotal(String.valueOf(order.getOrderTotalPointsPaid()));
            paymentTenderPoints.setPaymentType(pointName);
            paymentTenderPoints.setCurrencyCode(CommonConstants.POINT_CURRENCY_STRING);

            paymentTenders.add(paymentTenderPoints);
        }

        // Card payment
        final Money ccPaid = order.getOrderTotalMoneyPaid();
        if(!ccPaid.isNegativeOrZero()) {
            final OrderInfoAPI.PaymentInfo.PaymentTender paymentTenderCard = new OrderInfoAPI.PaymentInfo.PaymentTender();
            paymentTenderCard.setPaymentTotal(String.valueOf(ccPaid.getAmount()));
            paymentTenderCard.setCurrencyCode(order.getCurrencyCode());
            paymentTenderCard.setPaymentType(CommonConstants.PAYMENT_TYPE_CARD);

            paymentTenders.add(paymentTenderCard);

        }

        // Payroll deduction.
        final Money payrollPrice = order.getOrderTotalPayrollPrice();
        if(payrollPrice != null && payrollPrice.getAmount() != null && payrollPrice.getAmount().doubleValue() > 0) {
            final OrderInfoAPI.PaymentInfo.PaymentTender paymentTenderPayroll = new OrderInfoAPI.PaymentInfo.PaymentTender();
            paymentTenderPayroll.setPaymentTotal(String.valueOf(payrollPrice.getAmount()));
            paymentTenderPayroll.setCurrencyCode(order.getCurrencyCode());
            paymentTenderPayroll.setPaymentType(CommonConstants.PAYMENT_TYPE_PAYROLL);

            final Money payrollPeriodPrice = order.getOrderTotalPayrollPeriodPrice();
            if(payrollPeriodPrice != null && payrollPeriodPrice.getAmount() != null && payrollPeriodPrice.getAmount().doubleValue() > 0) {
                paymentTenderPayroll.setPaymentPerPeriod(String.valueOf(payrollPeriodPrice.getAmount()));
                paymentTenderPayroll.setNumberOfPayments(order.getPayrollFrequency());
                paymentTenderPayroll.setPayFrequency(order.getPayrollDuration());
            }

            paymentTenders.add(paymentTenderPayroll);
        }

        //Discount total
        final Money discountTotal = order.getOrderTotalDiscounts();
        if(discountTotal != null && discountTotal.getAmount() != null && discountTotal.getAmount().doubleValue() > 0) {
            final OrderInfoAPI.PaymentInfo.PaymentTender paymentTenderDiscount = new OrderInfoAPI.PaymentInfo.PaymentTender();
            paymentTenderDiscount.setCurrencyCode(order.getCurrencyCode());
            paymentTenderDiscount.setPaymentType(CommonConstants.PAYMENT_TYPE_DISCOUNT);
            paymentTenderDiscount.setPaymentTotal(String.valueOf(discountTotal.getAmount()));
        }

        return paymentTenders;
    }

    private Locale getLocaleFromOrder(final Order order) {
        return new Locale(order.getLanguageCode(), order.getCountryCode());
    }
}
