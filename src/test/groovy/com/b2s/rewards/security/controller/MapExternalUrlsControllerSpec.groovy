package com.b2s.rewards.security.controller

import com.b2s.rewards.common.util.CommonConstants
import com.b2s.rewards.security.util.ExternalUrlConstants
import com.b2s.security.oauth.service.OAuthTokenService
import com.b2s.shop.common.User
import com.b2s.apple.services.AppSessionInfo
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest

class MapExternalUrlsControllerSpec extends Specification{

    def OAuthTokenService oAuthTokenService
    def MockHttpSession session=Mock()
    def AppSessionInfo appSessionInfo=Mock()
    HttpServletRequest request = new MockHttpServletRequest(session:session, requestURI:
            "/apple-gr/service/validSession")

    @Subject
    final controller = new MapExternalUrlsController(appSessionInfo:appSessionInfo)

    def "isSessionValid"(){

        setup:
        oAuthTokenService.isTokenValid(_) >> true
        request.getHeaders() >> getHeaders()
        request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS) >> urls
        request.getSession().getAttribute("OAUTH_ATTRIBUTES") >> null
        appSessionInfo.currentUser()>>userObj

        when:
        def result=controller.isSessionValid(initial, request)

        then:
        result.size() == resultsize
        result.get(ExternalUrlConstants.KEYSTONE_URLS) == kbUrl

        if(CommonConstants.LoginType.FIVEBOX.getValue() == userObj.loginType) {
            result.containsKey(ExternalUrlConstants.SIGN_OUT_URL) && result.get(ExternalUrlConstants.SIGN_OUT_URL).toString().contains(signOutUrl)
            result.containsKey(ExternalUrlConstants.NAVIGATE_BACK_URL) && result.get(ExternalUrlConstants.NAVIGATE_BACK_URL).toString().contains(navigateBackUrl)
            result.containsKey(ExternalUrlConstants.TIME_OUT_URL) && result.get(ExternalUrlConstants.TIME_OUT_URL).toString().contains(timeOutUrl)
        }
        if(CommonConstants.LoginType.SAML.getValue() == userObj.loginType){
            if(userObj.varId == "RBC") {
            result.containsKey(ExternalUrlConstants.KEEP_ALIVE_URL_SOURCE) && result.get(ExternalUrlConstants.KEEP_ALIVE_URL_SOURCE).toString().contains(keepAliveUrl) }
            if(userObj.varId == "WF") {
                result.containsKey(ExternalUrlConstants.SIGN_OUT_URL) && result.get(ExternalUrlConstants.SIGN_OUT_URL).toString().contains(signOutUrl)
                result.containsKey(ExternalUrlConstants.NAVIGATE_BACK_URL) && result.get(ExternalUrlConstants.NAVIGATE_BACK_URL).toString().contains(navigateBackUrl)
                result.containsKey(ExternalUrlConstants.TIME_OUT_URL) && result.get(ExternalUrlConstants.TIME_OUT_URL).toString().contains(timeOutUrl)
                result.containsKey(ExternalUrlConstants.KEEP_ALIVE_URL) && result.get(ExternalUrlConstants.KEEP_ALIVE_URL).toString().contains(keepAliveUrl)
            }
        }

        where:
        urls           |userObj          |signOutUrl                                     |navigateBackUrl                      |timeOutUrl                                                    |keepAliveUrl                                      |initial || resultsize || kbUrl
        new HashMap<>()|getUserScotia()  |"login.jsp?returnTest=signOutBack"             |"login.jsp?returnTest=navigateBack"  |"login.jsp?returnTest=timeOutBack"                            |   null                                           | false  || 3          || null
        getUrls_PNC()  |getUserPNC()     |"webqa.commercialrewards.pnc.com/Logout/Logout"|"login.jsp?returnTest=navigateBack"  |"webqa.commercialrewards.pnc.com/Logout/Logout"               |   null                                           | true   || 4          || "https://webqa.commercialrewards.pnc.com/keyStoneUrls"
        getUrls_RBC()  |getUserRBC_Saml()|    null                                       |    null                             | null                                                         |"https://uat.rewardstepuat.com/b2r/rbc-keepalive" | true   || 2          || "https://webqa.commercialrewards.rbc.com/keyStoneUrls"
        getUrls_WF()   |getUserWF_Saml() |"wfbk-uat-mn.epsilon.com/home/LogOutRedir"     |"landingHome.do?supplier=Merchandise"|"uatwfatechcatalog.bridge2rewards.com/apple-gr/DomainLogin.do"|"https://wfbk-uat-mn.epsilon.com/home/keepalive"  | false  || 4          || null

    }

    def getHeaders() {
        HttpHeaders headers = new HttpHeaders()
        headers.add("host","localhost")
        return headers
    }

    def getUserScotia(){
        def user= new User()
        user.varId="SCOTIA"
        user.programId="AmexROC"
        user.locale=Locale.CANADA
        user.loginType = CommonConstants.LoginType.FIVEBOX.getValue()
        return user
    }

    def getUserPNC(){
        def user= new User()
        user.varId="PNC"
        user.programId="b2s_qa_only"
        user.locale=Locale.US
        user.loginType = CommonConstants.LoginType.FIVEBOX.getValue()
        return user
    }

    def getUserRBC_Saml(){
        def user= new User()
        user.varId="RBC"
        user.hostName = "https://webapp-vip-saml.apldev.bridge2solutions.net"
        user.programId="GCP"
        user.locale=Locale.CANADA
        user.loginType = CommonConstants.LoginType.SAML.getValue()
        return user
    }

    def getUserWF_Saml(){
        def user= new User()
        user.varId="WF"
        user.hostName = "https://webapp-vip-saml.apldev.bridge2solutions.net"
        user.programId="GCP"
        user.locale=Locale.US
        user.loginType = CommonConstants.LoginType.SAML.getValue()
        return user
    }

   def getUrls_PNC() {
       Map<String, String> urls = new HashMap<>()
       urls.put(ExternalUrlConstants.SIGN_OUT_URL,"https://webqa.commercialrewards.pnc.com/Logout/Logout")
       urls.put(ExternalUrlConstants.TIME_OUT_URL,"https://webqa.commercialrewards.pnc.com/Logout/Logout")
       urls.put(ExternalUrlConstants.KEYSTONE_URLS,"https://webqa.commercialrewards.pnc.com/keyStoneUrls")
       return urls
   }

    def getUrls_RBC() {
        Map<String, String> urls = new HashMap<>()
        urls.put(ExternalUrlConstants.KEEP_ALIVE_URL_SOURCE,"https://dev-vip-internal.cpdev.bridge2solutions.net/b2r/rbc-keepalive")
        urls.put(ExternalUrlConstants.KEYSTONE_URLS,"https://webqa.commercialrewards.rbc.com/keyStoneUrls")
        return urls
    }

    def getUrls_WF() {
        Map<String, String> urls = new HashMap<>()
        urls.put(ExternalUrlConstants.SIGN_OUT_URL,"https://wfbk-uat-mn.epsilon.com/home/LogOutRedir")
        urls.put(ExternalUrlConstants.TIME_OUT_URL,"https://uatwfatechcatalog.bridge2rewards.com/apple-gr/DomainLogin.do")
        urls.put(ExternalUrlConstants.NAVIGATE_BACK_URL,"https://uatwfcatalog.bridge2rewards.com/b2r/landingHome.do?supplier=Merchandise")
        urls.put(ExternalUrlConstants.KEEP_ALIVE_URL,"https://wfbk-uat-mn.epsilon.com/home/keepalive")
        urls.put(ExternalUrlConstants.KEYSTONE_URLS,"https://webqa.commercialrewards.ks.com/keyStoneUrls")
        return urls
    }

}

