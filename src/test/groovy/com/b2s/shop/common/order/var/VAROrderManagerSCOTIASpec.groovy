package com.b2s.shop.common.order.var

import com.b2s.apple.entity.VarProgramConfigEntity
import com.b2s.common.services.discountservice.DiscountServiceClient
import com.b2s.db.model.Order
import com.b2s.rewards.apple.dao.DemoUserDao
import com.b2s.rewards.apple.dao.VarProgramConfigDao
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.apple.services.MessageService
import com.b2s.apple.services.ProgramService
import com.b2s.shop.common.order.var.common.VarOrderManagerTestDataFactory
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession


class VAROrderManagerSCOTIASpec extends Specification {
    def programService = Mock(ProgramService)
    def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
    def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
    def applicationProperties = Mock(Properties)
    def discountServiceClient = Mock(DiscountServiceClient)
    def varProgramConfigDao = Mock(VarProgramConfigDao)
    def demoUserDao = Mock(DemoUserDao)
    @Subject
    def varOrderManagerSCOTIA = new VAROrderManagerSCOTIA(applicationProperties: applicationProperties,
            programService: programService, varProgramConfigDao: varProgramConfigDao,
            discountServiceClient: discountServiceClient, demoUserDao: demoUserDao,
            varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl,
            varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl
    )

    def "GetSignOutUrl"() {
        Properties properties = new Properties()
        properties.put("scotia.signOutUrl.fr_CA", "https://www.scotiarewards.com/fr-CA/Login/Logout")
        properties.put("scotia.signOutUrl.en_CA", "https://www.scotiarewards.com/en-CA/Login/Logout")
        properties.put("scotia.signOutUrl.en_US", "https://scotiarewardsphase1.bondstage.com")
        VAROrderManagerSCOTIA vomS = new VAROrderManagerSCOTIA(applicationProperties: properties)

        when:
        String url = vomS.getSignOutUrl(Locale.forLanguageTag(lang))
        then:
        url == resultURL

        where:
        lang    | resultURL
        "fr-CA" | "https://www.scotiarewards.com/fr-CA/Login/Logout"
        "en-CA" | "https://www.scotiarewards.com/en-CA/Login/Logout"
        "en-US" | "https://scotiarewardsphase1.bondstage.com"
    }


    def "getAccountInfo"() {

        setup:

        def user = Mock(UserScotia)
        def accountInfoLocal = null
        AccountInfo accountInfoRemote = getAccountInformation()
        def programService = Mock(ProgramService)
        Program program = new Program();
        program.setVarId("SCOTIA")
        program.setProgramId("b2s_qa_only")
        program.setIsLocal(true)
        program.setIsActive(true)
        programService.getProgram(_, _, _) >> program
        varIntegrationServiceRemoteImpl.getUserProfile(_, _, _) >> accountInfoRemote
        def varOrderManagerScotia = new VAROrderManagerSCOTIA(programService: programService)
        when:
        accountInfoLocal = varOrderManagerScotia.getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl)
        program.setIsLocal(false)
        accountInfoRemote = varOrderManagerScotia.getAccountInfo(user, program.getIsLocal(),
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
        def varOrderManagerSCOTIA = new VAROrderManagerSCOTIA(messageService: messageService)
        messageService.getMessage(*_) >> ""

        when:
        boolean result = varOrderManagerSCOTIA.placeOrder(order, user, program);

        then:
        result == false && order.getVarOrderId().equals("110")

    }

    def "selectUser - FiverBox Login "() {

        setup:
        def request = Mock(HttpServletRequest)
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> VarOrderManagerTestDataFactory.getDemoUserEntity()
        HttpSession httpSession = new MockHttpSession()
        request.getSession() >> httpSession
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.getAttribute(CommonConstants.LOCALE) >> "en_CA"
        request.requestURL >> new StringBuffer("https://rbc-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        programService.getProgram(_, _, _) >> VarOrderManagerTestDataFactory.getProgramDetails(true)
        programService.getProgramConfigValue(_, _, _) >> Optional.of("false")
        varProgramConfigDao.getVarProgramConfigByVarAndName(_, _) >> getVarProgramConfig()
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
        UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUri() >> new URI("https://rbc-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")

        when:
        User user = varOrderManagerSCOTIA.selectUser(request)
        then:
        user != null
    }

    def "selectUser - SAML Login "() {

        setup:
        def request = Mock(HttpServletRequest)
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> VarOrderManagerTestDataFactory.getDemoUserEntity()
        HttpSession httpSession = new MockHttpSession()
        request.getSession() >> httpSession
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.getParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP) >> " demo "
        request.requestURL >> new StringBuffer("https://rbc-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        request.getUserPrincipal() >> Mock(java.security.Principal)
        request.getUserPrincipal().getName() >> "User Principal"
        request.getParameter("locale") >> "en_CA"
        request.getParameter(CommonConstants.RELAY_STATE) >> "demo"
        request.getAttribute("varId") >> "RBC"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        request.getAttributeNames() >> Mock(java.util.Enumeration)
        request.getAttribute(VAROrderManagerSCOTIA.SAMLAttributes.POINTS_BALANCE.value) >> "0"
        request.getAttribute(VAROrderManagerSCOTIA.SAMLAttributes.VARID.value) >> "SCOTIA"
        request.getAttribute(VAROrderManagerSCOTIA.SAMLAttributes.PROGRAM_ID.value) >> "b2s_qa_only"
        varIntegrationServiceLocalImpl.getUserProfile(_, _, _) >> getAccountInformation()
        programService.getProgram(_, _, _) >> VarOrderManagerTestDataFactory.getProgramDetails(true)
        programService.getProgramConfigValue(_, _, _) >> Optional.of("false")
        varProgramConfigDao.getVarProgramConfigByVarAndName(_, _) >> getVarProgramConfig()

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
        UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUri() >> new URI("https://rbc-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")

        when:
        User user = varOrderManagerSCOTIA.selectUser(request)
        then:
        user != null
    }

    def " test getUserPoints for remote - Points"() {
        given:
        Program programDetails = VarOrderManagerTestDataFactory.getProgramDetails(false)
        User userDetails = VarOrderManagerTestDataFactory.getUserDetails()

        varIntegrationServiceRemoteImpl.getUserPoints(_ as String, _ as String, _ as String, _ as Map) >> 2000

        when:
        def points = varOrderManagerSCOTIA.getUserPoints(userDetails, programDetails)

        then:
        points == 2000
    }

    def " test getUserPoints for remote - Cash"() {
        given:
        Program program = VarOrderManagerTestDataFactory.getProgramDetails(false)
        program.getPayments().clear()
        program.setPayments(VarOrderManagerTestDataFactory.getPaymentListCash())
        User user = VarOrderManagerTestDataFactory.getUserDetails()

        when:
        def points = varOrderManagerSCOTIA.getUserPoints(user, program)

        then:
        points == 0
    }

    def "test session timeout with var id"() {
        Properties properties = new Properties()
        properties.put("scotia.session.timeout.minutes", "10")
        VAROrderManagerSCOTIA vomS = new VAROrderManagerSCOTIA(applicationProperties: properties)
        User user = VarOrderManagerTestDataFactory.getUserDetails()
        user.setVarId("Scotia")
        user.setProgramId("Scene")
        def request = Mock(HttpServletRequest)
        HttpSession httpSession = new MockHttpSession()
        request.getSession() >> httpSession

        when:
        vomS.setSessionTimeOut(request, user)
        then:
        request.getSession().getMaxInactiveInterval() == 600
    }

    def "test session timeout with var program id"() {
        Properties properties = new Properties()
        properties.put("scotia.session.timeout.minutes", "10")
        properties.put("scotia.scene.session.timeout.minutes", "20")
        VAROrderManagerSCOTIA vomS = new VAROrderManagerSCOTIA(applicationProperties: properties)
        User user = VarOrderManagerTestDataFactory.getUserDetails()
        user.setVarId("Scotia")
        user.setProgramId("Scene")
        def request = Mock(HttpServletRequest)
        HttpSession httpSession = new MockHttpSession()
        request.getSession() >> httpSession

        when:
        vomS.setSessionTimeOut(request, user)
        then:
        request.getSession().getMaxInactiveInterval() == 1200
    }

    List<VarProgramConfigEntity> getVarProgramConfig() {
        List<VarProgramConfigEntity> varProgramConfigEntities = new ArrayList<VarProgramConfigEntity>()
        VarProgramConfigEntity varProgramConfig = new VarProgramConfigEntity()
        varProgramConfig.setValue("silver,gold,Red,rosegold,rosegld,space_gray,blue,pink,product_red")
        varProgramConfig.setProgramId("b2s_qa_only")
        varProgramConfigEntities.add(varProgramConfig)
        return varProgramConfigEntities
    }

    AccountInfo getAccountInformation() {
        AccountInfo accountInfoRemote = new AccountInfo();
        accountInfoRemote.setPricingTier("Scotia Pricing")
        return accountInfoRemote
    }
}
