package com.b2s.rewards.apple.util

import com.b2s.apple.services.CartOrderConverterService
import com.b2s.apple.services.MessageService
import com.b2s.db.model.Order
import com.b2s.db.model.OrderLine
import com.b2s.rewards.apple.model.*
import com.b2s.rewards.common.context.AppContext
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.shop.common.order.SubscriptionManager
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class CartOrderConverterServiceSpec extends Specification {

    final messageService = Mock(MessageService)
    final subscriptionManager = Mock(SubscriptionManager)
    private ApplicationContext context;
    @Subject
    private CartOrderConverterService cartOrderConverterService = new CartOrderConverterService(messageService: messageService, subscriptionManager: subscriptionManager)

    def setup(){
        context=Mock()
    }

    def getProduct() {
        Product product = new Product()
        Offer offer = new Offer()
        offer.setUnpromotedSupplierItemPrice(new Price())
        offer.setUnpromotedSupplierTaxPrice(new Price())
        offer.setUnpromotedVarPrice(new Price())
        offer.setUnpromotedDisplayPrice(new Price(233, "dollar", 233000))
        offer.setTotalPrice(new Price())
        offer.setConvRate(1.00)
        offer.setRoundingIncrement(Money.ofMajor(CurrencyUnit.CAD,11L))
        offer.setBasePrice(new Price())
        offer.setVarPrice(new Price())
        product.setOffers(Arrays.asList(offer))
        product.setShipmentQuoteDate('Quote Date')
        product.setAdditionalInfo(['PricingModel': new PricingModel(paymentValue: 1.00, monthsSubsidized: 1, repaymentTerm: 3)])
        List<Option> options = new ArrayList<>()
        Option modelOption = new Option()
        modelOption.setI18Name("Model")
        modelOption.setKey("iPhone 13 Pro Max")
        modelOption.setName("model")
        modelOption.setValue("iPhone 13 Pro Max")
        modelOption.setOrderBy(0)
        options.add(modelOption)
        Option storageOption = new Option()
        storageOption.setI18Name("Storage")
        storageOption.setKey("1tb")
        storageOption.setName("storage")
        storageOption.setValue("1 TB")
        storageOption.setOrderBy(0)
        Option colorOption = new Option()
        colorOption.setI18Name("Color")
        colorOption.setKey("sierrablue")
        colorOption.setName("color")
        colorOption.setValue("Sierra Blue")
        colorOption.setOrderBy(0)
        colorOption.setSwatchImageUrl("https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-13-pro-max-blue-select_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&.v=1631652955000&qlt=95")
        options.add(colorOption)
        product.setOptions(options)
        return product
    }

    @Unroll
    def 'verify telephone if include space'() {
        setup:
        Cart cart = new Cart(shippingAddress: new com.b2s.rewards.apple.model.Address(phoneNumber: phoneNumber), cartTotal: new CartTotal
                (currency: Currency.getInstance(Locale.CANADA)));
        def product = getProduct()
        product.setPromotion(Optional.of(new Promotion(Promotion.builder().withDiscountPercentage(60))))
        CartItem cartItem = new CartItem(merchantId: "MER1", supplierId: 1, quantity: 1, productDetail: product, engrave: new Engrave(line1: 'Engrave line 1'),
                gift: new Gift(message1: 'This is a gift'))
        cart.setCartItems([cartItem])
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap)
        User user = new User(userid: 'felix', varId: 'rbc')

        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        expectedPhone == order.phone

        where:
        phoneNumber      | expectedPhone
        '4046732352'     | '4046732352'
        '(404) 732352'   | '404732352'
        '8 (404) 732352' | '8404732352'
        '+1 13840387936' | '+113840387936'
        '+8613840387936' | '+8613840387936'
        '(702)575-3918'  | '7025753918'
        ''               | ''
        null             | ''
    }

    @Unroll
    def 'verify establishmentFees'() {
        setup:
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA), establishmentFees: establishmentFees));
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap)
        User user = new User(userid: 'felix')

        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        establishmentFeesPoints == order.establishmentFeesPoints
        establishmentFeesPrice == order.establishmentFeesPrice

        where:
        establishmentFees                                | establishmentFeesPoints | establishmentFeesPrice
        new Price(Money.of(CurrencyUnit.CAD, 7.00), 700) | 700                     | 7.00
        new Price()                                      | 0                       | 0.00
        null                                             | null                    | null
    }

    @Unroll
    def 'verify order lines'() {
        setup:
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA)));
        CartItem cartItem = new CartItem(merchantId: "MER1", supplierId: 1)
        Product product = new Product()
        Offer offer = new Offer()
        offer.setUnpromotedSupplierItemPrice(new Price())
        offer.setUnpromotedSupplierTaxPrice(new Price())
        offer.setUnpromotedVarPrice(new Price())
        offer.setTotalPrice(new Price())
        offer.setConvRate(1.00)
        offer.setRoundingIncrement(Money.ofMajor(CurrencyUnit.CAD,11L))
        offer.setInverseRate(1.00)
        product.setOffers(Arrays.asList(offer))
        product.setShipmentQuoteDate('Quote Date')
        product.setAdditionalInfo(['PricingModel': new PricingModel(paymentValue: 1.00, monthsSubsidized: 1, repaymentTerm: 3)])
        cartItem.setProductDetail(product)
        cartItem.setQuantity(1)
        cartItem.setEngrave(new Engrave(line1: 'Engrave line 1'))
        cartItem.setGift(new Gift(message1: 'This is a gift'))
        Price price=new Price(Double.valueOf("100"),"CD",10);
        cart.getCartTotal().setPrice(price);
        cart.setCartItems(Arrays.asList(cartItem))
        Address address=new Address()
        address.setZip4("91769")
        address.setZip5("91769")
        address.setAddress3("test Address 3")
        address.setSubCity("test SubCIty")
        cart.setShippingAddress(address)
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap)
        User user = new User(userid: 'felix', varId: 'Delta')


        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        order.orderLines.size() == 1
        OrderLine orderLine = order.orderLines.get(0)
        orderLine.orderAttributes.find { attr -> attr.getName() == 'engravingLine1'}.getValue() == 'Engrave line 1'
        orderLine.orderAttributes.find { attr -> attr.getName() == 'giftMessage1'}.getValue() == 'This is a gift'
        orderLine.getSupplierId() == '1'
        orderLine.getQuantity() == 1
    }

    @Unroll
    def 'verify order lines created for discount item'() {
        setup:
        AppContext.applicationContext=context
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA)))
        def product = getProduct()
        product.setPromotion(Optional.of(new Promotion(Promotion.builder().withDiscountPercentage(60))))
        CartItem cartItem = new CartItem(merchantId: "MER1", supplierId: 1, quantity: 1, productDetail: product, engrave: new Engrave(line1: 'Engrave line 1'),
                gift: new Gift(message1: 'This is a gift'))

        Price price=new Price(Double.valueOf("100"),"CD",10);
        cart.getCartTotal().setPrice(price);
        cart.setCartItems([cartItem])

        DiscountCode discountCode = new DiscountCode(discountAmount: 10.00, discountCode: "DISCOUNT_CODE")
        cart.setDiscounts([discountCode])
        cart.setShippingAddress(new Address(zip4: "91769", zip5: "91769", address3: "test Address 3", subCity: "test SubCIty"))
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap, convRate: 2.0)
        User user = new User(userid: 'felix', varId: 'Delta')

        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        order.orderLines.size() == 2
        OrderLine orderLine = order.orderLines.get(0)
        OrderLine discountOrderLine = order.orderLines.get(1)
        orderLine.orderAttributes.find { attr -> attr.getName() == 'engravingLine1'}.getValue() == 'Engrave line 1'
        orderLine.attr1 == 'DISCOUNT_APPLIED'
        Double.valueOf(orderLine.attr2) == 60
        discountOrderLine.category == 'DISCOUNTCODE'

    }

    @Unroll
    def 'verify order lines created for gift item'() {
        setup:
        AppContext.applicationContext=context
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA)))
        CartItem cartItem = new CartItem(merchantId: "MER1", supplierId: 1, quantity: 1, productDetail: getProduct(), engrave: new Engrave(line1: 'Engrave line 1'),
                gift: new Gift(message1: 'This is a gift'))

        CartItem giftItem = new CartItem(merchantId: "MER1", supplierId: 1, quantity: 1, productDetail: product)
        giftItem.setEngrave(new Engrave(line1: 'Gift Engrave line 1'))

        Product giftProduct = getProduct()
        Promotion promotion = getPromotion(percentagePromo, promotionValue)
        giftProduct.setPromotion(Optional.of(promotion))
        giftItem.setProductDetail(giftProduct)

        cartItem.getSelectedAddOns().setGiftItem(giftItem)

        Price price=new Price(Double.valueOf("100"),"CD",10)
        cart.getCartTotal().setPrice(price);
        cart.setCartItems([cartItem])

        cart.setShippingAddress(new Address(zip4: "91769", zip5: "91769", address3: "test Address 3", subCity: "test SubCIty"))
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap, convRate: 2.0)
        User user = new User(userid: 'felix', varId: 'Delta')


        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        order.orderLines.size() == 2
        OrderLine orderLine = order.orderLines.get(0)
        OrderLine giftOrderLine = order.orderLines.get(1)
        orderLine.orderAttributes.find { attr -> attr.getName() == 'engravingLine1'}.getValue() == 'Engrave line 1'
        giftOrderLine.orderAttributes.find { attr -> attr.getName() == 'engravingLine1'}.getValue() == 'Gift Engrave line 1'
        giftOrderLine.attr1 == orderLine_attr1
        giftOrderLine.attr2 == orderLine_attr2
        giftOrderLine.attr3 == orderLine_attr3

        where:
        percentagePromo | promotionValue || orderLine_attr1              ||  orderLine_attr2 || orderLine_attr3
        true            |  100           || 'GIFT_ITEM'                  || '100'           ||  '1'
        true            |  80            || 'DISCOUNTED_GIFT_PERCENTAGE' || '80'            ||  '1'
        false           |  1234          || 'DISCOUNTED_GIFT_POINTS'     || '1234'          ||  '1'
    }

    def getPromotion(boolean isPercentagePromotion, int promotionValue){
        if(isPercentagePromotion){
            return new Promotion(Promotion.builder().withDiscountPercentage(new BigDecimal(promotionValue)))
        }else{
            return new Promotion(Promotion.builder().withFixedPointPrice(Money.of(CurrencyUnit.of("PNT"),
                    BigDecimal.valueOf(promotionValue))))
        }
    }

    @Unroll
    def 'verify order lines created for subscription'() {
        setup:
        AppContext.applicationContext=context
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA)))
        CartItem cartItem = new CartItem(merchantId: "MER1", supplierId: 1, quantity: 1,
                productDetail: getProduct(),
                engrave: new Engrave(line1: 'Engrave line 1'))
        def subscriptionItemId = "amp-tv-plus"
        def notAddedSubscriptionItemId = "amp-tv-plus-not-added"
        Subscription subscription = new Subscription(subscriptionItemId, 30,  true)
        Subscription notAddedToCartSubscription = new Subscription(notAddedSubscriptionItemId, 30 , false)

        Price price=new Price(Double.valueOf("100"),"CD",10);
        cart.getCartTotal().setPrice(price);
        cart.setCartItems([cartItem])

        Set<Subscription> subscriptions = new HashSet<>()
        subscriptions.add(subscription)
        subscriptions.add(notAddedToCartSubscription)
        cart.setSubscriptions(subscriptions)

        cart.setShippingAddress(new Address(zip4: "91769", zip5: "91769", address3: "test Address 3", subCity: "test SubCIty"))
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap, convRate: 2.0)
        User user = new User(userid: 'felix', varId: 'Delta')
        subscriptionManager.addSubscriptionOrderLine(_,_,_) >> GetSubscriptionOrderLines()
        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        order.orderLines.size() == 2
        OrderLine orderLine = order.orderLines.get(0)
        orderLine.getSupplierId() != CommonConstants.AMP_SUPPLIER_ID_STRING
        OrderLine subscriptionOrderLine = order.orderLines.get(1)
        subscriptionOrderLine.getSupplierId() == CommonConstants.AMP_SUPPLIER_ID_STRING
        subscriptionOrderLine.getItemId() == subscriptionItemId
    }

    @Unroll
    def 'verify order lines created for AppleCare+ Service Plan products - 1 Product with Service Plan'() {
        setup:
        AppContext.applicationContext=context
        Product product = getProduct()
        AddOns addOns = new AddOns()
        List<Product> servicePlans = new ArrayList<>()
        servicePlans.add(getProduct())
        addOns.setServicePlans(servicePlans)
        product.setAddOns(addOns)

        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA)))
        CartItem cartItem = new CartItem(merchantId: "1899", supplierId: 50, quantity: 1,
                productDetail: product,
                engrave: new Engrave(line1: 'Test Engrave'), selectedAddOns: getSelectedAddOns())

        Price price=new Price(Double.valueOf("100"),"CD",10);
        cart.getCartTotal().setPrice(price);
        cart.setCartItems([cartItem])

        cart.setShippingAddress(new Address(zip4: "91769", zip5: "91769", address3: "test Address 3", subCity: "test SubCIty"))
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap, convRate: 2.0)
        User user = new User(userid: 'felix', varId: 'Delta')

        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        //Totally 2 orderLines should be created ( 1-Product, 1-Service Plan)
        order.orderLines.size() == 2
        //orderLine created for Product
        OrderLine orderLine = order.orderLines.get(0)
        //orderLine created for Service Plan
        OrderLine servicePlanOrderLine = order.orderLines.get(1)
        //Product orderLine's Bundle Id & Service Plan orderLine's Bundle Id should be same
        orderLine.getBundleId() == servicePlanOrderLine.getBundleId()
        //Product orderLine's Bundle Id & Product orderLine's Line Num should be same
        orderLine.getBundleId() == orderLine.getLineNum().toString()
    }

    @Unroll
    def 'verify order lines created for AppleCare+ Service Plan products - 1 Product with Gift Item & Service Plan' () {
        setup:
        AppContext.applicationContext=context
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA)))
        CartItem cartItem = new CartItem(merchantId: "MER1", supplierId: 1, quantity: 1, productDetail: getProduct(), engrave: new Engrave(line1: 'Engrave line 1'),
                gift: new Gift(message1: 'This is a gift'))

        //Gift Item
        CartItem giftItem = new CartItem(merchantId: "MER1", supplierId: 1, quantity: 1, productDetail: product)
        giftItem.setEngrave(new Engrave(line1: 'Gift Engrave line 1'))

        Product giftProduct = getProduct()
        Promotion promotion = getPromotion(true, 100)
        giftProduct.setPromotion(Optional.of(promotion))
        giftItem.setProductDetail(giftProduct)

        cartItem.getSelectedAddOns().setGiftItem(giftItem)

        //Service Plan
        CartItem servicePlanProduct = new CartItem(merchantId: "1899", supplierId: 50000, quantity: 1,
                productDetail: getProduct(), engrave: new Engrave(line1: 'Test Engrave'))
        cartItem.getSelectedAddOns().setServicePlan(servicePlanProduct)

        Price price=new Price(Double.valueOf("100"),"CD",10);
        cart.getCartTotal().setPrice(price);
        cart.setCartItems([cartItem])

        cart.setShippingAddress(new Address(zip4: "91769", zip5: "91769", address3: "test Address 3", subCity: "test SubCIty"))
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap, convRate: 2.0)
        User user = new User(userid: 'felix', varId: 'Delta')

        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        //Totally 3 orderLines should be created (1-Product, 1-Gift Item, 1-Service Plan)
        order.orderLines.size() == 3
        //orderLine created for Product
        OrderLine orderLine = order.orderLines.get(0)
        //orderLine created for Service Plan
        OrderLine servicePlanOrderLine = order.orderLines.get(1)
        //orderLine created for Gift Item
        OrderLine giftOrderLine = order.orderLines.get(2)
        //Product's Bundle Id & Service Plan's Bundle Id should be same
        orderLine.getBundleId() == servicePlanOrderLine.getBundleId()
        //Service Plan's Bundle Id & Product's Line Num should be same
        servicePlanOrderLine.getBundleId() == orderLine.getLineNum().toString()
        //Product's Bundle Id & Product's Line Num should be same
        orderLine.getBundleId() == orderLine.getLineNum().toString()
        //Gift Item's Bundle Id should be empty
        giftOrderLine.getBundleId() == null
    }

    @Unroll
    def 'verify order lines - bridge2UnitBasePrice'() {
        setup:
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA)));
        CartItem cartItem = new CartItem(merchantId: "MER1", supplierId: 1)
        Product product = new Product()
        Offer offer = new Offer()
        offer.setConvRate(1.00)
        offer.setBridge2UnitBasePrice(Money.ofMajor(CurrencyUnit.CAD, 05L))
        product.setOffers(Arrays.asList(offer))
        cartItem.setProductDetail(product)
        cartItem.setQuantity(1)
        Price price = new Price(Double.valueOf("100"), "CD", 10);
        cart.getCartTotal().setPrice(price);
        cart.setCartItems(Arrays.asList(cartItem))
        cart.setShippingAddress(new Address())
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap)
        User user = new User(userid: 'felix', varId: 'Delta')

        when:
        Order order1 = cartOrderConverterService.convert(cart, user, applicationProperties, program)
        cart.getCartItems().get(0).getProductDetail().getOffers().get(0).setBridge2UnitBasePrice(null)
        Order order2 = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        order1.orderLines.size() == 1
        OrderLine orderLine1 = order1.orderLines.get(0)
        orderLine1.orderAttributes.find { attr -> attr.getName() == CommonConstants.BRIDGE2_UNIT_BASE_PRICE }.getValue() == 'CAD 5.00'
        order2.orderLines.get(0).orderAttributes.find { attr -> attr.getName() == CommonConstants.BRIDGE2_UNIT_BASE_PRICE } == null
    }

    @Unroll
    def 'verify order lines created for AppleCare+ Service Plan products - 1 Product with Gift Item & gift"s Service Plan' () {
        setup:
        AppContext.applicationContext=context
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.CANADA)))
        CartItem cartItem = new CartItem(merchantId: "MER1", supplierId: 1, quantity: 1, productDetail: getProduct(), engrave: new Engrave(line1: 'Engrave line 1'),
                gift: new Gift(message1: 'This is a gift'))

        //Gift Item
        CartItem giftItem = new CartItem(merchantId: "MER1", supplierId: 1, quantity: 1, productDetail: product)
        giftItem.setEngrave(new Engrave(line1: 'Gift Engrave line 1'))

        Product giftProduct = getProduct()
        Promotion promotion = getPromotion(true, 100)
        giftProduct.setPromotion(Optional.of(promotion))
        giftItem.setProductDetail(giftProduct)

        //Gift Service Plan
        CartItem giftServicePlanProduct = new CartItem(merchantId: "1899", supplierId: 50000, quantity: 1,
                productDetail: getProduct(), engrave: new Engrave(line1: 'Test Engrave'))
        giftItem.getSelectedAddOns().setServicePlan(giftServicePlanProduct)

        cartItem.getSelectedAddOns().setGiftItem(giftItem)

        //Service Plan
        CartItem servicePlanProduct = new CartItem(merchantId: "1899", supplierId: 50000, quantity: 1,
                productDetail: getProduct(), engrave: new Engrave(line1: 'Test Engrave'))
        cartItem.getSelectedAddOns().setServicePlan(servicePlanProduct)

        Price price=new Price(Double.valueOf("100"),"CD",10);
        cart.getCartTotal().setPrice(price);
        cart.setCartItems([cartItem])

        cart.setShippingAddress(new Address(zip4: "91769", zip5: "91769", address3: "test Address 3", subCity: "test SubCIty"))
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap, convRate: 2.0)
        User user = new User(userid: 'felix', varId: 'Delta')

        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        //Totally 4 orderLines should be created (1-Product, 1-Gift Item, 1-Service Plan, 1-Gift's Service Plan)
        order.orderLines.size() == 4
        //orderLine created for Product
        OrderLine orderLine = order.orderLines.get(0)
        //orderLine created for Service Plan
        OrderLine servicePlanOrderLine = order.orderLines.get(1)
        //orderLine created for Gift Item
        OrderLine giftOrderLine = order.orderLines.get(2)
        //orderLine created for gift's Service Plan
        OrderLine giftServicePlanOrderLine = order.orderLines.get(3)
        //Product's Bundle Id & Service Plan's Bundle Id should be same
        orderLine.getBundleId() == servicePlanOrderLine.getBundleId()
        //Service Plan's Bundle Id & Product's Line Num should be same
        servicePlanOrderLine.getBundleId() == orderLine.getLineNum().toString()
        //Product's Bundle Id & Product's Line Num should be same
        orderLine.getBundleId() == orderLine.getLineNum().toString()
        //Gift Item's Bundle Id & Gift Item Service Plan's Bundle Id should be same
        giftOrderLine.getBundleId() == giftServicePlanOrderLine.getBundleId()
        //Gift Item Service Plan's Bundle Id & Gift Item's Line Num should be same
        giftServicePlanOrderLine.getBundleId() == giftOrderLine.getLineNum().toString()
        //Gift Item's Bundle Id & Gift Item's Line Num should be same
        giftOrderLine.getBundleId() == giftOrderLine.getLineNum().toString()
    }

    def 'verify order with UserVarProgramCreditAdds'() {
        setup:
        Cart cart = new Cart(cartTotal: new CartTotal(currency: Currency.getInstance(Locale.US)))
        CartItem cartItem = new CartItem(merchantId: "MER1", supplierId: 1)
        CreditItem creditItem = new CreditItem(ccVarMargin: 0.3500000, varPrice: Money.of(CurrencyUnit.USD, 133.21),
                varProfit: 46.63d, pointsPurchased: 22250, effectiveConversionRate: 0.005987191011235955)
        cart.setCreditItem(creditItem)

        Product product = new Product(psid: '30001MMYQ3LL/A', name: 'Studio Display - product')
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
        cartItem.setQuantity(1)
        Price price = new Price(Double.valueOf("100"), "US", 10)
        cart.getCartTotal().setPrice(price)
        List<CartItem> cartItems = new ArrayList<>()
        cartItems.add(cartItem)
        cart.setCartItems(cartItems)

        CartItem creditLineItem = new CartItem(productId: CommonConstants.CAT_CREDIT_STR,
                supplierId: CommonConstants.SUPPLIER_TYPE_CREDIT, quantity: 1, productDetail: product)
        cart.setCreditLineItem(creditLineItem)

        Address address = new Address()
        address.setZip4("91769")
        address.setZip5("91769")
        address.setAddress3("test Address 3")
        address.setSubCity("test SubCIty")
        cart.setShippingAddress(address)
        Properties applicationProperties = Mock(Properties)
        Map configMap = new HashMap<String, Object>()
        Program program = new Program(config: configMap, targetCurrency: CurrencyUnit.USD, varId: 'FSV', programId: 'b2s_qa_only')
        User user = new User(userid: 'demo', varId: 'FSV', programId: 'b2s_qa_only')

        when:
        Order order = cartOrderConverterService.convert(cart, user, applicationProperties, program)

        then:
        order.orderLines.size() == 2
        OrderLine orderLine = order.orderLines.get(0)
        orderLine.getSupplierId() == '1'
        orderLine.getQuantity() == 1
        order.userVarProgramCreditAdds
        order.userVarProgramCreditAdds.ccVarMargin == 0.35f
        order.userVarProgramCreditAdds.ccVarPrice == Money.of(CurrencyUnit.USD, 133.21)
        order.userVarProgramCreditAdds.ccVarProfit == Money.of(CurrencyUnit.USD, 46.63)
        order.userVarProgramCreditAdds.pointsPurchased == 22250
        order.userVarProgramCreditAdds.effectiveConversionRate == Money.of(CurrencyUnit.USD, 0.01)
    }

    private CartAddOns getSelectedAddOns() {
        CartAddOns cartAddOns = new CartAddOns()

        CartItem servicePlanProduct = new CartItem(merchantId: "1899", supplierId: 50000, quantity: 1,
                productDetail: getProduct(),
                engrave: new Engrave(line1: 'Test Engrave'))

        cartAddOns.setServicePlan(servicePlanProduct)
        return cartAddOns
    }

    def GetSubscriptionOrderLines() {
        OrderLine orderLine = new OrderLine()
        orderLine.supplierId  = "40000"
        orderLine.itemId = "amp-tv-plus"
        return orderLine
    }

    def testgetDiscountedAmountInCents() {
        when:
        def result = CartOrderConverterService.getDiscountedAmountInCents(amount, discount)

        then:
        result == expectedResult

        where:
        amount      |   discount    |   expectedResult
        14          |   50.0        |   700
        10          |   70.0        |   300
        69.95       |   50.0        |   3498
        329.95      |   40.0        |   19797
        329         |   5           |   31255
        1319        |   5           |   125305
    }

}