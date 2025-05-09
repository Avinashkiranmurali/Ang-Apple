package com.b2s.rewards.apple.controller

import com.b2s.apple.services.AppSessionInfo
import com.b2s.apple.services.CartService
import com.b2s.apple.services.ProgramService
import com.b2s.common.services.exception.ServiceException
import com.b2s.common.services.exception.ServiceExceptionEnums
import com.b2s.db.model.Order
import com.b2s.db.model.OrderLine
import com.b2s.db.model.OrderLineAttribute
import com.b2s.rewards.apple.exceptionhandler.CustomRestExceptionHandler
import com.b2s.rewards.apple.model.*
import com.b2s.rewards.apple.util.AppleUtil
import com.b2s.rewards.apple.util.BasicAuthValidation
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.rewards.service.ShoppingCartService
import com.b2s.shop.common.User
import com.b2s.shop.common.order.OrderTransactionManager
import com.b2s.shop.common.order.var.VarOrderManagerHolder
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.collections.map.HashedMap
import org.junit.Assert
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Unroll

/**
* Created by srukmagathan on 3/3/2016.
**/
class PlaceOrderControllerSpec extends Specification{

    private CartService cartService;

    private ShoppingCartService shoppingCartService;

    private OrderTransactionManager manager;

    private Properties applicationProperties;

    private MockMvc mvc;

    private MockHttpSession session;

    private User user;

    private PlaceOrderController controller;

    private BasicAuthValidation basicAuthValidation;

    private VarOrderManagerHolder varOrderManagerHolder;

    private ProgramService programService;

    private AppSessionInfo appSessionInfo;

    def setup(){

        cartService=Mock()
        shoppingCartService=Mock()
        manager=Mock()
        applicationProperties=Mock()
        session=Mock()
        basicAuthValidation=Mock()
        varOrderManagerHolder=Mock()
        programService=Mock()
        appSessionInfo=Mock()

        controller=new PlaceOrderController(cartService:cartService,basicAuthValidation:basicAuthValidation,
                    manager:manager,applicationProperties:applicationProperties,
                varOrderManagerHolder:varOrderManagerHolder,programService:programService,
                orderErrorGenericUrl:"../../merchandise/landing.jsp#/store/cart?return=error",
                appSessionInfo:appSessionInfo)
        user=new User()

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build()
    }

    def cleanup(){

        cartService=null
        shoppingCartService=null
        manager=null
        applicationProperties=null
        session=null
        controller=null
        mvc=null
        user=null
    }

//    @Unroll
//    def "placeOrderRedirect - Covered few scenario and it is described next to expected column"(){
//
//        given:
//        ShoppingCart shoppingCart=Mock()
//        Cart cart=varCart   // varCart is input variable passed by data tables
//
//        cart.getShippingAddress().setFirstName("Sandy")
//        if(cart.getCartItems().size()!=0)
//        cart.getCartItems().get(0).setSupplierId(200)
//        Program program = new Program()
//        program.setVarId("1")
//        program.setProgramId("1")
//        Map<String, Object> configs = new HashMap<>()
//        configs.put("catalog_id","apple")
//        program.setConfig(configs)
//        List<com.b2s.rewards.apple.model.PaymentOption> payments = new ArrayList<>()
//        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption()
//        paymentOption.setPaymentOption("POINTS")
//        paymentOption.setSupplementaryPaymentType("VARIABLE")
//        payments.add(paymentOption)
//        program.setPayments(payments)
//
//        session.getAttribute("PROGRAM") >> program
//        session.getAttribute("USER") >> getUser()
//        session.getAttribute("APPLE_CART") >> cart
//        cartService.getShoppingCart(_) >>shoppingCart
//        manager.placeMyOrder(_,_,_) >> orderStatus  // orderStatus is input variable passed by data tables
//
//        when:
//        def result=mvc.perform(MockMvcRequestBuilders.get("/order/placeOrderRedirect")
//                        .param("last4","4567")
//                        .param("ccType","428")
//                        .session(session))
//                        .andReturn()
//
//        then:
//        result.response.status==status  // status is output variable passed by data tables
//        result.response.getHeaderValue("Location").equals(expectedRedirect) // expectedRedirect is output variable passed by data tables
//
//        where:  // data table
//        varCart     |orderStatus        ||status||expectedRedirect
//
//        getCart()   |getOrderStatus()   ||302   ||"../../merchandise/landing.jsp#/store/confirmation"        // Success scenario - Order placed successfully in both B2S and via VIS
//        new Cart()  |getOrderStatus()   ||302   ||"../../merchandise/landing.jsp#/store/cart?return=error"   // Failed - Cart doesn't have any item to place order
//        getCart()   |new Exception()    ||302   ||"../../merchandise/landing.jsp#/store/cart?return=error"   // When placeOrder ends up with error saying unable to insert in B2S DB or with VIS.
//
//    }

//COMMENTING AS MORE MOCKS NEED TO BE ADDED
//    def "currentOrderInformation - returns order information successfully"(){
//
//        given:
//        session.getAttribute("USER") >> getUser()
//        session.getAttribute("orderInformation") >> orderStatus // orderStatus is input variable passed by data table
//
//        when:
//        def result=mvc.perform(MockMvcRequestBuilders.get("/order/orderInformation")
//                .session(session))
//                .andReturn()
//
//        then:
//        result.response.status==status
//        result.response.contentAsString.contains(varOrderId)
//
//        where:  // data table
//        orderStatus      |status|varOrderId
//        getOrderStatus() |200   |getOrderStatus().varOrderId    // When we have OrderStatus in session then we get order details in response having varOrderId in it
//        null             |400   |""                             // If we don't have OrderStatus in session then we get empty response
//    }


    @Unroll
    def "placeOrder - with empty cart"(){

        given:
        appSessionInfo.getSessionCart() >> cart
        session.getAttribute("USER") >> getUser()
        session.getAttribute("APPLE_CART") >> cart

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post("/order/placeOrder")
                .content(AppleUtil.asJsonString(placeOrderRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .session(session))
                .andReturn()

        then:
        result.response.status==status
        result.response.contentAsString.contains(keyToCheckInRepsone)

        where:
        cart       | status | keyToCheckInRepsone

        new Cart() | 204    | "Cart is Empty now"

    }

    def "placeOrder - item doesn't exist or modified by other session in cart"(){

        given:
        Cart cart=getCart();
        ShoppingCart shoppingCart=Mock()
        cart.shippingAddress.firstName="Santhosh"
        cart.shippingAddress.isValidAddress = true
        cart.cartModifiedBySystem=true

        Program program = new Program();
        program.setVarId("1");
        program.setProgramId("1");
        Map<String, Object> configs = new HashMap<>();
        configs.put("catalog_id","apple");
        program.setConfig(configs);
        List<com.b2s.rewards.apple.model.PaymentOption> payments = new ArrayList<>();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE")
        payments.add(paymentOption)
        program.setPayments(payments)
        appSessionInfo.currentUser() >> getUser()
        appSessionInfo.getSessionCart() >> cart
        session.getAttribute("PROGRAM") >> program
        session.getAttribute("USER") >> getUser()
        session.getAttribute("APPLE_CART") >> cart
        cartService.getShoppingCart(_) >> shoppingCart

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest()


        when:
        def result = mvc.perform(MockMvcRequestBuilders.post("/order/placeOrder")
                .content(AppleUtil.asJsonString(placeOrderRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .session(session))
                .andReturn()

        then:
        result.response.status==409
        result.response.contentAsString.contains("Order Placement did not go through: Cart item is either modified by user through different session or no longer available")

    }

    def "placeOrder - with different Transaction ID"(){

        given:
        appSessionInfo.getSessionCart() >> cart
        session.getAttribute("USER") >> getUser()
        session.getAttribute("APPLE_CART") >> cart
        session.getAttribute(CommonConstants.PAYMENT_TRANSACTION_ID) >> "a1b2c3d4e5"

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(transactionId: "a1b2c3d4")

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post("/order/placeOrder")
                .content(AppleUtil.asJsonString(placeOrderRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .session(session))
                .andReturn()

        then:
        result.response.status==400
        result.response.contentAsString.contains("Error placing the order. Please contact administrator.")
    }

//    def "placeOrder - failed placing order due to some reason"(){
//
//        given:
//        ShoppingCart shoppingCart=Mock()
//        Cart cart=getCart()
//        OrderStatus orderStatus=getOrderStatus()
//        UserVarProgramCreditAdds uvp=new UserVarProgramCreditAdds();
//        VarProgram varProgram = new VarProgram();
//
//        cart.shippingAddress.firstName="Santhosh"
//        cart.cartItems.get(0).supplierId=200
//
//        orderStatus.orderCompleted=false  //  Marked as failed order
//        uvp.cost=500.00
//
//        varProgram.programId="1"
//        varProgram.varId="1"
//        varProgram.catalogId="apple"
//        varProgram.adds=uvp
//        varProgram.paymentOption=PaymentOption.POINTS_CC_SLIDER.toString()
//
//        user.program=varProgram
//
//        Program program = new Program()
//        program.setVarId("1")
//        program.setProgramId("1")
//        Map<String, Object> configs = new HashMap<>()
//        configs.put("catalog_id","apple")
//        program.setConfig(configs)
//        List<com.b2s.rewards.apple.model.PaymentOption> payments = new ArrayList<>()
//        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption()
//        paymentOption.setPaymentOption("POINTS")
//        paymentOption.setSupplementaryPaymentType("VARIABLE")
//        payments.add(paymentOption)
//        program.setPayments(payments)
//
//        session.getAttribute("PROGRAM") >> program
//        session.getAttribute("USER") >> getUser()
//        session.getAttribute("APPLE_CART") >> cart
//
//        cartService.getShoppingCart(_)>> shoppingCart
//        manager.placeMyOrder(_,_,_) >> orderStatus
//
//        when:
//        def result=mvc.perform(MockMvcRequestBuilders.get("/order/placeOrder")
//                .param("last4","1543")
//                .param("ccType","visa")
//                .session(session))
//                .andReturn()
//
//        then:
//        result.response.status==500
//        def jsonMsg = new JsonSlurper().parseText(result.response.contentAsString);
//        Assert.assertFalse(jsonMsg.orderCompleted)
//
//    }


//    def "placeOrder - success scenario - Places order in B2S DB and in VIS and return successfully"(){
//
//        given:
//        ShoppingCart shoppingCart=Mock()
//        Cart cart=getCart()
//        OrderStatus orderStatus=getOrderStatus()
//
//        cart.shippingAddress.firstName="Santhosh"
//        cart.cartItems.get(0).supplierId=200
//
//        Program program = new Program();
//        program.setVarId("1");
//        program.setProgramId("1");
//        Map<String, Object> configs = new HashMap<>();
//        configs.put("catalog_id","apple");
//        program.setConfig(configs);
//        List<com.b2s.rewards.apple.model.PaymentOption> payments = new ArrayList<>();
//        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
//        paymentOption.setPaymentOption("POINTS");
//        paymentOption.setSupplementaryPaymentType("VARIABLE")
//        payments.add(paymentOption)
//        program.setPayments(payments)
//
//        session.getAttribute("PROGRAM") >> program
//        session.getAttribute("USER") >> getUser()
//        session.getAttribute("APPLE_CART") >> cart
//        cartService.getShoppingCart(_)>>shoppingCart
//        manager.placeMyOrder(_,_,_) >> orderStatus
//
//        when:
//        def result=mvc.perform(MockMvcRequestBuilders.get("/order/placeOrder")
//                            .param("last4","1543")
//                            .param("ccType","visa")
//                            .session(session))
//                            .andReturn()
//
//        then:
//        result.response.status==200
//        def jsonMsg = new JsonSlurper().parseText(result.response.contentAsString);
//        Assert.assertTrue(jsonMsg.orderCompleted)
//
//    }

    def "placeOrder - throws exception while inserting order into DB"(){

        given:
        ShoppingCart shoppingCart=Mock()
        Cart cart=getCart()
        OrderStatus order=getOrderStatus()

        cart.shippingAddress.firstName="Santhosh"
        cart.shippingAddress.isValidAddress=true
        cart.cartItems.get(0).supplierId=200

        appSessionInfo.currentUser() >> getUser()
        session.getAttribute("USER") >> getUser()
        session.getAttribute("APPLE_CART") >> cart
        shoppingCartService.getShoppingCart(_,_,_,_) >>shoppingCart
        manager.placeMyOrder(_,_,_,_) >> new Exception()

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post("/order/placeOrder")
                .content(AppleUtil.asJsonString(placeOrderRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .session(session))
                .andReturn()

        then:
        result.response.status==500
        result.response.contentAsString.contains("Order Placement did not go through")

    }

    def "placeOrder - throws exception for BrowseOnly User"(){

        given:
        ShoppingCart shoppingCart=Mock()
        Cart cart=getCart()
        OrderStatus order=getOrderStatus()
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest()

        cart.shippingAddress.firstName="Santhosh"
        cart.shippingAddress.isValidAddress=true
        cart.cartItems.get(0).supplierId=200

        appSessionInfo.currentUser() >> getUser()
        appSessionInfo.getSessionCart() >> cart
        session.getAttribute("USER") >> getBrowseOnlyUser()
        session.getAttribute("APPLE_CART") >> cart
        shoppingCartService.getShoppingCart(_,_,_,_) >>shoppingCart
        manager.placeMyOrder(_,_,_,_) >> new Exception()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post("/order/placeOrder")
                .content(AppleUtil.asJsonString(placeOrderRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .session(session))
                .andReturn()

        then:
        result.response.status==400
        result.response.contentAsString.contains("Error placing the order. Please contact administrator.")

    }

    @Unroll
    def "confirmPurchase -   test exception scenario"(){

        given:
        session.getAttribute("USER") >> getUser()
        manager.getOrder(_) >> order
        manager.updateOrderLines(_,_)  >> updateFlag
        basicAuthValidation.isUserHasAccessToOrder(_) >> accessFlag

        when:
        def result=mvc.perform(MockMvcRequestBuilders.put("/order/confirmPurchase/"+orderId)
                .param("paymentStatus", paymentStatus)
                .session(session))
                .andReturn()
        then:
        result.resolvedException.message == expectedResponse
        result.resolvedException.getHttpStatus().value == status

        where:
        order        | updateFlag | accessFlag | orderId    | paymentStatus | queryType || status || expectedResponse
        null         | true       | true       | 123        | "true"        | null      || 404    || "ConfirmPurchase: Order ID not found in B2S."
        getOrder(11) | true       | true       | 123        | "false"       | null      || 400    || "This is a Demo order. Order cannot be confirmed."
        getOrder(99) | true       | true       | 123        | "false"       | null      || 400    || "Order is already in Failed status. No further change is possible. Please contact support."
        getOrder(0)  | true       | true       | 123        | "false"       | null      || 409    || "Order is being processed. Conflicting status."
        getOrder(0)  | true       | false      | 123        | "false"       | null      || 403    || "ConfirmPurchase: Order ID does not belongs to your entity."
        getOrder(2)  | true       | true       | 123        | "false"       | null      || 409    || "Order is either being processed or crossed a point that change in status is not possible at this time. Please contact support."
        getOrder()   | true       | false      | 2100454981 | "false"       | null      || 403    || "ConfirmPurchase: Order ID does not belongs to your entity."
    }

    @Unroll
    def "confirmPurchase - test no exception scenario"(){

        given:
        session.getAttribute("USER") >> getUser()
        manager.getOrder(_) >> order
        manager.updateOrderLines(_,_)  >> updateFlag
        basicAuthValidation.isUserHasAccessToOrder(_) >> accessFlag

        when:
        def result=mvc.perform(MockMvcRequestBuilders.put("/order/confirmPurchase/"+orderId)
                .param("paymentStatus", paymentStatus)
                .session(session))
                .andReturn()
        then:
        result.response.status==status
        result.response.contentAsString.contains(expectedResponse)

        where:
        order      | updateFlag | accessFlag | orderId                     | paymentStatus | queryType || status || expectedResponse
        getOrder() | true       | true       | 123                         | "true"        | "order"   || 200    || ""
        getOrder() | true       | true       | 123                         | "false"       | null      || 200    || ""
        getOrder() | true       | true       | 2100454981                  | "false"       | null      || 200    || ""
        getOrder() | true       | true       | 123                         | "true"        | "request" || 200    || ""
        getOrder() | true       | true       | 123                         | "trrue"       | null      || 400    || "Please check your input."
        getOrder() | true       | true       | "12A3"                      | "true"        | null      || 400    || "Please check your input."
        getOrder() | true       | true       | 123988989898989897876767575 | "trrue"       | null      || 400    || "Please check your input."
        getOrder() | true       | true       | 2100454981                  | "false"       | null      || 200    || ""
        getOrder() | true       | true       | 2100454981                  | "yes"         | null      || 200    || ""
        getOrder() | true       | true       | 2100454981                  | "1"           | null      || 200    || ""
    }


    def "confirmPurchase - throws Serviceexception while retrieving order from DB"(){

        given:
        session.getAttribute("USER") >> getUser()
        manager.getOrder(_)  >> {throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION)} // Throws exception
        basicAuthValidation.isUserHasAccessToOrder(_) >> true
        when:
        def result=mvc.perform(MockMvcRequestBuilders.put("/order/confirmPurchase/123")
                .param("paymentStatus", "false")
                .session(session))
                .andReturn()

        then:
        result.resolvedException.message == "Failed to Confirm Order Purchase. Please notify B2S."
        result.resolvedException.getHttpStatus().value == 500
    }

    def "confirmPurchase - throws Exception while updating orderlines"(){

        given:
        session.getAttribute("USER") >> getUser()
        manager.getOrder(_) >> getOrder()
        manager.updateOrderLines(_,_)  >> {throw new Exception()}       //  Throws exception
        basicAuthValidation.isUserHasAccessToOrder(_) >> true

        when:
        def result=mvc.perform(MockMvcRequestBuilders.put("/order/confirmPurchase/123")
                .param("paymentStatus", "true")
                .session(session))
                .andReturn()

        then:
        result.resolvedException.message == "Failed to Confirm Order Purchase. Please notify B2S."
        result.resolvedException.getHttpStatus().value == 500

    }

    @Unroll
    def "getPurchaseSelectionInformation - Refer Data table for test scenario "(){

        given:
        appSessionInfo.currentUser() >> getUser()
        session.getAttribute("USER") >> getUser()
        manager.getOrder(_) >> order

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/order/getPurchaseSelectionInfo/"+orderId)
                    .session(session))
                    .andReturn()

        then:
        result.response.status==status
        result.response.contentAsString.contains(expectedContent)

        where: // Data table
        order     |orderId|status|expectedContent

        getOrder()|""     |404   |""                                                   // Order Id is not available in request
        getOrder()|"12AB3"|400   |"PurchaseSelectionInfo: Order id should be numeric." // Order ID passed is not numeric
        null      |"123"  |404   |"PurchaseSelectionInfo: Order ID not found in B2S."  // Record with order ID 123 doesnt exist in B2S DB
        getOrder()|"123"  |500   |"Failed to get purchase selection information. Please notify B2S." // Data parsing error in B2S end

    }


    def "getPurchaseSelectionInformation - Failed to get order having order id - throws serviceexecution"(){

        given:

        manager.getOrder(_) >> {throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION)}
        session.getAttribute("USER") >> getUser()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/order/getPurchaseSelectionInfo/456")
                .session(session))
                .andReturn()

        then:
        result.response.status==500
        result.response.contentAsString.equals("ServiceException in PurchaseSelectionInfo service")

    }

    def "getPurchaseSelectionInformation - success scenario"(){

        given:
        Order order=new Order();
        OrderLine orderLine=new OrderLine()
        orderLine.orderStatus=0
        orderLine.supplierId=200
        orderLine.supplierItemPrice=100
        orderLine.supplierShippingPrice=2
        orderLine.quantity=1

        List attributeList=new ArrayList()
        OrderLineAttribute attribute=new OrderLineAttribute();
        attribute.id=1
        attribute.orderId=456
        attribute.lineNum=1
        attribute.name="engravingCode"
        attribute.value="EHV077N"

        attributeList.add(attribute)
        orderLine.orderAttributes=attributeList

        List<OrderLine> list=new ArrayList<>()
        list.add(orderLine)
        order.orderLines=(list)
        order.currencyCode="USD"
        order.orderDate=new Date()
        order.orderId=456
        order.languageCode="en_US"
        order.countryCode="US"
        order.varId="VitalityUS"


        applicationProperties.getProperty(_) >> "https://integrationtest.powerofvitality.com/vitality/bridge2/post"
        manager.getOrder(_) >> order
        appSessionInfo.currentUser() >> getUser()
        session.getAttribute("USER") >> getUser()
        session.getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> getProgram()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/order/getPurchaseSelectionInfo/456")
                .session(session))
                .andReturn()

        then:
        result.response.status == 200
        PurchaseSelectionInfoVitality jsonMsg = new ObjectMapper().readValue(result.response.contentAsString, PurchaseSelectionInfoVitality.class)
        Assert.assertEquals("456", jsonMsg.purchaseReference)

    }

    def "placeOrder - throws exception for Agent Browse User"(){

        given:
        ShoppingCart shoppingCart = Mock()
        Cart cart = getCart()
        OrderStatus order = getOrderStatus()
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest()

        cart.shippingAddress.firstName="Sathish"
        cart.shippingAddress.isValidAddress=true
        cart.cartItems.get(0).supplierId=200

        appSessionInfo.currentUser() >> getUser()
        appSessionInfo.getSessionCart() >> cart
        session.getAttribute("USER") >> getAgentBrowseUser()
        session.getAttribute("APPLE_CART") >> cart
        shoppingCartService.getShoppingCart(_,_,_,_) >>shoppingCart
        manager.placeMyOrder(_,_,_,_) >> new Exception()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post("/order/placeOrder")
                .content(AppleUtil.asJsonString(placeOrderRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .session(session))
                .andReturn()

        then:
        result.response.status == 400
        result.response.contentAsString.contains("Error placing the order. Please contact administrator.")

    }

    def getCart(){
        Cart cart=new Cart();
        List li=new ArrayList<CartItem>();
        li.add(new CartItem());
        cart.setCartItems(li);
        cart.setCreditItem(new CreditItem(ccLast4: 4321, creditCardType: "VISA"))
        return cart;
    }

    def getUser(){
        user.varId="VitalityUS"
        user.programId="prgId"
        user.locale=Locale.US
        return user
    }

    def getBrowseOnlyUser(){
        user.varId="CitiFintech"
        user.programId="Acquisition"
        user.locale=Locale.US
        user.browseOnly=true
        return user
    }

    def getOrder(orderStatus){
        if(orderStatus==null||orderStatus==""){
            orderStatus=-2
        }
        Order order=new Order();
        OrderLine orderLine=new OrderLine()
        orderLine.setOrderStatus(orderStatus)
        orderLine.setSupplierId("200");
        List<OrderLine> list=new ArrayList<>()
        list.add(orderLine)
        order.setOrderLines(list)
        order.setVarId("VitalityUS")
        return order
    }

    def getOrderStatus(){

        OrderStatus orderStatus=new OrderStatus()
        orderStatus.setVarOrderId("1234")
        orderStatus.setOrderCompleted(true)
        return orderStatus
    }

    def getProgram(){
        Program program=new Program();
        Map config =new HashedMap();
        program.setConfig(config);
        return program
    }

    def getAgentBrowseUser(){
        user.varId = "FDR"
        user.programId = "1"
        user.locale = Locale.US
        user.agentBrowse = true
        return user
    }



}
