package com.b2s.rewards.apple.controller

import com.b2s.apple.services.AppSessionInfo
import com.b2s.apple.services.ProgramService
import com.b2s.db.model.Order
import com.b2s.db.model.OrderLine
import com.b2s.db.model.OrderLineAttribute
import com.b2s.rewards.apple.dao.OrderStatusDescDao
import com.b2s.rewards.apple.exceptionhandler.CustomRestExceptionHandler
import com.b2s.rewards.apple.model.OrderInfoAPI
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.util.BasicAuthValidation
import com.b2s.shop.common.User
import com.b2s.shop.common.order.OrderTransactionManager
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.util.NestedServletException
import spock.lang.Specification
import spock.lang.Unroll

class OrdersControllerSpec extends Specification {

    private OrderTransactionManager orderManager;

    private BasicAuthValidation basicAuthValidation

    private ProgramService programService;

    private OrdersController controller;

    private MockMvc mvc;

    private MockMvc mockMvc;

    private MockHttpSession session;

    private User user;

    private OrderStatusDescDao orderStatusDescDao;

    private AppSessionInfo appSessionInfo;


    def setup() {

        orderManager = Mock()
        orderStatusDescDao = Mock()
        basicAuthValidation = Mock()
        programService = Mock()
        session = Mock()
        appSessionInfo=Mock();

        controller = new OrdersController(basicAuthValidation: basicAuthValidation,
                orderManager: orderManager, programService: programService,
                orderStatusDescDao: orderStatusDescDao,
                appSessionInfo:appSessionInfo)

        user = new User()

        mvc = MockMvcBuilders.standaloneSetup(controller).build()

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                 .setControllerAdvice(new CustomRestExceptionHandler())
                                 .build()
    }


    def cleanup() {
        orderManager = null
        session = null
        controller = null
        orderStatusDescDao = null
        mvc = null
        user = null
    }


    @Unroll
    def "test getOrder() for Exception - Refer data table comments for test scenario"() {

        given:
        basicAuthValidation.isUserHasAccessToOrder(_) >> accessFlag
        orderManager.getOrder(_) >> order

        when:
        mvc.perform(MockMvcRequestBuilders.get("/orders/" + orderId)
                .session(session))
                .andReturn()

        then:
        final NestedServletException exception = thrown()
        exception.rootCause.getHttpStatus().value() == expectedStatusCode
        exception.rootCause.message == expectedMessage

        where:
        order      | orderId    | accessFlag || expectedStatusCode || expectedMessage
        null       | 2100454980 | true       || 404                || "B2S Order ID not found"                    // If Order is null
        getOrder() | 0          | true       || 400                || "Not a valid orderId"                       // Order ID passed as 0
        getOrder() | 2100454981 | false      || 403                || "Order ID does not belongs to your entity." // If accessFlag is false
    }


    @Unroll
    def "test getOrder() - Refer data table comments for test scenario"() {

        given:
        session.getAttribute("USER") >> getUser()
        orderManager.getOrder(_) >> order
        basicAuthValidation.isUserHasAccessToOrder(_) >> accessFlag
        programService.getProgram(_, _, _) >> getProgram()
        controller.populateOrderDetails(_, _, _) >> getOrderInfoAPI()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.get("/orders/" + orderId)
                .session(session))
                .andReturn()

        then:
        result.response.status == expectedStatus
        result.response.contentAsString.contains(expectedContent)

        where:
        order      | orderId               | accessFlag || expectedStatus || expectedContent
        getOrder() | ""                    | true       || 404            || ""                         // Order ID is not available in request
        getOrder() | "12AB3"               | true       || 400            || "Please check your input." // Order ID passed as not numeric
        getOrder() | true                  | true       || 400            || "Please check your input." // Order ID passed as boolean
        getOrder() | 922337203685477580789 | true       || 400            || "Please check your input." // Order ID passed as more than long values maximum limit
        getOrder() | "TEST"                | true       || 400            || "Please check your input." // Order ID passed as string
    }

    def "test orderHistoryDetails - APIs"() {
        given:
        appSessionInfo.currentUser() >> getUser()

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get("/order/orderHistoryDetails/12345")
                .session(session))
                .andReturn()
        def result2 = mvc.perform(MockMvcRequestBuilders.get("/order/orderHistory/")
                .session(session))
                .andReturn()

        then:
        result.response.status == 404
        result.response.contentAsString.contains("Invalid user session")
        result2.response.status == 404
        result2.response.contentAsString.contains("User ID not found")
    }

    def getUser() {
        user.varId = "UA"
        user.programId = "MP"
        user.locale = Locale.US
        return user
    }

    def getOrderInfoAPI() {
        OrderInfoAPI orderInfoAPI = new OrderInfoAPI()
        orderInfoAPI.setOrderID(2100454980)
        return orderInfoAPI
    }

    def getProgram() {
        Program program = new Program()
        Map<String, Object> config = new HashMap<>()
        config.put("shopExperience", "DRP")
        program.setConfig(config)
        return program
    }


    def getOrder(orderStatus) {
        if (orderStatus == null || orderStatus == "") {
            orderStatus = 1
        }

        Order order = new Order();
        OrderLine orderLine = new OrderLine()
        orderLine.setOrderStatus(orderStatus)
        orderLine.setSupplierId("200");
        orderLine.setLineNum(12);
        orderLine.setSku("sku");
        orderLine.setName("Name");
        orderLine.setImageUrl("http://url");
        orderLine.setQuantity(1);
        orderLine.setCategory("cat");

        List<OrderLine> list = new ArrayList<>()
        list.add(orderLine)
        order.setOrderLines(list)
        order.setVarId("UA")
        order.setProgramId("MP")
        order.setCountryCode("US")

        List attributeList = new ArrayList()
        OrderLineAttribute attribute = new OrderLineAttribute();
        attribute.id = 1
        attribute.orderId = 456
        attribute.lineNum = 1
        attribute.name = "code"
        attribute.value = "value"

        attributeList.add(attribute)
        orderLine.orderAttributes = attributeList

        return order
    }


}