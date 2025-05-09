package com.b2s.shop.common.order.var

import com.b2s.apple.entity.MerchantEntity
import com.b2s.apple.entity.OrderLineAttributeEntity
import com.b2s.apple.services.PricingModelService
import com.b2s.common.services.discountservice.DiscountServiceClient
import com.b2s.db.model.Order
import com.b2s.db.model.OrderLine
import com.b2s.rewards.apple.dao.DemoUserDao
import com.b2s.rewards.apple.dao.MerchantListDao
import com.b2s.rewards.apple.dao.OrderAttributeValueDao
import com.b2s.rewards.apple.dao.OrderLineAttributeDao
import com.b2s.rewards.apple.integration.model.AccountBalance
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.integration.model.RedemptionOrderLine
import com.b2s.rewards.apple.integration.model.RedemptionResponse
import com.b2s.rewards.apple.integration.model.UserInformation
import com.b2s.rewards.apple.model.OrderAttributeValue
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.util.HttpClientUtil
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.apple.services.MessageService
import com.b2s.apple.services.ProgramService
import com.b2s.shop.common.order.var.common.VarOrderManagerTestDataFactory
import org.springframework.http.HttpMethod
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.Principal

class VAROrderManagerPNCSpec extends Specification {
    def programService = Mock(ProgramService)
    def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
    def applicationProperties = Mock(Properties)
    def discountServiceClient = Mock(DiscountServiceClient)
    def pricingModelService = Mock(PricingModelService)
    def demoUserDao = Mock(DemoUserDao)
    def visHttpClientUtil = Mock(HttpClientUtil)
    def defaultHttpClientUtil = Mock(HttpClientUtil)
    def messageService = Mock(MessageService)
    def merchantDao = Mock(MerchantListDao)
    def orderLineAttributeDao = Mock(OrderLineAttributeDao)
    def orderAttributeValueDao = Mock(OrderAttributeValueDao)

    @Subject
    def varIntegrationServiceRemoteImpl = new VarIntegrationServiceRemoteImpl(visHttpClientUtil: visHttpClientUtil,
            defaultHttpClientUtil: defaultHttpClientUtil, applicationProperties: applicationProperties,
            merchantDao: merchantDao, orderLineAttributeDao: orderLineAttributeDao,
            orderAttributeValueDao: orderAttributeValueDao)

    def varOrderManagerPNC = new VAROrderManagerPNC(varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl,
            programService: programService, applicationProperties: applicationProperties,
            discountServiceClient: discountServiceClient,
            varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl, demoUserDao: demoUserDao,
            messageService: messageService)

    def "getAccountInfo"() {

        setup:

        def user = Mock(User)
        def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
        def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
        def accountInfoLocal = null
        AccountInfo accountInfoRemote = new AccountInfo();
        accountInfoRemote.setPricingTier("PNC Pricing")
        def programService = Mock(ProgramService)
        Program program = new Program();
        program.setVarId("PNC")
        program.setProgramId("b2s_qa_only")
        program.setIsLocal(true)
        program.setIsActive(true)
        programService.getProgram(_, _, _) >> program
        varIntegrationServiceRemoteImpl.getUserProfile(_, _, _) >> accountInfoRemote
        def varOrderManagerPNC = new VAROrderManagerPNC(programService: programService)
        when:
        accountInfoLocal = varOrderManagerPNC.getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl)
        program.setIsLocal(false)
        accountInfoRemote = varOrderManagerPNC.getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl)
        then:
        accountInfoLocal == null && accountInfoRemote != null

    }

    def "test orderPlace for Exception"() {

        setup:

        def order = Mock(Order)
        User user = new User()
        def program = Mock(Program)
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110
        def messageService = Mock(MessageService)
        def varOrderManagerPNC = new VAROrderManagerPNC(messageService: messageService)
        messageService.getMessage(*_) >> ""


        when:
        boolean result = varOrderManagerPNC.placeOrder(order, user, program);

        then:
        result == false && order.getVarOrderId().equals("110")
    }

    def "selectUser - SAMLLogin "() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> getAccountInformation()
        populatePNCMockInfo(request)

        when:
        User user = varOrderManagerPNC.selectUser(request)
        then:
        user != null
    }

    def "selectUser - Anonymous"() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE) >> "true"
        populatePNCMockInfo(request)

        when:
        User user = varOrderManagerPNC.selectUser(request)
        then:
        user != null
    }

    def "selectUser - FiveBox"() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.RELAY_STATE) >> null
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> VarOrderManagerTestDataFactory.getDemoUserEntity()
        request.getParameter(CommonConstants.IS_ANONYMOUS) >> "true"
        request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE) >> "false"
        populatePNCMockInfo(request)

        when:
        User user = varOrderManagerPNC.selectUser(request)
        then:
        user != null
    }

    def " test getUserPoints for remote - Points"() {
        given:
        Program programDetails = VarOrderManagerTestDataFactory.getProgramDetails(false)
        User userDetails = VarOrderManagerTestDataFactory.getUserDetails()
        applicationProperties.getProperty(CommonConstants.VIS_ACCOUNTS_SERVICE_URL) >> "http://vis-vip.apexaqa1.bridge2solutions.net"
        defaultHttpClientUtil.getHttpResponse(_ as String, _ as Class, _ as HttpMethod, _) >> getAccountBalance()

        when:
        def points = varOrderManagerPNC.getUserPoints(userDetails, programDetails)

        then:
        points == 2000
    }

    def " test getUserPoints for Local"() {
        given:
        Program programDetails = VarOrderManagerTestDataFactory.getProgramDetails(true)
        User userDetails = VarOrderManagerTestDataFactory.getUserDetails()
        varIntegrationServiceLocalImpl.getLocalUserPoints(_) >> 2000;
        when:
        def points = varOrderManagerPNC.getUserPoints(userDetails, programDetails)

        then:
        points == 2000
    }

    def "test Redemption Response with VarOrderLineId"() {
        given:
        Program programDetails = VarOrderManagerTestDataFactory.getProgramDetails(false)
        Order orderDetails = getOrderDetails()
        setOrderLines(orderDetails.getOrderLines())
        User userDetails = VarOrderManagerTestDataFactory.getUserDetails()
        messageService.getMessage(*_)>>""
        applicationProperties.getProperty(CommonConstants.VIS_ACCOUNTS_SERVICE_URL) >> "http://vis-vip.apexaqa1.bridge2solutions.net"
        defaultHttpClientUtil.getHttpResponse(_ as String, _ as Class, _ as HttpMethod, _) >> getRedemptionResponseWithVarOrderLineId()
        merchantDao.getMerchant(_ as Integer, _ as Integer) >> getMerchantEntity()
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_,_,_) >> new ArrayList<OrderLineAttributeEntity>()

        when:
        def placeOrderSuccess = varOrderManagerPNC.placeOrder(orderDetails, userDetails, programDetails)

        then:
        placeOrderSuccess
        orderDetails.getOrderLines().get(0).getOrderLineType() == "VarOrderLineId"
        orderDetails.getOrderLines().get(1).getOrderLineType() == "VarOrderLineId2"
    }

    def "test Redemption Response without VarOrderLineId"() {
        given:
        Program programDetails = VarOrderManagerTestDataFactory.getProgramDetails(false)
        Order orderDetails = getOrderDetails()
        setOrderLines(orderDetails.getOrderLines())
        User userDetails = VarOrderManagerTestDataFactory.getUserDetails()
        messageService.getMessage(*_)>>""
        applicationProperties.getProperty(CommonConstants.VIS_ACCOUNTS_SERVICE_URL) >> "http://vis-vip.apexaqa1.bridge2solutions.net"
        defaultHttpClientUtil.getHttpResponse(_ as String, _ as Class, _ as HttpMethod, _) >> getRedemptionResponseWithoutVarOrderLineId()
        merchantDao.getMerchant(_ as Integer, _ as Integer) >> getMerchantEntity()
        orderLineAttributeDao.findByOrderIdAndLineNumAndName(_,_,_) >> new ArrayList<OrderLineAttributeEntity>()

        when:
        def placeOrderSuccess = varOrderManagerPNC.placeOrder(orderDetails, userDetails, programDetails)

        then:
        placeOrderSuccess
        orderDetails.getOrderLines().get(0).getOrderLineType() == null
    }

    private AccountBalance getAccountBalance(){
        AccountBalance accountBalance = new AccountBalance()
        accountBalance.setPointsBalance(2000)
        return accountBalance
    }

    private MerchantEntity getMerchantEntity(){
        MerchantEntity merchantEntity = new MerchantEntity()
        merchantEntity.setName("Merchant")
        merchantEntity.setSupplierId(1)
        merchantEntity.setMerchantId(1)
        merchantEntity.setId(1)
        return merchantEntity
    }

    private RedemptionResponse getRedemptionResponseWithVarOrderLineId(){
        RedemptionResponse redemptionResponse = new RedemptionResponse()
        RedemptionOrderLine redemptionOrderLine = new RedemptionOrderLine()
        redemptionOrderLine.setVarOrderLineId("VarOrderLineId")
        redemptionOrderLine.setOrderLineId("1")
        List<RedemptionOrderLine> redemptionOrderLineList = new ArrayList<RedemptionOrderLine>()
        redemptionOrderLineList.add(redemptionOrderLine)

        RedemptionOrderLine redemptionOrderLine2 = new RedemptionOrderLine()
        redemptionOrderLine2.setVarOrderLineId("VarOrderLineId2")
        redemptionOrderLine2.setOrderLineId("2")
        redemptionOrderLineList.add(redemptionOrderLine2)

        redemptionResponse.setOrderLines(redemptionOrderLineList)
        return redemptionResponse
    }

    private RedemptionResponse getRedemptionResponseWithoutVarOrderLineId(){
        RedemptionResponse redemptionResponse = new RedemptionResponse()
        RedemptionOrderLine redemptionOrderLine = new RedemptionOrderLine()
        redemptionOrderLine.setOrderLineId("1")
        List<RedemptionOrderLine> redemptionOrderLineList = new ArrayList<RedemptionOrderLine>()
        redemptionOrderLineList.add(redemptionOrderLine)
        redemptionResponse.setOrderLines(redemptionOrderLineList)
        return redemptionResponse
    }

    private void setOrderLines(List<OrderLine> orderLines){
        OrderLine orderLine = new OrderLine()
        orderLine.setOrderId(1L);
        orderLine.setLineNum(1)
        orderLine.setSupplierId("200")
        orderLine.setQuantity(1)
        orderLine.setItemPoints(Double.valueOf(3847))
        orderLine.setSupplierItemPrice(2500)
        orderLine.setShippingPoints(Double.valueOf(0))
        orderLine.setSupplierShippingPrice(0)
        orderLine.setConvRate(0.01)
        orderLine.setB2sItemProfitPrice(1)
        orderLine.setSupplierId("1")
        orderLine.setMerchantId("1")
        orderLine.setOrderLinePoints(300)
        orderLines.add(orderLine)

        OrderLine orderLine2 = new OrderLine()
        orderLine2.setOrderId(1L);
        orderLine2.setLineNum(2)
        orderLine2.setSupplierId("200")
        orderLine2.setQuantity(1)
        orderLine2.setItemPoints(Double.valueOf(3847))
        orderLine2.setSupplierItemPrice(2500)
        orderLine2.setShippingPoints(Double.valueOf(0))
        orderLine2.setSupplierShippingPrice(0)
        orderLine2.setConvRate(0.01)
        orderLine2.setB2sItemProfitPrice(1)
        orderLine.setSupplierId("1")
        orderLine2.setMerchantId("1")
        orderLine2.setOrderLinePoints(300)
        orderLines.add(orderLine2)
    }

    private Order getOrderDetails() {
        return new Order(orderId: 1234, varOrderId: 'var1234', orderDate: new Date(), languageCode: 'en',
                countryCode: 'US', orderLines: new ArrayList<OrderLine>(),
                orderAttributeValues: new ArrayList<OrderAttributeValue>())
    }

    private void populatePNCMockInfo(HttpServletRequest request) {
        request.getHeaderNames() >> new Enumeration<String>() {
            @Override
            boolean hasMoreElements() {
                return false
            }

            @Override
            String nextElement() {
                return ""
            }
        }
        UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUri() >> new URI("https://pnc-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        request.getAttribute(CommonConstants.POINTS_BALANCE) >> "1000"
        Program program = VarOrderManagerTestDataFactory.getProgramDetails(true)
        Map<String, Object> config = new HashMap<String, Object>()
        config.put(CommonConstants.VIMS_ADDITIONAL_INFO, "AdditionalInfo")
        program.setConfig(config)
        programService.getProgram(_, _, _) >> program
        programService.getProgramConfigValue(_, _, _) >> Optional.of("true")
        pricingModelService.getPricingModels(_, _, _) >> null
        applicationProperties.get(CommonConstants.DISCOUNTCODE_QUERY_PARAM_OTP) >> "discountCode"
        applicationProperties.getProperty("session.timeout.minutes") >> "200"
        discountServiceClient.getValidDiscountCode(_, _) >> VarOrderManagerTestDataFactory.getCouponDetails()
        varIntegrationServiceLocalImpl.getLocalUserPoints(_) >> null
    }

    private void getHttpServletRequest(HttpServletRequest request) {
        HttpSession httpSession = new MockHttpSession()
        request.getAttribute(CommonConstants.PROGRAM_ID) >> "b2s_qa_only"
        request.getSession() >> httpSession
        request.getParameter(CommonConstants.USER_ID) >> "Anonymous_user"
        request.getParameter(CommonConstants.VAR_ID) >> "pnc"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        request.getParameter("discountCode") >> "discountCode"
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.requestURL >> new StringBuffer("https://pnc-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        request.getParameterValues(CommonConstants.RELAY_STATE) >> VarOrderManagerTestDataFactory.getRelayState()
        request.getAttribute(CommonConstants.LOCALE) >> "en_CA"
        request.getUserPrincipal() >> Mock(Principal)
        request.getUserPrincipal().getName() >> "User Principal"
        request.getAttribute("AdditionalInfo") >> "AdditionalInfo"
    }

    AccountInfo getAccountInformation() {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setPricingTier("Scotia Pricing")
        accountInfo.setUserInformation(new UserInformation());

        AccountBalance accountBalance = new AccountBalance()
        accountBalance.setPointsBalance(1000)
        accountBalance.setCurrency("USD")
        accountInfo.setAccountBalance(accountBalance)

        return accountInfo
    }
}
