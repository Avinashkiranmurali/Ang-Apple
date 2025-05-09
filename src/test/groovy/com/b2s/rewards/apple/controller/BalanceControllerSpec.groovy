package com.b2s.rewards.apple.controller

import com.b2s.apple.services.JwtService
import com.b2s.rewards.apple.exceptionhandler.CustomRestExceptionHandler
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

class BalanceControllerSpec extends Specification {

    JwtService jwtService = Mock();
    private MockMvc mockMvc;
    private User user;


    @Subject
    BalanceController balanceController = new BalanceController(jwtService: jwtService)

    MockHttpSession session = new MockHttpSession()



    def setup() {
        session=Mock()

        mockMvc = MockMvcBuilders.standaloneSetup(balanceController)
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build()

        user=new User();
        user.varId="VitalityUS"
        user.programId="prgId"
        user.locale=Locale.US
    }

    def cleanup(){
        session=null
        user=null
        mockMvc = null
    }

    def 'test without content-type & accept - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()
        String token = "test"

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post('/participant/balance')
                        .content(token)
                        .session(session))
                        .andReturn()

        then:
        //HTTP response status code is 415, if the content-type is unsupported
        result.response.status == 415
    }

    def 'test mismatch/not acceptable content-type - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()
        String token = "test"

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post('/participant/balance')
                .content(token)
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 415, if the content-type is not acceptable or mismatch
        result.response.status == 415
    }

    def 'test mismatch/not acceptable accept & without content-type - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()
        String token = "test"

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post('/participant/balance')
                .content(token)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 415, if the accept is not acceptable or mismatch and without content-type
        result.response.status == 415
    }

    def 'test mismatch/not acceptable accept & content-type - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()
        String token = "test"

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post('/participant/balance')
                .content(token)
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 415, if the content type & accept is not acceptable or mismatch
        result.response.status == 415
    }

    def 'test valid content-type & valid request - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()
        String token = "test";

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post('/participant/balance')
                .content(token)
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        //HTTP response status code is 200, if the content-type & accept is valid
        result.response.status == 200
        result.response.getHeader(CommonConstants.HTTP_HEADER_CONTENT_TYPE) == CommonConstants.APPLICATION_JSON
        result.response.getHeader("Content-Security-Policy") == "frame-ancestors 'none'"
        result.response.getHeader("Strict-Transport-Security") == "max-age=16070400"
        result.response.getHeader("X-Content-Type-Options") == "nosniff"
        result.response.getHeader("X-Frame-Options") == "DENY"
    }

    def 'test valid HTTP code 400 - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post('/participant/balance')
                .content(content)
                .contentType(MediaType.TEXT_PLAIN_VALUE )
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        result.response.status        == responseStatus

        where:
        content || responseStatus
        "test"  || 200 //OK
        ""      || 400 //BAD_REQUEST
    }

    def 'test valid HTTP code 401 - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post('/participant/balance')
                .content("test")
                .contentType(MediaType.TEXT_PLAIN_VALUE )
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn()

        then:
        result.response.status        == 401 //UNAUTHORIZED
    }

    def 'test valid HTTP code 404 - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .content("test")
                .contentType(MediaType.TEXT_PLAIN_VALUE )
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        result.response.status == responseStatus

        where:
        url                            || responseStatus
        '/participant/balance'         || 200 //OK
        '/participant/balanceTest'     || 404 //NOT_FOUND
        '/participantTest/balance'     || 404 //NOT_FOUND
        '/participantTest/balanceTest' || 404 //NOT_FOUND
    }

    def 'test valid HTTP code 405 with PUT instead of POST - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.put('/participant/balance')
                .content("test")
                .contentType(MediaType.TEXT_PLAIN_VALUE )
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        result.response.status        == 405 //METHOD_NOT_ALLOWED
    }

    def 'test valid HTTP code 405 with GET instead of POST - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.get('/participant/balance')
                .content("test")
                .contentType(MediaType.TEXT_PLAIN_VALUE )
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .session(session))
                .andReturn()

        then:
        result.response.status        == 405 //METHOD_NOT_ALLOWED
    }

    def 'test valid HTTP code 415 - balance API'() {

        given:
        session.getAttribute("USER") >> getUser()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.post('/participant/balance')
                .content(token)
                .contentType(contentType)
                .accept(accept)
                .session(session))
                .andReturn()

        then:
        result.response.status == responseStatus

        where:
        token  | contentType                    | accept                                || responseStatus
        "test" | MediaType.TEXT_PLAIN_VALUE     | MediaType.APPLICATION_JSON_UTF8_VALUE || 200 //OK
        "test" | MediaType.APPLICATION_ATOM_XML | MediaType.APPLICATION_ATOM_XML        || 415 //UNSUPPORTED_MEDIA_TYPE
    }

    def getUser(){
        user.varId="VitalityUS"
        user.programId="programId"
        user.locale=Locale.US
        return user
    }
}