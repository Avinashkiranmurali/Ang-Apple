package com.b2s.rewards.apple.controller

import com.b2s.apple.services.AppSessionInfo
import com.b2s.rewards.apple.model.Cart
import com.b2s.rewards.apple.model.CartItem
import com.b2s.rewards.apple.model.CartTotal
import com.b2s.rewards.apple.model.Category
import com.b2s.rewards.apple.model.Engrave
import com.b2s.rewards.apple.model.Gift
import com.b2s.rewards.apple.model.PaymentOption
import com.b2s.rewards.apple.model.Price
import com.b2s.rewards.apple.model.Product
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.model.SupplementaryPaymentLimit
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.apple.services.CartService
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * Unit test specifications for {@link CartController}.
 */
class CartControllerSpec extends Specification {

    final session = new MockHttpSession()
    final cartService = Mock(CartService)
    final appSessionInfo = Mock(AppSessionInfo)

    @Subject
    final cartController = new CartController(cartService: cartService,
            appSessionInfo:appSessionInfo)

    final mockMvc = MockMvcBuilders.standaloneSetup(cartController).build()

    def 'get cart from session - directly call controller'() {
        given:
        final user = new User()
        final sessionCart = buildMockCart()
        final sessionProgram = buildMockProgram()

        and:
        final session = new MockHttpSession()
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user)
        appSessionInfo.currentUser() >> user
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, sessionProgram)
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, sessionCart)
        appSessionInfo.getSessionCart() >> sessionCart

        final request = new MockHttpServletRequest()
        request.session = session

        final response = new MockHttpServletResponse()

        when:
        final cartResponseEntity = cartController.getCart(request, response)

        then:
        1 * cartService.getCart(user, sessionCart, sessionProgram) >> sessionCart

        and: 'validate the original sessionCart did not change'
        sessionCart.cartItems[0].productDetail.categories.size() == buildMockCart().cartItems[0].productDetail.categories.size()
        sessionCart.cartItems[0].productDetail.categories[0].parents.size() == buildMockCart().cartItems[0].productDetail.categories[0].parents.size()
        sessionCart.cartItems[0].productDetail.categories[0].parents[0].parents.size() == buildMockCart().cartItems[0].productDetail.categories[0].parents[0].parents.size()

        and: 'validate the UI response Cart has filtered Categories and Parents'
        cartResponseEntity.body.cartItems[0].productDetail.categories.size() == 1
        cartResponseEntity.body.cartItems[0].productDetail.categories[0].parents.size() == 1
        cartResponseEntity.body.properties.get("cartItemsTotalCount")==0

    }

    private static Cart buildMockCart() {
        final CartTotal cartTotal = new CartTotal(price: new Price(700.0, "CAD", 100000))

        final paymentMinLimit = new Price(Double.valueOf(140), "CAD", 20000)
        final paymentMaxLimit = new Price(Double.valueOf(560), "CAD", 80000)
        final splitTenderPaymentLimit = new SupplementaryPaymentLimit(rewardsMinLimit: paymentMinLimit, paymentMaxLimit: paymentMaxLimit)

        final grandParent1 = new Category(slug: '1')
        final grandParent2 = new Category(slug: '2')
        final grandParent3 = new Category(slug: '3')
        final grandParent4 = new Category(slug: '4')
        final parent1 = new Category(slug: '1-1', parents: [grandParent1, grandParent2])
        final parent2 = new Category(slug: '1-2', parents: [grandParent3, grandParent4])
        final grandChild1 = new Category(slug: '1-1-1', parents: [parent1, parent2])
        final categories = [grandChild1]
        final product = new Product(name: 'Some Product', psid: 'some-psid', categories: categories)
        final cartItem = new CartItem(productDetail: product)
        cartItem.setSupplierId(200)
        return new Cart(cartTotal: cartTotal, displayCartTotal: cartTotal, cartItems: [cartItem], supplementaryPaymentLimit: splitTenderPaymentLimit)
    }

    private static Program buildMockProgram() {
        final paymentOption = new PaymentOption(paymentOption: 'POINTS', supplementaryPaymentLimitType: 'P', supplementaryPaymentMaxLimit: 80)
        final Program program = new Program(varId: '1', programId: 'b2s_qa_only', payments: [paymentOption], config: ['catalogId': 'apple'])
        return program
    }

    @Unroll
    def " test cartIemsCount when multiple products added with different quantity "() {
        setup:
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA)));
        cart.setCreditLineItem(new CartItem())
        CartItem cartItem1 = new CartItem(merchantId: "MER1", supplierId: 1)
        CartItem cartItem2 = new CartItem(merchantId: "MER2", supplierId: 2)
//cart item 1
        cartItem1.setQuantity(cartItem1Quantity)
        cartItem1.setEngrave(new Engrave(line1: 'Engrave line 1'))
        cartItem1.setGift(new Gift(message1: 'This is a gift'))
//cart item 2
        cartItem2.setQuantity(cartItem2Quantity)
        cartItem2.setEngrave(new Engrave(line1: 'Engrave line 1'))
        cartItem2.setGift(new Gift(message1: 'This is a gift'))

        Price price=new Price(Double.valueOf("100"),"CD",10);
        cart.getCartTotal().setPrice(price);
        cart.setCartItems(Arrays.asList(cartItem1,cartItem2))

        when:
        final def cartResponseEntity = cartController.getModifiedCart(cart)

        then:
        cartResponseEntity.getCartItemsTotalCount() == totalCount
        where:
        cartItem1Quantity | cartItem2Quantity || totalCount
        10                | 10                || 20
        0                 | 1                 || 1
        null              | null              || 0
    }

}
