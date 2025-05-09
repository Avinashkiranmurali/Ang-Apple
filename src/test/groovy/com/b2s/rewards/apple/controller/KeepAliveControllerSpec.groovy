package com.b2s.rewards.apple.controller

import com.b2s.rewards.security.controller.KeepAliveController
import com.b2s.shop.common.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll


/**
 *  Created by rpillai on 3/21/2016.
 */
class KeepAliveControllerSpec extends Specification{


    private Properties applicationProperties;

    private ObjectMapper objectMapper;

    private KeepAliveController controller;

    private MockMvc mvc;

    private MockHttpSession session;
    private MockHttpServletRequest request;

    private User user;

    private String keepAliveUrl = "https://integrationtest.powerofvitality.com/vitality/bridge2/keep_alive";
    private String timeOutUrl = "https://vitality.rewardsteplocal.com:8080/auth/login";
    private String navigateBackUrl = "https://integrationtest.powerofvitality.com/vitality/apple_watch";


    def setup(){

        applicationProperties=Mock();
        applicationProperties.getProperty("vitalityus.keepAliveUrl") >> keepAliveUrl
        applicationProperties.getProperty("vitalityus.timeOutUrl") >> timeOutUrl
        applicationProperties.getProperty("vitalityus.navigateBackUrl") >> navigateBackUrl

        //objectMapper=Mock();
        controller=new KeepAliveController(applicationProperties:applicationProperties);
        controller.afterPropertiesSet();
        session=Mock();
        request=Mock();
        user=new User();
        mvc=MockMvcBuilders.standaloneSetup(controller).build()
    }

    def cleanup(){

        controller=null;
        applicationProperties=null;
        objectMapper=null;
        session=null;
        request= null;
        user=null;
        mvc=null;
    }

    @Unroll
    @Ignore
    def "keepalive"(){

        given:
        session.getAttribute("USER") >> getUser()

        when:
        def result=mvc.perform(MockMvcRequestBuilders.get("/keepalive-url-source")
                .param("callback",callback)
                .param("initial",initial)
                .session(session))
                .andReturn()

        then:
        session.isNew() >> isNewSession
        result.response.status==status
        result.response.contentAsString.contains(navigateBackUrl)==hasNavigateBackUrl
        result.response.contentAsString.contains(timeOutUrl)==hasTimeOutUrl
        result.response.contentAsString.contains(keepAliveUrl)==hasKeepAliveUrl


        where:

        callback            | initial   | isNewSession   ||  status || hasNavigateBackUrl || hasTimeOutUrl  || hasKeepAliveUrl

        "JSON_CALLBACK"     | "true"    | false          ||  200    || true               || true           || true
        "JSON_CALLBACK"     | ""        | false          ||  200    || false              || false          || true
        ""                  | ""        | false          ||  400    || false              || false          || false
        "JSON_CALLBACK"     | ""        | true           ||  403    || false              || false          || false

    }

    @Unroll
    @Ignore
    def "keepalive with signout"(){

        given:
        session.getAttribute("USER") >> getUser()
        applicationProperties.getProperty("vitalityus.signOutUrl") >> "https://integrationtest.powerofvitality.com/vitality/sign_out"

        when:
        def result=mvc.perform(MockMvcRequestBuilders.get("/keepalive-url-source")
                .param("callback",callback)
                .param("initial",initial)
                .session(session))
                .andReturn()

        then:
        session.isNew() >> isNewSession
        result.response.status==status
        result.response.contentAsString.contains(navigateBackUrl)==hasNavigateBackUrl
        result.response.contentAsString.contains(timeOutUrl)==hasTimeOutUrl
        result.response.contentAsString.contains(keepAliveUrl)==hasKeepAliveUrl


        where:


        callback            | initial   | isNewSession   ||  status || hasNavigateBackUrl || hasTimeOutUrl  || hasKeepAliveUrl

        "JSON_CALLBACK"     | "true"    | false          ||  200    || true               || true           || true
        "JSON_CALLBACK"     | ""        | false          ||  200    || false              || false          || true
        ""                  | ""        | false          ||  400    || false              || false          || false
        "JSON_CALLBACK"     | ""        | true           ||  403    || false              || false          || false


    }


    @Unroll
    def "keep alive with post"(){

        given:
        session.getAttribute("USER") >> getUser()

        when:
        def result=mvc.perform(MockMvcRequestBuilders.post("/keepalive-url-source")
                .param("callback",callback)
                .param("initial",initial)
                .session(session))
                .andReturn()

        then:
        session.isNew() >> isNewSession
        result.response.status==status
        //result.response.contentAsString.contains(keyToCheckInResponseMessage)==isKeyAvailableInResponse


        where:

        callback            | initial   | isNewSession   ||  status

        "JSON_CALLBACK"     | "true"    | false          ||  405
        "JSON_CALLBACK"     | ""        | false          ||  405
        ""                  | ""        | false          ||  405
        "JSON_CALLBACK"     | ""        | true           ||  405

    }

    def "getUser"(){
        user.varId="VitalityUS"
        user.programId="b2s_qa_only"
        user.locale=Locale.US
        return user
    }
}

