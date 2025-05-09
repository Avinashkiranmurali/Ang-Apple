package com.b2s.apple.services;

import com.b2s.apple.entity.PaymentEntity;
import com.b2s.db.model.BundledPricingOption;
import com.b2s.rewards.apple.dao.OrderHistoryDao;
import com.b2s.rewards.apple.dao.OrderLineStatusHistoryDao;
import com.b2s.rewards.apple.dao.PaymentDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.OrderHistory;
import com.b2s.shop.common.RefundSummary;
import com.b2s.shop.common.ReturnLineItem;
import com.b2s.shop.common.User;
import com.b2s.common.services.exception.ServiceException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;

import java.sql.Timestamp;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OrderHistoryServiceTest {

    private OrderHistoryService orderHistoryService;

    @Mock
    private Properties applicationProperties;

    @Mock
    private OrderHistoryDao orderHistoryDao;

    @Mock
    private OrderLineStatusHistoryDao orderLineStatusHistoryDao;

    @Mock
    private PaymentDao paymentDao;

    @Mock
    private User user;

    @Mock
    private VarProgramMessageService varProgramMessageService;

    String SHIPMENT_CARRIER_FEDERAL_EXPRESS = "FEDERAL EXPRESS";

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        orderHistoryService = new OrderHistoryService();
        user = user = new User();
        user.setLocale(Locale.US);
        user.setVarId("UA");
        Whitebox.setInternalState(orderHistoryService,"applicationProperties",applicationProperties);
        Whitebox.setInternalState(orderHistoryService,"orderLineStatusHistoryDao",orderLineStatusHistoryDao);
        Whitebox.setInternalState(orderHistoryService,"orderHistoryDao",orderHistoryDao);
        Whitebox.setInternalState(orderHistoryService,"paymentDao",paymentDao);
        Whitebox.setInternalState(orderHistoryService,"varProgramMessageService",varProgramMessageService);
    }

    @Test
    public void testYamatoShippingTrackingURL() throws ServiceException {

        String trackingURL = "http://www.yamato.com/shipId=90";
        Program program = getProgram();

        Orders orders = createTestOrder();
        orders.getLineItemList().get(0)
            .setOrderLineShipmentNotifications(populateOrderLineShipmentNotification("Yamato", "12345", trackingURL));

        when(applicationProperties.getProperty(any(String.class))).thenReturn("KEY_VALUE");
        when(paymentDao.getSaleDetails(Long.valueOf(orders.getOrderId())))
            .thenReturn(null);
        when(varProgramMessageService.getMessages(any(), any(), any())).thenReturn(new Properties());

        OrderHistory orderHistory = orderHistoryService.getOrderHistory(user,false, orders, program);

        Assert.assertEquals(trackingURL, orderHistory.getLineItems().get(0).getShipmentInfo().getTrackingID());
    }

    @Test
    public void testShunFengShippingTrackingURL() throws ServiceException {

        String trackingURL =  "http://www.ShunFeng.com/shipId=90";
        Program program = getProgram();

        Orders orders = createTestOrder();
        orders.getLineItemList().get(0)
            .setOrderLineShipmentNotifications(populateOrderLineShipmentNotification("Shun Feng","12345",trackingURL));

        when(applicationProperties.getProperty(any(String.class))).thenReturn("KEY_VALUE");
        when(paymentDao.getSaleDetails(Long.valueOf(orders.getOrderId())))
            .thenReturn(null);
        OrderHistory orderHistory = orderHistoryService.getOrderHistory(user,false, orders, program);

        Assert.assertEquals(trackingURL, orderHistory.getLineItems().get(0).getShipmentInfo().getTrackingID());
    }

    @Test
    public void testSchenkerPTEShippingTrackingURL() throws ServiceException {

        String trackingURL =  "http://www.Schenker.com/shipId=90";
        Program program = getProgram();

        Orders orders = createTestOrder();

        List<OrderLineItemAttribute> orderLineItemAttributes = new ArrayList<>();
        orderLineItemAttributes.add(populateOrderLineItemAttribute(Long.valueOf(orders.getOrderId()), 0, CommonConstants.SHIPPING_AVAILABILITY, "Available shortly"));
        orderLineItemAttributes.add(populateOrderLineItemAttribute(Long.valueOf(orders.getOrderId()), 0, CommonConstants.SHIPPING_AVAILABILITY_OLD, "Available sooner"));

        orders.getLineItemList().get(0).getOrderLineAttributes().addAll(orderLineItemAttributes);
        orders.getLineItemList().get(0)
            .setOrderLineShipmentNotifications(populateOrderLineShipmentNotification("Schenker PTE","12345",trackingURL));

        when(applicationProperties.getProperty(any(String.class))).thenReturn("KEY_VALUE");
        when(paymentDao.getSaleDetails(Long.valueOf(orders.getOrderId())))
            .thenReturn(null);
        OrderHistory orderHistory = orderHistoryService.getOrderHistory(user,false, orders, program);

        Assert.assertEquals(trackingURL, orderHistory.getLineItems().get(0).getShipmentInfo().getTrackingID());
    }

    @Test
    public void testStarTrackShippingTrackingURL() throws ServiceException {

        String trackingURL =  "http://www.StarTrack.com/shipId=90";
        Program program = getProgram();

        Orders orders = createTestOrder();

        List<OrderLineItemAttribute> orderLineItemAttributes = new ArrayList<>();
        orderLineItemAttributes.add(populateOrderLineItemAttribute(Long.valueOf(orders.getOrderId()), 0, CommonConstants.ENGRAVING_LINE_1, "Engraving Line1"));
        orderLineItemAttributes.add(populateOrderLineItemAttribute(Long.valueOf(orders.getOrderId()), 0, CommonConstants.ENGRAVING_LINE_2, "Engraving Line2"));

        orderLineItemAttributes.add(populateOrderLineItemAttribute(Long.valueOf(orders.getOrderId()), 0, CommonConstants.SHIPPING_AVAILABILITY, "12/12/2022"));
        orderLineItemAttributes.add(populateOrderLineItemAttribute(Long.valueOf(orders.getOrderId()), 0, CommonConstants.SHIPPING_AVAILABILITY_OLD, "Available sooner"));

        orders.getLineItemList().get(0).getOrderLineAttributes().addAll(orderLineItemAttributes);
        orders.getLineItemList().get(0).setOrderLineShipmentNotifications(populateOrderLineShipmentNotification("StarTrack","12345",trackingURL));

        when(applicationProperties.getProperty(any(String.class))).thenReturn("KEY_VALUE");
        when(paymentDao.getSaleDetails(Long.valueOf(orders.getOrderId())))
            .thenReturn(null);
        OrderHistory orderHistory = orderHistoryService.getOrderHistory(user,false, orders, program);

        Assert.assertEquals(trackingURL, orderHistory.getLineItems().get(0).getShipmentInfo().getTrackingID());
    }

    @Test
    public void testFedexShippingTrackingURL() throws ServiceException {
        String trackingURL =  "https://www.fedex.com/fedextrack/?action=track&trackingnumber=12345&cntry_code=us&locale=en_us";
        Program program =  getProgram();

        Orders orders = createTestOrder();
        orders.getLineItemList().get(0)
                .setOrderLineShipmentNotifications(populateOrderLineShipmentNotification(SHIPMENT_CARRIER_FEDERAL_EXPRESS, "12345", trackingURL));
        when(applicationProperties.getProperty(any(String.class))).thenReturn("KEY_VALUE");
        when(varProgramMessageService.getMessages(any(), any(), any())).thenReturn(new Properties());

        OrderHistory orderHistory = orderHistoryService.getOrderHistory(user,false, orders, program);

        Assert.assertEquals(trackingURL, orderHistory.getLineItems().get(0).getShipmentInfo().getTrackingID());
    }

    @Test
    public void testFedexShippingTrackingURLBasedOnVPM() throws ServiceException {
        String trackingURL =  "https://www.fedex.com/fedextrack/?action=track&trackingnumber=12345&cntry_code=us&locale=en_us";
        String trackingURLinVPM =  "https://www.fedex.com/fedextrack/?action=track&trackingnumber=%TRACK_NUMBER%&cntry_code=%TRACK_COUNTRY%&locale=%TRACK_LOCALE%";
        Program program =  getProgram();

        Orders orders = createTestOrder();
        orders.getLineItemList().get(0)
                .setOrderLineShipmentNotifications(populateOrderLineShipmentNotification(SHIPMENT_CARRIER_FEDERAL_EXPRESS, "12345", trackingURL+"&source=OLSN"));

        when(applicationProperties.getProperty(any(String.class))).thenReturn("KEY_VALUE");
        Properties properties = new Properties();
        properties.put(CommonConstants.FED, trackingURLinVPM);
        when(varProgramMessageService.getMessages(any(), any(), any())).thenReturn(properties);

        OrderHistory orderHistoryVPM = orderHistoryService.getOrderHistory(user,false, orders, program);

        when(varProgramMessageService.getMessages(any(), any(), any())).thenReturn(properties);
        Assert.assertEquals(trackingURL, orderHistoryVPM.getLineItems().get(0).getShipmentInfo().getTrackingID());
    }

    @Test
    public void testGetLostStolenPreviousStatus() {
        final Long orderId = 123L;
        when(orderLineStatusHistoryDao.loadStatusHistoryLatestFirstByOrderIdLineNum(orderId,1))
            .thenReturn(getOrderLineStatusHistoryLine1());
        when(orderLineStatusHistoryDao.loadStatusHistoryLatestFirstByOrderIdLineNum(orderId,2))
            .thenReturn(getOrderLineStatusHistoryLine2());

        String lineOneStatus = orderHistoryService.getLostStolenPreviousStatus(orderId,1);
        Assert.assertEquals("0",lineOneStatus);

        String lineTwoStatus = orderHistoryService.getLostStolenPreviousStatus(orderId,2);
        Assert.assertEquals("3",lineTwoStatus);
    }

    @Test
    public void testOrderHistoryFirstImageURL()
        throws ServiceException {
        Program program = getProgram();
        program.getConfig().put(CommonConstants.SHOW_VAR_ORDER_ID, true);
        when(orderHistoryDao.getOrderHistory(user, null, false))
            .thenReturn(getOrdersList());
        List<OrderHistory> orderHistoryList = orderHistoryService.getOrderHistory(user, program, null);

        Assert.assertNotNull(orderHistoryList);
        Assert.assertEquals("ww.palle.com/airbuds.jpg", orderHistoryList.get(0).getImageURL());
        Assert.assertEquals("ww.palle.com/iphone.jpg", orderHistoryList.get(1).getImageURL());
    }

    @Test(expected = ServiceException.class)
    public void testOrderHistoryThrowException() throws ServiceException {
        Program program = getProgram();
        program.getConfig().put(CommonConstants.ORDER_HISTORY_ALL_PROGRAMS_PROGRAM_CONFIG_KEY, "throwException");
        when(orderHistoryDao.getOrderHistory(user, null, false))
                .thenReturn(null);

        orderHistoryService.getOrderHistory(user, program, null);
    }

    @Test
    public void testOrderHistoryWithInvalidOrderLine()
        throws ServiceException {
        Program program = getProgram();
        String imgUrl = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-13-pro-graphite-select?wid=1200&hei=1200&fmt=jpeg&qlt=80&.v=1631652957000&wid=75&hei=75";
        List<Orders> orderHistory = getOrdersList();
        Integer orderId1 = 11111;
        Integer orderId2 = 22222;
        Integer orderId3 = 33333;
        Integer orderId4 = 44444;
        orderHistory.get(0).setOrderId(orderId1);
        orderHistory.get(1).setOrderId(orderId2);

        Orders order3 = populateOrders();
        order3.setOrderId(orderId3);
        Integer lineNum = 1;
        Integer statusId = CommonConstants.ORDER_STATUS_BACK_ORDERED;
        OrderLineItem lineItem1 = populateOrderLineItem(order3.getOrderId(), lineNum, statusId, imgUrl);
        order3.getLineItemList().add(lineItem1);
        lineNum = 2;
        statusId = CommonConstants.ORDER_STATUS_STARTED;
        OrderLineItem lineItem2 = populateOrderLineItem(order3.getOrderId(), lineNum, statusId, imgUrl);
        order3.getLineItemList().add(lineItem2);
        lineNum = 3;
        statusId = CommonConstants.ORDER_STATUS_COMPLETED;
        OrderLineItem lineItem3 = populateOrderLineItem(order3.getOrderId(), lineNum, statusId, imgUrl);
        order3.getLineItemList().add(lineItem3);
        orderHistory.add(order3);

        Orders order4 = populateOrders();
        order4.setOrderId(orderId4);
        lineNum = 1;
        statusId = CommonConstants.ORDER_STATUS_BACK_ORDERED;
        lineItem1 = populateOrderLineItem(order4.getOrderId(), lineNum, statusId, imgUrl);
        order4.getLineItemList().add(lineItem1);
        lineNum = 2;
        statusId = CommonConstants.ORDER_STATUS_SHIPPED;
        lineItem2 = populateOrderLineItem(order4.getOrderId(), lineNum, statusId, imgUrl);
        order4.getLineItemList().add(lineItem2);
        lineNum = 3;
        statusId = CommonConstants.ORDER_STATUS_PROCESSING;
        lineItem3 = populateOrderLineItem(order4.getOrderId(), lineNum, statusId, imgUrl);
        order4.getLineItemList().add(lineItem3);
        lineNum = 4;
        statusId = CommonConstants.ORDER_STATUS_COMPLETED;
        OrderLineItem lineItem4 = populateOrderLineItem(order4.getOrderId(), lineNum, statusId, imgUrl);
        order4.getLineItemList().add(lineItem4);
        orderHistory.add(order4);
        lineNum = 5;
        OrderLineItem lineItem5 = populateOrderLineItem(order4.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MV7N2?wid=1200&hei=1200&fmt=jpeg&qlt=95&.v=1551489688005&wid=75&hei=75");
        lineItem5.setSupplierId(CommonConstants.SUPPLIER_TYPE_SERVICE_PLAN_S);
        order4.getLineItemList().add(lineItem5);
        lineNum = 6;
        OrderLineItem lineItem6 = populateOrderLineItem(order4.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MV7N2?wid=1200&hei=1200&fmt=jpeg&qlt=95&.v=1551489688005&wid=75&hei=75");
        lineItem6.setSupplierId(CommonConstants.SUPPLIER_TYPE_CREDIT_S);
        lineItem6.setB2sItemProfitPrice(0);
        lineItem6.setVarItemProfitPrice(0);
        order4.getLineItemList().add(lineItem6);

        when(orderHistoryDao.getOrderHistory(user, null, false))
            .thenReturn(orderHistory);

        List<OrderHistory> orderHistoryList = orderHistoryService.getOrderHistory(user, program, null);
        Assert.assertNotNull(orderHistoryList);
        Assert.assertEquals(3, orderHistoryList.size());

        OrderHistory orderHistory1 = orderHistoryList.get(0);
        Assert.assertEquals(orderId1, orderHistory1.getOrderId());

        OrderHistory orderHistory2 = orderHistoryList.get(1);
        Assert.assertEquals(orderId2, orderHistory2.getOrderId());

        //Not expected to display an order, if any of it's line item has any of the Status ID (-2,21,24,25,98,11 or 99)
        //3rd Order with 3 order lines are not added to the History as one of the order Line with Order status '-2'
        //So 4th Order with 4 line items are added to History List
        OrderHistory orderHistory3 = orderHistoryList.get(2);
        Assert.assertNotEquals(orderId3, orderHistory3.getOrderId());
        Assert.assertEquals(orderId4, orderHistory3.getOrderId());
    }

    @Test
    public void testOrderHistoryOrderLineProgressUI() throws ServiceException{
        Program program = getProgram();

        Orders order = populateOrders();
        OrderAttributeValue orderAttribute = populateOrderAttribute(Long.valueOf(order.getOrderId()),
                CommonConstants.CREDIT_CARD_LAST_FOUR_DIGIT, "3017");
        order.getOrderAttributeValueList().add(orderAttribute);
        orderAttribute = populateOrderAttribute(Long.valueOf(order.getOrderId()),
                CommonConstants.TIME_ZONE_ID, "Asia/Calcutta");
        order.getOrderAttributeValueList().add(orderAttribute);

        Integer lineNum = 1;
        Integer statusId = -1; //Submitted to Order Service
        OrderLineItem lineItem1 = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-13-pro-graphite-select?wid=1200&hei=1200&fmt=jpeg&qlt=80&.v=1631652957000&wid=75&hei=75");
        lineItem1.getOrderLineAttributes().add(
            populateOrderLineItemAttribute(Long.valueOf(order.getOrderId()), lineNum, CommonConstants.PRODUCT_OPTIONS,
                "iPhone 13 Pro|128GB|Graphite"));
        order.getLineItemList().add(lineItem1);

        lineNum = 2;
        statusId = 0; //Processing
        OrderLineItem lineItem2 = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/38-alu-space-sport-black-nc-1up?wid=1200&hei=1200&fmt=jpeg&qlt=80&.v=1594318693000&wid=75&hei=75");
        lineItem2.setAttr1(CommonConstants.GIFT_ITEM);

        OrderLineItemAttribute orderLineItemAttribute = populateOrderLineItemAttribute(Long.valueOf(order.getOrderId()),
                lineNum, CommonConstants.PRODUCT_OPTIONS, "Aluminium|Black|Space Grey|38mm|Apple Watch|GPS");
        lineItem2.getOrderLineAttributes().add(orderLineItemAttribute);
        orderLineItemAttribute = populateOrderLineItemAttribute(Long.valueOf(order.getOrderId()),
                lineNum, CommonConstants.ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_AMOUNT, "429.0");
        lineItem2.getOrderLineAttributes().add(orderLineItemAttribute);
        orderLineItemAttribute = populateOrderLineItemAttribute(Long.valueOf(order.getOrderId()),
                lineNum, CommonConstants.ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_POINTS, "66000");
        lineItem2.getOrderLineAttributes().add(orderLineItemAttribute);
        orderLineItemAttribute = populateOrderLineItemAttribute(Long.valueOf(order.getOrderId()),
                lineNum, CommonConstants.SHIPPING_AVAILABILITY, new Date().toString());
        lineItem2.getOrderLineAttributes().add(orderLineItemAttribute);
        order.getLineItemList().add(lineItem2);

        lineNum = 3;
        statusId = 3; //Completed
        OrderLineItem lineItem3 = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/apple-tv-4k-hero-select-202104?wid=1200&hei=1200&fmt=jpeg&qlt=80&.v=1619139498000&wid=75&hei=75");
        lineItem3.setAttr1(CommonConstants.DISCOUNTED_GIFT_PERCENTAGE);
        lineItem3.getOrderLineAttributes().add(
            populateOrderLineItemAttribute(Long.valueOf(order.getOrderId()), lineNum, CommonConstants.PRODUCT_OPTIONS,
                "Apple TV|32GB"));
        lineItem3
            .setOrderLineShipmentNotifications(populateOrderLineShipmentNotification("FEDEX","12345","http://fedex.com/Tracking?action=track&tracknumber_list=1Z2214490368758760&cntry_code=us"));
        order.getLineItemList().add(lineItem3);

        lineNum = 4;
        statusId = 6;  //Cmplt-Returned
        OrderLineItem lineItem4 = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/imac-27-cto-hero-202008?wid=1200&hei=1200&fmt=jpeg&qlt=80&.v=1594932848000&wid=75&hei=75");
        lineItem4.setAttr1(CommonConstants.DISCOUNTED_GIFT_POINTS);
        lineItem4.getOrderLineAttributes().add(
            populateOrderLineItemAttribute(Long.valueOf(order.getOrderId()), lineNum, CommonConstants.PRODUCT_OPTIONS,
                "256GB|8GB 2666MHz DDR4 memory|3.1GHz 6-core 10th–generation Intel Core i5, Turbo Boost up to 4.5GHz"));
        order.getLineItemList().add(lineItem4);

        lineNum = 5;
        statusId = 7;  //Cmplt-Cancelled
        OrderLineItem lineItem5 = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MV7N2?wid=1200&hei=1200&fmt=jpeg&qlt=95&.v=1551489688005&wid=75&hei=75");
        order.getLineItemList().add(lineItem5);

        lineNum = 6;
        statusId = 3;  //Completed
        OrderLineItem lineItem6 = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MV7N2?wid=1200&hei=1200&fmt=jpeg&qlt=95&.v=1551489688005&wid=75&hei=75");
        lineItem6.setSupplierId(CommonConstants.SUPPLIER_TYPE_SERVICE_PLAN_S);
        order.getLineItemList().add(lineItem6);

        lineNum = 7;   //Completed
        OrderLineItem lineItem7 = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MV7N2?wid=1200&hei=1200&fmt=jpeg&qlt=95&.v=1551489688005&wid=75&hei=75");
        lineItem7.setSupplierId(CommonConstants.SUPPLIER_TYPE_CREDIT_S);
        lineItem7.setB2sItemProfitPrice(0);
        lineItem7.setVarItemProfitPrice(0);
        order.getLineItemList().add(lineItem7);

        when(orderHistoryDao.getOrderHistoryDetails(user, 10, false))
            .thenReturn(order);//Not var_order_id
        when(paymentDao.getSaleDetails(10L))
            .thenReturn(createPaymentEntities());

        when(applicationProperties.getProperty(any(String.class))).thenReturn("KEY_VALUE");
        when(varProgramMessageService.getMessages(any(), any(), any())).thenReturn(new Properties());

        OrderHistory orderHistory = orderHistoryService.getOrderHistoryDetails(user, "10", program);

        Assert.assertNotNull(orderHistory);
        Assert.assertNotNull(orderHistory.getLineItems());

        OrderHistory.OrderLineInfo orderPlaced = orderHistory.getLineItems().get(0);
        Assert.assertNotNull(orderPlaced.getOrderLineProgress());
        Assert.assertTrue(OrderHistoryService.OS_ORDER_PLACED.equalsIgnoreCase(orderPlaced.getOrderLineProgress().getStatus()));
        Assert.assertFalse(orderPlaced.getIsGift());

        OrderHistory.OrderLineInfo processing = orderHistory.getLineItems().get(1);
        Assert.assertNotNull(processing.getOrderLineProgress());
        Assert.assertTrue(OrderHistoryService.OS_PROCESSING.equalsIgnoreCase(processing.getOrderLineProgress().getStatus()));
        Assert.assertTrue(processing.getIsGift());

        OrderHistory.OrderLineInfo shipped = orderHistory.getLineItems().get(2);
        Assert.assertNotNull(shipped.getOrderLineProgress());
        Assert.assertTrue(OrderHistoryService.OS_SHIPPED.equalsIgnoreCase(shipped.getOrderLineProgress().getStatus()));
        Assert.assertTrue(shipped.getIsGift());

        OrderHistory.OrderLineInfo returned = orderHistory.getLineItems().get(3);
        Assert.assertNotNull(returned.getOrderLineProgress());
        Assert.assertTrue(OrderHistoryService.OS_RETURNED.equalsIgnoreCase(returned.getOrderLineProgress().getStatus()));
        Assert.assertTrue(returned.getIsGift());

        OrderHistory.OrderLineInfo cancelled = orderHistory.getLineItems().get(4);
        Assert.assertNotNull(cancelled.getOrderLineProgress());
        Assert.assertTrue(OrderHistoryService.OS_CANCELLED.equalsIgnoreCase(cancelled.getOrderLineProgress().getStatus()));
        Assert.assertFalse(cancelled.getIsGift());

        OrderHistory.OrderLineInfo servicePlan = orderHistory.getLineItems().get(5);
        Assert.assertNotNull(servicePlan.getOrderLineProgress());
        Assert.assertTrue(OrderHistoryService.OS_SHIPPED.equalsIgnoreCase(servicePlan.getOrderLineProgress().getStatus()));
        Assert.assertFalse(servicePlan.getIsGift());
    }

    @Test
    public void testOrderHistoryWithRefundSummaryPointsAndCash()
        throws ServiceException {
        Program program = getProgram();

        Orders order = populateOrders();
        order.getOrderAttributeValueList().add(populateOrderAttribute(Long.valueOf(order.getOrderId()),
            CommonConstants.CREDIT_CARD_LAST_FOUR_DIGIT, "3017"));

        List<OrderAttributeValue> orderAttributeValues = new ArrayList<>();
        orderAttributeValues.add(populateOrderAttribute(Long.valueOf(order.getOrderId()),
                CommonConstants.TIME_ZONE_ID, "America/New_York"));
        order.setOrderAttributeValueList(orderAttributeValues);

        Integer lineNum = 1;
        Integer statusId = 7;  //Cmplt-Cancelled
        when(applicationProperties.getProperty(CommonConstants.UA_STATUS + (statusId)))
            .thenReturn(CommonConstants.ORDER_STATUS_UA_CANCELLED);
        OrderLineItem lineItem5 = populateOrderLineItem(order.getOrderId(), lineNum, statusId,
            "https://store.storeimages.cdn-apple.com/4982/as-images.apple" +
                ".com/is/MV7N2?wid=1200&hei=1200&fmt=jpeg&qlt=95&.v=1551489688005&wid=75&hei=75");
        order.getLineItemList().add(lineItem5);

        when(orderHistoryDao.getOrderHistoryDetails(user, 10, false))
            .thenReturn(order);
        when(paymentDao.getSaleDetails(10L))
            .thenReturn(createPaymentEntities());

        OrderHistory orderHistory = orderHistoryService.getOrderHistoryDetails(user, "10", program);
        Assert.assertNotNull(orderHistory);
        Assert.assertNotNull(orderHistory.getLineItems());

        RefundSummary refundSummary = orderHistory.getRefundSummary();
        ReturnLineItem lineItem = refundSummary.getLineItems().get(0);
        Price expectedItemPrice = new Price(99.0D, "USD", 3);
        Price expectedRefund = new Price(1.03D, "USD", 3);

        Double totalAmount = refundSummary.getTotal().getAmount();
        Assert.assertNotNull(refundSummary);
        Assert.assertEquals(1, refundSummary.getLineItems().size());
        Assert.assertEquals("ipod", lineItem.getProductName());
        Assert.assertEquals(expectedItemPrice.getAmount(), lineItem.getItemPrice().getAmount());
        Assert.assertEquals(expectedItemPrice.getPoints(), lineItem.getItemPrice().getPoints());
        Assert.assertEquals(2, lineItem.getTaxPrice().getPoints());
        Assert.assertEquals(java.util.Optional.of(99.0), java.util.Optional.of(totalAmount));
        Assert.assertEquals(5, refundSummary.getTotal().getPoints());
        Assert.assertEquals(expectedRefund.getAmount(), refundSummary.getRefunds().getAmount());
        Assert.assertEquals(expectedRefund.getPoints(), refundSummary.getRefunds().getPoints());
    }

    @Test
    public void testRefundSummaryPointsOnly()
        throws ServiceException {
        Program program = getProgram();
        User user = new User();
        user.setVarId("FDR");
        mockOrderLineItems(user);

        OrderHistory orderHistory = orderHistoryService.getOrderHistoryDetails(user, "10", program);
        RefundSummary refundSummary = orderHistory.getRefundSummary();
        Price expectedTotalPrice = new Price(33.0, "USD", 35687);
        Price expectedRefund = new Price(0D, "USD", 35679);


        Assert.assertEquals(expectedTotalPrice.getAmount(), refundSummary.getTotal().getAmount());
        Assert.assertEquals(expectedTotalPrice.getPoints(), refundSummary.getTotal().getPoints());
        Assert.assertEquals(expectedRefund.getAmount(), refundSummary.getRefunds().getAmount());
        Assert.assertEquals(expectedRefund.getPoints(), refundSummary.getRefunds().getPoints());
    }

    @Test
    public void testOrderHistoryDetailsForUnBundledType()
        throws ServiceException {
        Program program = getProgram();
        User user = new User();
        user.setVarId("UA");
        user.setProgramId("MP");
        mockOrderLineItems(user);

        OrderHistory orderHistory = orderHistoryService.getOrderHistoryDetails(user, "10", program);

        Double unBundledItemItem1PriceAmount = orderHistory.getLineItems().get(0).getUnitPrice().getAmount();
        Double unBundledItemItem2PriceAmount = orderHistory.getLineItems().get(1).getUnitPrice().getAmount();
        Double unBundledItemItem3PriceAmount = orderHistory.getLineItems().get(2).getUnitPrice().getAmount();
        Double expectedPriceTotalAmount = unBundledItemItem1PriceAmount + unBundledItemItem2PriceAmount + unBundledItemItem3PriceAmount;

        //Validate if displayPrice.amount in DB used as it is, if available
        Assert.assertEquals(Double.valueOf(143.2), unBundledItemItem1PriceAmount);

        //Validate if supplier_item_price in DB used as it is in case of Unbundled & UnPromoted line item price
        Assert.assertEquals(Double.valueOf(23.0), unBundledItemItem2PriceAmount);

        //Validate if promoted item prices, promoted tax & fees applied properly to lineItem
        Assert.assertEquals(Double.valueOf(9.4), unBundledItemItem3PriceAmount);

        //Validate subTotal in OrderHistory & sum of all price Total from OrderLine items list are equal
        Assert.assertEquals(orderHistory.getOrderSubTotal().getAmount(), expectedPriceTotalAmount);
    }

    @Test
    public void testOrderHistoryDetailsForBundledType()
        throws ServiceException {
        Program program = getProgram();
        User user = new User();
        user.setVarId("Delta");
        mockOrderLineItems(user);

        program.setBundledPricingOption(BundledPricingOption.BUNDLED);
        OrderHistory bundledOrderHistory = orderHistoryService.getOrderHistoryDetails(user, "10", program);
        Double bundledItem1PriceAmount = bundledOrderHistory.getLineItems().get(0).getUnitPrice().getAmount();
        Double bundledItem2PriceAmount = bundledOrderHistory.getLineItems().get(1).getUnitPrice().getAmount();
        Double bundledItem3PriceAmount = bundledOrderHistory.getLineItems().get(2).getUnitPrice().getAmount();
        Double expectedBundledPriceTotalAmount =
            bundledItem1PriceAmount + bundledItem2PriceAmount + bundledItem3PriceAmount;

        //Validate if var_order_line_price in DB used as it is in case of Bundled & UnPromoted line item price
        Assert.assertEquals(Double.valueOf(25.0), bundledItem2PriceAmount);

        //Validate if discounted_var_order_Line values is used for promoted item prices and Bundled Cases
        Assert.assertEquals(Double.valueOf(10), bundledItem3PriceAmount);

        //Validate subTotal in BundledOrderHistory & sum of all price Total from OrderLine items list are equal
        Assert.assertEquals(bundledOrderHistory.getOrderSubTotal().getAmount(), expectedBundledPriceTotalAmount);
    }

    @Test
    public void testRefundSummaryPointsAndCashQuantity()
        throws ServiceException {
        Program program = getProgram();
        Orders order = populateOrders();

        Integer lineNum = 1;
        Integer statusId = 7;  //Cmplt-Cancelled
        when(applicationProperties.getProperty(CommonConstants.UA_STATUS + (statusId)))
            .thenReturn(CommonConstants.ORDER_STATUS_UA_CANCELLED);
        OrderLineItem lineItem1 = populateOrderLineItem(order.getOrderId(), lineNum, statusId,
            "img", "skuAirTag",
            "AirTAG Pack 1",23345D,2300,30.0,13345,2.0);
        lineItem1.setQuantity(2);
        order.getLineItemList().add(lineItem1);

          when(orderHistoryDao.getOrderHistoryDetails(user, 10, false))
            .thenReturn(order);
        when(paymentDao.getSaleDetails(10L))
            .thenReturn(createPaymentEntities());

        OrderHistory orderHistory = orderHistoryService.getOrderHistoryDetails(user, "10", program);
        RefundSummary refundSummary = orderHistory.getRefundSummary();
        Price expectedTotalPrice = new Price(46.0, "USD", 46694);
        Price expectedRefund = new Price(30D, "USD", 13345);

        Assert.assertEquals(expectedTotalPrice.getAmount(), refundSummary.getTotal().getAmount());
        Assert.assertEquals(expectedTotalPrice.getPoints(), refundSummary.getTotal().getPoints());
        Assert.assertEquals(expectedRefund.getAmount(), refundSummary.getRefunds().getAmount());
        Assert.assertEquals(expectedRefund.getPoints(), refundSummary.getRefunds().getPoints());
    }

    @Test
    public void testOrderHistoryWithMultipleShipmentScenario() throws ServiceException{
        Program program = getProgram();
        Orders order = populateOrders();

        Integer lineNum = 1;
        Integer statusId = 3; //Completed
        OrderLineItem lineItem = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/apple-tv-4k-hero-select-202104?wid=1200&hei=1200&fmt=jpeg&qlt=80&.v=1619139498000&wid=75&hei=75");
        lineItem.getOrderLineAttributes().add(
            populateOrderLineItemAttribute(Long.valueOf(order.getOrderId()), lineNum, CommonConstants.PRODUCT_OPTIONS,
                "Apple TV|32GB"));

        Integer id = 111;
        String trackingNumber = String.valueOf(id);
        String carrierName = "FEDEX" + trackingNumber;
        List<OrderLineShipmentNotification> orderLineShipmentNotifications = populateOrderLineShipmentNotification(carrierName, trackingNumber,"http://fedex.com/Tracking?action=track&tracknumber_list=1Z2214490368758760&cntry_code=us");
        id = 222;
        trackingNumber = String.valueOf(id);
        carrierName = "FEDEX" + trackingNumber;
        OrderLineShipmentNotification orderLineShipmentNotification = getOrderLineShipmentNotification(carrierName, trackingNumber,"http://fedex.com/Tracking?action=track&tracknumber_list=1Z2214490368758760&cntry_code=us");
        orderLineShipmentNotification.setId(id);
        orderLineShipmentNotifications.add(orderLineShipmentNotification);
        id = 333;
        trackingNumber = String.valueOf(id);
        carrierName = "FEDEX" + trackingNumber;
        orderLineShipmentNotification = getOrderLineShipmentNotification(carrierName, trackingNumber,"http://fedex.com/Tracking?action=track&tracknumber_list=1Z2214490368758760&cntry_code=us");
        orderLineShipmentNotification.setId(id);
        orderLineShipmentNotifications.add(orderLineShipmentNotification);
        id = 555;
        trackingNumber = String.valueOf(id);
        carrierName = "FEDEX" + trackingNumber;
        orderLineShipmentNotification = getOrderLineShipmentNotification(carrierName, trackingNumber,"http://fedex.com/Tracking?action=track&tracknumber_list=1Z2214490368758760&cntry_code=us");
        orderLineShipmentNotification.setId(id);
        orderLineShipmentNotifications.add(orderLineShipmentNotification);
        id = 444;
        trackingNumber = String.valueOf(id);
        carrierName = "FEDEX" + trackingNumber;
        orderLineShipmentNotification = getOrderLineShipmentNotification(carrierName, trackingNumber,"http://fedex.com/Tracking?action=track&tracknumber_list=1Z2214490368758760&cntry_code=us");
        orderLineShipmentNotification.setId(id);
        orderLineShipmentNotifications.add(orderLineShipmentNotification);
        lineItem.setOrderLineShipmentNotifications(orderLineShipmentNotifications);
        order.getLineItemList().add(lineItem);

        when(varProgramMessageService.getMessages(any(), any(), any())).thenReturn(new Properties());
        when(orderHistoryDao.getOrderHistoryDetails(user, 10, false))
            .thenReturn(order);
        when(applicationProperties.getProperty(any(String.class))).thenReturn("KEY_VALUE");

        OrderHistory orderHistory = orderHistoryService.getOrderHistoryDetails(user, "10", program);

        Assert.assertNotNull(orderHistory);
        Assert.assertNotNull(orderHistory.getLineItems());

        OrderHistory.OrderLineInfo shipped = orderHistory.getLineItems().get(0);
        Assert.assertNotNull(shipped.getOrderLineProgress());
        Assert.assertTrue(OrderHistoryService.OS_SHIPPED.equalsIgnoreCase(shipped.getOrderLineProgress().getStatus()));
        OrderHistory.ShipmentDeliveryInfo shipmentDeliveryInfo = shipped.getShipmentInfo();
        Assert.assertNotNull(shipmentDeliveryInfo);
        //Picking Latest Shipment Information based on ID
        Assert.assertTrue(shipmentDeliveryInfo.getCarrierName().equalsIgnoreCase("FEDEX555"));
    }

    @Test
    public void testOrderHistoryLineItemThrowException() throws ServiceException {
        Program program = getProgram();
        Orders order = populateOrders();

        Integer lineNum = 1;
        Integer statusId = 3;  //Completed
        when(applicationProperties.getProperty(CommonConstants.UA_STATUS + (statusId)))
                .thenReturn(CommonConstants.ORDER_STATUS_COMPLETED_DESC);
        OrderLineItem lineItem1 = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "img1", "skuIphone",
                "Iphone 5s 64 GB", 11.0, 0, 0.0, 0, 2.0);
        OrderLineItemAttribute orderLineItemAttribute = new OrderLineItemAttribute();
        orderLineItemAttribute.setName(CommonConstants.ORDER_ATTR_KEY_DISPLAY_PRICE_AMOUNT);
        orderLineItemAttribute.setValue("throwException");
        lineItem1.getOrderLineAttributes().add(orderLineItemAttribute);
        order.getLineItemList().add(lineItem1);

        when(orderHistoryDao.getOrderHistory(any(), anyInt(), anyBoolean()))
                .thenReturn(List.of(order));

        List<OrderHistory> orderHistories = orderHistoryService.getOrderHistory(user, program, 1);

        Assert.assertNotNull(orderHistories);
        Assert.assertSame(1, orderHistories.size());
    }

    Program getProgram() {
        Program program = new Program();
        program.setVarId("RBC");
        program.setProgramId("acquisition");
        return program;
    }

    private void mockOrderLineItems(final User user) {
        Orders order = populateOrders();

        Integer lineNum = 1;
        Integer statusId = 3;  //Completed
        when(applicationProperties.getProperty(CommonConstants.UA_STATUS + (statusId)))
            .thenReturn(CommonConstants.ORDER_STATUS_COMPLETED_DESC);
        OrderLineItem lineItem1 = populateOrderLineItem(order.getOrderId(), lineNum, statusId, "img1", "skuIphone",
            "Iphone 5s 64 GB", 11.0, 0, 0.0, 0, 2.0);
        OrderLineItemAttribute orderLineItemAttribute = new OrderLineItemAttribute();
        orderLineItemAttribute.setName(CommonConstants.ORDER_ATTR_KEY_DISPLAY_PRICE_AMOUNT);
        orderLineItemAttribute.setValue("143.2");
        lineItem1.getOrderLineAttributes().add(orderLineItemAttribute);
        order.getLineItemList().add(lineItem1);

        lineNum = 2;
        statusId = 7;  //Cmplt-Cancelled
        when(applicationProperties.getProperty(CommonConstants.UA_STATUS + (statusId)))
            .thenReturn(CommonConstants.ORDER_STATUS_UA_CANCELLED);
        OrderLineItem lineItem2 = populateOrderLineItem(order.getOrderId(), lineNum, statusId,
            "img", "skuAirTag",
            "AirTAG Pack 1", 23345D, 2300, 0.0, 23345, 2.0);
        lineItem2.setVarOrderLinePrice(2500);
        order.getLineItemList().add(lineItem2);

        lineNum = 3;
        statusId = 7;  //Cmplt-Cancelled
        when(applicationProperties.getProperty(CommonConstants.UA_STATUS + (statusId)))
            .thenReturn(CommonConstants.ORDER_STATUS_UA_CANCELLED);
        OrderLineItem lineItem3 = populateOrderLineItem(order.getOrderId(), lineNum, statusId,
            "img", "skuImac",
            "IMac 24", 12334D, 1200, 0.0, 12334, 6.0);
        lineItem3.setDiscountedTaxes(30);
        lineItem3.setDiscountedFees(30);
        lineItem3.setDiscountedVarOrderLinePrice(1000);
        order.getLineItemList().add(lineItem3);

        when(orderHistoryDao.getOrderHistoryDetails(user, 10, false))
            .thenReturn(order);
        when(paymentDao.getSaleDetails(10L))
            .thenReturn(createPaymentEntities());
    }

    private List<PaymentEntity> createPaymentEntities(){
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setFirstName("ABC");
        paymentEntity.setLastName("DEF");
        paymentEntity.setAddress1("AddressLine1 testing AddressLine2 testing");
        paymentEntity.setCity("citytesting");
        paymentEntity.setState("CT");
        paymentEntity.setZip("12-34");
        paymentEntity.setCountry("United States");
        return List.of(paymentEntity);
    }

    private Orders createTestOrder() {
        Orders order = populateOrders();
        order.setOrderDate(new Date());
        OrderLineItem lineItem = populateOrderLineItem(order.getOrderId(), 1, 12, "ww.palle.com/ipod.jpg");
        OrderLineItem lineItem2 = populateOrderLineItem(order.getOrderId(), 2, 12, "ww.palle.com/airbuds.jpg");
        order.getLineItemList().add(lineItem);
        order.getLineItemList().add(lineItem2);
        return order;
    }

    private Orders createTestOrdersWithImg(String imgURL) {
        Orders order = populateOrders();
        OrderLineItem lineItem = populateOrderLineItem(order.getOrderId(), 1, 12, imgURL);
        order.getLineItemList().add(lineItem);
        return order;

    }

    private OrderAttributeValue populateOrderAttribute(final Long orderId, final String name, final String value){
        OrderAttributeValue orderAttributeCCLast4Value = new OrderAttributeValue();
        orderAttributeCCLast4Value.setOrderId(orderId);
        orderAttributeCCLast4Value.setName(name);
        orderAttributeCCLast4Value.setValue(value);
        return orderAttributeCCLast4Value;
    }

    private Orders populateOrders() {
        Orders order = new Orders();
        order.setOrderDate(new Date());
        order.setOrderId(10);
        order.setLanguageCode("en");
        order.setCountryCode("AU");
        order.setCurrencyCode("AUD");
        order.setUserPoints(1234);
        order.setOrderAttributeValueList(new ArrayList<>());
        order.setLineItemList(new ArrayList<>());
        return order;
    }

    private OrderLineItem populateOrderLineItem(final Integer orderId, final Integer lineNum, final Integer statusId,
        final String imgUrl, final String sku, final String name,final Double itemPoints,
        final Integer supplierItemPrice,  final Double adjPriceAmount,
        final Integer adjPointAmount, final Double taxPoints){
        OrderLineItem lineItem = createOrderLineItem(orderId, lineNum, statusId, imgUrl);

        lineItem.setSku(sku);
        lineItem.setName(name);
        lineItem.setSupplierItemPrice(supplierItemPrice);
        lineItem.setItemPoints(itemPoints);

        createOrderLineAdjustment(lineItem, adjPriceAmount, adjPointAmount);
        lineItem.setTaxPoints(taxPoints);
        lineItem.setOrderLineAttributes(new ArrayList<>());

        return lineItem;
    }

    private void createOrderLineAdjustment(final OrderLineItem lineItem, final Double priceAmount,
        final Integer pointAmount) {
        OrderLineAdjustment orderLineAdjustment = new OrderLineAdjustment();
        orderLineAdjustment.setAdjustmentType("P");
        orderLineAdjustment.setPriceAmount(priceAmount);
        orderLineAdjustment.setPointAmount(pointAmount);
        orderLineAdjustment.setStatusId(1);
        List<OrderLineAdjustment> lineAdjustmentList = new ArrayList<>();
        lineAdjustmentList.add(orderLineAdjustment);
        lineItem.setOrderLineAdjustmentList(lineAdjustmentList);
    }

    private OrderLineItem createOrderLineItem(final Integer orderId, final Integer lineNum, final Integer statusId,
                                              final String imgUrl) {
        OrderLineItem lineItem = new OrderLineItem();
        lineItem.setSupplierId(CommonConstants.APPLE_SUPPLIER_ID_STRING);
        lineItem.setQuantity(1);
        lineItem.setImageURL(imgUrl);
        lineItem.setSupplierTaxPrice(2);
        lineItem.setOrderLinePoints(12);
        lineItem.setSupplierShippingPrice(12);
        lineItem.setShippingPoints(9D);
        lineItem.setShippingMethod("Domstic Shipping");

        OrderLineItemId orderLineItemId = new OrderLineItemId();
        orderLineItemId.setOrderId(orderId);
        orderLineItemId.setLineNum(lineNum);
        lineItem.setId(orderLineItemId);

        OrderLineStatus orderLineStatus = new OrderLineStatus();
        orderLineStatus.setStatusId(statusId);
        lineItem.setStatus(orderLineStatus);

        return lineItem;
    }


    private OrderLineItem populateOrderLineItem(final Integer orderId, final Integer lineNum, final Integer statusId,
                                                final String imgUrl) {
        return populateOrderLineItem(orderId, lineNum, statusId, imgUrl, "sku0", "ipod", 3.56, 9900, 1.03d, 3, 2.0);
    }

    private OrderLineItemAttribute populateOrderLineItemAttribute(final Long orderId, final Integer lineNum,
        final String name, final String value) {
        OrderLineItemAttribute orderLineItemAttribute = new OrderLineItemAttribute();
        orderLineItemAttribute.setOrderId(orderId);
        orderLineItemAttribute.setLineNum(lineNum);
        orderLineItemAttribute.setName(name);
        orderLineItemAttribute.setValue(value);
        return orderLineItemAttribute;
    }

    private List<OrderLineShipmentNotification> populateOrderLineShipmentNotification(final String shippingCarrier,
        final String trackingNumber, final String trackingUrl){
        List<OrderLineShipmentNotification> orderLineShipmentNotifications = new ArrayList<>();
        OrderLineShipmentNotification orderLineShipmentNotification =
            getOrderLineShipmentNotification(shippingCarrier, trackingNumber, trackingUrl);
        orderLineShipmentNotifications.add(orderLineShipmentNotification);
        return orderLineShipmentNotifications;
    }

    private OrderLineShipmentNotification getOrderLineShipmentNotification(final String shippingCarrier,
        final String trackingNumber, final String trackingUrl) {
        OrderLineShipmentNotification orderLineShipmentNotification = new OrderLineShipmentNotification();
        orderLineShipmentNotification.setId(111);
        orderLineShipmentNotification.setShippingCarrier(shippingCarrier);
        orderLineShipmentNotification.setTrackingNumber(trackingNumber);
        orderLineShipmentNotification.setTrackingUrl(trackingUrl);
        return orderLineShipmentNotification;
    }

    private List<Orders> getOrdersList() {
        List<Orders> orderList = new ArrayList<>();
        Orders order = createTestOrdersWithImg("ww.palle.com/airbuds.jpg");
        orderList.add(order);
        List<OrderAttributeValue> orderAttributeValues = new ArrayList<>();
        orderAttributeValues.add(populateOrderAttribute(Long.valueOf(order.getOrderId()),
                CommonConstants.TIME_ZONE_ID, "America/New_York"));
        order.setOrderAttributeValueList(orderAttributeValues);

        orderList.add(createTestOrdersWithImg("ww.palle.com/iphone.jpg"));
        return orderList;
    }

    private List<OrderLineStatusHistory> getOrderLineStatusHistoryLine1(){
        List<OrderLineStatusHistory> olsdhList = new ArrayList<>();
        OrderLineStatusHistory olsh = new OrderLineStatusHistory();
        OrderLineStatusHistoryId olshId = new OrderLineStatusHistoryId();
        OrderLineStatus ols = new OrderLineStatus();

        olshId.setLineNum(1);
        olshId.setModifiedDate(Timestamp.valueOf("2020-10-20 10:10:03.0"));
        olshId.setOrderId(123L);
        ols.setStatusId(CommonConstants.ORDER_STATUS_LOST_STOLEN);
        ols.setStatusDesc("LostStolen");
        olshId.setStatus(ols);
        olsh.setId(olshId);
        olsdhList.add(olsh);

        olsh = new OrderLineStatusHistory();
        olshId = new OrderLineStatusHistoryId();
        olshId.setLineNum(1);
        olshId.setModifiedDate(Timestamp.valueOf("2020-10-20 10:10:02.0"));
        olshId.setOrderId(123L);
        ols = new OrderLineStatus();
        ols.setStatusId(CommonConstants.ORDER_STATUS_PROCESSING);
        ols.setStatusDesc("Processing");
        olshId.setStatus(ols);
        olsh.setId(olshId);
        olsdhList.add(olsh);

        olsh = new OrderLineStatusHistory();
        olshId = new OrderLineStatusHistoryId();
        olshId.setLineNum(1);
        olshId.setModifiedDate(Timestamp.valueOf("2020-10-20 10:10:01.0"));
        olshId.setOrderId(123L);
        ols = new OrderLineStatus();
        ols.setStatusId(CommonConstants.ORDER_STATUS_STARTED);
        ols.setStatusDesc("Started");
        olshId.setStatus(ols);
        olsh.setId(olshId);
        olsdhList.add(olsh);

        return olsdhList;
    }

    private List<OrderLineStatusHistory> getOrderLineStatusHistoryLine2(){
        List<OrderLineStatusHistory> olsdhList = new ArrayList<>();
        OrderLineStatusHistory olsh = new OrderLineStatusHistory();
        OrderLineStatusHistoryId olshId = new OrderLineStatusHistoryId();
        OrderLineStatus ols = new OrderLineStatus();

        olshId.setLineNum(2);
        olshId.setModifiedDate(Timestamp.valueOf("2020-10-20 10:10:04.0"));
        olshId.setOrderId(123L);
        ols.setStatusId(CommonConstants.ORDER_STATUS_COMPLETED);
        ols.setStatusDesc("Completed");
        olshId.setStatus(ols);
        olsh.setId(olshId);
        olsdhList.add(olsh);

        olsh = new OrderLineStatusHistory();
        olshId = new OrderLineStatusHistoryId();
        olshId.setLineNum(2);
        olshId.setModifiedDate(Timestamp.valueOf("2020-10-20 10:10:03.0"));
        olshId.setOrderId(123L);
        ols = new OrderLineStatus();
        ols.setStatusId(CommonConstants.ORDER_STATUS_SHIPPED);
        ols.setStatusDesc("Shipped");
        olshId.setStatus(ols);
        olsh.setId(olshId);
        olsdhList.add(olsh);

        olsh = new OrderLineStatusHistory();
        olshId = new OrderLineStatusHistoryId();
        olshId.setLineNum(2);
        olshId.setModifiedDate(Timestamp.valueOf("2020-10-20 10:10:02.0"));
        olshId.setOrderId(123L);
        ols = new OrderLineStatus();
        ols.setStatusId(CommonConstants.ORDER_STATUS_LOST_STOLEN);
        ols.setStatusDesc("LostStolen");
        olshId.setStatus(ols);
        olsh.setId(olshId);
        olsdhList.add(olsh);

        olsh = new OrderLineStatusHistory();
        olshId = new OrderLineStatusHistoryId();
        olshId.setLineNum(2);
        olshId.setModifiedDate(Timestamp.valueOf("2020-10-20 10:10:01.0"));
        olshId.setOrderId(123L);
        ols = new OrderLineStatus();
        ols.setStatusId(CommonConstants.ORDER_STATUS_STARTED);
        ols.setStatusDesc("Started");
        olshId.setStatus(ols);
        olsh.setId(olshId);
        olsdhList.add(olsh);

        return olsdhList;
    }
}
