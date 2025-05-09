package com.b2s.shop.common.order.var

import com.b2s.apple.entity.DemoUserEntity
import com.b2s.apple.services.MessageService
import com.b2s.apple.services.ProgramService
import com.b2s.db.model.Order
import com.b2s.db.model.OrderLine
import com.b2s.rewards.apple.dao.DemoUserDao
import com.b2s.rewards.apple.integration.model.AccountBalance
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.integration.model.AccountStatus
import com.b2s.rewards.apple.integration.model.Address
import com.b2s.rewards.apple.integration.model.CancelRedemptionResponse
import com.b2s.rewards.apple.integration.model.EmailAddress
import com.b2s.rewards.apple.integration.model.PhoneNumber
import com.b2s.rewards.apple.integration.model.RedemptionResponse
import com.b2s.rewards.apple.integration.model.UserInformation
import com.b2s.rewards.apple.model.OrderAttributeValue
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.exception.B2RException
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.shop.common.order.var.common.VarOrderManagerTestDataFactory
import org.apache.commons.collections4.map.HashedMap
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.Principal

class VAROrderManagerFSVSpec extends Specification {

    def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
    def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
    def programService = Mock(ProgramService)
    def applicationProperties = Mock(Properties)
    def messageService = Mock(MessageService)
    def varIntegrationServices = new HashMap<String, VarIntegrationService>()
    def demoUserDao = Mock(DemoUserDao)

    def varOrderManagerFSV = new VAROrderManagerFSV(applicationProperties: applicationProperties,
            programService:  programService, varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl,
            varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl, demoUserDao: demoUserDao,
            messageService: messageService, varIntegrationServices: varIntegrationServices)

    def "test - getUser"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        mockSAMLFSVAttributes(request)

        request.getUserPrincipal() >> Mock(Principal)
        request.getUserPrincipal().getName() >> 'userPrincipal'

        when:
        User user = varOrderManagerFSV.getUser(request)

        then:
        user != null
        user.getProgramId() == 'b2s_qa_only'
        user.getVarId() == 'FSV'
        user.getUserId() == 'userPrincipal'
        user.getSid() == '40Xsuborf865585'
        user.getCsid() == 'aff5fefeea235168c3d1e0607c52a81256f648eef4a4e3f4ac13a61c04284dd0'
        !user.isBrowseOnly()
    }

    def "test - selectUser for IllegalArgumentException"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getParameter(CommonConstants.USER_ID) >> "test"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> getDemoUserEntity()

        request.getSession() >> Mock(HttpSession)
        programService.getProgram(_ as String, _ as String, _ as Locale) >> null

        when:
        varOrderManagerFSV.selectUser(request)

        then:
        final B2RException exception = thrown()
        exception.message == "Program doesn't exist or inactive for programId b2s_qa_only"

    }

    def "test - SAMLLogin selectUser"() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        mockSAMLFSVAttributes(request)

        Program program = new Program()
        program.setIsActive(true)
        program.setVarId("FSV")
        program.setIsLocal(false)
        programService.getProgram(_, _, _) >> program
        populateFSVMockInfo(request)
        varIntegrationServiceRemoteImpl.getUserProfile(_, _, _) >> getFISAccountInfo()

        when:
        User user = (User) varOrderManagerFSV.selectUser(request)

        then:
        user != null
        user.getProgramId() == 'b2s_qa_only'
        user.getVarId() == 'FSV'
        user.getUserId() == 'User Principal'
        user.getSid() == '40Xsuborf865585'
        user.getCsid() == 'aff5fefeea235168c3d1e0607c52a81256f648eef4a4e3f4ac13a61c04284dd0'
        !user.isBrowseOnly()
    }

    def "test - 5boxLogin selectUser"() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        request.getParameter(CommonConstants.VAR_ID) >> "FSV"
        request.getParameter(CommonConstants.LOCALE) >> "en_US"
        request.getParameter(CommonConstants.IS_ANONYMOUS) >> "true"
        request.getParameter(CommonConstants.IS_BROWSE_ONLY) >> "true"

        AccountInfo accountInfo = new AccountInfo()
        UserInformation userInformation = new UserInformation()
        accountInfo.setUserInformation(userInformation)
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> accountInfo
        varIntegrationServices.put("varIntegrationServiceLocalImpl", varIntegrationServiceLocalImpl)
        Program program = new Program()
        program.setIsActive(true)
        program.setVarId("FSV")
        program.setIsLocal(false)
        programService.getProgram(_, _, _) >> program
        populateFSVMockInfo(request)
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> getDemoUserEntity()

        when:
        User user = (User) varOrderManagerFSV.selectUser(request)

        then:
        user != null
        user.getProgramId() == 'b2s_qa_only'
        user.getVarId() == 'FSV'
        user.getUserId() == 'userId'
        user.isBrowseOnly()
        user.isAnonymous()
    }

    def "test - local getUserPoints"() {
        given:
        Program programDetails = getProgramDetails(true)
        User userDetails = getUserDetails()

        varIntegrationServiceLocalImpl.getLocalUserPoints(_ as User) >> 1000

        when:
        def points = varOrderManagerFSV.getUserPoints(userDetails, programDetails)

        then:
        points == 1000
    }


    def "test - remote getUserPoints"() {
        given:
        Program programDetails = getProgramDetails(false)
        User userDetails = getUserDetails()

        varIntegrationServiceRemoteImpl.getUserPoints(_ as String, _ as String, _ as String, _ as Map) >> 2000

        when:
        def points = varOrderManagerFSV.getUserPoints(userDetails, programDetails)

        then:
        points == 2000
    }

    def "test - getBalanceUserPoints"() {
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
        varOrderManagerFSV.getBalanceUserPoints(order, user)
        varOrderManagerFSV.getBalanceUserPoints(order1, user1)

        then:
        user1.getPoints() == 0 && user.getPoints() == 9000

    }


    def "test - local placeOrder"() {

        given:
        Program programDetails = getProgramDetails(true)
        Order orderDetails = getOrderDetails()
        User userDetails = getUserDetails()

        messageService.getMessage(*_) >> ""
        RedemptionResponse redemptionResponse = Mock(RedemptionResponse)
        varIntegrationServiceLocalImpl.performRedemption(_ as Order, _ as User, _ as Program) >> redemptionResponse

        when:
        def placeOrderSuccess = varOrderManagerFSV.placeOrder(orderDetails, userDetails, programDetails)

        then:
        placeOrderSuccess
    }


    def "test - remote placeOrder"() {

        given:
        Program programDetails = getProgramDetails(false)
        Order orderDetails = getOrderDetails()
        User userDetails = getUserDetails()

        messageService.getMessage(*_) >> ""
        RedemptionResponse redemptionResponse = Mock(RedemptionResponse)
        varIntegrationServiceRemoteImpl.performRedemption(_ as Order, _ as User, _ as Program) >> redemptionResponse

        when:
        def placeOrderSuccess = varOrderManagerFSV.placeOrder(orderDetails, userDetails, programDetails)

        then:
        placeOrderSuccess
    }


    def "test - placeOrder failure"() {
        given:
        Program programDetails = Mock(Program)
        Order orderDetails = getOrderDetails()
        User userDetails = getUserDetails()

        def messageService = Mock(MessageService)
        programDetails.setIsLocal(false)
        messageService.getMessage(*_) >> ""

        when:
        def placeOrderSuccess = varOrderManagerFSV.placeOrder(orderDetails, userDetails, programDetails)

        then:
        !placeOrderSuccess
    }

    def "test - local cancelOrder"() {

        given:
        Program programDetails = getProgramDetails(true)
        Order orderDetails = getOrderDetails()
        User userDetails = getUserDetails()

        CancelRedemptionResponse cancelRedemptionResponse = new CancelRedemptionResponse()
        varIntegrationServiceLocalImpl.performRedemption(_ as Order, _ as User, _ as Program) >> cancelRedemptionResponse

        when:
        def cancelOrderSuccess = varOrderManagerFSV.cancelOrder(orderDetails, userDetails, programDetails)

        then:
        cancelOrderSuccess
    }


    def "test - remote cancelOrder"() {

        given:
        Program programDetails = getProgramDetails(false)
        Order orderDetails = getOrderDetails()
        User userDetails = getUserDetails()

        CancelRedemptionResponse cancelRedemptionResponse = new CancelRedemptionResponse()
        varIntegrationServiceRemoteImpl.performRedemption(_ as Order, _ as User, _ as Program) >> cancelRedemptionResponse

        when:
        def cancelOrderSuccess = varOrderManagerFSV.cancelOrder(orderDetails, userDetails, programDetails)

        then:
        cancelOrderSuccess
    }

    def "test - updateOrderStatus"() {

        given:
        Map<String, Object> properties = new HashedMap<>()
        final String varOrderId = "1"
        final String orderId = "E00000007"
        final String lineNum = "10"
        String carrier = "mock"
        String shippingDesc = "speed delivery"
        String trackingNum = "4147202003368576"
        final String status = "4"
        final Double points = 1000

        when:
        def updateOrderSuccess = varOrderManagerFSV.updateOrderStatus(properties, varOrderId, orderId, lineNum, carrier, shippingDesc, trackingNum, status, points)

        then:
        !updateOrderSuccess
    }

    private void mockSAMLFSVAttributes(HttpServletRequest request) {
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.VARID.value) >> "FSV"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.PROGRAM_ID.value) >> "b2s_qa_only"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.POINTS_BALANCE.value) >> "1001"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.BROWSE_ONLY.value) >> "false"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.ANONYMOUS.value) >> "false"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.LOCALE.value) >> "en_US"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.COUNTRY.value) >> "US"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.NAVBACKURL.value) >> "https://uchooserewards.rewardstepuat.com/b2r/landingHome.do"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.CSID.value) >> "aff5fefeea235168c3d1e0607c52a81256f648eef4a4e3f4ac13a61c04284dd0"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.SID.value) >> "40Xsuborf865585"
        request.getAttribute(VAROrderManagerFSV.SAMLAttributes.KEYSTONE_BASE_URL.value) >> "https://uchooserewards.rewardstepuat.com"
    }

    private void getHttpServletRequest(HttpServletRequest request) {
        HttpSession httpSession = new MockHttpSession()
        request.getAttribute(CommonConstants.PROGRAM_ID) >> "b2s_qa_only"
        request.getSession() >> httpSession
        request.getParameter(CommonConstants.VAR_ID) >> "FSV"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        request.getParameter("discountCode") >> "discountCode"
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.requestURL >> new StringBuffer("https://uchooserewards.rewardstepuat.com/b2r/landingHome.do")
        request.getParameterValues(CommonConstants.RELAY_STATE) >> VarOrderManagerTestDataFactory.getRelayState()
        request.getAttribute(CommonConstants.LOCALE) >> "en_US"
        request.getUserPrincipal() >> Mock(Principal)
        request.getUserPrincipal().getName() >> "User Principal"
        request.getAttribute("AdditionalInfo") >> "AdditionalInfo"
    }

    private void populateFSVMockInfo(HttpServletRequest request) {
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

    DemoUserEntity getDemoUserEntity() {
        DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId()
        demoUserId.setProgramId("b2s_qa_only")
        demoUserId.setVarId("FSV")
        demoUserId.setUserId("userId")

        DemoUserEntity demoUserEntity = new DemoUserEntity()
        demoUserEntity.setFirstname("SM")
        demoUserEntity.setDemoUserId(demoUserId)
        demoUserEntity.setZip("30005")

        return demoUserEntity
    }

    static User getUserDetails() {
        User user = new User()
        user.setPoints(1001)
        user.setProgramid("b2s_qa_only")
        user.setUserid("abc123")
        user.setVarid("FSV")
        user.setPassword("abc123")
        return user
    }

    static Program getProgramDetails(Boolean isLocal) {
        Program program = new Program()
        program.setVarId("FSV")
        program.setProgramId("abc123")
        program.setIsLocal(isLocal)
        program.setIsActive(true)
        return program
    }

    static Order getOrderDetails() {
        return new Order(orderId: 1234, varOrderId: 'var1234', orderDate: new Date(), languageCode: 'en',
                countryCode: 'US', orderLines: new ArrayList<OrderLine>(),
                orderAttributeValues: new ArrayList<OrderAttributeValue>())
    }

    AccountInfo getFISAccountInfo() {
        AccountInfo accountInfoFIS = new AccountInfo()

        AccountBalance accountBalance = new AccountBalance()
        accountBalance.setPointsBalance(510904)
        accountBalance.setPointsName("POINTS")
        accountBalance.setCurrency("POINTS")
        accountInfoFIS.setAccountBalance(accountBalance)

        AccountStatus accountStatus = new AccountStatus()
        accountStatus.setStatusCode("ACTIVE")
        accountStatus.setStatusMessage("Active")
        accountStatus.setAccessType("STANDARD")
        accountInfoFIS.setAccountStatus(accountStatus)

        UserInformation userInformation = new UserInformation()

        userInformation.setFirstName("GOOFY")
        userInformation.setLastName("GOOF")

        PhoneNumber phoneNumber = new PhoneNumber()
        phoneNumber.setNumber("9179239292")
        final PhoneNumber[] phoneNumbers = new PhoneNumber[1]
        phoneNumbers[0] = phoneNumber
        userInformation.setPhoneNumbers(phoneNumbers)

        final Address address = new Address()
        address.setLine1("123 ELM STREET")
        address.setCity("NEW YORK")
        address.setStateCode("NY")
        address.setPostalCode("10036")
        userInformation.setAddress(address)

        EmailAddress emailAddress = new EmailAddress()
        emailAddress.setType("OTHER")
        emailAddress.setEmail("UPQATEST@AFFINITYSOLUTIONS.COM")
        final EmailAddress[] emailAddresses = new EmailAddress[1]
        emailAddresses[0] = emailAddress
        userInformation.setEmailAddresses(emailAddresses)

        Map<String, String> additionalInfo = new HashMap<>()
        additionalInfo.put("headerImageUrl", "https://209.123.134.210/e/FISERV/timages/5741-LOGO.jpg")
        additionalInfo.put("varProgramReturnUrl", "https://alerus.com/")
        additionalInfo.put("retrievedAccountId", "38564620")
        userInformation.setAdditionalInfo(additionalInfo)
        accountInfoFIS.setUserInformation(userInformation)

        accountInfoFIS.setProgramId(Optional.of("40C"))
        return accountInfoFIS
    }
}
