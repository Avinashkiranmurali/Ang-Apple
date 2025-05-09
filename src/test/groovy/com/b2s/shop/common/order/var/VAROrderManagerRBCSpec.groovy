package com.b2s.shop.common.order.var

import com.b2s.apple.entity.DemoUserEntity
import com.b2s.apple.services.PricingModelService
import com.b2s.apple.services.ProgramService
import com.b2s.common.services.discountservice.DiscountServiceClient
import com.b2s.db.model.Order
import com.b2s.rewards.apple.dao.DemoUserDao
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.apple.services.MessageService
import com.b2s.shop.common.order.var.common.VarOrderManagerTestDataFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

class VAROrderManagerRBCSpec extends Specification {
    def programService = Mock(ProgramService)
    def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
    def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
    def applicationProperties = Mock(Properties)
    def discountServiceClient = Mock(DiscountServiceClient)
    def pricingModelService = Mock(PricingModelService)
    def demoUserDao = Mock(DemoUserDao)
    @Subject
    def varOrderManagerRBC = new VAROrderManagerRBC(varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl,
            programService: programService, applicationProperties: applicationProperties,
            discountServiceClient: discountServiceClient, pricingModelService: pricingModelService,
            varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl, demoUserDao: demoUserDao)

    def "test orderPlace for Exception"() {

        setup:

        def order = Mock(Order)
        User user = new User()
        def program = Mock(Program)
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110
        def messageService = Mock(MessageService)
        def varOrderManagerRBC = new VAROrderManagerRBC(messageService: messageService)
        messageService.getMessage(*_) >> ""
        when:
        boolean result = varOrderManagerRBC.placeOrder(order, user, program);

        then:
        result == false && order.getVarOrderId().equals("110")

    }

    def "selectUser - Anonymous User "() {

        setup:
        def request = Mock(HttpServletRequest)
        HttpSession httpSession = new MockHttpSession()
        request.getSession() >> httpSession
        request.getParameter(CommonConstants.USER_ID) >> "Anonymous_user"
        request.getParameter(CommonConstants.VAR_ID) >> "RBC"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        request.getParameter("discountCode") >> "discountCode"
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.requestURL >> new StringBuffer("https://rbc-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        request.getParameterValues(CommonConstants.RELAY_STATE) >> VarOrderManagerTestDataFactory.getRelayState()
        request.getAttribute(CommonConstants.LOCALE) >> "en_CA"
        request.getParameter(CommonConstants.RELAY_STATE) >> "relay"
        programService.getProgram(_, _, _) >> VarOrderManagerTestDataFactory.getProgramDetails(true)
        programService.getProgramConfigValue(_, _, _) >> Optional.of("true")
        pricingModelService.getPricingModels(_, _, _) >> null
        applicationProperties.get(CommonConstants.DISCOUNTCODE_QUERY_PARAM_OTP) >> "discountCode"
        discountServiceClient.getValidDiscountCode(_, _) >> VarOrderManagerTestDataFactory.getCouponDetails()
        varIntegrationServiceLocalImpl.getLocalUserPoints(_) >> null

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
        User user = varOrderManagerRBC.selectUser(request)
        then:
        user != null
    }

    def "selectUser - FiverBox Login "() {

        setup:
        def request = Mock(HttpServletRequest)
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> getDemoUserEntity()
        HttpSession httpSession = new MockHttpSession()
        request.getSession() >> httpSession
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.requestURL >> new StringBuffer("https://rbc-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        programService.getProgram(_, _, _) >> VarOrderManagerTestDataFactory.getProgramDetails(true)
        programService.getProgramConfigValue(_, _, _) >> Optional.of("false")
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
        User user = varOrderManagerRBC.selectUser(request)
        then:
        user != null
    }

    def "selectUser - SAML Login "() {

        setup:
        def request = Mock(HttpServletRequest)
        demoUserDao.findByDemoUserIdAndPassword(_, _) >> getDemoUserEntity()
        HttpSession httpSession = new MockHttpSession()
        request.getSession() >> httpSession
        request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT) >> null
        request.getParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP) >> " demo "
        request.requestURL >> new StringBuffer("https://rbc-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        request.getUserPrincipal() >> Mock(java.security.Principal)
        request.getUserPrincipal().getName() >> "User Principal"
        request.getAttribute(CommonConstants.PROGRAM_ID) >> "b2s_qa_only"

        request.getAttribute("varId") >> "RBC"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        programService.getProgram(_, _, _) >> VarOrderManagerTestDataFactory.getProgramDetails(true)
        programService.getProgramConfigValue(_, _, _) >> Optional.of("false")
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
        User user = varOrderManagerRBC.selectUser(request)
        then:
        user != null
    }

    def " test getUserPoints for remote - Points"() {
        given:
        Program programDetails = VarOrderManagerTestDataFactory.getProgramDetails(false)
        User userDetails = VarOrderManagerTestDataFactory.getUserDetails()

        varIntegrationServiceRemoteImpl.getUserPoints(_ as String, _ as String, _ as String, _ as Map) >> 2000

        when:
        def points = varOrderManagerRBC.getUserPoints(userDetails, programDetails)

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
        def points = varOrderManagerRBC.getUserPoints(user, program)

        then:
        points == 0
    }

    def getHeaders() {
        HttpHeaders headers = new HttpHeaders()
        headers.add("host", "localhost")
        return headers
    }

    DemoUserEntity getDemoUserEntity() {
        DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId()
        demoUserId.setProgramId("b2s_qa_only")
        demoUserId.setVarId("RBC")
        demoUserId.setUserId("demo")

        DemoUserEntity demoUserEntity = new DemoUserEntity()
        demoUserEntity.setFirstname("Eric")
        demoUserEntity.setDemoUserId(demoUserId)
        demoUserEntity.setZip("1234-122-123")

        return demoUserEntity
    }

    def "test updateUser"() {
        setup:
        def user = new User()
        def request = Mock(HttpServletRequest)
        request.getParameter(CommonConstants.USER_ID) >> userID
        request.getParameter(CommonConstants.VAR_ID) >> varID
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> programID
        request.getAttribute(CommonConstants.LOCALE) >> locale

        varOrderManagerRBC.updateUser(request, user, "CA", anonymous)


        expect:
        user.getLocale() == resultLocale
        user.getUserId() == userID
        user.getVarId() == varID
        user.getProgramId() == programID
        user.isAnonymous() == anonymous

        where:
        locale  || resultLocale           || userID     || varID || programID     || anonymous
        null    || null                   || 'RBC_USER' || 'RBC' || 'b2s_qa_only' || true
        'en_CA' || new Locale("en", "CA") || 'RBC_USER' || 'RBC' || 'b2s_qa_only' || false
        'fr_CA' || new Locale("fr", "CA") || 'RBC_USER' || 'RBC' || 'b2s_qa_only' || true

    }



}