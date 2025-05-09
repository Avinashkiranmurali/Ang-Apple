package com.b2s.common.services.pricing

import com.b2s.apple.mapper.PricingServiceRequestMapperV2
import com.b2s.apple.mapper.PricingServiceResponseMapperV2
import com.b2s.apple.model.CartPricingResponseDTO
import com.b2s.apple.services.CartPricingRestApi
import com.b2s.common.services.pricing.impl.LocalPricingServiceV2
import com.b2s.common.util.BigMoneyDeserializer
import com.b2s.common.util.CurrencyUnitDeserializer
import com.b2s.common.util.CurrencyUnitKeyDeserializer
import com.b2s.common.util.MoneyDeserializer
import com.b2s.rewards.apple.integration.model.PaymentOptions
import com.b2s.rewards.apple.model.*
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import com.b2s.web.B2RReloadableResourceBundleMessageSource
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.guava.GuavaModule
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.google.gson.JsonParseException
import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import org.springframework.mock.web.MockHttpSession
import org.springframework.util.ResourceUtils
import spock.lang.Specification
import spock.lang.Subject

class LocalPricingServiceV2Spec extends Specification {


    PricingServiceRequestMapperV2 pricingServiceRequestMapper = Mock()
    PricingServiceResponseMapperV2 pricingServiceResponseMapper = Mock()
    CartPricingRestApi pricingClient = Mock()
    ObjectMapper objectMapper = setObjectMapper()
    B2RReloadableResourceBundleMessageSource messageSource = Mock()
    final httpSession = new MockHttpSession()

    @Subject
    private LocalPricingServiceV2 localPricingServiceV2 = new LocalPricingServiceV2(
            pricingServiceRequestMapper: pricingServiceRequestMapper,
            pricingServiceResponseMapper: pricingServiceResponseMapper,
            pricingClient: pricingClient, messageSource: messageSource, httpSession: httpSession)

    def 'calculateCartPrice - pointsonly, splitpay '() {
        given:
        Cart cart = getCart()
        User user = getUser()
        user.points = points
        Program program = getProgram()
        def addPoints = 1

        when:
        pricingServiceRequestMapper.from(_, _, _, _, _) >> null

        CartPricingResponseDTO cartPricingResponseDTO = null;
        File pricingJSONFile = null;
        try {
            //Sample Pricing Response
            pricingJSONFile = ResourceUtils.getFile("classpath:json/"+file);
            //JSON to Object Mapper using Jackson
            cartPricingResponseDTO = objectMapper.readValue(pricingJSONFile, CartPricingResponseDTO.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pricingClient.getPrice(_) >> cartPricingResponseDTO
        messageSource.getMessage(_, _, _, _) >> CommonConstants.INVALID_ADDRESS_ERROR_DEFAULT_MSG
        1 * pricingServiceResponseMapper.populateCartPrices(_, _, _, _)

        then:
        localPricingServiceV2.calculateCartPrice(cart, user, program, addPoints)

        expect:
        cart.redemptionPaymentLimit != null
        cart.redemptionPaymentLimit.pointsMaxLimit.points == pointsMaxLimit
        cart.redemptionPaymentLimit.pointsMinLimit.points == pointsMinLimit
        cart.redemptionPaymentLimit.cashMaxLimit.amount   == cashMaxLimit
        cart.redemptionPaymentLimit.cashMinLimit.amount   == cashMinLimit
        cart.redemptionPaymentLimit.useMaxPoints.points   == useMaxPointsP
        cart.redemptionPaymentLimit.useMaxPoints.amount   == useMaxPointsA
        cart.redemptionPaymentLimit.useMinPoints.points   == useMinPointsP
        cart.redemptionPaymentLimit.useMinPoints.amount   == useMinPointsA
        def cartMaxLimitLocal = 0
        if (cart.redemptionPaymentLimit.cartMaxLimit != null) {
            cartMaxLimitLocal = cart.redemptionPaymentLimit.cartMaxLimit.points
        }
        cartMaxLimitLocal == cartMaxLimit
        cart.cost == cost
        cart.ccPayment == cart.cost
        cart.pointsPayment == cart.cartTotal.price.points - cart.addPoints

        where:
        points/*balance scenarios*/               |pointsMaxLimit|pointsMinLimit|cashMaxLimit|cashMinLimit|useMaxPointsP|useMaxPointsA|useMinPointsP|useMinPointsA|cartMaxLimit|cost   |file
        300000/*high balance*/                    | 31800        | 15900        | 114.84     | 0.0        | 31800       | 0.0         | 15900       | 114.83775   |  0         | 0.0   |"pricing_response_US.json"
        40000/*high balance, with custom pts 20k*/| 31800        | 15900        | 114.84     | 0.0        | 31800       | 0.0         | 15900       | 114.83775   |  0         |85.23  |"pricing_response_US_custom_points.json"
        31800/*balance equals Cart max points*/   | 31800        | 15900        | 114.84     | 0.0        | 31800       | 0.0         | 15900       | 114.83775   |  0         |85.23  |"pricing_response_US_custom_points.json"
        20000/*low balance,above pts restriction*/| 20000        | 15900        | 114.84     | 0.0        | 20000       | 85.2255     | 15900       | 114.83775   | 40000      |85.23  |"pricing_response_US_custom_points.json"
        10000/*low balance,below pts restriction*/| 15900        | 15900        | 114.84     | 0.0        | 15900       | 114.83775   | 15900       | 114.83775   | 20000      |157.45 |"pricing_response_US_below_restriction_points.json"
    }

    def 'calculateCartPrice - pointsonly, splitpay with dollar'() {
        given:
        Cart cart = getCart()
        Price price = new Price(419.44, "USD", 41944);
        cart.cartTotal.price = price
        cart.convRate = 100.0

        User user = getUser()
        user.varId = "WF"
        user.programId = "A1"
        user.points = points
        Program program = getProgram2()
        def addPoints = 1

        when:
        pricingServiceRequestMapper.from(_, _, _, _, _) >> null

        CartPricingResponseDTO cartPricingResponseDTO = null;
        File pricingJSONFile = null;
        try {
            //Sample Pricing Response
            pricingJSONFile = ResourceUtils.getFile("classpath:json/"+file);
            //JSON to Object Mapper using Jackson
            cartPricingResponseDTO = objectMapper.readValue(pricingJSONFile, CartPricingResponseDTO.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pricingClient.getPrice(_) >> cartPricingResponseDTO
        messageSource.getMessage(_, _, _, _) >> CommonConstants.INVALID_ADDRESS_ERROR_DEFAULT_MSG
        1 * pricingServiceResponseMapper.populateCartPrices(_, _, _, _)

        then:
        localPricingServiceV2.calculateCartPrice(cart, user, program, addPoints)

        expect:
        cart.redemptionPaymentLimit != null
        cart.redemptionPaymentLimit.pointsMaxLimit.points == pointsMaxLimit
        cart.redemptionPaymentLimit.pointsMinLimit.points == pointsMinLimit
        cart.redemptionPaymentLimit.cashMaxLimit.amount   == cashMaxLimit
        cart.redemptionPaymentLimit.cashMinLimit.amount   == cashMinLimit
        cart.redemptionPaymentLimit.useMaxPoints.points   == useMaxPointsP
        cart.redemptionPaymentLimit.useMaxPoints.amount   == useMaxPointsA
        cart.redemptionPaymentLimit.useMinPoints.points   == useMinPointsP
        cart.redemptionPaymentLimit.useMinPoints.amount   == useMinPointsA
        def cartMaxLimitLocal = 0
        if (cart.redemptionPaymentLimit.cartMaxLimit != null) {
            cartMaxLimitLocal = cart.redemptionPaymentLimit.cartMaxLimit.points
        }
        cartMaxLimitLocal == cartMaxLimit
        cart.cost == cost

        where:
        points/*balance scenarios*/               |pointsMaxLimit|pointsMinLimit|cashMaxLimit|cashMinLimit|useMaxPointsP|useMaxPointsA|useMinPointsP|useMinPointsA|cartMaxLimit|cost   |file
        50000/*high balance*/                     | 41944        | 11944        | 300.0      | 0.0        | 41944       | 0.0         | 11944       | 300.0       |  0         | 0.0   |"pricing_response_pointsonly_dollar.json"
        50000/*high balance, with custom pts 12k*/| 41944        | 11944        | 300.0      | 0.0        | 41944       | 0.0         | 11944       | 300.0       |  0         |299.44 |"pricing_response_pointsonly_custom.json"
        41944/*balance equals Cart max points*/   | 41944        | 11944        | 300.0      | 0.0        | 41944       | 0.0         | 11944       | 300.0       |  0         |299.44 |"pricing_response_pointsonly_custom.json"
        20000/*low balance,above pts restriction*/| 20000        | 11944        | 300.0      | 0.0        | 20000       | 219.44      | 11944       | 300.0       |  100000    |0.0    |"pricing_response_pointsonly_above_restriction.json"
        8000/*low balance,below pts restriction*/ | 11944        | 11944        | 300.0      | 0.0        | 11944       | 300.0       | 11944       | 300.0       |  40000     |0.0    |"pricing_response_pointsonly_below_restriction.json"
    }

    def 'calculateCartPrice - pointsfixed, splitpay'() {
        given:
        Cart cart = getCart()
        Price price = new Price(256.23, "CAD", 31247);
        cart.cartTotal.price = price
        cart.convRate = 121.9512195121951

        User user = getUser()
        user.varId = "SCOTIA"
        user.programId = "Amex"
        user.locale = Locale.CANADA
        user.points = points
        Program program = getProgram3()
        def addPoints = 1

        when:
        pricingServiceRequestMapper.from(_, _, _, _, _) >> null

        CartPricingResponseDTO cartPricingResponseDTO = null;
        File pricingJSONFile = null;
        try {
            //Sample Pricing Response
            pricingJSONFile = ResourceUtils.getFile("classpath:json/"+file);
            //JSON to Object Mapper using Jackson
            cartPricingResponseDTO = objectMapper.readValue(pricingJSONFile, CartPricingResponseDTO.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pricingClient.getPrice(_) >> cartPricingResponseDTO
        messageSource.getMessage(_, _, _, _) >> CommonConstants.INVALID_ADDRESS_ERROR_DEFAULT_MSG
        1 * pricingServiceResponseMapper.populateCartPrices(_, _, _, _)

        then:
        localPricingServiceV2.calculateCartPrice(cart, user, program, addPoints)

        expect:
        cart.redemptionPaymentLimit != null
        cart.redemptionPaymentLimit.pointsMaxLimit.points == pointsMaxLimit
        cart.redemptionPaymentLimit.pointsMinLimit.points == pointsMinLimit
        cart.redemptionPaymentLimit.cashMaxLimit.amount   == cashMaxLimit
        cart.redemptionPaymentLimit.cashMinLimit.amount   == cashMinLimit
        cart.redemptionPaymentLimit.useMaxPoints.points   == useMaxPointsP
        cart.redemptionPaymentLimit.useMaxPoints.amount   == useMaxPointsA
        cart.redemptionPaymentLimit.useMinPoints.points   == useMinPointsP
        cart.redemptionPaymentLimit.useMinPoints.amount   == useMinPointsA
        def cartMaxLimitLocal = 0
        if (cart.redemptionPaymentLimit.cartMaxLimit != null) {
            cartMaxLimitLocal = cart.redemptionPaymentLimit.cartMaxLimit.points
        }
        cartMaxLimitLocal == cartMaxLimit
        cart.cost == cost

        where://for SCOTIA high balance split pay is not applicable
        points/*balance scenarios*/               |pointsMaxLimit|pointsMinLimit|cashMaxLimit |cashMinLimit|useMaxPointsP|useMaxPointsA     |useMinPointsP|useMinPointsA      |cartMaxLimit|cost        |file
        40000/*high balance*/                     | 31247        | 31247        | 212.86      | 0.0        | 31247       | 0.0              | 31247       | 0.0               |  0         | 0.0        |"pricing_response_SCOTIA.json"
        20000/*low balance,above pts restriction*/| 20000        | 20000        | 212.86      | 0.0        | 20000       |95.77253076923077 | 20000       | 95.77253076923077 | 100000     |95.77       |"pricing_response_pointsfixed_above_restriction.json"
        6000/*low balance,below pts restriction*/ | 6250         | 6250         | 212.86      | 0.0        | 6250        |212.85906923076922| 6250        | 212.85906923076922| 30000      |214.98      |"pricing_response_pointsfixed_below_restriction.json"
    }

    def 'calculateCartPrice - pointsonly'() {
        given:
        Cart cart = getPointsOnlyCart()
        User user = getPointsOnlyUser()
        Program program = getPointsOnlyProgram()

        when:
        pricingServiceRequestMapper.from(_, _, _, _, _) >> null

        CartPricingResponseDTO cartPricingResponseDTO = null;
        try {
            //JSON to Object Mapper using Jackson
            cartPricingResponseDTO = objectMapper.readValue(ResourceUtils.getFile("classpath:json/pricing_response_pointsOnly.json"), CartPricingResponseDTO.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pricingClient.getPrice(_) >> cartPricingResponseDTO
        messageSource.getMessage(_, _, _, _) >> CommonConstants.INVALID_ADDRESS_ERROR_DEFAULT_MSG
        1 * pricingServiceResponseMapper.populateCartPrices(_, _, _, _)

        then:
        localPricingServiceV2.calculateCartPrice(cart, user, program, null)

        expect:
        cart.pointsBalance == user.points -  cart.pointsPayment

    }

    def getPointsOnlyCart() {
        Cart cart = new Cart()
        List li = new ArrayList<CartItem>()
        li.add(new CartItem())
        cart.setCartItems(li)

        CartTotal cartTotal = new CartTotal()
        Price price = new Price(266.43, "USD", 26643);
        cartTotal.setPrice(price)
        cart.setCartTotal(cartTotal)
        cart.setConvRate(100.0)

        return cart;
    }


    def getCart() {

        Cart cart = new Cart()
        List li = new ArrayList<CartItem>()
        li.add(new CartItem())
        cart.setCartItems(li)

        CartTotal cartTotal = new CartTotal()
        Price price = new Price(170.13, "USD", 31800);
        cartTotal.setPrice(price)
        cart.setCartTotal(cartTotal)
        cart.setConvRate(185.1851851851852)

        return cart;
    }

    def getPointsOnlyUser() {
        User user = new User()
        user.varId = "Demo"
        user.programId = "loyalty-pointsonly"
        user.locale = Locale.US
        user.points = 850000
        return user
    }

    def getUser() {
        User user = new User()
        user.varId = "UA"
        user.programId = "MP"
        user.locale = Locale.US
        return user
    }

    def getPointsOnlyProgram() {
        Program program = new Program()
        program.setVarId("Demo")
        program.setProgramId("loyalty-pointsonly")
        program.setConvRate(1.0)
        Map<String, Object> configs = new HashMap<>()
        configs.put("catalog_id", "apple-us-en")
        program.setConfig(configs)
        List<PaymentOption> payments = new ArrayList<>()
        PaymentOption option = new PaymentOption()
        option.setPaymentOption("POINTS")
        option.setSupplementaryPaymentType("DEFAULT")
        payments.add(option)
        program.setPayments(payments)

        VarProgramRedemptionOption varProgramRedemptionOption = VarProgramRedemptionOption.builder()
                .withId(2)
                .withVarId("Demo")
                .withProgramId("loyalty-pointsonly")
                .withPaymentOption(PaymentOptions.POINTSONLY.getPaymentOption())
                .withPaymentMinLimit(0)
                .withPaymentMaxLimit(0)
                .withOrderBy(1)
                .withLimitType(CommonConstants.PERCENTAGE)
                .withPaymentProvider(null)
                .withActive(true)
                .build()

        List<VarProgramRedemptionOption> varProgramRedemptionOptions = new ArrayList<>()
        varProgramRedemptionOptions.add(varProgramRedemptionOption)
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions)
        program.setRedemptionOptions(redemptionOptions)

        return program
    }

    def getProgram() {
        Program program = new Program()
        program.setVarId("UA")
        program.setProgramId("MP")
        program.setConvRate(1.538462)
        Map<String, Object> configs = new HashMap<>()
        configs.put("catalog_id", "apple-us-en")
        program.setConfig(configs)
        List<PaymentOption> payments = new ArrayList<>()
        PaymentOption option = new PaymentOption()
        option.setPaymentOption("POINTS")
        option.setSupplementaryPaymentType("VARIABLE")
        option.setSupplementaryPaymentMaxLimit(50)
        payments.add(option)
        program.setPayments(payments)

        VarProgramRedemptionOption varProgramRedemptionOption1 = VarProgramRedemptionOption.builder()
                .withId(1)
                .withVarId(user.getVarId())
                .withProgramId(user.getProgramId())
                .withPaymentOption(PaymentOptions.SPLITPAY.getPaymentOption())
                .withPaymentMinLimit(0)
                .withPaymentMaxLimit(50)
                .withOrderBy(2)
                .withLimitType(CommonConstants.PERCENTAGE)
                .withPaymentProvider(null)
                .withActive(true)
                .build()
        VarProgramRedemptionOption varProgramRedemptionOption2 = VarProgramRedemptionOption.builder()
                .withId(2)
                .withVarId(user.getVarId())
                .withProgramId(user.getProgramId())
                .withPaymentOption(PaymentOptions.POINTSONLY.getPaymentOption())
                .withPaymentMinLimit(50)
                .withPaymentMaxLimit(0)
                .withOrderBy(1)
                .withLimitType(CommonConstants.PERCENTAGE)
                .withPaymentProvider(null)
                .withActive(true)
                .build()

        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        varProgramRedemptionOptions1.add(varProgramRedemptionOption1)
        varProgramRedemptionOptions2.add(varProgramRedemptionOption2)
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)

        return program
    }



    def getProgram2() {
        Program program = new Program()
        program.setVarId("WF")
        program.setProgramId("A1")
        program.setConvRate(0.0077)
        Map<String, Object> configs = new HashMap<>()
        configs.put("catalog_id", "apple-us-en")
        program.setConfig(configs)
        List<PaymentOption> payments = new ArrayList<>()
        PaymentOption option = new PaymentOption()
        option.setPaymentOption("POINTS")
        option.setSupplementaryPaymentType("VARIABLE")
        option.setSupplementaryPaymentMaxLimit(80)
        payments.add(option)
        program.setPayments(payments)

        VarProgramRedemptionOption varProgramRedemptionOption1 = VarProgramRedemptionOption.builder()
                .withId(1)
                .withVarId(user.getVarId())
                .withProgramId(user.getProgramId())
                .withPaymentOption(PaymentOptions.SPLITPAY.getPaymentOption())
                .withPaymentMinLimit(0)
                .withPaymentMaxLimit(300)
                .withOrderBy(2)
                .withLimitType(CommonConstants.DOLLAR)
                .withPaymentProvider(null)
                .withActive(true)
                .build()

        VarProgramRedemptionOption varProgramRedemptionOption2 = VarProgramRedemptionOption.builder()
                .withId(2)
                .withVarId(user.getVarId())
                .withProgramId(user.getProgramId())
                .withPaymentOption(PaymentOptions.SPLITPAY.getPaymentOption())
                .withPaymentMinLimit(0)
                .withPaymentMaxLimit(80)
                .withOrderBy(2)
                .withLimitType(CommonConstants.PERCENTAGE)
                .withPaymentProvider(null)
                .withActive(true)
                .build()
        VarProgramRedemptionOption varProgramRedemptionOption3 = VarProgramRedemptionOption.builder()
                .withId(3)
                .withVarId(user.getVarId())
                .withProgramId(user.getProgramId())
                .withPaymentOption(PaymentOptions.POINTSONLY.getPaymentOption())
                .withPaymentMinLimit(20)
                .withPaymentMaxLimit(0)
                .withOrderBy(1)
                .withLimitType(CommonConstants.PERCENTAGE)
                .withPaymentProvider(null)
                .withActive(true)
                .build()

        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        varProgramRedemptionOptions1.add(varProgramRedemptionOption1)
        varProgramRedemptionOptions1.add(varProgramRedemptionOption2)
        varProgramRedemptionOptions2.add(varProgramRedemptionOption3)
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)

        return program
    }

    def getProgram3() {
        Program program = new Program()
        program.setVarId("SCOTIA")
        program.setProgramId("Amex")
        program.setConvRate(1.21951219)
        Map<String, Object> configs = new HashMap<>()
        configs.put("catalog_id", "apple-ca-en")
        program.setConfig(configs)
        List<PaymentOption> payments = new ArrayList<>()
        PaymentOption option = new PaymentOption()
        option.setPaymentOption("POINTS")
        option.setSupplementaryPaymentType("FIXED")
        option.setSupplementaryPaymentMaxLimit(80)
        payments.add(option)
        program.setPayments(payments)

        VarProgramRedemptionOption varProgramRedemptionOption1 = VarProgramRedemptionOption.builder()
                .withId(1)
                .withVarId(user.getVarId())
                .withProgramId(user.getProgramId())
                .withPaymentOption(PaymentOptions.SPLITPAY.getPaymentOption())
                .withPaymentMinLimit(0)
                .withPaymentMaxLimit(80)
                .withOrderBy(2)
                .withLimitType(CommonConstants.PERCENTAGE)
                .withPaymentProvider(null)
                .withActive(true)
                .build()
        VarProgramRedemptionOption varProgramRedemptionOption2 = VarProgramRedemptionOption.builder()
                .withId(2)
                .withVarId(user.getVarId())
                .withProgramId(user.getProgramId())
                .withPaymentOption(PaymentOptions.POINTSFIXED.getPaymentOption())
                .withPaymentMinLimit(20)
                .withPaymentMaxLimit(0)
                .withOrderBy(1)
                .withLimitType(CommonConstants.PERCENTAGE)
                .withPaymentProvider(null)
                .withActive(true)
                .build()

        List<VarProgramRedemptionOption> varProgramRedemptionOptions1 = new ArrayList<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptions2 = new ArrayList<>()
        varProgramRedemptionOptions1.add(varProgramRedemptionOption1)
        varProgramRedemptionOptions2.add(varProgramRedemptionOption2)
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        redemptionOptions.put(PaymentOptions.SPLITPAY.getPaymentOption(), varProgramRedemptionOptions1)
        redemptionOptions.put(PaymentOptions.POINTSONLY.getPaymentOption(), varProgramRedemptionOptions2)
        program.setRedemptionOptions(redemptionOptions)

        return program
    }

    def setObjectMapper() {

        SimpleModule module = new SimpleModule()
        module.addDeserializer(Money.class, new MoneyDeserializer())
        module.addDeserializer(CurrencyUnit.class, new CurrencyUnitDeserializer())
        module.addDeserializer(BigMoney.class, new BigMoneyDeserializer())
        module.addKeyDeserializer(CurrencyUnit.class, new CurrencyUnitKeyDeserializer())

        ObjectMapper objMapper = new ObjectMapper().registerModules(new GuavaModule(), new JodaModule(), module)

        return objMapper
    }

}
