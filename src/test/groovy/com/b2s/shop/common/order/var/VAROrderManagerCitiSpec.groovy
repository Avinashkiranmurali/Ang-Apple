package com.b2s.shop.common.order.var

import com.b2s.apple.entity.DemoUserEntity
import com.b2s.apple.services.PricingModelService
import com.b2s.apple.services.ProgramService
import com.b2s.apple.services.VarProgramMessageService
import com.b2s.common.services.discountservice.DiscountServiceClient
import com.b2s.db.model.Order
import com.b2s.rewards.apple.dao.DemoUserDao
import com.b2s.rewards.apple.integration.model.AccountBalance
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.integration.model.UserInformation
import com.b2s.rewards.apple.model.PaymentOption
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.model.VarProgramRedemptionOption
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.shop.common.order.var.common.VarOrderManagerTestDataFactory
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.Principal

import static com.b2s.rewards.common.util.CommonConstants.DBA

class VAROrderManagerCitiSpec extends Specification {
    def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
    def varIntegrationServices = new HashMap<String, VarIntegrationService>()
    def programService = Mock(ProgramService)
    def pricingModelService = Mock(PricingModelService)
    def applicationProperties = Mock(Properties)
    def discountServiceClient = Mock(DiscountServiceClient)
    def varProgramMessageService = Mock(VarProgramMessageService)
    def demoUserDao = Mock(DemoUserDao)

    def varOrderManagerCiti = new VAROrderManagerCiti(varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl,
            varIntegrationServices: varIntegrationServices, programService: programService,
            applicationProperties: applicationProperties, demoUserDao: demoUserDao,
            discountServiceClient: discountServiceClient,varProgramMessageService: varProgramMessageService)


    def "test orderPlace for Exception"() {

        setup:

        def order = Mock(Order)
        User user = new User()
        def program = Mock(Program)
        program.getIsLocal() >> true
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110
       varIntegrationServiceLocalImpl.performRedemption(*_) >> null
        when:
        boolean result = varOrderManagerCiti.placeOrder(order, user, program);

        then:
        result == false && order.getVarOrderId().equals("110")

    }

    def "selectUser - FiveBox "() {

        setup:

        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getScheme() >> "https"
        request.getServerPort() >> 0
        request.getServerName() >> "appleDev"
        request.getAttribute(CommonConstants.CITI_LOGIN_REQUIRED) >> "true"
        DemoUserEntity demoUserEntity = VarOrderManagerTestDataFactory.getDemoUserEntity()
        DemoUserEntity.DemoUserId demoUserId = demoUserEntity.getDemoUserId()
        demoUserId.setProgramId("b2s_qa_only")
        demoUserId.setVarId("Citi")
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> demoUserEntity
        AccountInfo accountInfo = getAccountInformation()
        UserInformation userInformation = new UserInformation()
        userInformation.setDeceased(true)
        accountInfo.setUserInformation(userInformation)
        applicationProperties.getProperty(CommonConstants.CITI_HEADER_NAME_FORMAT) >> "Citi Header"
        varProgramMessageService.getMessages(_, _, _) >> applicationProperties

        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> accountInfo
        varIntegrationServices.put("varIntegrationServiceLocalImpl", varIntegrationServiceLocalImpl)
        populateCitiMockInfo(request)

        when:
        User user = varOrderManagerCiti.selectUser(request)
        then:
        user != null

    }

    def "selectUser - SAML "() {

        setup:

        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.CITI_SAML_REQ_PARAM_FOR_SAML_RESP) >> "elTOken"
        request.getAttribute(VAROrderManagerCiti.SAMLAttributes.COUNTRY_CODE.value) >> "AU"
        request.getAttribute(VAROrderManagerCiti.SAMLAttributes.LANGUAGE_CODE.value) >> "ENG"
        request.getAttribute(VAROrderManagerCiti.SAMLAttributes.MBR_TIER_CODE.value) >> "MBR"
        request.getAttribute(VAROrderManagerCiti.SAMLAttributes.SESSION_STATE.value) >> "L"
        request.getAttribute(VAROrderManagerCiti.SAMLAttributes.MEMBER_ID.value) >> "MemberId"
        request.getAttribute(VAROrderManagerCiti.SAMLAttributes.MBR_DISPLAY_NAME.value) >> "dispayName"
        request.getAttribute(VAROrderManagerCiti.SAMLAttributes.MBR_AVAILABLE_BALANCE.value) >> "000010000"
        request.getAttribute(VAROrderManagerCiti.SAMLAttributes.MBR_ADDRESS_1.value) >> "ADDRESS 1"
        request.getAttribute(VAROrderManagerCiti.SAMLAttributes.SOURCE_CODE.value) >> "SOURCE 1"

        request.getScheme() >> "https"
        request.getServerPort() >> 0
        request.getServerName() >> "appleDev"
        request.getAttribute(CommonConstants.CITI_LOGIN_REQUIRED) >> "true"
        DemoUserEntity demoUserEntity = VarOrderManagerTestDataFactory.getDemoUserEntity()
        DemoUserEntity.DemoUserId demoUserId = demoUserEntity.getDemoUserId()
        demoUserId.setProgramId("b2s_qa_only")
        demoUserId.setVarId("Citi")
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> demoUserEntity
        AccountInfo accountInfo = getAccountInformation()
        UserInformation userInformation = new UserInformation()
        userInformation.setDeceased(true)
        accountInfo.setUserInformation(userInformation)
        applicationProperties.getProperty(CommonConstants.CITI_HEADER_NAME_FORMAT) >> "Citi Header"
        varProgramMessageService.getMessages(_, _, _) >> applicationProperties

        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> accountInfo
        varIntegrationServices.put("varIntegrationServiceLocalImpl", varIntegrationServiceLocalImpl)
        populateCitiMockInfo(request)

        when:
        User user = varOrderManagerCiti.selectUser(request)
        then:
        user != null

    }

    def "test OBO redeem - SAML "() {

        setup:

        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getSession().setAttribute("COUNTRY_CODE", "HK")
        request.getSession().setAttribute("LANGUAGE_CODE", "ENG")
        request.getSession().setAttribute("MBR_TIER_CODE", "HKREWARDS")
        request.getSession().setAttribute("CITI_SAML_SESSION_STATE", "O")
        request.getSession().setAttribute("AGENT_ID", "Agent1")
        request.getSession().setAttribute("BYPASS_EMAIL_ADDRESS_IND", "N")
        request.getSession().setAttribute("DISPLAY_CURRENCY_CODE", "HKD")
        request.getSession().setAttribute("DISPLAY_CURRENCY_CODE", "N")
        request.getSession().setAttribute("GR_ID", "GR132465767")
        request.getSession().setAttribute("IP_ADDRESS", "10.25.22.125")
        request.getSession().setAttribute("KEEP_ALIVE_URL", "https://pat.cbgrus.uatglobalrewards.com/keepSessionAlive.htm")
        request.getSession().setAttribute("LOGOUT_URL", "https://pat.cbgrus.uatglobalrewards.com/logout.htm")
        request.getSession().setAttribute("MBR_ADDRESS_1", "FLAT")
        request.getSession().setAttribute("MBR_ADDRESS_2", "BLACK")
        request.getSession().setAttribute("MBR_ADDRESS_3", "MAIN")
        request.getSession().setAttribute("MBR_AVAILABLE_BALANCE", "328514224")
        request.getSession().setAttribute("MBR_BUSINESS_PHONE", "29627083")
        request.getSession().setAttribute("MBR_CELL_PHONE", "66931974")
        request.getSession().setAttribute("MBR_CITY", "WAN")
        request.getSession().setAttribute("MBR_DISPLAY_NAME", "Mr. Lastname")
        request.getSession().setAttribute("MBR_EMAIL_ID", "CitiGRBuild_TestEmail@Epsilon.com")
        request.getSession().setAttribute("MBR_FORMATTED_POSTAL_CODE", "999077")
        request.getSession().setAttribute("MBR_HOME_PHONE", "29627082")
        request.getSession().setAttribute("MBR_POINTS_SUB_TYPE", "Points")
        request.getSession().setAttribute("MBR_SPLIT_TENDER_ELIG", "N")
        request.getSession().setAttribute("MBR_STATE", "HONG")
        request.getSession().setAttribute("MEMBER_ALLOWED_TO_REDEEM", "Y")
        request.getSession().setAttribute("MEMBER_ID", "9344067249608014")
        request.getSession().setAttribute("NAV_BACK_URL", "https://pat.cbgrus.uatglobalrewards.com/welcome.htm")
        request.getSession().setAttribute("RELAY_STATE", "Landing.html?type=MERCHANDISE")
        request.getSession().setAttribute("SESSION_ID", "5e04d3c6-3a8b-4672-bd27-62f9de95a6ca")
        request.getSession().setAttribute("SESSION_ID", "N")

        request.getScheme() >> "https"
        request.getServerPort() >> 0
        request.getServerName() >> "appleDev"
        request.getAttribute(CommonConstants.CITI_LOGIN_REQUIRED) >> "true"
        DemoUserEntity demoUserEntity = VarOrderManagerTestDataFactory.getDemoUserEntity()
        DemoUserEntity.DemoUserId demoUserId = demoUserEntity.getDemoUserId()
        demoUserId.setProgramId("b2s_qa_only")
        demoUserId.setVarId("Citi")
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> demoUserEntity
        AccountInfo accountInfo = getAccountInformation()
        UserInformation userInformation = new UserInformation()
        userInformation.setDeceased(true)
        accountInfo.setUserInformation(userInformation)
        applicationProperties.getProperty(CommonConstants.CITI_HEADER_NAME_FORMAT) >> "Citi Header"
        varProgramMessageService.getMessages(_, _, _) >> applicationProperties
        Program program = VarOrderManagerTestDataFactory.getProgramDetails(true)
        program.setRedemptionOptions(new HashMap<String, List<VarProgramRedemptionOption>>())
        program.getConfig().put(CommonConstants.CITI_OBO_REDEEMABLE, CommonConstants.CITI_OBO_REDEEM_POINTS_ONLY)
        programService.getProgram(_, _, _) >> program

        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> accountInfo
        varIntegrationServices.put("varIntegrationServiceLocalImpl", varIntegrationServiceLocalImpl)
        populateCitiMockInfo(request)

        when:
        User user = varOrderManagerCiti.selectUser(request)
        then:
        program.getConfig().get(CommonConstants.CITI_OBO_REDEEM_PAYMENT_TEMPLATE) == CommonConstants.CITI_OBO_REDEEM_POINTS_DEFAULT
        program.getConfig().get(CommonConstants.CITI_OBO_REDEEM_PAYMENT_TYPE) == CommonConstants.CITI_OBO_REDEEM_POINTS_ONLY

    }

    private void getHttpServletRequest(HttpServletRequest request) {
        HttpSession httpSession = new MockHttpSession()
        request.getSession() >> httpSession
        request.getSession().getAttribute(CommonConstants.CITI_SAML_SESSION_STATE) >> "O"
        request.getParameter(CommonConstants.VAR_ID) >> "Citi"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        request.getParameter("discountCode") >> "discountCode"
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.requestURL >> new StringBuffer("https://Citi-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        request.getParameterValues(CommonConstants.RELAY_STATE) >> VarOrderManagerTestDataFactory.getRelayState()
        request.getAttribute(CommonConstants.LOCALE) >> "en_CA"
        request.getUserPrincipal() >> Mock(Principal)
        request.getUserPrincipal().getName() >> "User Principal"
        request.getAttribute("AdditionalInfo") >> "AdditionalInfo"
    }

    AccountInfo getAccountInformation() {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setPricingTier("Citi Pricing")
        accountInfo.setUserInformation(new UserInformation());

        AccountBalance accountBalance = new AccountBalance()
        accountBalance.setPointsBalance(1000)
        accountBalance.setCurrency("USD")
        accountInfo.setAccountBalance(accountBalance)

        return accountInfo
    }

    private void populateCitiMockInfo(HttpServletRequest request) {
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
        UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUri() >> new URI("https://WF-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        request.getAttribute(CommonConstants.POINTS_BALANCE) >> "1000"
        Program program = VarOrderManagerTestDataFactory.getProgramDetails(true)
        Map<String, Object> config = new HashMap<String, Object>()
        config.put(CommonConstants.VIMS_ADDITIONAL_INFO, "AdditionalInfo")
        config.put(VAROrderManagerWF.SAMLAttributes.BROWSE_ONLY.value, "true")
        config.put(CommonConstants.IGNORE_PROFILE_ADDRESS, true)
        config.put(CommonConstants.IS_BROWSE_ONLY, true)
        config.put(CommonConstants.CITI_OBO_REDEEMABLE, "points_only")
        program.setConfig(config)
        PaymentOption paymentOption = new  PaymentOption()
        List<PaymentOption> paymentOptionList = new ArrayList<PaymentOption>()
        paymentOptionList.add(paymentOption)
        program.setPayments(paymentOptionList)
        programService.getProgram(_, _, _) >> program
        programService.getProgramConfigValue(_, _, _) >> Optional.of("true")
        pricingModelService.getPricingModels(_, _, _) >> null
        applicationProperties.get(CommonConstants.DISCOUNTCODE_QUERY_PARAM_OTP) >> "discountCode"
        applicationProperties.getProperty("session.timeout.minutes") >> "200"
        discountServiceClient.getValidDiscountCode(_, _) >> VarOrderManagerTestDataFactory.getCouponDetails()
        varIntegrationServiceLocalImpl.getLocalUserPoints(_) >> null
    }
}