package com.b2s.shop.common.order.var

import com.b2s.db.model.Order
import com.b2s.rewards.apple.integration.model.AccountBalance
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.rewards.security.util.ExternalUrlConstants
import com.b2s.shop.common.User
import com.b2s.apple.services.MessageService
import com.b2s.apple.services.ProgramService
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

class VAROrderManagerDeltaSpec extends Specification {

    def programService = Mock(ProgramService)
    def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
    def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
    def applicationProperties = Mock(Properties)

    @Subject
    def varOrderManagerDelta = new VAROrderManagerDelta(varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl,
            varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl, programService: programService,
            applicationProperties: applicationProperties)


    def "getAccountInfo"() {
        setup:
        def user = Mock(User)
        def accountInfoLocal = null
        AccountInfo accountInfoRemote = new AccountInfo();
        accountInfoRemote.setPricingTier("Delta Pricing")
        Program program = new Program();
        program.setVarId("Delta")
        program.setProgramId("b2s_qa_only")
        program.setIsLocal(true)
        program.setIsActive(true)
        programService.getProgram(_, _, _) >> program
        varIntegrationServiceRemoteImpl.getUserProfile(_,_,_) >> accountInfoRemote

        when:
        accountInfoLocal = varOrderManagerDelta.getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl)
        program.setIsLocal(false)
        accountInfoRemote = varOrderManagerDelta.getAccountInfo(user, program.getIsLocal(),
                varIntegrationServiceLocalImpl, varIntegrationServiceRemoteImpl)

        then:
        accountInfoLocal == null && accountInfoRemote != null
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
        varIntegrationServiceLocalImpl.performRedemption(_) >> Exception
        messageService.getMessage(_)>>""

        when:
        boolean result = varOrderManagerDelta.placeOrder(order, user, program);

        then:
        result == false && order.getVarOrderId().equals("110")
    }

    def "test keystone session attributes"() {
        setup:
        HttpSession httpSession = new MockHttpSession()
        HttpServletRequest httpServletRequest= Mock(HttpServletRequest)
        httpServletRequest.getSession() >> httpSession

        httpServletRequest.servletPath >> "/apple-gr"
        httpServletRequest.requestURL >> new StringBuffer("https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")

        String csid = "c9e63d850e89a2a2b2b068a68c3e3d407b7b6e3f71bbfaa83cfd68168d0d9ae2"
        String keystoneBaseUrl = "https://delta-vip-internal.cpdev.bridge2solutions.net"

        httpServletRequest.getParameter(CommonConstants.RELAY_STATE) >> "https://webapp-vip-saml.apldev.bridge2solutions.net/apple-gr/ssoLoginAction.do?referer=https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do"
        httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.VARID.value) >> "Delta"
        httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.PROGRAMID.value) >> "DM"
        httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.POINTSBALANCE.value) >> "1000"
        httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.BROWSEONLY.value) >> "false"
        httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.ANONYMOUS.value) >> "false"
        httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.LOCALE.value) >> "en_US"
        //httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.COUNTRY.value) >> "US"
        httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.NAVBACKURL.value) >> "https://delta-vip-internal.cpqabf.bridge2solutions.net/b2r/ui/index#!/landing"
        httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.CSID.value) >> csid
        httpServletRequest.getAttribute( VAROrderManagerDelta.SAMLAttributes.USERID.value) >> "123456"
        httpServletRequest.getAttribute( ExternalUrlConstants.KEYSTONE_BASE_URL) >> keystoneBaseUrl

        httpServletRequest.getUserPrincipal() >> Mock(java.security.Principal)
        httpServletRequest.getUserPrincipal().getName() >> "User Principal"
        httpServletRequest.getScheme() >> "https"
        httpServletRequest.getHeaderNames() >> new Enumeration<String>() {
            @Override
            boolean hasMoreElements() {
                return false
            }

            @Override
            String nextElement() {
                return ""
            }
        }
        UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(httpServletRequest)).build().toUri() >> new URI("https://delta-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")

        Program program = new Program();
        program.setVarId("Delta")
        program.setProgramId("DM")
        program.setIsLocal(false)
        program.setIsActive(true)
        programService.getProgram(_, _, _) >> program

        AccountInfo accountInfoRemote = new AccountInfo();
        accountInfoRemote.setAccountBalance(Mock(AccountBalance))
        varIntegrationServiceRemoteImpl.getUserProfile(_,_,_) >> accountInfoRemote

        when:
        User user = varOrderManagerDelta.selectUser(httpServletRequest)
        Map<String, Object> extUrls = (Map<String, Object>)httpSession.getAttribute(ExternalUrlConstants.EXTERNAL_URLS)

        final List<String> keepAliveUrls = null
        final List<String> logOutUrls = null
        final List<String> balanceUpdateUrls = null
        extUrls.each { externalUrlsKey, externalUrlsValue ->
            if(externalUrlsKey.equalsIgnoreCase(ExternalUrlConstants.KEYSTONE_URLS)){
                final Map<String, Object> keystoneUrls = (Map<String, Object>)extUrls.get(ExternalUrlConstants.KEYSTONE_URLS)
                keystoneUrls.each { keystoneUrlsKey, keystoneUrlsValue ->
                    if(keystoneUrls.get(ExternalUrlConstants.KEEP_ALIVE)){
                        keepAliveUrls = (List<String>)keystoneUrls.get(ExternalUrlConstants.KEEP_ALIVE)
                    }
                    if(keystoneUrls.get(ExternalUrlConstants.LOG_OUT)){
                        logOutUrls = (List<String>)keystoneUrls.get(ExternalUrlConstants.LOG_OUT)
                    }
                    if(keystoneUrls.get(ExternalUrlConstants.BALANCE_UPDATE)){
                        balanceUpdateUrls = (List<String>)keystoneUrls.get(ExternalUrlConstants.BALANCE_UPDATE)
                    }
                }
            }
        }

        then:
        user.getCsid() == csid

        keepAliveUrls.size() == 3
        keepAliveUrls.get(0) == keystoneBaseUrl + ExternalUrlConstants.B2R_KEEP_ALIVE_PATH
        keepAliveUrls.get(1) == keystoneBaseUrl + ExternalUrlConstants.MERCH_KEEP_ALIVE_PATH
        keepAliveUrls.get(2) == keystoneBaseUrl + ExternalUrlConstants.TRAVEL_KEEP_ALIVE_PATH

        logOutUrls.size() == 1
        logOutUrls.get(0) == keystoneBaseUrl + ExternalUrlConstants.B2R_LOG_OUT_PATH

        balanceUpdateUrls.size() == 3
        balanceUpdateUrls.get(0) == keystoneBaseUrl + ExternalUrlConstants.B2R_BALANCE_PATH
        balanceUpdateUrls.get(1) == keystoneBaseUrl + ExternalUrlConstants.MERCH_BALANCE_PATH
        balanceUpdateUrls.get(2) == keystoneBaseUrl + ExternalUrlConstants.TRAVEL_BALANCE_PATH
    }
}
