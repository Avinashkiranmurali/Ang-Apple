package com.b2s.shop.common.order.var

import com.b2s.apple.services.MessageService
import com.b2s.apple.services.ProgramService
import com.b2s.db.model.Order
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.security.oauth.OAuthConfig
import com.b2s.security.oauth.OAuthCredentials
import com.b2s.security.oauth.Token
import com.b2s.security.oauth.service.OAuthTokenService
import com.b2s.shop.common.User
import com.b2s.shop.common.order.util.OAuthRequestParam
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

class VAROrderManagerVirginAUSpec extends Specification {

    def applicationProperties = Mock(Properties)
    def programService = Mock(ProgramService)
    def messageService = Mock(MessageService)
    def oauthConfig = Mock(OAuthConfig)
    def oAuthTokenService = Mock(OAuthTokenService)

    def varOrderManagerVirginAU = new VAROrderManagerVirginAU(programService : programService,
            applicationProperties : applicationProperties, messageService : messageService, oauthConfig : oauthConfig, oAuthTokenService : oAuthTokenService)

    def "getAccountInfo"() {

        setup:

        def user = Mock(User)
        def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
        def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
        def accountInfoLocal = null
        AccountInfo accountInfoRemote = new AccountInfo();
        accountInfoRemote.setPricingTier("VirginAU Pricing")
        Program program = new Program();
        program.setVarId("VirginAU")
        program.setProgramId("b2s_qa_only")
        program.setIsLocal(true)
        program.setIsActive(true)
        programService.getProgram(_, _, _) >> program
        varIntegrationServiceRemoteImpl.getUserProfile(_,_,_) >> accountInfoRemote

        when:
        accountInfoLocal = varOrderManagerVirginAU.getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl)
        program.setIsLocal(false)
        accountInfoRemote = varOrderManagerVirginAU.getAccountInfo(user, program.getIsLocal(),
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
        messageService.getMessage(*_) >> ""

        when:
        boolean result = varOrderManagerVirginAU.placeOrder(order, user, program);

        then:
        result == false && order.getVarOrderId().equals("110")

    }

    def "test selectUser"() {

        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getParameter(CommonConstants.USER_ID) >> "Anonymous"
        request.getParameter(CommonConstants.VAR_ID) >> "VirginAU"
        request.getParameter(CommonConstants.PROGRAM_ID_NON_CAMEL_CASE) >> "b2s_qa_only"
        request.getParameter(OAuthRequestParam.CODE.getValue()) >> null
        request.requestURL >> new StringBuffer("https://virginau/b2r/landingHome.do")
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

        request.getSession() >> Mock(HttpSession)
        applicationProperties.getProperty(CommonConstants.CHASE_SSO_ROOT_URL_KEY) >> "http://ssorooturl"
        applicationProperties.getProperty(CommonConstants.CHASE_ANALYTICS_ROOT_URL_KEY) >> "http://ssoanalyticsurl"
        applicationProperties.getProperty("virginau.session.timeout.minutes") >> "30"
        programService.getProgram(_ as String, _ as String, _ as Locale) >> new Program('isActive' : true)
        programService.addOrUpdateExternalUrls(_,_,_,_,_) >> new HashMap<String, String>()
        UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUri() >> new URI("https://virginua/b2r/landingHome.do")
        OAuthCredentials credentials = new OAuthCredentials()
        credentials.setRedirectUri("")
        credentials.setClientId("")
        oauthConfig.getOAuthCredentials(_) >> credentials
        oAuthTokenService.getTokenFomCode(_,_,_) >> new Token()

        when:
        User user = varOrderManagerVirginAU.selectUser(request)
        then:
        user.isAnonymous() == true

    }
}
