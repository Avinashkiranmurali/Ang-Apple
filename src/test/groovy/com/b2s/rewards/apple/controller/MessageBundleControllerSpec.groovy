package com.b2s.rewards.apple.controller

import com.b2s.rewards.common.context.AppContext
import com.b2s.shop.common.User
import com.b2s.web.B2RReloadableResourceBundleMessageSource
import org.springframework.context.ApplicationContext
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

/**
 *  Created by srukmagathan on 2/23/2016.
 */
@spock.lang.Ignore
class MessageBundleControllerSpec extends Specification{


    private MockMvc mvc

    private User user

    private MockHttpSession session

    private MessageBundleController controller

    private ApplicationContext context;


    def setup(){
        controller=new MessageBundleController();
        mvc=MockMvcBuilders.standaloneSetup(controller).build()
        context=Mock()
        session=Mock()

        user=new User();
        user.varId="VitalityUS"
        user.programId="prgId"
        user.locale=Locale.US

    }

    def cleanup(){
        mvc=null
        controller=null
        session=null
        user=null
    }

    def "list - get list of message from var specific properties files"(){

        given:
        session.getAttribute("USER") >> user
        AppContext.applicationContext=context
        B2RReloadableResourceBundleMessageSource messageSource=Mock()
        Properties properties=new Properties();
        properties.setProperty("k","v")

        when:
        def result=mvc.perform(MockMvcRequestBuilders.get("/messages").session(session)).andReturn()

        then:
        1*context.getBean("messageSourceVitalityUS") >> messageSource;
        1*messageSource.getAllMessages(_) >> properties
        result.response.status==200
        result.response.contentAsString.length()>2

        cleanup:
        messageSource=null
        properties=null
    }

    def "list - get list of message from general properties files"(){

        given:
        session.getAttribute("USER") >> user
        AppContext.applicationContext=context
        B2RReloadableResourceBundleMessageSource messageSource=Mock()
        Properties properties=new Properties();
        properties.setProperty("k","v")

        when:
        def result=mvc.perform(MockMvcRequestBuilders.get("/messages").session(session)).andReturn()

        then:
        1*context.getBean("messageSourceVitalityUS") >> null;
        1*context.getBean("messageSource") >> messageSource
        1*messageSource.getAllMessages(_) >> properties
        result.response.status==200
        result.response.contentAsString.length()>2
 
        cleanup:
        messageSource=null
        properties=null
    }


    def "list - when message source is null create new properties"(){

        given:
        session.getAttribute("USER") >> user
        AppContext.applicationContext=context

        when:
        def result=mvc.perform(MockMvcRequestBuilders.get("/messages").session(session)).andReturn()

        then:
        result.response.status==200
        result.response.contentAsString.length()==14

    }

    def "list - Merge/overwrite existing properties with program specific properties key/value pair. "(){

        given:
        session.getAttribute("USER") >> user
        AppContext.applicationContext=context
        B2RReloadableResourceBundleMessageSource messageSource=Mock()
        Properties properties=new Properties();
        properties.setProperty("prgId.k","v")

        when:
        def result=mvc.perform(MockMvcRequestBuilders.get("/messages").session(session)).andReturn()

        then:
        1*context.getBean("messageSourceVitalityUS") >> messageSource;
        1*messageSource.getAllMessages(_) >> properties
        result.response.status==200
        result.response.contentAsString.length()>2

        cleanup:
        messageSource=null
        properties=null
    }


}
