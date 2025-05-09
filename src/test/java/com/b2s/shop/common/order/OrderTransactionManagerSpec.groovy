package com.b2s.shop.common.order

import com.b2s.rewards.apple.dao.OrderAttributeValueDao
import com.b2s.rewards.apple.model.*
import com.b2s.apple.services.CartOrderConverterService
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.shop.common.order.var.VarOrderManagerHolder
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author rjesuraj
 * Date : 9/22/2017
 * Time : 4:05 PM
 *
 *
 */
class OrderTransactionManagerSpec extends Specification {

    VarOrderManagerHolder varOrderManagerHolder = Mock()
    OrderAttributeValueDao orderAttributeValueDao = Mock()
    Properties applicationProperties = Mock()
    CartOrderConverterService cartOrderConverterService = Mock()

    @Subject
    OrderTransactionManager orderTransactionManager = new OrderTransactionManager(applicationProperties: applicationProperties, varOrderManagerHolder: varOrderManagerHolder,
                    orderAttributeValueDao: orderAttributeValueDao,cartOrderConverterService:cartOrderConverterService)


    def "test checkMinMaxPaymentLimit"() {
        setup:

        def cart = new Cart()
        def cartTotal = new CartTotal()

        def program = new Program()
        def paymentOptions = new ArrayList<PaymentOption>()
        def paymentOption = new PaymentOption()
        def result
        paymentOption.paymentMinLimit = paymentMinLimit
        paymentOption.paymentMaxLimit = paymentMaxLimit
        paymentOptions.add(paymentOption)
        paymentOption.setPaymentOption(CommonConstants.CAT_PAYROLLDEDUCTION_STR)
        program.setPayments(paymentOptions)
        cartTotal.price = price
        cartTotal.discountedPrice = discountedPrice
        cart.cartTotal = cartTotal
        result = orderTransactionManager.minMaxPaymentLimitNotSatisfied(cart, program, Mock(User.class))


        expect:
        result == resultValue

        where:
        paymentMinLimit || paymentMaxLimit || discountedPrice              || price | resultValue
        99              || 1000            || null                         || new Price(100.0d, "", 1000) | false
        111             || 1000            || null                         || new Price(100.0d, "", 1000) | true
        99              || 1000            || null                         || new Price(1200.0d, "", 1000) | true
        99              || 1000            || new Price(80.0d, "", 1000)   || new Price(100.0d, "", 1000) | true
        99              || 1000            || new Price(1200.0d, "", 1000) || new Price(100.0d, "", 1000) | true

    }
}