package com.b2s.common.services

import com.b2r.paymentserver.api.PaymentServerClient
import com.b2r.paymentserver.api.model.PaymentGateway
import com.b2r.paymentserver.api.model.response.CaptureResponse
import com.b2s.apple.entity.PaymentEntity
import com.b2s.apple.model.finance.CreditCardDetails
import com.b2s.apple.services.PaymentServerV2Service
import com.b2s.apple.services.VarProgramMessageService
import com.b2s.rewards.apple.model.*
import com.b2s.rewards.common.context.AppContext
import com.b2s.shop.common.User
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static com.b2s.rewards.common.util.CommonConstants.*

class PaymentServerV2ServiceSpec extends Specification {

    PaymentServerClient paymentServerClient = Mock(PaymentServerClient)
    VarProgramMessageService varProgramMessageService = Mock(VarProgramMessageService)
    Properties applicationProperties=Mock(Properties)
    ApplicationContext context = Mock(ApplicationContext)

    @Subject
    private PaymentServerV2Service paymentServerV2Service = new PaymentServerV2Service(
            paymentServerClient: paymentServerClient,
            varProgramMessageService: varProgramMessageService,
            applicationProperties: applicationProperties)

    void setup() {
        applicationProperties.getProperty(DBA) >> "United Apple product"
        applicationProperties.getProperty(PAY_SERVER_EXTERNAL_URL) >> "https://pay-vip.apldev.bridge2solutions.net/paymentserver/api"
        applicationProperties.getProperty(PAY_SERVER_INTERNAL_URL) >> "http://pay02.apldev.bridge2solutions.net:8080/paymentserver/api"

        varProgramMessageService.getMessages(Optional.of('UA'), Optional.ofNullable('MP'), Locale.US.toString()) >> applicationProperties
    }

    @Unroll
    def 'test create transaction'() {
        given:
        User user = getUser()
        Program program = getProgram()
        String sessionId = '123333'
        boolean overrideProgramDemoForPaymentServerMode = true
        String requestDomain = null
        double chargeAmount = 10.00
        String randomId = '00000000'

        paymentServerClient.createTransaction(_) >> transactionUri

        try {
            String transactionId = paymentServerV2Service.createTransaction(user, program, sessionId,
                    overrideProgramDemoForPaymentServerMode, requestDomain, chargeAmount, randomId)
            assert transactionId == transactionID
        }
        catch (Exception ex) {
            assert  ex.message == exceptionMessage
        }

        where:
        transactionUri                                                                                     || transactionID                          || exceptionMessage
        'http://localhost:8080/paymentserver/api/transaction/VUF8TVB8bWF0aGl8MTIzMzM0fDAwMDAwMDAw'.toURI() || 'VUF8TVB8bWF0aGl8MTIzMzM0fDAwMDAwMDAw' || null
        null                                                                                               || null                                   || 'Payment transaction is NULL - caused by an exception on payment server'
    }

    @Unroll
    def 'test capture transaction failure scenario'() {
        setup:
        User user = getUser()
        PaymentEntity paymentEntity = new PaymentEntity()
        paymentServerClient.captureTransaction(_) >> captureTransactionResponse

        when:
        boolean captureTransaction = paymentServerV2Service.captureTransaction(user, paymentEntity, transactionID)

        then:
        captureTransaction == expectedResponse

        where:
        captureTransactionResponse                                           | transactionID                          || expectedResponse
        new CaptureResponse(error: true, errorResponse: 'Unable to capture') | 'VUF8TVB8bWF0aGl8MTIzMzM0fDAwMDAwMDAw' || false
        getResponse()                                                        | 'VUF8TVB8bWF0aGl8MTIzMzM0fDAwMDAwMDAw' || true
        getResponse()                                                        | 'VUF8TVB8bWF0a Aw'                     || false
        null                                                                 | 'VUF8TVB8bWF0aGl8MTIzMzM0fDAwMDAwMDAw' || false
    }

    def 'test map credit card details'() {
        given:
        User user = getUser()
        Program program = getProgram()
        Cart cart = getCart()
        AppContext.applicationContext = context
        cart.creditItem = new CreditItem(currency: CurrencyUnit.USD, varPrice: Money.of(CurrencyUnit.USD, 133.21))

        when:
        final CreditCardDetails cardDetails = getCardDetails()
        paymentServerV2Service.addCCLineItemInCart(cardDetails, cart, user, program)

        then:
        user.getBillTo() != null  //To check 'BillTo' value present though it is transient
        user.getBillTo().firstName == cardDetails.firstName
        cart.creditItem.ccLast4 == cardDetails.last4
        cart.creditItem.creditCardType == cardDetails.ccType
    }

    def 'test map credit card details with First name and last name'() {
        given:
        User user = getUser()
        Program program = getProgram()
        Cart cart = getCart()
        AppContext.applicationContext = context
        CreditItem creditItem = new CreditItem()
        creditItem.setBaseItemPrice(20.00)
        cart.creditItem = new CreditItem(currency: CurrencyUnit.USD)

        when:
        final CreditCardDetails cardDetails = getCardDetails()
        cardDetails.setCcUsername("FirstName LastName")
        paymentServerV2Service.addCCLineItemInCart(cardDetails, cart, user, program)

        then:
        cart.creditItem.getCcFirstName() == "FirstName"
        cart.creditItem.getCcLastName() == "LastName"
    }

    def 'test capture transaction with no response'() {
        given:
        User user = getUser()
        String transactionId = 'VUF8TVB8bWF0aGl8MTIzMzM0fDAwMDAwMDAw'
        PaymentEntity paymentEntity = new PaymentEntity()

        paymentServerClient.createTransaction(_) >> null

        when:
        boolean captureTransaction = paymentServerV2Service.captureTransaction(user, paymentEntity, transactionId)

        then:
        !captureTransaction
    }

    def 'test capture transaction'() {
        given:
        User user = getUser()
        String transactionId = 'VUF8TVB8bWF0aGl8MTIzMzM0fDAwMDAwMDAw'
        PaymentEntity paymentEntity = new PaymentEntity()

        CaptureResponse captureResponse = new CaptureResponse()
        captureResponse.setErrorResponse('Unable to capture')
        paymentServerClient.createTransaction(_) >> captureResponse

        when:
        boolean captureTransaction = paymentServerV2Service.captureTransaction(user, paymentEntity, transactionId)

        then:
        !captureTransaction
    }

    @Unroll
    def 'test allowed card types'() {
        given:
        Program program = getProgram()
        program.config.put(SUPPORTED_CREDIT_CARD_TYPES,supported_cards)

        expect:
        paymentServerV2Service.getAllowedCardTypes(program) == result

        where:
        supported_cards                 || result
        'VISA,MASTERCARD,AMEX,DISCOVER' || 'VISA,Master Card,American Express,Discover'
        'VISA'                          || 'VISA'
        'NONE'                          || null
        'VISA,MASTERCARD,NONE'          || null
        'UNKNOWN'                       || null
    }

    def 'test payment gateway'() {
        given:
        Program program = getProgram()
        program.config.put(PAYMENT_GATEWAY, vpcValue)

        expect:
        paymentServerV2Service.getPaymentGateway(program) == paymentGateway

        where:
        vpcValue    || paymentGateway
        'PF'        || PaymentGateway.PAYPAL
        'BT'        || PaymentGateway.BRAINTREE
        'PF test'   || PaymentGateway.PAYPAL
        null        || PaymentGateway.PAYPAL
    }

    @Unroll
    def 'test get DBname'() {
        given:
        User user = new User(varId: 'UA', programId: programId, locale: Locale.US)

        when:
        def result = paymentServerV2Service.getDbaName(user)

        then:
        result == dbName

        where:
        programId     || dbName
        'MP'          || 'United Apple product'
        'b2s_qa_only' || null
    }

    def 'test creditLineItem with varPrice'() {
        given:
        User user = getUser()
        Program program = getProgram()
        Cart cart = getCart()
        AppContext.applicationContext = context
        cart.creditItem = new CreditItem(currency: CurrencyUnit.USD, baseItemPrice: 200.0d, varPrice: Money.of(CurrencyUnit.USD, 133.21))

        when:
        final CreditCardDetails cardDetails = getCardDetails()
        paymentServerV2Service.addCCLineItemInCart(cardDetails, cart, user, program)

        then:
        cart.creditItem.ccLast4 == cardDetails.last4
        cart.creditItem.creditCardType == cardDetails.ccType
        cart.creditLineItem != null
        cart.creditLineItem.productDetail
        cart.creditLineItem.productDetail.defaultOffer
        cart.creditLineItem.productDetail.defaultOffer.varPrice
        cart.creditLineItem.productDetail.defaultOffer.varPrice.amount == -133.21d
    }

    def getUser() {
        return new User(varId: 'UA', programId: 'MP', locale: Locale.US)
    }

    def getProgram() {
        return new Program(varId: 'UA', programId: 'MP', isDemo: true, targetCurrency: CurrencyUnit.USD,
            ccFilters: Arrays.asList(new Program.CCBin("415609")))
    }

    def getProduct(){

        return product
    }

    def getCart() {
        Cart cart = new Cart()
        List li = new ArrayList<CartItem>()
        CartItem cartItem = new CartItem()
        cartItem.setSupplierId(20000)

        def product = new Product(psid: '30001MMYQ3LL/A', name: 'Studio Display - product')
        Offer offer = new Offer()
        offer.setUnpromotedSupplierItemPrice(new Price())
        offer.setUnpromotedSupplierTaxPrice(new Price())
        offer.setUnpromotedVarPrice(new Price())
        offer.setTotalPrice(new Price())
        offer.setConvRate(1.00)
        offer.setRoundingIncrement(Money.ofMajor(CurrencyUnit.USD, 11L))
        offer.setInverseRate(1.00)
        offer.setVarItemMargin(1.0d)
        offer.basePrice = new Price(100.0d, "", 1000)
        product.setOffers(Arrays.asList(offer))
        cartItem.setProductDetail(product)

        li.add(cartItem)
        cart.setCartItems(li)

        CartTotal cartTotal = new CartTotal()
        Price price = new Price(170.13, "USD", 31800);
        cartTotal.setPrice(price)
        cart.setCartTotal(cartTotal)
        cart.setConvRate(185.1851851851852)

        return cart;
    }

    def getCardDetails(){
        return new CreditCardDetails(last4: 2232, ccType: 'VISA', ccUsername: 'Mathivanan', phoneNumber: '999-998-8888',
                addr1: '5900 Windward Pkwy', city: 'Anchorage', state: 'AK', zip: '23456', country: 'US')
    }

    def getResponse() {
        return new CaptureResponse(transactionId: 'VUF8TVB8bWF0aGl8RDc0MkI2QjUyOTExNzc3NDFDMDUyMEVBQjBFMTBFNTZ8clAxNWg3MnkzdVBIMVZLVzMyZ3M2MTZmYlZZdUxQeFJlZmU1NkM5MHRNY3lIb0xLczI',
            referenceNumber: 'Z8INIRES9A', ccLastFour: '6786', error: false)
    }
}