package com.b2s.shop.common.order.var

import com.b2s.apple.services.ProgramService
import com.b2s.db.model.Order
import com.b2s.rewards.apple.integration.model.AccountBalance
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

class VAROrderManagerFDRSpec extends Specification {

    def programService = Mock(ProgramService)
    def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
    def varIntegrationServiceLocalImpl = Mock(VarIntegrationServiceLocalImpl)
    def applicationProperties = Mock(Properties)

    @Subject
    def varOrderManagerFDR = new VAROrderManagerFDR(varIntegrationServiceLocalImpl: varIntegrationServiceLocalImpl,
            varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl, programService: programService,
            applicationProperties: applicationProperties)

    def "test orderPlace for Exception"() {

        setup:

        def order = Mock(Order)
        User user = new User()
        def program = Mock(Program)
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110
        def varOrderManagerFDR = new VAROrderManagerFDR()

        when:
        boolean result = varOrderManagerFDR.placeOrder(order, user, program);

        then:
        result == false && order.getVarOrderId().equals("110")

    }

    def "test selectUser SAML fulfillmentAgent"() {

        setup:
        HttpServletRequest httpServletRequest= Mock(HttpServletRequest)
        httpServletRequest.servletPath >> "/apple-gr"
        httpServletRequest.requestURL >> new StringBuffer("https://fdr-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")

        httpServletRequest.getParameter(CommonConstants.RELAY_STATE) >> "https://webapp-vip-saml.apldev.bridge2solutions.net/apple-gr/ssoLoginAction.do?referer=https://fdr-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do"
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.VARID.value) >> var_id
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.PROGRAM_ID.value) >> program_id
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.POINTS_BALANCE.value) >> "0"
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.BROWSE_ONLY.value) >> browse_only
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.CSID.value) >> "6543a91c1f30136100d592570a5862af136921b84bb1a8d9ffda5782338c711a"
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.LOCALE.value) >> "en_US"
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.KEEPALIVEURL.value) >> "null"
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.COUNTRY.value) >> "US"
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.ANONYMOUS.value) >> "false"
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.SID.value) >> "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1OTM0ODk2MDAsImFwcElkIjoicmV3YXJkcyIsIm9wZXJhdG9ySWQiOiJQTFBKSEEwMiIsIk9yZ0lEIjoiSkhBIiwidXNlcklkIjoiUExQSkhBMDIiLCJuYmYiOjE1ODgzNDA3NTUsImlzcyI6Ik5TQSIsIm9jc1NoZWxsIjoiUExQSkhBMDIiLCJwcml2aWxlZ2VzIjpbIlBMUFBDQyIsIlBMUFBSVCIsIlBMUFBWVCIsIlBMUERBRCIsIlBMUERVRCIsIlBMUERBUCIsIlBMUERETCIsIlBMUERDUCIsIlBMUERDQyIsIlBMUERSVCIsIlBMUERWVCIsIlBMUFZBUCIsIlBMUE1WRiIsIlBMUE1WVSIsIlBMUE1VUCIsIlBMUE1BRCIsIlBMUE1ETCIsIlBMUE1BUCIsIlBMUE1SVCIsIlBMUE1DUCIsIlBMUE1WUCIsIlBMUE1WVCIsIlBMUEZBRCIsIlBMUE1DQyIsIlBMUEVBUCIsIlBMUFVBUCIsIlBMUEZETCIsIlBMUEZVUCIsIlJXQURVIiwiUExQUlNDIiwiUExQUkRNIiwiUldISExEIiwiUldWUlBTIiwiUldWVFJEIiwiUldBVFJEIiwiSExOU0EiLCJSV1ZSUyIsIlBMUFJDUCIsIlBMUFJETCIsIlBMUFJBUCIsIlBMUFJVUCIsIlBMUFJBRCIsIlBMUEFETSIsIlJXQURKQiIsIlJXQVJXUCIsIlBMUFBDUCIsIlBMUFBBUCIsIlBMUFBVUCIsIlBMUFBETCIsIlBMUFBBRCIsIlBMUFJWVCIsIlBMUFJSVCIsIlBMUFJDQyJdLCJ1c2VyTmFtZSI6IkVDaHJpc3QiLCJ4NTAwSWQiOiJBQUFBNTIyNyIsImVudGl0bGVtZW50IjpbXSwiZ3JvdXBzIjpbIlJXQURVIiwiSExOU0EiLCJQTFBEQ1AiLCJQTFBSU0MiLCJQTFBSUlQiLCJSV1ZSUyIsIlBMUERETCIsIlBMUEZETCIsIlBMUERVRCIsIlBMUFVBUCIsIlJXVlZDUiIsIk5TQVRFUyIsIlBMUFBDUCIsIlBMUFJDUCIsIlJXQURKQiIsIlBMUEVBUCIsIlBSTUZORCIsIlJXQURKUCIsIlBSTUFERCIsIlBMUFJVUCIsIkNBU0VNRyIsIlBMUFBBRCIsIlBMUERBUCIsIlJXQ0FUTyIsIlJXQVJHUCIsIlBMUERBRCIsIlBMUEZBRCIsIk1BU0siLCJQTFBQVVAiLCJSV0FSU0MiLCJQTFBGVVAiLCJSV0FSV1AiLCJSV1ZSUFMiLCJQTFBQREwiLCJQTFBSREwiLCJQTFBSRE0iLCJSV0FSREQiLCJQTFBQVlQiLCJSV0hITEQiLCJQTFBSVlQiLCJQTFBQUlQiLCJQTFBWQVAiLCJQTFBEUlQiLCJQTFBBRE0iLCJQTFBQQVAiLCJQTFBSQVAiLCJQTFBEVlQiLCJQTFBSQUQiXSwic2Vzc2lvbiI6ImI5MDNhYjM2LTgxYWMtNDI3Ni04YzMxLWNhOWE4NzYzOTQ5MyIsImp0aSI6IjhiZjhmMDg0LWZhYzgtNDI4NC05MDQ0LTYyOGRlYmViZTI2MSIsImNsaWVudElkZW50aWZpZXIiOiIxMTQ2Iiwic3lzdGVtSWRlbnRpZmllciI6IjMyMzYiLCJwcmluY2lwYWxJZGVudGlmaWVyIjoiODExOSIsImFnZW50SWRlbnRpZmllciI6IjAwMDAiLCJhY2NvdW50SWQiOiJGU0VOQyhleUpqYjI1bWFXY2lPaUptY3k1d1ltVXVhMlY1TGpFdVVFSkZWMGxVU0ZOSVFUSTFOa0ZPUkRJMU5rSkpWRUZGVXkxRFFrTXRRa01pTENKa1lYUmhJam9pUlU1REtGWnFaSGcyWlRGRmNrcDNMM2hyYlRGR0wxb3hZbmRIWVZWelNTOUliMUJsUjBadWIwUmhVQzlaTDJScFJIVkNNMjA0VkRoTWIwZ3lUSGhoYm10WGFVZ3BJbjA9KSIsInBpaWQiOiJWT0woUXlOa1UyMXhkVUF3VUR0Y1VEczZLZz09KSIsImN1c3RvbWVyRXh0ZXJuYWxJZGVudGlmaWVyIjoiQzIwMDM2MTUyNjQyNjI1MDUxMDUwNTIxIiwicHJlc2VudGF0aW9uSWQiOiJGU0VOQyhleUpqYjI1bWFXY2lPaUptY3k1d1ltVXVhMlY1TGpFdVVFSkZWMGxVU0ZOSVFUSTFOa0ZPUkRJMU5rSkpWRUZGVXkxRFFrTXRRa01pTENKa1lYUmhJam9pUlU1REtGTlRXR3g2UW5weFYwaE1hRE5NTnpGRGJGUnRaMWN4TDNKQmRWSm1WekIwWW10VlFuSjBLMXBFUTJsM1dUbGhOakVyZVZOVGVGaGFXbmx5VXpSSVRFTXBJbjA9KSIsImNhcmRUeXBlIjoiZGViaXQiLCJtZW1vVHlwZSI6IkNJUyBUaW1lIiwiYXVkIjoiRUNTIiwiY2lkIjoiQUFBQTUyMjcwMDEiLCJ3ZWJQcmVzZW50YXRpb25JZCI6IlBMUEpIQTAxIiwibG9nb3V0VXJsIjoiaHR0cHM6Ly9jYXQuZmlyc3RkYXRhZXNlcnZpY2VzLmNvbS9wa21zbG9nb3V0IiwicmV0dXJuVXJsIjoiaHR0cHM6Ly9jYXQuZmlyc3RkYXRhZXNlcnZpY2VzLmNvbS9yZXdhcmRzLyMvdmlldy92aWV3X2NhcmRob2xkZXJfYWNjb3VudC90YWJfY2FyZGhvbGRlcl9hY2NvdW50In0.NSns8URpIoWwpGckAlZ4kgPwGz4UYCZJX91OG9gRRx4"
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.FULFILLMENT_AGENT.value) >> saml_fulfillmentAgent
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.NAVBACKURL.value) >> "https://fdr-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do"
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.AGENT_BROWSE.value) >> agent_browse
        httpServletRequest.getAttribute( VAROrderManagerFDR.SAMLAttributes.CYCLE.value) >> "T"
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
        UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(httpServletRequest)).build().toUri() >> new URI("https://fdr-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")

        Program program = new Program();
        program.setVarId("FDR")
        program.setProgramId("Demo")
        program.setIsLocal(false)
        program.setIsActive(true)
        programService.getProgram(_, _, _) >> program

        AccountInfo accountInfoRemote = new AccountInfo();
        accountInfoRemote.setAccountBalance(Mock(AccountBalance))

        httpServletRequest.getSession() >> Mock(HttpSession)

        varIntegrationServiceRemoteImpl.getUserProfile(_,_,_) >> accountInfoRemote

        when:
        User user = varOrderManagerFDR.selectUser(httpServletRequest)

        then:
        user.getAdditionalInfo().get("cycle") == "T"
        user.getAdditionalInfo().get(CommonConstants.FULFILLMENT_AGENT) == user_additionalInfo_fulfillmentAgent

        where:
        var_id| program_id  | browse_only | anonymous | saml_fulfillmentAgent | agent_browse || user_additionalInfo_fulfillmentAgent
        "FDR" |   "Demo"    |   "false"   |  "true"   |  "AHAPLP (ESS)"       |   "true"    || "AHAPLP (ESS)"
        "FDR" |"b2s_qa_only"|   "true"    |  "false"  |  "BHAPLP (ESS)"       |   "false"     || "BHAPLP (ESS)"
        "FDR" |   "Demo"    |   "false"   |  "false"  |  "CHAPLP (ESS)"       |   "true"    || "CHAPLP (ESS)"
        "FDR" |"b2s_qa_only"|   "true"    |  "true"   |  "JHAPLP (ESS)"       |   "false"     || "JHAPLP (ESS)"


    }
}
