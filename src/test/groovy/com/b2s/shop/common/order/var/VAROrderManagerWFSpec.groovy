package com.b2s.shop.common.order.var

import com.b2s.apple.entity.VarProgramConfigEntity
import com.b2s.apple.services.MessageService
import com.b2s.apple.services.PricingModelService
import com.b2s.apple.services.ProgramService
import com.b2s.common.services.discountservice.DiscountServiceClient
import com.b2s.db.model.Order
import com.b2s.rewards.apple.dao.DemoUserDao
import com.b2s.rewards.apple.dao.VarProgramConfigDao
import com.b2s.rewards.apple.integration.model.AccountBalance
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.integration.model.UserInformation
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.rewards.security.util.ExternalUrlConstants
import com.b2s.shop.common.User
import com.b2s.shop.common.order.var.common.VarOrderManagerTestDataFactory
import org.apache.commons.lang.StringUtils
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.Principal

import static com.b2s.rewards.common.util.CommonConstants.*

class VAROrderManagerWFSpec extends Specification {
    def programService = Mock(ProgramService)
    def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
    def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
    def varProgramConfigEntity = Mock(VarProgramConfigEntity)
    def applicationProperties = Mock(Properties)
    def discountServiceClient = Mock(DiscountServiceClient)
    def pricingModelService = Mock(PricingModelService)
    def demoUserDao = Mock(DemoUserDao)
    def varProgramConfigDao = Mock(VarProgramConfigDao)
    def varIntegrationServices = new HashMap<String, VarIntegrationService>()
    @Subject
    def varOrderManagerWF = new VAROrderManagerWF(varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl,
            programService: programService, applicationProperties: applicationProperties,
            discountServiceClient: discountServiceClient, varProgramConfigDao: varProgramConfigDao,
            varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl, demoUserDao: demoUserDao,
            varIntegrationServices: varIntegrationServices)

    def "getAccountInfo"() {

        setup:

        def user = Mock(User)
        def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
        def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
        def accountInfoLocal = null
        AccountInfo accountInfoRemote = new AccountInfo();
        accountInfoRemote.setPricingTier("WF Pricing")
        def programService = Mock(ProgramService)
        Program program = new Program();
        program.setVarId("WF")
        program.setProgramId("b2s_qa_only")
        program.setIsLocal(true)
        program.setIsActive(true)
        programService.getProgram(_, _, _) >> program
        varIntegrationServiceRemoteImpl.getUserProfile(_, _, _) >> accountInfoRemote
        def varOrderManagerWF = new VAROrderManagerWF(programService: programService)
        when:
        accountInfoLocal = varOrderManagerWF.getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl)
        program.setIsLocal(false)
        accountInfoRemote = varOrderManagerWF.getAccountInfo(user, program.getIsLocal(),
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
        def varOrderManagerWF = new VAROrderManagerWF(messageService: messageService)
        messageService.getMessage(*_) >> ""


        when:
        boolean result = varOrderManagerWF.placeOrder(order, user, program);

        then:
        result == false && order.getVarOrderId().equals("110")
    }
    def "selectUser - SAMLLogin "() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.VARID.value) >> "WF"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.PROGRAM_ID.value) >> "b2s_qa_only"
        AccountInfo accountInfo = getAccountInformation()
        UserInformation userInformation = new UserInformation()
        userInformation.setDeceased(true)
        accountInfo.setUserInformation(userInformation)
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> accountInfo
        varIntegrationServices.put("varIntegrationServiceLocalImpl",varIntegrationServiceLocalImpl)
        Program program = getMockProgram()
        programService.getProgram(_, _, _) >> program
        populateWFMockInfo(request)

        when:
        UserWF user = (UserWF) varOrderManagerWF.selectUser(request)
        then:
        user != null
        user.getNavflag() == null


    }

    def "test selectUser for IllegalArgumentException"() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> getAccountInformation()
        Program program = getMockProgram()
        programService.getProgram(_, _, _) >> program
        populateWFMockInfo(request)

        when:
        varOrderManagerWF.selectUser(request)
        then:

        final IllegalArgumentException exception = thrown()
        exception.message == "VAR ID and PROGRAM ID cannot be empty"

    }

    def "selectUser - SAMLLogin - Anonymous "() {

        setup:
        def request = Mock(HttpServletRequest)
        def navFlag = "1"
        def keepAliveUrl =  "http://epsilin.KeepaliveMock.com"
        getHttpServletRequest(request)
        getSelectUserMock(request, navFlag, keepAliveUrl)

        when:
        UserWF user = (UserWF) varOrderManagerWF.selectUser(request)
        HashMap<String, Serializable> externalUrls = request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS)
        then:
        user != null
        user.getNavflag()!=null && user.getNavflag().toString().equalsIgnoreCase("1")
        externalUrls != null
        externalUrls.get(ExternalUrlConstants.KEEP_ALIVE_URL_POINTS_BANK) == "http://epsilin.KeepaliveMock.com"
        externalUrls.get(ExternalUrlConstants.KEEP_ALIVE_URL) == null
    }

    def "selectUser - SAMLLogin - WF - Epsilon KeepAlive override "() {

        setup:
        def request = Mock(HttpServletRequest)
        def navFlag = "2"
        def keepAliveUrl =  "http://epsilin.KeepaliveMock.com,http://wf.KeepaliveMock.com"
        getHttpServletRequest(request)
        getSelectUserMock(request, navFlag, keepAliveUrl)
        applicationProperties.getProperty(_) >> ""
        applicationProperties.getProperty(_, _) >> ""

        when:
        UserWF user = (UserWF) varOrderManagerWF.selectUser(request)
        HashMap<String, Serializable> externalUrls = request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS)
        then:
        user != null
        user.getNavflag()!=null && user.getNavflag().toString().equalsIgnoreCase("2")
        externalUrls != null
        externalUrls.get(ExternalUrlConstants.KEEP_ALIVE_URL_POINTS_BANK) == "http://epsilin.KeepaliveMock.com"
        externalUrls.get(ExternalUrlConstants.KEEP_ALIVE_URL) == "http://wf.KeepaliveMock.com"
    }

    def "selectUser - Anonymous"() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "Anonymous_user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE) >> "true"
        populateWFMockInfo(request)
        Program program = getMockProgram()
        programService.getProgram(_, _, _) >> program

        when:
        UserWF user = (UserWF) varOrderManagerWF.selectUser(request)
        then:
        user != null
    }

    def "selectUser - FiveBox"() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> null
        request.getParameter(CommonConstants.DECEASED) >> "true"
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> VarOrderManagerTestDataFactory.getDemoUserEntity()
        request.getParameter(CommonConstants.IS_ANONYMOUS) >> "true"
        request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE) >> "false"
        populateWFMockInfo(request)
        Program program = getMockProgram()
        programService.getProgram(_, _, _) >> program

        when:
        User user = varOrderManagerWF.selectUser(request)
        then:
        user != null
    }

    def "test WF - 2-Way Navigation - Sign Off"() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.VARID.value) >> "WF"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.PROGRAM_ID.value) >> "b2s_qa_only"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.NAVFLAG.value) >> nav

        //SignOut Redirect URL
        applicationProperties.getProperty("cp.1.signOutUrl") >> "https://wf.com/cp1-signOut"
        applicationProperties.getProperty("cp.2.signOutUrl") >> "https://wf.com/cp2-signOut"
        applicationProperties.getProperty("smb.1.signOutUrl") >> "https://wf.com/smb1-signOut"
        applicationProperties.getProperty("smb.2.signOutUrl") >> "https://wf.com/smb2-signOut"

        //Partner SignOut URL
        applicationProperties.getProperty("cp.2.partnerSignOutUrl", StringUtils.EMPTY) >> "https://wf.com/cp2-PartnerSignOut"
        applicationProperties.getProperty("smb.2.partnerSignOutUrl", StringUtils.EMPTY) >> "https://wf.com/smb2-PartnerSignOut"

        //TimeOut Redirect URL
        applicationProperties.getProperty("WF.timeOutUrl", StringUtils.EMPTY) >> "https://wf.com/WF-TimeOutUrl"

        //Partner TimeOut URL
        applicationProperties.getProperty("WF.2.partnerTimeOutUrl", StringUtils.EMPTY) >> "https://wf.com/WF-PartnerTimeOut2"
        applicationProperties.getProperty("cp.2.partnerTimeOutUrl", StringUtils.EMPTY) >> "https://wf.com/cp2-PartnerTimeOut"
        applicationProperties.getProperty("smb.2.partnerTimeOutUrl", StringUtils.EMPTY) >> "https://wf.com/smb2-PartnerTimeOut"

        varIntegrationServices.put("varIntegrationServiceLocalImpl", varIntegrationServiceLocalImpl)
        Program program = getMockProgram()
        program.getConfig().put(CommonConstants.IS_TWO_WAY_NAV_ENABLED, true)
        program.getConfig().put("programType", type)

        programService.getProgram(_, _, _) >> program
        populateWFMockInfo(request)

        expect:
        UserWF user = (UserWF) varOrderManagerWF.selectUser(request)
        def emptyArray = ["", "", ""] as String[]
        HashMap<String, Serializable> externalUrls = request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS)
        List<String> partnerSignOutUrls = Objects.isNull(externalUrls.get(ExternalUrlConstants.PARTNER_SIGN_OUT_URLS))
                ? new ArrayList<>(Arrays.asList(emptyArray)) : (List<String>) externalUrls.get(ExternalUrlConstants.PARTNER_SIGN_OUT_URLS)
        List<String> partnerTimeOutUrl = Objects.isNull(externalUrls.get(ExternalUrlConstants.PARTNER_TIME_OUT_URLS))
                ? new ArrayList<>(Arrays.asList(emptyArray)) : (List<String>) externalUrls.get(ExternalUrlConstants.PARTNER_TIME_OUT_URLS)

        signOutUrlValue == externalUrls.get(ExternalUrlConstants.SIGN_OUT_URL)
        timeOutUrlValue == externalUrls.get(ExternalUrlConstants.TIME_OUT_URL)
        partnerTimeOutUrl1 == partnerTimeOutUrl.get(0)
        partnerTimeOutUrl2 == partnerTimeOutUrl.get(1)
        partnerSignOutUrl == partnerSignOutUrls.get(0)

        where:
        nav | type  || signOutUrlValue               || timeOutUrlValue                || partnerSignOutUrl                    || partnerTimeOutUrl1                  || partnerTimeOutUrl2
        "1" | "cp"  || "https://wf.com/cp1-signOut"  || "https://wf.com/WF-TimeOutUrl" || ""                                   || ""                                  || ""
        "2" | "cp"  || "https://wf.com/cp2-signOut"  || "https://wf.com/WF-TimeOutUrl" || "https://wf.com/cp2-PartnerSignOut"  || "https://wf.com/WF-PartnerTimeOut2" || "https://wf.com/cp2-PartnerTimeOut"
        "1" | "smb" || "https://wf.com/smb1-signOut" || "https://wf.com/WF-TimeOutUrl" || ""                                   || ""                                  || ""
        "2" | "smb" || "https://wf.com/smb2-signOut" || "https://wf.com/WF-TimeOutUrl" || "https://wf.com/smb2-PartnerSignOut" || "https://wf.com/WF-PartnerTimeOut2" || "https://wf.com/smb2-PartnerTimeOut"
    }

    def "selectUser - SAMLLogin with Session User Information "() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.VARID.value) >> "WF"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.PROGRAM_ID.value) >> "b2s_qa_only"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.AUTHORIZED_FIRST_NAME.value) >> saml_FIRST_NAME
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.AUTHORIZED_LAST_NAME.value) >> saml_LAST_NAME

        AccountInfo accountInfo = getAccountInformation()
        UserInformation userInformation = new UserInformation()
        userInformation.setDeceased(true)
        accountInfo.setUserInformation(userInformation)
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> accountInfo
        varIntegrationServices.put("varIntegrationServiceLocalImpl",varIntegrationServiceLocalImpl)
        Program program = getMockProgram(programType)
        programService.getProgram(_, _, _) >> program
        populateWFMockInfo(request)

        when:
        UserWF user = (UserWF) varOrderManagerWF.selectUser(request)

        then:
        user != null
        user.getAdditionalInfo().get(OWNER_TYPE) == user_AddInfo_OwnerType

        where:
        programType | saml_FIRST_NAME | saml_LAST_NAME || user_AddInfo_OwnerType
        SMB_PROGRAM | "MATH"          | "SAN"          || AUTHORIZED
        SMB_PROGRAM | "MATH"          | null           || AUTHORIZED
        SMB_PROGRAM | null            | "SAN"          || AUTHORIZED
        SMB_PROGRAM | null            | null           || null
        "cp"        | "MATH"          | "SAN"          || null
    }

      private void populateWFMockInfo(HttpServletRequest request) {
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

        programService.getProgramConfigValue(_, _, _) >> Optional.of("true")
        pricingModelService.getPricingModels(_, _, _) >> null
        applicationProperties.get(CommonConstants.DISCOUNTCODE_QUERY_PARAM_OTP) >> "discountCode"
        applicationProperties.getProperty("session.timeout.minutes") >> "200"
        discountServiceClient.getValidDiscountCode(_, _) >> VarOrderManagerTestDataFactory.getCouponDetails()
        varIntegrationServiceLocalImpl.getLocalUserPoints(_) >> null
    }

    private void getSelectUserMock(HttpServletRequest request, String navFlag, String keepAliveUrl) {
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getAttribute(CommonConstants.ANONYMOUS_SAML_ATTRIBUTE) >> "true"
        populateWFMockInfo(request)
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.NAVFLAG.value) >> navFlag
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.KEEPALIVEURL.value) >> keepAliveUrl
        Program program = getMockProgram()
        Map<String, Object> config = program.getConfig();
        config.put(CommonConstants.IS_TWO_WAY_NAV_ENABLED, true)
        program.setConfig(config)
        programService.getProgram(_, _, _) >> program
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> getAccountInformation()
    }

    private Program getMockProgram(String programType) {
        Program program = VarOrderManagerTestDataFactory.getProgramDetails(true)
        Map<String, String> config = new HashMap<>()
        config.put(CommonConstants.VIMS_ADDITIONAL_INFO, "AdditionalInfo")
        config.put(VAROrderManagerWF.SAMLAttributes.BROWSE_ONLY.value, "true")

        if(StringUtils.isNotBlank(programType)){
            config.put(PROGRAM_TYPE, programType)
        }
        program.setConfig(config)
        program
    }

    private void getHttpServletRequest(HttpServletRequest request) {
        HttpSession httpSession = new MockHttpSession()
        request.getAttribute(CommonConstants.PROGRAM_ID) >> "b2s_qa_only"
        request.getSession() >> httpSession
        request.getParameter(CommonConstants.VAR_ID) >> "WF"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        request.getParameter("discountCode") >> "discountCode"
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.requestURL >> new StringBuffer("https://WF-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        request.getParameterValues(CommonConstants.RELAY_STATE) >> VarOrderManagerTestDataFactory.getRelayState()
        request.getAttribute(CommonConstants.LOCALE) >> "en_CA"
        request.getUserPrincipal() >> Mock(Principal)
        request.getUserPrincipal().getName() >> "User Principal"
        request.getAttribute("AdditionalInfo") >> "AdditionalInfo"
    }

    AccountInfo getAccountInformation() {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setPricingTier("WF Pricing")
        accountInfo.setUserInformation(new UserInformation());

        AccountBalance accountBalance = new AccountBalance()
        accountBalance.setPointsBalance(1000)
        accountBalance.setCurrency("USD")
        accountInfo.setAccountBalance(accountBalance)

        return accountInfo
    }
    @Unroll
    def "selectUser - SAMLLogin #scenario"() {

        setup:
        def request = Mock(HttpServletRequest)
        getHttpServletRequest(request)
        request.getParameter(CommonConstants.USER_ID) >> "user"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.VARID.value) >> "WF"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.PROGRAM_ID.value) >> "b2s_qa_only"
        request.getAttribute(VAROrderManagerWF.SAMLAttributes.DEVICEEXPERIENCE.value) >> DeviceExperience
        AccountInfo accountInfo = getAccountInformation()
        User userInfo = new User()
        userInfo.setVarId("WF");
        userInfo.setProgramId("b2s_qa_only");
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> accountInfo
        varIntegrationServices.put("varIntegrationServiceLocalImpl", varIntegrationServiceLocalImpl)
        Program program = getMockProgram()
        programService.getProgram(_, _, _) >> program
        populateWFMockInfo(request)

        and:
        varProgramConfigDao.getVarProgramConfigValue(_, _, _, _) >> getVarProgramConfig(VpcFlag)

        when:
        UserWF user = (UserWF) varOrderManagerWF.selectUser(request)
        then:
        user.getSuppressTimeoutAndKeepAliveEnabled() == expected
        where:
        scenario                             | DeviceExperience | VpcFlag | expected
        "NativeApp with VPC not configured"  | "N"              | null    | true
        "NativeApp with VPC enabled"         | "N"              | true    | true
        "NativeApp with VPC disabled"        | "N"              | false   | false
        "BrowserApp with VPC not configured" | "B"              | null    | false
        "BrowserApp with VPC enabled"        | "B"              | true    | false
        "BrowserApp with VPC disabled"       | "B"              | false   | false
    }

    def getVarProgramConfig(Boolean vpcFlag) {
        def value = vpcFlag != null ? String.valueOf(vpcFlag) : true
        return value
    }

}
