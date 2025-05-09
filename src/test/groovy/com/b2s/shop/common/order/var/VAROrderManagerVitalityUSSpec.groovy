package com.b2s.shop.common.order.var

import com.b2s.apple.entity.DemoUserEntity
import com.b2s.apple.services.ProgramService
import com.b2s.db.model.Order
import com.b2s.rewards.apple.dao.DemoUserDao
import com.b2s.rewards.apple.dao.PricingModelConfigurationDao
import com.b2s.rewards.apple.integration.model.AccountInfo
import com.b2s.rewards.apple.integration.model.UserInformation
import com.b2s.rewards.apple.model.Category
import com.b2s.rewards.apple.model.Offer
import com.b2s.rewards.apple.model.Option
import com.b2s.rewards.apple.model.Price
import com.b2s.rewards.apple.model.PricingModel
import com.b2s.rewards.apple.model.PricingModelConfiguration
import com.b2s.rewards.apple.model.Product
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.util.VarProgramConfigHelper
import com.b2s.shop.common.User
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

class VAROrderManagerVitalityUSSpec extends Specification {

    def varIntegrationServiceRemoteImpl = Mock(VarIntegrationServiceRemoteImpl)
    def programService = Mock(ProgramService)
    def demoUserDao = Mock(DemoUserDao)
    def pricingModelConfigurationDao = Mock(PricingModelConfigurationDao)
    def applicationProperties = Mock(Properties)

    @Subject
    def varOrderManagerVITALITY =
            new VAROrderManagerVITALITYUS(varIntegrationServiceRemoteImpl: varIntegrationServiceRemoteImpl,
                    programService: programService, pricingModelConfigurationDao: pricingModelConfigurationDao,
                    demoUserDao: demoUserDao, applicationProperties: applicationProperties)

    def "selectUser "() {

        setup:
        def request = Mock(HttpServletRequest)
        String CODE = "code";
        request.getParameter(CODE) >> "code"
        request.requestURL >> new StringBuffer("https://vitality-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")
        HttpSession httpSession = new MockHttpSession()
        request.getSession() >> httpSession
        request.getScheme() >> "https"
        request.getServerPort() >> 0
        request.getServerName() >> "appleDev"
        request.getHeaderNames() >> new Enumeration<String>() {
            @Override
            boolean hasMoreElements() {
                return false
            }

            @Override
            String nextElement() {
                return ""
            }
        }
        UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUri() >> new URI("https://vitality-vip-internal.cpdev.bridge2solutions.net/b2r/landingHome.do")


        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setPricingTier("VITALITY US Pricing")

        Map<String, String> additionalInfo = new HashMap<String, String>()
        additionalInfo.put("memberId", "Member")
        additionalInfo.put("programId", "programId")
        additionalInfo.put("activationFee", "USD 100");
        additionalInfo.put("financedAmount", "USD 50");
        additionalInfo.put("maxMonthlyPayment", "USD 111");
        additionalInfo.put("programLength", "4");
        additionalInfo.put("delta", "22")

        UserInformation userInformation = new UserInformation()
        userInformation.setAdditionalInfo(additionalInfo)
        accountInfo.setUserInformation(userInformation)
        varIntegrationServiceRemoteImpl.getAccountInfo(_, _, _) >> accountInfo
        Program program = getProgram()
        Map<String, Object> config = new HashMap<String, Object>()
        config.put(CommonConstants.VIMS_PRICING_API, true)
        program.setConfig(config)
        programService.getProgram(_, _, _) >> program
        applicationProperties.getProperty("session.timeout.minutes") >> "200"

        when:
        User user = varOrderManagerVITALITY.selectUser(request)
        then:
        user != null
    }

    def "selectUser - Account info Empty"() {

        setup:
        def httpServletRequest = Mock(HttpServletRequest)
        String CODE = "code";
        httpServletRequest.getParameter(CODE) >> "code"
        Map<String, String> externalsUrls = new HashMap<String, String>()
        externalsUrls.put("URL1", "http://bakkt.com")
        httpServletRequest.getServerPort() >> 0
        httpServletRequest.getScheme() >> "http"
        httpServletRequest.getServerName() >> "appleDev"

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setPricingTier("VITALITY US Pricing")

        varIntegrationServiceRemoteImpl.getAccountInfo(_, _, _) >> accountInfo
        programService.getProgram(_, _, _) >> getProgram()

        when:
        User user = varOrderManagerVITALITY.selectUser(httpServletRequest)
        then:
        user != null
    }

    def "selectUser - DemoUser Empty"() {

        setup:
        def request = Mock(HttpServletRequest)
        String CODE = "code";
        request.getParameter(CODE) >> "code"

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setPricingTier("VITALITY US Pricing")

        Map<String, String> additionalInfo = new HashMap<String, String>()
        additionalInfo.put("memberId", "Member")
        additionalInfo.put("programId", "programId")
        additionalInfo.put("activationFee", "100");
        additionalInfo.put("financedAmount", "50");
        additionalInfo.put("maxMonthlyPayment", "111");
        additionalInfo.put("programLength", "4");
        additionalInfo.put("delta", "delta")

        UserInformation userInformation = new UserInformation()
        userInformation.setAdditionalInfo(additionalInfo)
        accountInfo.setUserInformation(userInformation)
        varIntegrationServiceRemoteImpl.getAccountInfo(_, _, _) >> accountInfo
        Program program = getProgram()
        Map<String, Object> config = new HashMap<String, Object>()
        config.put(CommonConstants.VIMS_PRICING_API, true)
        program.setConfig(config)
        programService.getProgram(_, _, _) >> program

        when:
        User user = varOrderManagerVITALITY.selectUser(request)
        then:
        user != null && user.getVarId().equalsIgnoreCase("VitalityUS")

    }

    def "selectUser - Demo User"() {

        setup:
        def request = Mock(HttpServletRequest)
        Program program = getProgram()
        Map<String, Object> config = new HashMap<String, Object>()
        config.put(CommonConstants.SINGLE_ITEM_PURCHASE, true)
        program.setConfig(config)
        program.setIsActive(true)
        programService.getProgram(_, _, _) >> program

        demoUserDao.findByDemoUserIdAndPassword(_, _) >> getDemoUserEntity()

        when:
        User user = varOrderManagerVITALITY.selectUser(request)
        then:
        user != null
    }

    def "test orderPlace"() {

        setup:

        def order = Mock(Order)
        User user = new User()
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110

        when:
        boolean result = varOrderManagerVITALITY.placeOrder(order, user, getProgram());

        then:
        result == true && order.getVarOrderId() == "110"

    }

    def "test CancelOrder"() {

        setup:

        def order = Mock(Order)
        User user = new User()
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110

        when:
        boolean result1 = varOrderManagerVITALITY.cancelOrder(order)
        boolean result2 = varOrderManagerVITALITY.cancelOrder(order, user, new Program())

        then:
        result1 == true && result2 == true

    }

    def "test computePricingModel()"() {

        setup:

        def order = Mock(Order)
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110

        Product product = new Product()
        Category category = new Category()
        category.setSlug("watch")
        List<Category> categoryList = new ArrayList<Category>()
        categoryList.add(category)
        product.setCategory(categoryList)

        List<Option> options = new ArrayList<Option>()
        Option option = new Option()
        option.setName("caseSize")
        option.setValue("watch")
        options.add(option)
        product.setOptions(options)

        List<Offer> offers = new ArrayList<Offer>()
        Price b2sItemPrice = new Price(3100.0, "USD", 2)
        Offer offer = new Offer()
        offer.setB2sItemPrice(b2sItemPrice)

        offers.add(offer)
        product.setOffers(offers)
        when:

        pricingModelConfigurationDao.getByVarIdProgramIdPriceType(_, _, _) >> getPricingModelConfigurationList()
        pricingModelConfigurationDao.getSubsidyByVarIdProgramIdPriceKey(_, _, _) >> getPricingModelConfiguration()

        varOrderManagerVITALITY.computePricingModel(product, getUser(), getProgram())
        then:
        PricingModel pricingModel =
                product.getAdditionalInfo().get(CommonConstants.PRICING_MODEL)

        pricingModel.getTotalDueTodayBeforeTax() == 700.0
        pricingModel.getUpgradeCost() == 599.0


    }

    def "test computePricingModel() -createPricingModelFromUserInfo"() {

        setup:

        def order = Mock(Order)
        order.getOrderId() >> 101
        order.getVarOrderId() >> 110
        Product product = new Product()
        Category category = new Category()
        category.setSlug("watch")
        List<Category> categoryList = new ArrayList<Category>()
        categoryList.add(category)
        product.setCategory(categoryList)

        List<Option> options = new ArrayList<Option>()
        Option option = new Option()
        option.setName("caseSize")
        option.setValue("watch")
        options.add(option)
        product.setOptions(options)

        List<Offer> offers = new ArrayList<Offer>()
        Price b2sItemPrice = new Price(11.0, "USD", 2)
        Offer offer = new Offer()
        offer.setB2sItemPrice(b2sItemPrice)

        offers.add(offer)
        product.setOffers(offers)

        Program program = getProgram()
        Map<String, Object> config = new HashMap<String, Object>()
        config.put(CommonConstants.VIMS_PRICING_API, true)
        program.setConfig(config)

        when:

        pricingModelConfigurationDao.getByVarIdProgramIdPriceType(_, _, _) >> getPricingModelConfigurationList()
        pricingModelConfigurationDao.getSubsidyByVarIdProgramIdPriceKey(_, _, _) >> getPricingModelConfiguration()

        varOrderManagerVITALITY.computePricingModel(product, getUser(), program)
        then:
        PricingModel pricingModel =
                product.getAdditionalInfo().get(CommonConstants.PRICING_MODEL)

        pricingModel.getTotalDueTodayBeforeTax() == 7.0

    }

    def "test ValidLogin"() {

        setup:

        User user = getUser()
        Map<String, String> additionalInfo = new HashMap<String, String>()
        additionalInfo.put("pricingId", "ss")
        user.setAdditionalInfo(additionalInfo)
        Program program = getProgram()
        program.setProgramId(CommonConstants.VITALITYUS_TVGCORPORATE_PROGRAM)
        when:
        pricingModelConfigurationDao.getAllSubsidiesByVarIdProgramIdPriceKey(_, _, _) >> null
        boolean result1 = varOrderManagerVITALITY.isValidLogin(user, program)

        then:
        result1 == false

    }

    DemoUserEntity getDemoUserEntity() {
        DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId()
        demoUserId.setProgramId("b2s_qa_only")
        demoUserId.setVarId("delta")
        demoUserId.setUserId("demo")

        DemoUserEntity demoUserEntity = new DemoUserEntity()
        demoUserEntity.setFirstname("Eric")
        demoUserEntity.setDemoUserId(demoUserId)
        demoUserEntity.setZip("1234-122-123")

        return demoUserEntity
    }

    Program getProgram() {
        Program program = new Program();
        program.setVarId("Delta")
        program.setProgramId("b2s_qa_only")
        program.setIsLocal(true)
        program.setIsActive(true)
        return program
    }

    User getUser() {
        User user = new UserVitality()
        user.setProgramId("sdf")
        user.setVarId("delta")

        Map<String, String> additionalInfo = new HashMap<String, String>()
        additionalInfo.put("Size", "ss")
        user.setAdditionalInfo(additionalInfo)
        user.setActivationFee("7")
        user.setMaxMonthlyPayment("2")
        user.setDelta("3")
        user.setProgramLength(2)
        user.setFinancedAmount("5")

        return user
    }

    PricingModelConfiguration getPricingModelConfiguration() {

        PricingModelConfiguration pricingModel = new PricingModelConfiguration()
        pricingModel.setPriceKey("watch|caseSize")
        pricingModel.setPaymentValue(100.0)
        pricingModel.setDelta(1000.0)
        return pricingModel
    }

    List<PricingModelConfiguration> getPricingModelConfigurationList() {
        PricingModelConfiguration pricingModel = new PricingModelConfiguration()
        PricingModelConfiguration pricingModel2 = new PricingModelConfiguration()
        List<PricingModelConfiguration> configurationList = new ArrayList<PricingModelConfiguration>()
        pricingModel.setPriceKey("watch|caseSize")
        pricingModel.setPaymentValue(100.0)
        pricingModel2.setPriceKey("watch|watch")
        pricingModel2.setPaymentValue(101.0)
        configurationList.add(pricingModel)
        configurationList.add(pricingModel2)

        return configurationList

    }

}
