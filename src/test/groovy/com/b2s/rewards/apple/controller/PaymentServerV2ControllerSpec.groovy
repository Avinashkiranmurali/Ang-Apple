package com.b2s.rewards.apple.controller

import com.b2s.apple.services.AppSessionInfo
import com.b2s.apple.services.PaymentServerV2Service
import com.b2s.rewards.apple.model.Cart
import com.b2s.rewards.apple.model.Program
import com.b2s.shop.common.User
import org.joda.money.CurrencyUnit
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest

import static com.b2s.rewards.common.util.CommonConstants.*

class PaymentServerV2ControllerSpec extends Specification {

    PaymentServerV2Service paymentService = Mock(PaymentServerV2Service)
    Properties applicationProperties=Mock(Properties);
    MockHttpSession session = Mock(MockHttpSession)
    HttpServletRequest request = Mock(HttpServletRequest)
    final appSessionInfo = Mock(AppSessionInfo)
    private MockMvc mvc

    @Subject
    final PaymentServerV2Controller paymentServerV2Controller = new PaymentServerV2Controller(paymentService:
            paymentService, applicationProperties: applicationProperties, appSessionInfo: appSessionInfo)

    def setup() {
        mvc = MockMvcBuilders.standaloneSetup(paymentServerV2Controller).build()
        appSessionInfo.currentUser() >> getUser()
        session.getAttribute(USER_SESSION_OBJECT) >> getUser()
        session.getAttribute(APPLE_CART_SESSION_OBJECT) >> getCart()
        session.getAttribute(PROGRAM_SESSION_OBJECT) >> getProgram()
        request.getSession() >> session

        applicationProperties.getProperty(PAY_SERVER_EXTERNAL_URL) >> "https://pay-vip.apldev.bridge2solutions.net/paymentserver/api"
        applicationProperties.getProperty(PAY_SERVER_INTERNAL_URL) >> "http://pay02.apldev.bridge2solutions.net:8080/paymentserver/api"
    }

    def "get 201 response"() {

        given:
        paymentService.createTransaction(_, _, _, _, _, _) >> 'VUF8TVB8bWF0aGl8MTIzMzM0fDAwMDAwMDAw'

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post("/payment/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .session(session))
                .andReturn()

        then:
        result.response.status == 201

    }

    def getUser() {
        User user = new User()
        user.setVarId('UA')
        user.setProgramId('MP')
        user.setLocale(Locale.US)
        user
    }

    def getProgram() {
        Program program = new Program()
        program.setIsDemo(true)
        program.setTargetCurrency(CurrencyUnit.USD)
        program
    }

    def getCart() {
        Cart cart = new Cart()
        cart.setCost(10.00)
        cart
    }
}