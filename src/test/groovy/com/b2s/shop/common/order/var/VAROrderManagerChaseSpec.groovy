package com.b2s.shop.common.order.var

import com.b2s.db.model.Order
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.integration.model.CancelRedemptionResponse
import com.b2s.rewards.apple.integration.model.RedemptionResponse
import com.b2s.rewards.apple.integration.model.UserInformation
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.exception.B2RException
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.shop.common.order.var.common.VarOrderManagerChaseTestDataFactory
import com.b2s.apple.services.MessageService
import com.b2s.apple.services.ProgramService
import com.b2s.shop.common.order.var.common.VarOrderManagerTestDataFactory
import org.apache.commons.collections4.map.HashedMap
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.Principal

/**
 * Unit tests for vAR Order Manager Chase
 *
 * Created by ahajamohideen on 07/05/2019.
 */

class VAROrderManagerChaseSpec extends Specification {

    def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
    def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
    def programService = Mock(ProgramService)
    def applicationProperties = Mock(Properties)
    def messageService = Mock(MessageService)
    def varIntegrationServices = new HashMap<String, VarIntegrationService>()

    def varOrderManagerChase = new VAROrderManagerChase(applicationProperties: applicationProperties, programService:
            programService, varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl,
            varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl,
            messageService: messageService, varIntegrationServices: varIntegrationServices)

    final static String LOCALE = "locale"
    final static String USERID = "userId"
    final static String EMAIL = "email"
    final static String TELEPHONE = "telephone"
    final static String POINTBALANCE = "pointBalance"
    final static String ADDRESS1_ADDRESSLINE1 = "address1.addressLine1"
    final static String ADDRESS1_ADDRESSLINE2 = "address1.addressLine2"
    final static String ADDRESS1_COUNTRY = "address1.country"
    final static String ADDRESS1_POSTAL = "address1.postal"
    final static String ADDRESS1_STATE = "address1.state"
    final static String ADDRESS1_CITY = "address1.city"

    def "getUser for Invalid locale format as null or empty"() {

        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getAttribute(LOCALE) >> "#"

        when:
        varOrderManagerChase.getUser(request)

        then:
        final IllegalArgumentException exception = thrown()
        exception.message == "Invalid locale format: #"

    }


    def "getUser for Invalid locale format"() {

        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getAttribute(LOCALE) >> "ab_ZA35345"
        request.getAttribute(POINTBALANCE) >> "1000"

        when:
        varOrderManagerChase.getUser(request)

        then:
        final IllegalArgumentException exception = thrown()
        exception.message == "Invalid locale format: ab_ZA35345"
    }


    def "getUser for user principal as blank"() {

        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getAttribute(LOCALE) >> "ab_ZA_35345"
        request.getAttribute(POINTBALANCE) >> "1000"
        request.getAttribute(ADDRESS1_ADDRESSLINE1) >> "5 th cross street ,Atlanta"
        request.getAttribute(ADDRESS1_ADDRESSLINE2) >> "7 th cross street ,Atlanta"
        request.getAttribute(ADDRESS1_POSTAL) >> "12345"
        request.getAttribute(ADDRESS1_CITY) >> "Wilmington"
        request.getAttribute(ADDRESS1_STATE) >> "NY"
        request.getAttribute(ADDRESS1_COUNTRY) >> "US"

        when:
        varOrderManagerChase.getUser(request)

        then:
        final IllegalArgumentException exception = thrown()
        exception.message == "User id(request.getUserPrincipal().getName()) is blank."
    }

    def "getUser"() {

        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getAttribute(LOCALE) >> "ab_ZA_35345"
        request.getAttribute(USERID) >> "LPB2ZZSURExp"
        request.getAttribute(ADDRESS1_ADDRESSLINE1) >> "5 th cross street ,Atlanta"
        request.getAttribute(ADDRESS1_ADDRESSLINE2) >> "7 th cross street ,Atlanta"
        request.getAttribute(ADDRESS1_POSTAL) >> "12345"
        request.getAttribute(ADDRESS1_STATE) >> "NY"
        request.getAttribute(ADDRESS1_COUNTRY) >> "US"
        request.getAttribute(ADDRESS1_CITY) >> "Wilmington"
        request.getAttribute(EMAIL) >> "ab_ZA@bd.com"
        request.getAttribute(TELEPHONE) >> "542524323"
        request.getAttribute(POINTBALANCE) >> "1000"

        request.getUserPrincipal() >> Mock(Principal)
        request.getUserPrincipal().getName() >> "userPrincipal"

        when:
        UserChase user = varOrderManagerChase.getUser(request)

        then:
        user != null
        user.getUserId() == request.getAttribute(USERID)
        user.getAddresses()[0].getAddress1() == request.getAttribute(ADDRESS1_ADDRESSLINE1)
        user.getAddresses()[0].getAddress2() == request.getAttribute(ADDRESS1_ADDRESSLINE2)
        user.getPoints() == Integer.parseInt(request.getAttribute(POINTBALANCE) as String)
        user.getEmail() == request.getAttribute(EMAIL)
        user.getAddresses()[0].getPhoneNumber() == request.getAttribute(TELEPHONE)
    }

    def "test selectUser for IllegalArgumentException"() {

        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getParameter(CommonConstants.USER_ID) >> "Anonymous"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "ChasePremium"


        request.getSession() >> Mock(HttpSession)

        applicationProperties.getProperty(CommonConstants.CHASE_SSO_ROOT_URL_KEY) >> "http://ssorooturl"
        applicationProperties.getProperty(CommonConstants.CHASE_ANALYTICS_ROOT_URL_KEY) >> "http://ssoanalyticsurl"
        programService.getProgram(_ as String, _ as String, _ as Locale) >> Mock(Program)

        when:
        varOrderManagerChase.selectUser(request)

        then:
        final B2RException exception = thrown()
        exception.message == "Program doesn't exist or inactive for programId ChasePremium"

    }


    def "test placeOrder  for local"() {

        given:
        Program programDetails = VarOrderManagerChaseTestDataFactory.getProgramDetails(true)
        Order orderDetails = VarOrderManagerChaseTestDataFactory.getOrderDetails()
        User userDetails = VarOrderManagerChaseTestDataFactory.getUserDetails()

        messageService.getMessage(*_)>>""
        RedemptionResponse redemptionResponse = Mock(RedemptionResponse)
        varIntegrationServiceLocalImpl.performRedemption(_ as Order, _ as User, _ as Program) >> redemptionResponse

        when:
        def placeOrderSuccess = varOrderManagerChase.placeOrder(orderDetails, userDetails, programDetails)

        then:
        placeOrderSuccess
    }


    def "test placeOrder  for remote"() {

        given:
        Program programDetails = VarOrderManagerChaseTestDataFactory.getProgramDetails(false)
        Order orderDetails = VarOrderManagerChaseTestDataFactory.getOrderDetails()
        User userDetails = VarOrderManagerChaseTestDataFactory.getUserDetails()

        messageService.getMessage(*_)>>""
        RedemptionResponse redemptionResponse = Mock(RedemptionResponse)
        varIntegrationServiceRemoteImpl.performRedemption(_ as Order, _ as User, _ as Program) >> redemptionResponse

        when:
        def placeOrderSuccess = varOrderManagerChase.placeOrder(orderDetails, userDetails, programDetails)

        then:
        placeOrderSuccess
    }


    def "test placeOrder  failure"() {

        given:
        Program programDetails = Mock(Program)
        Order orderDetails = VarOrderManagerChaseTestDataFactory.getOrderDetails()
        User userDetails = VarOrderManagerChaseTestDataFactory.getUserDetails()

        def messageService = Mock(MessageService)
        programDetails.getIsLocal()>> true
        messageService.getMessage(*_)>>""

        when:
        def placeOrderSuccess = varOrderManagerChase.placeOrder(orderDetails, userDetails, programDetails)

        then:
        !placeOrderSuccess
    }

    def "test cancelOrder for local"() {

        given:
        Program programDetails = VarOrderManagerChaseTestDataFactory.getProgramDetails(true)
        Order orderDetails = VarOrderManagerChaseTestDataFactory.getOrderDetails()
        User userDetails = VarOrderManagerChaseTestDataFactory.getUserDetails()

        CancelRedemptionResponse cancelRedemptionResponse = new CancelRedemptionResponse()
        varIntegrationServiceLocalImpl.performRedemption(_ as Order, _ as User, _ as Program) >> cancelRedemptionResponse

        when:
        def cancelOrderSuccess = varOrderManagerChase.cancelOrder(orderDetails, userDetails, programDetails)

        then:
        cancelOrderSuccess

    }


    def "test cancelOrder for remote"() {

        given:
        Program programDetails = VarOrderManagerChaseTestDataFactory.getProgramDetails(false)
        Order orderDetails = VarOrderManagerChaseTestDataFactory.getOrderDetails()
        User userDetails = VarOrderManagerChaseTestDataFactory.getUserDetails()

        CancelRedemptionResponse cancelRedemptionResponse = new CancelRedemptionResponse()
        varIntegrationServiceRemoteImpl.performRedemption(_ as Order, _ as User, _ as Program) >> cancelRedemptionResponse

        when:
        def cancelOrderSuccess = varOrderManagerChase.cancelOrder(orderDetails, userDetails, programDetails)

        then:
        cancelOrderSuccess

    }

    def " test updateOrderStatus "() {

        given:
        Map<String, Object> properties = new HashedMap<>();
        final String varOrderId = "1"
        final String orderId = "E00000007"
        final String lineNum = "10"
        String carrier = "mock"
        String shippingDesc = "speed delivery"
        String trackingNum = "4147202003368576"
        final String status = "4"
        final Double points = 1000

        when:
        def updateOrderSuccess = varOrderManagerChase.updateOrderStatus(properties, varOrderId, orderId, lineNum, carrier, shippingDesc, trackingNum, status, points)

        then:
        !updateOrderSuccess

    }

    def " test getUserPoints for local"() {
        given:
        Program programDetails = VarOrderManagerChaseTestDataFactory.getProgramDetails(true)
        User userDetails = VarOrderManagerChaseTestDataFactory.getUserDetails()

        varIntegrationServiceLocalImpl.getLocalUserPoints(_ as User) >> 1000

        when:
        def points = varOrderManagerChase.getUserPoints(userDetails, programDetails)

        then:
        points == 1000
    }


    def " test getUserPoints for remote"() {
        given:
        Program programDetails = VarOrderManagerChaseTestDataFactory.getProgramDetails(false)
        User userDetails = VarOrderManagerChaseTestDataFactory.getUserDetails()

        varIntegrationServiceRemoteImpl.getUserPoints(_ as String, _ as String, _ as String, _ as Map) >> 2000

        when:
        def points = varOrderManagerChase.getUserPoints(userDetails, programDetails)

        then:
        points == 2000
    }

    def "getBalanceUserPoints"(){
        setup:

        def order = Mock(Order)
        def order1 = Mock(Order)
        User user = new User()
        user.setInitialUserBalance(10000)
        User user1 = new User()
        user1.setInitialUserBalance(0)
        order.getOrderTotalPointsIncludingDiscountsAndCredits() >> 1000
        order1.getOrderTotalPointsIncludingDiscountsAndCredits() >> 1000
        when:
        varOrderManagerChase.getBalanceUserPoints(order,user)
        varOrderManagerChase.getBalanceUserPoints(order1,user1)
        then:
        user1.getPoints() == 0 && user.getPoints() == 9000

    }

    def "test orderPlace for Exception"() {

        setup:

        def order = Mock(Order)
        User user = new User()
        def program = Mock(Program)
        program.getIsLocal()>>true
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110
        def messageService = Mock(MessageService)

        when:
        boolean result = varOrderManagerChase.placeOrder(order, user, program);

        then:
        result == false && order.getVarOrderId().equals("110")

    }

    def "selectUser - SAMLLogin - Chase Adobe Analytics "() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP) >> " demo "
        mockSAMLChaseAttributes(request)
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.ANALYTICS_WINDOW.value) >> "jp_rpc=0420&jp_aoc=00420"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.ANALYTICS_URL.value) >> "jp_cmp=cc/mom_nov_2021/ema/freedom_applestore"


        AccountInfo accountInfo = new AccountInfo()
        UserInformation userInformation = new UserInformation()
        userInformation.setDeceased(true)
        accountInfo.setUserInformation(userInformation)
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> accountInfo
        varIntegrationServices.put("varIntegrationServiceLocalImpl", varIntegrationServiceLocalImpl)
        Program program = new Program()
        program.setIsActive(true)
        program.setVarId("Chase")
        programService.getProgram(_, _, _) >> program
        populateChaseMockInfo(request)

        when:
        UserChase user = (UserChase) varOrderManagerChase.selectUser(request)
        then:
        user != null
        user.getAnalyticsWindow().get("jp_rpc") == "0420"
        user.getAnalyticsWindow().get("jp_aoc") == "00420"
        user.getAnalyticsUrl() == "jp_cmp=cc/mom_nov_2021/ema/freedom_applestore"
    }

    private void mockSAMLChaseAttributes(HttpServletRequest request) {
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.VARID.value) >> "Chase"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.USERID.value) >> "user"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.PROGRAMID.value) >> "b2s_qa_only"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.LOCALE.value) >> "en_US"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.NAVBACKURL.value) >> "naveBackURL"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.BROWSEONLY.value) >> "false"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.SESSIONSTATE.value) >> "session"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.FIRSTNAME.value) >> "Sethu"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.LASTNAME.value) >> "Sundar"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.ADDRESS1_ADDRESSLINE1.value) >> "address1"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.ADDRESS1_CITY.value) >> "NY"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.ADDRESS1_STATE.value) >> "NY"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.ADDRESS1_POSTAL.value) >> "12233"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.ADDRESS1_COUNTRY.value) >> "US"
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.POINTBALANCE.value) >> "1010001"
    }

    private void getHttpServletRequest(HttpServletRequest request) {
        HttpSession httpSession = new MockHttpSession()
        request.getAttribute(CommonConstants.PROGRAM_ID) >> "b2s_qa_only"
        request.getSession() >> httpSession
        request.getParameter(CommonConstants.VAR_ID) >> "CHASE"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        request.getParameter("discountCode") >> "discountCode"
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.requestURL >> new StringBuffer("https://Chase-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome" +
                ".do")
        request.getParameterValues(CommonConstants.RELAY_STATE) >> VarOrderManagerTestDataFactory.getRelayState()
        request.getAttribute(CommonConstants.LOCALE) >> "en_US"
        request.getUserPrincipal() >> Mock(Principal)
        request.getUserPrincipal().getName() >> "User Principal"
        request.getAttribute("AdditionalInfo") >> "AdditionalInfo"
    }

    private void populateChaseMockInfo(HttpServletRequest request) {
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
    }


}
