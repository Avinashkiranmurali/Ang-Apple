package com.b2s.rewards.apple.controller

import com.b2s.apple.model.finance.CreditCardDetails
import com.b2s.apple.services.PaymentServerV2Service
import com.b2s.rewards.apple.model.Cart
import com.b2s.rewards.apple.model.CreditItem
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import org.apache.commons.collections.map.HashedMap
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification
import spock.lang.Subject

class CardDetailsControllerSpec extends Specification {


    PaymentServerV2Service paymentService = Mock(PaymentServerV2Service)
    final session = new MockHttpSession()
    final request = new MockHttpServletRequest()

    @Subject
    final cardDetailsController = new CardDetailsController(paymentService: paymentService)

    def "test saveCardDetails"() {

        given:
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, getUser())
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart())
        request.session = session

        when:
        final CreditCardDetails cardDetails = getCardDetails()
        final response = cardDetailsController.saveCardDetails(cardDetails,request)

        then:
        response.statusCodeValue == 200
    }

    def getUser() {
        User user = new User()
        user.varId = "UA"
        user.programId = "MP"
        user.locale = Locale.US
        return user
    }

    def getProgram(){
        Program program=new Program()
        Map config =new HashedMap()
        program.setConfig(config)
        return program
    }

    def getCart(){
        Cart cart=new Cart()
        cart.creditItem = new CreditItem()
        return cart;
    }

    def getCardDetails(){
        CreditCardDetails cardDetails=new CreditCardDetails()
        cardDetails.last4 = "2232"
        cardDetails.ccType = "VISA"
        cardDetails.ccNum = "4929736909778457"
        cardDetails.ccUsername = "Saranya Kathirvel"
        cardDetails.addr1 = "4236  Veltri Drive"
        cardDetails.city = "Anchorage"
        cardDetails.state = "AK"
        cardDetails.zip = "23456"
        cardDetails.country = "US"
        cardDetails.phoneNumber = "907-272-3609"
        return cardDetails
    }
}
