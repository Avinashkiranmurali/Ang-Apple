
package com.b2s.apple.services

import com.b2s.apple.mapper.ProductMapper
import com.b2s.common.services.discountservice.CouponCodeValidator
import com.b2s.common.services.pricing.impl.LocalPricingServiceV2
import com.b2s.common.services.productservice.ProductServiceV3
import com.b2s.rewards.apple.dao.ShoppingCartDao
import com.b2s.rewards.apple.dao.ShoppingCartItemDao
import com.b2s.rewards.apple.integration.model.AddToCartResponse
import com.b2s.rewards.apple.integration.model.PaymentOptions
import com.b2s.rewards.apple.model.*
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.rewards.model.Merchant
import com.b2s.rewards.model.Supplier
import com.b2s.shop.common.User
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class CartServiceSpec extends Specification {

    def appleShoppingCartDao = Mock(ShoppingCartDao)

    def appleShoppingCartItemDao = Mock(ShoppingCartItemDao)

    def pricingServiceV2 = Mock(LocalPricingServiceV2)

    def cartService = new CartService(appleShoppingCartDao : appleShoppingCartDao,
            appleShoppingCartItemDao: appleShoppingCartItemDao, pricingServiceV2: pricingServiceV2)

    def user =  new User()

    def "test addSubscriptions() without cart items" () {
        setup:
        def cart = new Cart()

        when:
        def result = cartService.addSubscriptions(getProgram(), cart, user)

        then:
        !result

    }

    def "test addSubscriptions() with cart items and without product details and categories" () {
        setup:
        def cart = new Cart()

        cart.cartItems = [new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(),
                          new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(),
                          new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(),
                          new CartItem(), new CartItem()]

        List<ShoppingCartItem> shoppingCartItems = new ArrayList<>()

        appleShoppingCartDao.get(_, _, _) >> new ShoppingCart()
        appleShoppingCartItemDao.getShoppingCartItems(_) >> shoppingCartItems

        when:
        def result = cartService.addSubscriptions(getProgram(), cart, user)

        then:
        !result

    }

    def "test addSubscriptions() with cart item and product details - AMP Config available on Apple & PS response" () {
        setup:
        def cart = new Cart()
        def (Product product, CartItem cartItem) = getMockCartItemIpad()
        product.setAmpSubscriptionConfig(getMockAmpConfigsForPS())
        cartItem.setProductDetail(product)

        cart.cartItems = [cartItem]
        mockShoppingCartItem()


        when:
        cartService.addSubscriptions(getProgram(), cart, user)

        then:
        cart.subscriptions.size() != null
        cart.getSubscriptions().iterator().next().itemId.equalsIgnoreCase("amp-news-plus")
        !cart.getSubscriptions().iterator().next().addedToCart

    }

    def "test addSubscriptions() with cart item and product details - AMP Config available on Apple & not on PS response" () {
        setup:
        def cart = new Cart()
        def (Product product, CartItem cartItem) = getMockCartItemIpad()
        cartItem.setProductDetail(product)
        cart.cartItems = [cartItem]
        mockShoppingCartItem()

        when:
        cartService.addSubscriptions(getProgram(), cart, user)

        then:
        cart.subscriptions.size() == 0
    }

    def "test addSubscriptions() - AMP Config not available on Apple's end & available on PS response" () {
        setup:
        def cart = new Cart()
        def (Product product, CartItem cartItem) = getMockCartItemIpad()
        product.setAmpSubscriptionConfig(getMockAmpConfigsForPS())
        cartItem.setProductDetail(product)

        cart.cartItems = [cartItem]
        mockShoppingCartItem()
        Program program = getProgram()
        program.setAmpSubscriptionConfig(null)
        when:
        cartService.addSubscriptions(program, cart, user)

        then:
        cart.subscriptions == null
    }

    def "test addSubscriptions() - AMP Config available on Apple & PS response - AMP Explicitly disabled via VPC" () {
        setup:
        def cart = new Cart()
        def (Product product, CartItem cartItem) = getMockCartItemIpad()
        product.setAmpSubscriptionConfig(getMockAmpConfigsForPS())
        cartItem.setProductDetail(product)

        cart.cartItems = [cartItem]
        mockShoppingCartItem()
        Program program = getProgram()
        Map<String, Object> config = new HashMap<String, Object>()
        config.put(CommonConstants.DISABLE_AMP, true)
        program.setConfig(config)

        when:
        cartService.addSubscriptions(program, cart, user)

        then:
        cart.subscriptions == null

    }

    def "test addSubscriptions() with qualifying item having higher duration" () {
        /*   Cart has two items which qualifies for amp-music-plus (7 days ipad for  & 30 days for iphone). As per our
              requirement, user has to be given subscription with higher duration*/
        setup:
        def cart = new Cart()
        def (Product product, CartItem cartItem) = getMockCartItemIpad()
        product.setAmpSubscriptionConfig(getMockAmpConfigsForPS())
        cartItem.setProductDetail(product)

        def (Product productIphone, CartItem cartItemIphone) = getMockCartItemsIphone()

        Set<String> ampConfigs = new TreeSet<>()
        ampConfigs.add("amp-music-plus")
        productIphone.setAmpSubscriptionConfig(ampConfigs)
        cartItemIphone.setProductDetail(productIphone)

        cart.cartItems = [cartItem]

        cart.getCartItems().add(cartItemIphone)
        mockShoppingCartItem()


        when:
        cartService.addSubscriptions(getProgram(), cart, user)
        Subscription subscription = cart.getSubscriptions().iterator().next()

        then:
        cart.subscriptions.size() != null
        subscription.getDuration() == 30
        subscription.getItemId().equalsIgnoreCase("amp-music-plus")
    }

    def "test addSubscriptions() - AMP Services with multiple cart items will pick the higher duration AMP service" () {

        setup:
        def cart = new Cart()
        Program program = getProgram()
        program.setAmpSubscriptionConfig(setAMPSubscriptionConfigs())

        cart.setCartItems(getCartItem)
        mockShoppingCartItem()

        when:
        cartService.addSubscriptions(program, cart, user)

        then:
        cart.subscriptions.size() == subscriptionSize
        subscriptionTypeAndMaximumDurationValidation(cart.subscriptions, ampItemId, duration) == ampItemId

        where:
        getCartItem                                       || subscriptionSize     || ampItemId        || duration
        getCartItemsList('iphone', 'ipad')   ||          3           ||  "amp-music"     ||  60
        getCartItemsList('iphone', 'ipad')   ||          3           ||  "amp-tv-plus"   ||  7
        getCartItemsList('iphone', 'ipad')   ||          3           ||  "amp-news-plus" ||  60
        getCartItemsList('iphone', 'ipad')   ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('mac', 'ipad')      ||          3           ||  "amp-music"     ||  60
        getCartItemsList('mac', 'ipad')      ||          3           ||  "amp-tv-plus"   ||  15
        getCartItemsList('mac', 'ipad')      ||          3           ||  "amp-news-plus" ||  90
        getCartItemsList('mac', 'ipad')      ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('mac', 'tv')        ||          3           ||  "amp-music"     ||  30
        getCartItemsList('mac', 'tv')        ||          3           ||  "amp-tv-plus"   ||  30
        getCartItemsList('mac', 'tv')        ||          3           ||  "amp-news-plus" ||  90
        getCartItemsList('mac', 'tv')        ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('iphone', 'tv')     ||          3           ||  "amp-music"     ||  30
        getCartItemsList('iphone', 'tv')     ||          3           ||  "amp-tv-plus"   ||  30
        getCartItemsList('iphone', 'tv')     ||          3           ||  "amp-news-plus" ||  30
        getCartItemsList('iphone', 'tv')     ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('mac', 'iphone')    ||          3           ||  "amp-music"     ||  30
        getCartItemsList('mac', 'iphone')    ||          3           ||  "amp-tv-plus"   ||  15
        getCartItemsList('mac', 'iphone')    ||          3           ||  "amp-news-plus" ||  90
        getCartItemsList('mac', 'iphone')    ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('ipad', 'tv')       ||          3           ||  "amp-music"     ||  60
        getCartItemsList('ipad', 'tv')       ||          3           ||  "amp-tv-plus"   ||  30
        getCartItemsList('ipad', 'tv')       ||          3           ||  "amp-news-plus" ||  60
        getCartItemsList('ipad', 'tv')       ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('iphone', 'iphone') ||          3           ||  "amp-music"     ||  30
        getCartItemsList('iphone', 'iphone') ||          3           ||  "amp-tv-plus"   ||  7
        getCartItemsList('iphone', 'iphone') ||          3           ||  "amp-news-plus" ||  30
        getCartItemsList('iphone', 'iphone') ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('ipad', 'ipad')     ||          3           ||  "amp-music"     ||  60
        getCartItemsList('ipad', 'ipad')     ||          3           ||  "amp-tv-plus"   ||  7
        getCartItemsList('ipad', 'ipad')     ||          3           ||  "amp-news-plus" ||  60
        getCartItemsList('ipad', 'ipad')     ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('mac', 'mac')       ||          3           ||  "amp-music"     ||  30
        getCartItemsList('mac', 'mac')       ||          3           ||  "amp-tv-plus"   ||  15
        getCartItemsList('mac', 'mac')       ||          3           ||  "amp-news-plus" ||  90
        getCartItemsList('mac', 'mac')       ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('tv', 'tv')         ||          1           ||  "amp-tv-plus"   ||  30
        getCartItemsList('tv', 'tv')         ||          1           ||  "amp-music-plus"||  0
        getCartItemsList('mac', 'watch')     ||          3           ||  "amp-music"     ||  30
        getCartItemsList('mac', 'watch')     ||          3           ||  "amp-tv-plus"   ||  15
        getCartItemsList('mac', 'watch')     ||          3           ||  "amp-news-plus" ||  90
        getCartItemsList('mac', 'watch')     ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('iphone', 'watch')  ||          3           ||  "amp-music"     ||  30
        getCartItemsList('iphone', 'watch')  ||          3           ||  "amp-tv-plus"   ||  7
        getCartItemsList('iphone', 'watch')  ||          3           ||  "amp-news-plus" ||  30
        getCartItemsList('iphone', 'watch')  ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('ipad', 'watch')    ||          3           ||  "amp-music"     ||  60
        getCartItemsList('ipad', 'watch')    ||          3           ||  "amp-tv-plus"   ||  7
        getCartItemsList('ipad', 'watch')    ||          3           ||  "amp-news-plus" ||  60
        getCartItemsList('ipad', 'watch')    ||          3           ||  "amp-music-plus"||  0
        getCartItemsList('tv', 'watch')      ||          1           ||  "amp-tv-plus"   ||  30
        getCartItemsList('watch', 'watch')   ||          0           ||  null            ||  0

    }


    /*
    * setAMPSubscriptionConfigs method sets the AMP - Category mapping configs
    *  |------------------------------------------------------------|
    *  |  AMP's  | iphone |   ipad   |   mac   |   tv   |   watch   |
    *  |------------------------------------------------------------|
    *  | Music   |  -     |    60    |   30    |   -    |    -      |
    *  |  TV+    |  7     |    7     |   15    |   30   |    -      |
    *  | News+   |  30    |    60    |   90    |   -    |    -      |
    *  | Music+  |  -     |    -     |    -    |   -    |    -      |
    *  |------------------------------------------------------------|
    *
    */

    def setAMPSubscriptionConfigs() {

        Set<AMPConfig> ampConfigs = new HashSet<>()

        //iPhone
        AMPConfig config1 = AMPConfig.builder()
                .withCategory("iphone")
                .withItemId("amp-music")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build()
        ampConfigs.add(config1)

        AMPConfig config11 = AMPConfig.builder()
                .withCategory("iphone")
                .withItemId("amp-tv-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(7)
                .build()
        ampConfigs.add(config11)

        AMPConfig config111 = AMPConfig.builder()
                .withCategory("iphone")
                .withItemId("amp-news-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build()
        ampConfigs.add(config111)

        //iPad
        AMPConfig config2 = AMPConfig.builder()
                .withCategory("ipad")
                .withItemId("amp-music")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(60)
                .build()
        ampConfigs.add(config2)

        AMPConfig config22 = AMPConfig.builder()
                .withCategory("ipad")
                .withItemId("amp-tv-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(7)
                .build()
        ampConfigs.add(config22)

        AMPConfig config222 = AMPConfig.builder()
                .withCategory("ipad")
                .withItemId("amp-news-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(60)
                .build()
        ampConfigs.add(config222)

        //Mac
        AMPConfig config3 = AMPConfig.builder()
                .withCategory("mac")
                .withItemId("amp-music")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build()
        ampConfigs.add(config3)

        AMPConfig config33 = AMPConfig.builder()
                .withCategory("mac")
                .withItemId("amp-tv-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(15)
                .build()
        ampConfigs.add(config33)

        AMPConfig config333 = AMPConfig.builder()
                .withCategory("mac")
                .withItemId("amp-news-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(90)
                .build()
        ampConfigs.add(config333)

        //TV
        AMPConfig config4 = AMPConfig.builder()
                .withCategory("tv")
                .withItemId("amp-tv-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build()
        ampConfigs.add(config4)

        //Music Plus
        AMPConfig config5 = AMPConfig.builder()
                .withCategory("homepod-mini")
                .withItemId("amp-music-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build()
        ampConfigs.add(config4)

        return ampConfigs

    }

    def "test generateCartWithPriceInfo() with cart response"() {
        setup:
        Product product = new Product()
        Supplier supplier = new Supplier()
        supplier.setSupplierId(200)//"MERCHANDISE"
        product.setSupplier(supplier)
        pricingServiceV2.calculateCartPrice(_, _, _) >> null

        when:
        def cart = cartService.generateCartWithPriceInfo(user, getProgram(), product)

        then:
        cart
    }

    def subscriptionTypeAndMaximumDurationValidation(Set<Subscription> subscriptions, String ampItemId, Integer duration) {

        for (Subscription subscription : subscriptions) {
            if (ampItemId != null) {
                if (ampItemId == "amp-music-plus") {
                    return ampItemId
                }
                if (duration > 0) {
                    //Validating the subscription type with it's higher duration's config mapping
                    if (subscription.getItemId().equalsIgnoreCase(ampItemId) && subscription.getDuration() == duration) {
                        return subscription.itemId
                    }
                }
            } else {
                return null
            }
        }

    }

    def getCartItemsList(String slug1, String slug2) {

        List<CartItem> cartItems = new ArrayList<>()

        Set<String> ampsFromPS = new TreeSet<>()
        ampsFromPS.add("amp-news-plus")
        ampsFromPS.add("amp-music")
        ampsFromPS.add("amp-tv-plus")

        def (Product product1, CartItem cartItem1) = getMockCartItems(slug1, slug1)
        product1.setAmpSubscriptionConfig(ampsFromPS)
        cartItem1.setProductDetail(product1)
        cartItems.add(cartItem1)

        def (Product product2, CartItem cartItem2) = getMockCartItems(slug2, slug2)
        product2.setAmpSubscriptionConfig(ampsFromPS)
        cartItem2.setProductDetail(product2)
        cartItems.add(cartItem2)

        cartItems
    }


    private void mockShoppingCartItem() {
        List<ShoppingCartItem> shoppingCartItems = new ArrayList<>()

        appleShoppingCartDao.get(_, _, _) >> new ShoppingCart()
        appleShoppingCartItemDao.getShoppingCartItems(_) >> shoppingCartItems
    }

    private List getMockCartItemIpad() {
        CartItem cartItem = new CartItem()
        cartItem.addedDate = new Date()
        cartItem.id = 123456
        cartItem.imageURL = "http://test"
        cartItem.merchantId = "30001"
        Product product = new Product()

        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "ipad"
        category.name = "iPad Pro"
        categories.add(category)
        product.setCategory(categories)
        [product, cartItem]
    }

    private List getMockCartItemsIphone() {
        CartItem cartItem = new CartItem()
        cartItem.addedDate = new Date()
        cartItem.id = 123457
        cartItem.imageURL = "http://test"
        cartItem.merchantId = "30001"
        Product product = new Product()

        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "iphone"
        category.name = "iPhone"
        categories.add(category)
        product.setCategory(categories)
        [product, cartItem]
    }

    private List getMockCartItems(String slug, String name) {
        CartItem cartItem = new CartItem()
        cartItem.addedDate = new Date()
        cartItem.id = 123457
        cartItem.imageURL = "http://test"
        cartItem.merchantId = "30001"
        Product product = new Product()

        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = slug
        category.name = name
        categories.add(category)
        product.setCategory(categories)
        [product, cartItem]
    }

    def "test getSubscriptionFromAllCategory() for hero & non hero categories" () {
        setup:
        AMPConfig config = AMPConfig.builder()
                .withCategory("ipad")
                .withItemId("amp-news-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build();

        TreeSet<String> ampConfigs = getMockAmpConfigsForPS()

        when:
        Set<Subscription> result = cartService.getSubscriptionFromAllCategory(getCategoryList, config, new TreeSet<Subscription>())

        then:
        result.size() == expectedSize

        where:
        getCategoryList         || expectedSize
        getHeroCategory()       || 1
        getNonHeroCategory()    || 0

    }

    private TreeSet<String> getMockAmpConfigsForPS() {
        Set<String> ampConfigs = new TreeSet<>()
        ampConfigs.add("amp-news-plus")
        ampConfigs
    }

    def "test getSubscriptionFromAllCategory()-iphone category & iphone 12 product with parent category as iphone"() {

        setup:
        AMPConfig config = AMPConfig.builder()
                .withCategory("iphone")
                .withItemId("amp-news-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build();

        when:
        Set<Subscription> result = cartService.getSubscriptionFromAllCategory(getCategoryList, config, new TreeSet<>())

        then:
        result.size() == expectedSize

        where:
        getCategoryList                 || expectedSize
        getIphoneCategoryProduct()      || 1
        getIphone12CategoryProduct()    || 1

    }

    def "test getSubscriptionFromAllCategory()-macbook category & imac24 product with parent category as macbook"() {

        setup:
        AMPConfig config = AMPConfig.builder()
                .withCategory("macbook")
                .withItemId("amp-news-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build();



        when:
        Set<Subscription> result = cartService.getSubscriptionFromAllCategory(getCategoryList, config, new TreeSet<>())

        then:
        result.size() == expectedSize

        where:
        getCategoryList                 || expectedSize
        getMacBookCategoryProduct()     || 1
        getIMac24CategoryProduct()      || 1

    }

    def "test getSubscriptionFromAllCategory()-ipad category & ipad keyboards product with parent category as ipad"() {

        setup:
        AMPConfig config = AMPConfig.builder()
                .withCategory("ipad")
                .withItemId("amp-news-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build();

        when:
        def service = new CartService()
        Set<Subscription> result = service.getSubscriptionFromAllCategory(getCategoryList, config, new TreeSet<>())

        then:
        result.size() == expectedSize

        where:
        getCategoryList                     || expectedSize
        getIPadCategoryProduct()            || 1
        getIPadKeyboardsCategoryProduct()   || 1

    }

    def " test CanAddToCart fails "() {
        setup:
        def cart = new Cart()

        cart.cartItems = [new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(),
                          new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(),
                          new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(),
                          new CartItem(), new CartItem()]

        when:
        def service = new CartService()
        def result = service.canAddToCart(cart, new Program(), Mock(AddToCartResponse), Mock(Product))

        then:
        !result
    }

    def " test CanAddToCart "() {
        setup:
        def cart = new Cart()

        cart.cartItems = [new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(),
                          new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(),
                          new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(), new CartItem(),
                          new CartItem()]

        when:
        def service = new CartService()
        def result = service.canAddToCart(cart, new Program(), Mock(AddToCartResponse), Mock(Product))

        then:
        !result
    }

    def "test MinNotMet and MaxExceed values case"() {
        setup:
        def CartService cartService = new CartService()
        def cart = new Cart()
        def cartTotal = new CartTotal()

        def program = new Program()
        def paymentOptions = new ArrayList<PaymentOption>()
        def paymentOption = new PaymentOption()
        paymentOption.paymentMinLimit = paymentMinLimit
        paymentOption.paymentMaxLimit = paymentMaxLimit
        paymentOptions.add(paymentOption)
        paymentOption.setPaymentOption(CommonConstants.CAT_PAYROLLDEDUCTION_STR)
        program.setPayments(paymentOptions)
        cartTotal.price = price
        cartTotal.discountedPrice = discountedPrice
        cart.cartTotal = cartTotal
        cartService.configPaymentMinMaxLimitToCart(cart, program)


        expect:
        cart.paymentLimit.minNotMet == minNotMetValue
        cart.paymentLimit.maxExceed == maxExceedValue

        where:
        paymentMinLimit || paymentMaxLimit || discountedPrice                    || price                       | minNotMetValue             | maxExceedValue
        99              || 1000            || null                               || new Price(100.0d, "", 1000) | false                      | false
        111             || 1000            || null                               || new Price(100.0d, "", 1000) | true                       | false
        99              || 1000            || null                               || new Price(1200.0d, "", 1000)| false                      | true
        99              || 1000            || new Price(80.0d, "", 1000)         || new Price(100.0d, "", 1000) | true                       | false
        99              || 1000            || new Price(1200.0d, "", 1000)       || new Price(100.0d, "", 1000) | false                      | true

    }

    def "test getFontCode based on length"() {
        setup:
        def CartService cartService = new CartService()
        def Engrave engrave = new Engrave()

        engrave.fontCode = defaultFontCode
        engrave.line1 = line1

        engrave.engraveFontConfigurations = [
                new EngraveFontConfiguration(engraveConfigId:1555,charLengthFrom:1, charLengthTo:2, fontCode:"ESR230B"),
                new EngraveFontConfiguration(engraveConfigId:1555,charLengthFrom:3, charLengthTo:3, fontCode:"ESR210B"),
                new EngraveFontConfiguration(engraveConfigId:1555,charLengthFrom:4, charLengthTo:4, fontCode:"ESR180B"),
                new EngraveFontConfiguration(engraveConfigId:1555,charLengthFrom:5, charLengthTo:10,fontCode:"ESR140B"),
                new EngraveFontConfiguration(engraveConfigId:1555,charLengthFrom:11,charLengthTo:13,fontCode:"ESR110B"),
                new EngraveFontConfiguration(engraveConfigId:1555,charLengthFrom:14,charLengthTo:99,fontCode:"ESR060B")]

        expect:
        fontCode == cartService.getFontCode(engrave)

        where:
        defaultFontCode || line1 || fontCode
        "EHV055N" || "早上好" || "ESR210B"
        "EHV055N" || "bonne journée" || "ESR110B"
        "EHV055N" || "a" || "ESR230B"
        "EHV055N" || "ab" || "ESR230B"
        "EHV055N" || "abc" || "ESR210B"
        "EHV055N" || "abcd" || "ESR180B"
        "EHV055N" || "abcde" || "ESR140B"
        "EHV055N" || "abcdef" || "ESR140B"
        "EHV055N" || "abcdefg" || "ESR140B"
        "EHV055N" || "abcdefgh" || "ESR140B"
        "EHV055N" || "abcdefghi" || "ESR140B"
        "EHV055N" || "abcdefghij" || "ESR140B"
        "EHV055N" || "abcdefghijk" || "ESR110B"
        "EHV055N" || "abcdefghijkl" || "ESR110B"
        "EHV055N" || "abcdefghijklm" || "ESR110B"
        "EHV055N" || "abcdefghijklmn" || "ESR060B"
        "EHV055N" || "abcdefghijklmnopqrstuvwxyz" || "ESR060B"
    }

    @Unroll
    def "test getAddPoints"() {
        setup:
        def CartService cartService = new CartService()
        User user = new User()
        user.points = userPoints

        Cart cart = new Cart()
        CartTotal cartTotal = new CartTotal()
        cartTotal.setPrice(cartPrice)
        cart.setDisplayCartTotal(cartTotal)
        cart.convRate = 100.0

        when:
        def addedPoints = cartService.getAddPoints(cart, user, varProgram)

        then:
        addedPoints == addedPointsResult

        where:
        varProgram                         | userPoints | cartPrice                       || addedPointsResult
        getUA()                            | 30000      | new Price(500.00, "USD", 50000) || 20000
        getUA()                            | 50000      | new Price(500.00, "USD", 50000) || 0
        getPNC()                           | 80000      | new Price(500.00, "USD", 50000) || 0
        getPNC()                           | 40000      | new Price(500.00, "USD", 50000) || 10000
        getScotia()                        | 80000      | new Price(500.00, "USD", 50000) || 0
        getScotia()                        | 30000      | new Price(500.00, "USD", 50000) || 20000
        getDelta()                         | 30000      | new Price(500.00, "USD", 50000) || 20000
        getDelta()                         | 70000      | new Price(500.00, "USD", 50000) || 0
        getChase()                         | 30000      | new Price(500.00, "USD", 50000) || 20000
        getChase()                         | 90000      | new Price(500.00, "USD", 50000) || 0
        getVirginAUCashOnly()              | 50000      | new Price(500.00, "USD", 50000) || 50000
        getVirginAUPointsOnlyAndSplitPay() | 30000      | new Price(500.00, "USD", 50000) || 20000
        getVirginAUPointsOnlyAndSplitPay() | 65000      | new Price(500.00, "USD", 50000) || 0
        getWF()                            | 30000      | new Price(500.00, "USD", 50000) || 20000
        getWF()                            | 750000     | new Price(500.00, "USD", 50000) || 0
        VitalityCACashOnly()               | 50000      | new Price(500.00, "USD", 50000) || 50000
        VitalityUSCashOnly()               | 50000      | new Price(500.00, "USD", 50000) || 50000
        getRBCPointsOnlyAndSplitPay()      | 30000      | new Price(500.00, "USD", 50000) || 20000
        getRBCPointsOnlyAndSplitPay()      | 50500      | new Price(500.00, "USD", 50000) || 0
        getRBCCashOnly()                   | 50000      | new Price(500.00, "USD", 50000) || 50000
        getDemoCashOnly()                  | 50000      | new Price(500.00, "USD", 50000) || 50000
        getDemoPointsOnlyAndSplitPay()     | 40000      | new Price(500.00, "USD", 50000) || 10000
        getDemoPointsOnlyAndSplitPay()     | 50000      | new Price(500.00, "USD", 50000) || 0
        getDemoPointsFixed()               | 35000      | new Price(500.00, "USD", 50000) || 15000
        getDemoPointsFixed()               | 50000      | new Price(500.00, "USD", 50000) || 0
        getFDR()                           | 15000      | new Price(500.00, "USD", 50000) || 35000
        getFDR()                           | 60000      | new Price(500.00, "USD", 50000) || 0
        getFDRPointsOnly()                 | 20000      | new Price(500.00, "USD", 50000) || 30000
        getFDRPointsOnly()                 | 60000      | new Price(500.00, "USD", 50000) || 0
        getFDR_PSCU()                      | 40000      | new Price(500.00, "USD", 50000) || 10000
        getFDR_PSCU()                      | 50000      | new Price(500.00, "USD", 50000) || 0
    }

    def 'get cart - DiscountedGiftWithPurchase'() {
        given:
        final user = new User()
        final shoppingCart = buildMockShoppingCart()
        final couponCodeValidator = Mock(CouponCodeValidator)
        final appleShoppingCartDao = Mock(ShoppingCartDao)
        final appleShoppingCartItemDao = Mock(ShoppingCartItemDao)
        final pricingServiceV2 = Mock(LocalPricingServiceV2);
        final giftPromoService = Mock(GiftPromoService)
        final productMapper = Mock(ProductMapper)
        final productServiceV3 = Mock(ProductServiceV3)

        @Subject
        final cartServices = new CartService(couponCodeValidator: couponCodeValidator,
                appleShoppingCartDao: appleShoppingCartDao, appleShoppingCartItemDao: appleShoppingCartItemDao,
                giftPromoService: giftPromoService, pricingServiceV2: pricingServiceV2,
                productMapper: productMapper, productServiceV3: productServiceV3)

        and:
        appleShoppingCartDao.get(_, _, _) >> shoppingCart
        appleShoppingCartItemDao.getShoppingCartItems(shoppingCart) >> buildMockShoppingCartItems()
        productServiceV3.getAppleProductDetail(_, _, _, _, _, _, _, _) >> mockProduct()
        final List<String> listOfGiftsPsids = new ArrayList<>()
        listOfGiftsPsids.add("30001ABCDEDF/F")
        giftPromoService.getGiftPsids(_) >> listOfGiftsPsids
        List<GiftItem> giftItemList = new ArrayList<>()
        GiftItem giftItem = new GiftItem()
        giftItem.setProductId("30001ABCDEDF/F")
        giftItem.setDiscount(8000)
        giftItem.setDiscountType("points")
        giftItemList.add(giftItem)
        giftPromoService.getGiftItemList(_, _, _) >> giftItemList
        final Program program = new Program(varId: 'UA', programId: 'MP', config: [enableSmartPricing: true])
        productMapper.getSmartPrice(_, _) >> getSmartPrice()

        when:
        Cart result = cartServices.getCart(user, null, program)
        CartItem giftItemResult = result.getCartItems().get(0).getSelectedAddOns().getGiftItem()

        then:
        result != null
        result.getCartItems().size() == 1
        giftItemResult != null
        giftItemResult.getDiscount() == 8000
        giftItemResult.getDiscountType() == "points"

    }

    def getSmartPrice() {
        def smartPrice = new SmartPrice()
        smartPrice.setAmount(179.84)
        smartPrice.setCurrencyCode("USD")
        smartPrice.setPoints(24800)
        return smartPrice
    }

    private Product mockProduct() {
        Product product = new Product()
        List<Offer> offerList = new ArrayList<>()
        Offer offer = new Offer()
        Merchant merchant = new Merchant()
        merchant.setId(1)
        merchant.setMerchantId("30001")
        merchant.setSupplierId("200")
        merchant.setName("Apple")
        offer.setMerchant(merchant)
        offerList.add(offer)
        product.setOffers(offerList)
        Supplier supplier = new Supplier()
        supplier.setSupplierId(123)
        product.setSupplier(supplier)
        product.setAvailable(true)
        return  product
    }

    private static ShoppingCart buildMockShoppingCart() {
        ShoppingCart cart = new ShoppingCart()
        cart.setId(1234L)
        cart.setProgramId("b2s")
        cart.setUserId("sethu")
        cart.setVarId("fdr")
        return cart
    }

    private static List<ShoppingCartItem> buildMockShoppingCartItems() {
        final String optionsXml = "<xml><options>" +
                "<name>engrave</name><value>{\"line1\":\"Product Line 1\",\"line2\":\"\",\"font\":\"Helvetica Neue\"," +
                "\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\",\"widthDimension\":\"45mm\"}</value>" +
                "<name>giftItem</name><value>{\"productId\": \"30001ABCDEDF/F\", \"engrave\":{\"line1\":\"Gift Line 1\"," +
                "\"line2\":\"\",\"font\":\"Helvetica Neue\",\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\"," +
                "\"widthDimension\":\"45mm\"}}</value>" +
                "</options></xml>"

        List<ShoppingCartItem> shoppingCartItems = new ArrayList<>()
        ShoppingCartItem cartItem = new ShoppingCartItem()
        cartItem.setId(1L)
        cartItem.setOptionsXml(optionsXml)
        cartItem.setProductName("Ipod -Airpod")
        cartItem.setSupplierId(CommonConstants.APPLE_SUPPLIER_ID)
        cartItem.setQuantity(1)
        shoppingCartItems.add(cartItem)
        return shoppingCartItems
    }

    def getUA()
    {
        Program program = new Program()
        program.setVarId("UA")
        program.setProgramId("MP")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program

    }
    def getPNC()
    {
        Program program = new Program()
        program.setVarId("PNC")
        program.setProgramId("b2s_qa_only")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions1)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getFDR()
    {
        Program program = new Program()
        program.setVarId("FDR")
        program.setProgramId("Demo")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getFDRPointsOnly()
    {
        Program program = new Program()
        program.setVarId("FDR")
        program.setProgramId("PLPEBY01")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions1)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getFDR_PSCU()
    {
        Program program = new Program()
        program.setVarId("FDR_PSCU")
        program.setProgramId("b2s_qa_only")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getScotia()
    {
        Program program = new Program()
        program.setVarId("SCOTIA")
        program.setProgramId("b2s_qa_only")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSFIXED.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program

    }

    def getChase()
    {
        Program program = new Program()
        program.setVarId("Chase")
        program.setProgramId("b2s_qa_only")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getDelta()
    {
        Program program = new Program()
        program.setVarId("Chase")
        program.setProgramId("apple_qa")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getVirginAUPointsOnlyAndSplitPay()
    {
        Program program = new Program()
        program.setVarId("VirginAU")
        program.setProgramId("b2s_qa_only")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getVirginAUCashOnly()
    {
        Program program = new Program()
        program.setVarId("VirginAU")
        program.setProgramId("VIP")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.CASHONLY.getPaymentOption(), varProgramRedemptionOptions1)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def VitalityCACashOnly()
    {
        Program program = new Program()
        program.setVarId("VitalityCA")
        program.setProgramId("Manulife")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.CASHONLY.getPaymentOption(), varProgramRedemptionOptions1)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def VitalityUSCashOnly()
    {
        Program program = new Program()
        program.setVarId("VitalityUS")
        program.setProgramId("b2s_qa_only")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.CASHONLY.getPaymentOption(), varProgramRedemptionOptions1)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getWF()
    {
        Program program = new Program()
        program.setVarId("WF")
        program.setProgramId("A1")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getRBCPointsOnlyAndSplitPay()
    {
        Program program = new Program()
        program.setVarId("RBC")
        program.setProgramId("b2s_qa_only")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getRBCCashOnly()
    {
        Program program = new Program()
        program.setVarId("RBC")
        program.setProgramId("PBA")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.CASHONLY.getPaymentOption(), varProgramRedemptionOptions1)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getDemoCashOnly()
    {
        Program program = new Program()
        program.setVarId("Demo")
        program.setProgramId("wellness")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.CASHONLY.getPaymentOption(), varProgramRedemptionOptions1)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getDemoPointsOnlyAndSplitPay()
    {
        Program program = new Program()
        program.setVarId("Demo")
        program.setProgramId("loyalty-ccslider")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getDemoPointsFixed()
    {
        Program program = new Program()
        program.setVarId("Demo")
        program.setProgramId("loyalty-ccfixed")
        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSFIXED.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)
        return program
    }

    def getProgram() {
        Program program = new Program()
        program.setVarId("UA")
        program.setProgramId("MP")
        Set<AMPConfig> ampConfigs = new HashSet<>()

        AMPConfig config = AMPConfig.builder()
                .withCategory("ipad")
                .withItemId("amp-news-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build();
        ampConfigs.add(config)

        AMPConfig config2 = AMPConfig.builder()
                .withCategory("ipad")
                .withItemId("amp-music-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(7)
                .build();
        ampConfigs.add(config2)


        AMPConfig config3 = AMPConfig.builder()
                .withCategory("iphone")
                .withItemId("amp-music-plus")
                .withUseStaticLink(true)
                .withUpdateDate(new java.util.Date())
                .withUpdatedBy("user")
                .withDuration(30)
                .build();
        ampConfigs.add(config3)
        program.setAmpSubscriptionConfig(ampConfigs)
        return program
    }

    def getHeroCategory() {
        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "ipad"
        category.name = "iPad"
        categories.add(category)
        return categories
    }

    def getNonHeroCategory() {
        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "accessories"
        category.name = "Air Tag"
        categories.add(category)
        return categories
    }

    def getIphoneCategoryProduct() {
        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "iphone"
        category.name = "iPhone"
        categories.add(category)
        return categories
    }

    def getIphone12CategoryProduct() {
        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "iphone-iphone-12"
        category.name = "iPhone 12"
        List<Category> parents = new ArrayList<>()
        Category parentCategory = new Category()
        category.depth = 1
        category.slug = "iphone"
        category.name = "iPhone"
        parents.add(parentCategory)
        category.setParents(parents)
        categories.add(category)
        return categories
    }

    def getMacBookCategoryProduct() {
        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "macbook"
        category.name = "Mac Book"
        categories.add(category)
        return categories
    }

    def getIMac24CategoryProduct() {
        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "imac-24"
        category.name = "iMac 24"
        List<Category> parents = new ArrayList<>()
        Category parentCategory = new Category()
        category.depth = 1
        category.slug = "macbook"
        category.name = "Mac Book"
        parents.add(parentCategory)
        category.setParents(parents)
        categories.add(category)
        return categories
    }

    def getIPadCategoryProduct() {
        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "ipad"
        category.name = "iPad"
        categories.add(category)
        return categories
    }

    def getIPadKeyboardsCategoryProduct() {
        List<Category> categories = new ArrayList<>()
        Category category = new Category()
        category.depth = 1
        category.slug = "ipad-keyboards"
        category.name = "iPad Keyboard"
        List<Category> parents = new ArrayList<>()
        Category parentCategory = new Category()
        category.depth = 1
        category.slug = "ipad"
        category.name = "iPad"
        parents.add(parentCategory)
        category.setParents(parents)
        categories.add(category)
        return categories
    }

}


