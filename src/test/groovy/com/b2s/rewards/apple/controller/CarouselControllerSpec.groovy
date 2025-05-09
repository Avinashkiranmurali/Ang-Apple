package com.b2s.rewards.apple.controller

import com.b2s.apple.services.AppSessionInfo
import com.b2s.apple.services.CarouselService
import com.b2s.common.services.exception.ServiceException
import com.b2s.common.services.exception.ServiceExceptionEnums
import com.b2s.rewards.apple.integration.model.CarouselResponse
import com.b2s.rewards.apple.model.*
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

class CarouselControllerSpec extends Specification {

    final session = new MockHttpSession()
    final carouselService = Mock(CarouselService)
    final appSessionInfo = Mock(AppSessionInfo)

    @Subject
    final carouselController = new CarouselController(carouselService: carouselService,
            appSessionInfo: appSessionInfo)

    final mockMvc = MockMvcBuilders.standaloneSetup(carouselController).build()

    def 'get carousel response'() {
        given:
        def user = new User()
        appSessionInfo.currentUser() >> user
        def sessionProgram = getProgram()
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, sessionProgram)
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)
        def carouselName = 'Carousel'
        carouselService.getCarouselResponse(_,_,_) >> [CarouselResponse.builder().withName(carouselName).build()]
        def request = new MockHttpServletRequest()
        request.session = session
        def response = new MockHttpServletResponse()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.get("/carousel/pcp")
                .session(session))
                .andReturn()

        then:
        Objects.nonNull(result)
        result.response.status == 200
        result.response.contentAsString.contains(carouselName)
    }

    def 'get carousel response throw exception'() {
        given:
        def user = new User()
        appSessionInfo.currentUser() >> user
        def sessionProgram = getProgram()
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, sessionProgram)
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)
        carouselService.getCarouselResponse(_,_,_) >> {
            throw new ServiceException(ServiceExceptionEnums.SERVICE_EXECUTION_EXCEPTION)
        }
        def request = new MockHttpServletRequest()
        request.session = session
        def response = new MockHttpServletResponse()

        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.get("/carousel/pcp")
                .session(session))
                .andReturn()

        then:
        Objects.nonNull(result)
        result.response.status == 404
        result.response.contentAsString.equalsIgnoreCase('[]')
    }

    def getProgram() {
        def paymentOption = new PaymentOption(paymentOption: 'POINTS', supplementaryPaymentLimitType: 'P',
                supplementaryPaymentMaxLimit: 80)
        def program = new Program(varId: '1', programId: 'b2s_qa_only', payments: [paymentOption], config:
                ['catalogId': 'apple'])
        return program
    }
}
