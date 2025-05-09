package com.b2s.shop.common.order.var

import com.b2s.apple.entity.DemoUserEntity
import com.b2s.db.model.Order
import com.b2s.rewards.apple.dao.DemoUserDao
import com.b2s.rewards.apple.integration.model.AccountBalance
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.integration.model.Address
import com.b2s.rewards.apple.integration.model.CancelRedemptionResponse
import com.b2s.rewards.apple.integration.model.EmailAddress
import com.b2s.rewards.apple.integration.model.PhoneNumber
import com.b2s.rewards.apple.integration.model.RedemptionResponse
import com.b2s.rewards.apple.integration.model.UserInformation
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.util.HttpClientUtil
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.apple.services.ProgramService
import com.b2s.shop.common.order.util.OAuthRequestParam
import com.b2s.shop.common.order.var.common.VarOrderManagerChaseTestDataFactory
import com.b2s.shop.common.order.var.common.VarOrderManagerTestDataFactory
import org.apache.commons.collections.MapUtils
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.Principal

class VAROrderManagerUASpec extends Specification {


    private VarIntegrationServiceLocalImpl varIntegrationServiceLocalImpl
    private VarIntegrationServiceRemoteImpl varIntegrationServiceRemoteImpl

    def "getAccountInfo"() {

        setup:

        def user = Mock(User)
        def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
        def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
        def accountInfoLocal = null
        AccountInfo accountInfoRemote = new AccountInfo();
        accountInfoRemote.setPricingTier("UA Pricing")
        def programService = Mock(ProgramService)
        Program program = new Program();
        program.setVarId("UA")
        program.setProgramId("b2s_qa_only")
        program.setIsLocal(true)
        program.setIsActive(true)
        programService.getProgram(_, _, _) >> program
        varIntegrationServiceRemoteImpl.getUserProfile(_,_,_) >> accountInfoRemote
        def varOrderManagerUA = new VAROrderManagerUA(programService: programService)
        when:
        accountInfoLocal = varOrderManagerUA.getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl)
        program.setIsLocal(false)
        accountInfoRemote = varOrderManagerUA.getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl)
        then:
        accountInfoLocal == null && accountInfoRemote != null


    }

    def "test orderPlace for Exception"() {

        setup:

        def order = Mock(Order)
        User user = new User()
        user.getAdditionalInfo().put(CommonConstants.UA_SERVICE_SUBSCRIPTION_DISPLAY_CHECKBOX, "true")
        user.getAdditionalInfo().put(CommonConstants.UA_SERVICE_SUBSCRIPTION_IS_CHECKED, "true")

        def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
        def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
        def applicationProperties = Mock(Properties)
        def httpClient = Mock(HttpClientUtil)

        def program = Mock(Program)
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110

        def varOrderManagerUA = new VAROrderManagerUA(varIntegrationServiceLocalImpl:varIntegrationServiceLocalImpl,
                varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl,  applicationProperties:
                applicationProperties, httpClient: httpClient)
        program.getIsLocal()>>false
        RedemptionResponse redemptionResponse=new RedemptionResponse()
        redemptionResponse.setVarOrderId("123");
        redemptionResponse.getAdditionalInfo().put("fraudStatus", "REVIEW")
        varIntegrationServiceRemoteImpl.performRedemption(*_)>> redemptionResponse
        program.getConfig() >> new HashMap<>()
        program.getConfig().put(CommonConstants.UA_SUPPORTED_CHANNEL_CODE_KEY, "abc")

        when:

        boolean result = varOrderManagerUA.placeOrder(order, user, program);

        then:
        result == true && order.getVarOrderId().equals("110")

    }


    def "test cancelOrder for remote"() {

        given:
        Program programDetails = VarOrderManagerChaseTestDataFactory.getProgramDetails(false)
        Order orderDetails = VarOrderManagerChaseTestDataFactory.getOrderDetails()
        User userDetails = VarOrderManagerChaseTestDataFactory.getUserDetails()

        varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
        def varOrderManagerUA = new VAROrderManagerUA(varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl)
        CancelRedemptionResponse cancelRedemptionResponse = new CancelRedemptionResponse()
        varIntegrationServiceRemoteImpl.performRedemption(_ as Order, _ as User, _ as Program) >> cancelRedemptionResponse

        when:
        def cancelOrderSuccess = varOrderManagerUA.cancelOrder(orderDetails, userDetails, programDetails)

        then:
        cancelOrderSuccess

    }

    def "getUser"() {

        given:
        def varOrderManagerUA = new VAROrderManagerUA(varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl)
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getParameter(OAuthRequestParam.ONLINE_AUTH_CODE.getValue()) >> "82344234"
        request.getParameter(OAuthRequestParam.OFFLINE_AUTH_CODE.getValue()) >> "9219892"

        request.getUserPrincipal() >> Mock(Principal)
        request.getUserPrincipal().getName() >> "userPrincipal"

        when:
        User user = varOrderManagerUA.getUser(request)

        then:
        user != null
        user.getUserId() == "123456789"
        user.getVarId() == "UA"
        user.getProgramId() == "MP"
        MapUtils.isNotEmpty(user.getAdditionalInfo())
        user.getAdditionalInfo().get(CommonConstants.ONLINE_AUTH_CODE) == "82344234"
        user.getAdditionalInfo().get(CommonConstants.OFFLINE_AUTH_CODE) == "9219892"

    }

    def "selectUser - FiveBox "() {

        setup:
        def request = Mock(HttpServletRequest)
        def programService = Mock(ProgramService)
        def applicationProperties = Mock(Properties)
        def demoUserDao = Mock(DemoUserDao)
        def httpClient = Mock(HttpClientUtil)
        def varIntegrationServices = new HashMap<String, VarIntegrationService>()
        def varOrderManagerUA = new VAROrderManagerUA(varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl,
                varIntegrationServices: varIntegrationServices, programService: programService,
                applicationProperties: applicationProperties, demoUserDao: demoUserDao, httpClient: httpClient)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP) >> " demo "
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> getDemoUserEntity()
        mockSAMLUAAttributes(request)
        varOrderManagerUA.selectLocalUser(_, _) >> new User()
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> getUserInfo()
        varIntegrationServices.put("varIntegrationServiceLocalImpl", varIntegrationServiceLocalImpl)
        Program program = new Program()
        program.setIsActive(true)
        program.setVarId("Chase")
        Map<String, Object> config = new HashMap<>()
        config.put(CommonConstants.UA_SUPPORTED_CHANNEL_CODE_KEY, "abc")

        program.setConfig(config)
        program.setIsLocal(false)
        programService.getProgram(_, _, _) >> program
        populateUAMockInfo(request)

        when:
        User user = varOrderManagerUA.selectUser(request)
        then:
        user != null
        user.additionalInfo.size() > 0
        user.additionalInfo.get(CommonConstants.UA_SERVICE_SUBSCRIPTION_DISPLAY_CHECKBOX) == "true"
        user.additionalInfo.get(CommonConstants.UA_SERVICE_SUBSCRIPTION_IS_CHECKED) == "false"

    }

    def "selectUser - OATH "() {

        setup:
        def request = Mock(HttpServletRequest)
        def programService = Mock(ProgramService)
        def applicationProperties = Mock(Properties)
        def demoUserDao = Mock(DemoUserDao)
        def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
        def httpClient = Mock(HttpClientUtil)
        def varIntegrationServices = new HashMap<String, VarIntegrationService>()
        def varOrderManagerUA = new VAROrderManagerUA(varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl,
                varIntegrationServices: varIntegrationServices, programService: programService,
                applicationProperties: applicationProperties, demoUserDao: demoUserDao, httpClient: httpClient)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getParameter(OAuthRequestParam.ONLINE_AUTH_CODE.getValue()) >> "123456"
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> getDemoUserEntity()
        mockSAMLUAAttributes(request)
        varOrderManagerUA.selectLocalUser(_, _) >> new User()
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> getUserInfo()
        varIntegrationServices.put("varIntegrationServiceLocalImpl", varIntegrationServiceLocalImpl)
        Program program = new Program()
        program.setIsActive(true)
        program.setVarId("Chase")
        Map<String, Object> config = new HashMap<>()
        config.put(CommonConstants.UA_SUPPORTED_CHANNEL_CODE_KEY, "abc")

        program.setConfig(config)
        program.setIsLocal(true)
        programService.getProgram(_, _, _) >> program
        populateUAMockInfo(request)

        when:
        User user = varOrderManagerUA.selectUser(request)
        then:
        user != null
        user.getPoints() == 1000
        user.getEmail() == "sethupathy.sundaramoorthy@bakkt.com"
        user.getPhone() == "8148689993"
        user.additionalInfo.size() > 0
        user.additionalInfo.get("fraudSessionID") == "fraud123"
        user.additionalInfo.get("memberID") == "mem123"

    }

    private AccountInfo getUserInfo() {
        AccountInfo accountInfo = new AccountInfo()
        UserInformation userInformation = new UserInformation()
        userInformation.setDeceased(true)

        EmailAddress emailAddress = new EmailAddress()
        emailAddress.setEmail("sethupathy.sundaramoorthy@bakkt.com")
        userInformation.setEmailAddresses([emailAddress] as EmailAddress[])

        PhoneNumber phoneNumber = new PhoneNumber()
        phoneNumber.setNumber("8148689993")
        userInformation.setPhoneNumbers([phoneNumber] as PhoneNumber[])

        Address address = new Address()
        userInformation.setAddress(address)
        Map<String, String> additionalInfo = new HashMap<>()
        additionalInfo.put("memberID", "mem123")
        additionalInfo.put("fraudSessionID", "fraud123")
        userInformation.setAdditionalInfo(additionalInfo)
        accountInfo.setUserInformation(userInformation)

        AccountBalance accountBalance = new AccountBalance()
        accountBalance.setPointsBalance(1000)
        accountInfo.setAccountBalance(accountBalance)
        accountInfo
    }

    private void populateUAMockInfo(HttpServletRequest request) {
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
        demoUserId.setProgramId("MP")
        demoUserId.setVarId("UA")
        demoUserId.setUserId("demo")

        DemoUserEntity demoUserEntity = new DemoUserEntity()
        demoUserEntity.setFirstname("Sethu")
        demoUserEntity.setDemoUserId(demoUserId)
        demoUserEntity.setZip("1234-122-123")

        return demoUserEntity
    }

    private void getHttpServletRequest(HttpServletRequest request) {
        HttpSession httpSession = new MockHttpSession()
        request.getAttribute(CommonConstants.PROGRAM_ID) >> "MP"
        request.getSession() >> httpSession
        request.getParameter(CommonConstants.VAR_ID) >> "UA"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "MP"
        request.getParameter("discountCode") >> "discountCode"
        request.getParameter("pword") >> "pword"
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.requestURL >> new StringBuffer("https://ua.cpdev.bridge2solutions.net/b2r/landingHome" +
                ".do")
        request.getParameterValues(CommonConstants.RELAY_STATE) >> VarOrderManagerTestDataFactory.getRelayState()
        request.getAttribute(CommonConstants.LOCALE) >> "en_US"
        request.getUserPrincipal() >> Mock(Principal)
        request.getUserPrincipal().getName() >> "User Principal"
        request.getAttribute("AdditionalInfo") >> "AdditionalInfo"
    }

    private void mockSAMLUAAttributes(HttpServletRequest request) {
        request.getAttribute(VAROrderManagerChase.SAMLAttributes.VARID.value) >> "UA"
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


}
