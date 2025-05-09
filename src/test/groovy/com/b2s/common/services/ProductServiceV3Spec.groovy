package com.b2s.common.services

import com.b2s.apple.entity.RelevantLanguageEntity
import com.b2s.apple.mapper.OptionMapper
import com.b2s.apple.mapper.ProductMapper
import com.b2s.apple.services.CartService
import com.b2s.apple.services.CategoryConfigurationService
import com.b2s.apple.services.EngravingService
import com.b2s.apple.services.GiftPromoService
import com.b2s.apple.services.ProductCarouselImageService
import com.b2s.apple.services.VarProgramCatalogConfigService
import com.b2s.common.services.productservice.ProductServiceFactoryWrapper
import com.b2s.common.services.productservice.ProductServiceV3
import com.b2s.common.services.requests.productservice.MultiProductDetailRequest
import com.b2s.common.services.responses.productservice.CoreProductDetailResponse
import com.b2s.common.services.transformers.TransformersHolder
import com.b2s.common.services.transformers.productservice.MultiProductDetailToCoreProductDetail
import com.b2s.common.services.transformers.productservice.MultiProductDetailToProductDetail
import com.b2s.common.services.util.CategoryRepository
import com.b2s.common.services.util.ImageObfuscatory
import com.b2s.rewards.apple.dao.RelevantLanguageDao
import com.b2s.rewards.apple.integration.model.PaymentOptions
import com.b2s.rewards.apple.model.Cart
import com.b2s.rewards.apple.model.CartItem
import com.b2s.rewards.apple.model.CartTotal
import com.b2s.rewards.apple.model.Category
import com.b2s.rewards.apple.model.Engrave
import com.b2s.rewards.apple.model.GiftItem
import com.b2s.rewards.apple.model.Option
import com.b2s.rewards.apple.model.PaymentOption
import com.b2s.rewards.apple.model.Price
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.model.RedemptionPaymentLimit
import com.b2s.rewards.apple.model.SmartPrice
import com.b2s.rewards.apple.model.VarProgramRedemptionOption
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.service.product.client.api.ProductServiceClientImpl
import com.b2s.service.product.client.application.detail.MultiProductDetailResponse
import com.b2s.service.product.client.application.detail.ProductDetailRequest
import com.b2s.service.product.client.application.search.ProductSearchRequest
import com.b2s.service.product.client.common.CatalogRequestContext
import com.b2s.service.product.common.domain.*
import com.b2s.service.product.common.domain.response.*
import com.b2s.service.product.common.domain.shipping.DeliveryInformation
import com.b2s.service.product.common.domain.shipping.ShippingInformation
import com.b2s.service.product.common.domain.shipping.Source
import com.b2s.shop.common.User
import com.b2s.shop.common.order.var.VAROrderManagerIF
import com.b2s.shop.common.order.var.VarOrderManagerHolder
import com.google.common.collect.Sets
import org.apache.commons.collections.CollectionUtils
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import org.joda.time.DateTime
import spock.lang.Specification
import spock.lang.Subject

import java.math.RoundingMode
import java.sql.Timestamp

import static com.b2s.rewards.common.util.CommonConstants.*

class ProductServiceV3Spec extends Specification {

    Properties applicationProperties = Mock()
    //ProductServiceClientFactory productServiceClientFactory = Mock()
    ProductServiceClientImpl impl = Mock()
    ProductServiceFactoryWrapper productServiceFactoryWrapper = Mock()
    MultiProductDetailToProductDetail detailToProductDetail = Mock()
    MultiProductDetailToCoreProductDetail detailToCoreProductDetail = Mock()
    EngravingService engravingService = Mock(EngravingService)
    GiftPromoService giftPromoService = Mock(GiftPromoService)
    TransformersHolder<MultiProductDetailRequest, Set<ProductDetailRequest>, MultiProductDetailResponse,
            CoreProductDetailResponse> detailTransformersHolder = new TransformersHolder(detailToProductDetail, detailToCoreProductDetail)
    RelevantLanguageDao relevantLanguageDao = Mock()
    CategoryConfigurationService categoryConfigurationService = Mock()
    CategoryRepository categoryRepository = Mock()
    ImageObfuscatory imageObfuscatory = Mock()
    VarProgramCatalogConfigService varProgramCatalogConfigService = Mock()
    ProductCarouselImageService productCarouselImageService = Mock()
    CartService cartService = Mock()

    def optionMapper = new OptionMapper(categoryConfigurationService: categoryConfigurationService)
    def productMapper = new ProductMapper(categoryConfigurationService: categoryConfigurationService, optionMapper:
            optionMapper, imageObfuscatory: imageObfuscatory)

    @Subject
    private ProductServiceV3 productServiceV3 = new ProductServiceV3(productServiceFactoryWrapper,
            detailTransformersHolder)

    def setup() {
        productServiceV3.varProgramCatalogConfigService = varProgramCatalogConfigService
        productServiceV3.engravingService = engravingService
        productServiceV3.giftPromoService = giftPromoService
        productServiceV3.sortFilterOptionsLocales = Arrays.asList("en_us", "en_CA")
        productServiceV3.productCarouselImageService = productCarouselImageService
        productServiceV3.cartService = cartService
    }

    def 'getProductSearchRequestBuilder()'() {

        given:
        def categorySlugs = Sets.newHashSet("macbook-pro")
        def keyword = null
        def sort = "DISPLAY_PRICE"
        def sortOrder = "ASCENDING"
        def locale = Locale.CANADA
        def pointsRange = null
        def pageSize = 12
        def resultOffSet = 0
        def program = getProgram()
        def promoTag = null
        def withVariations = false
        def facetsFilters = getFacetsFilters()

        when:
        productServiceV3.applicationProperties = applicationProperties
        applicationProperties.getProperty("searchResultLimit") >> 100
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program
                .getProgramId(), "macbook-pro.facets") >> Arrays.asList("processor", "storage", "touchBar", "color", "screenSize", "graphics", "memory")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program
                .getProgramId(), SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories,macbook-pro,watch")

        then:
        def result = productServiceV3.getProductSearchRequestBuilder(categorySlugs, keyword, sort, sortOrder,
                locale, pointsRange, pageSize, resultOffSet, program, promoTag, withVariations,withFacets, facetsFilters)

        expect:
        result.dynamicFilters.isEmpty()==emptyOutput
        if (withFacets){
            result.dynamicFilters.containsKey("color") && result.dynamicFilters.get("color")
                    .contains("Silver")
        }


        where:
        withFacets || emptyOutput
        true       || false
        false      || true

    }

    def 'getProductSearchRequestBuilder() with SplitTender config'() {

        given:
        def categorySlugs = Sets.newHashSet("macbook-pro")
        def keyword = null
        def sort = "DISPLAY_PRICE"
        def sortOrder = "ASCENDING"
        def locale = Locale.CANADA
        def pointsRange = null
        def pageSize = 12
        def resultOffSet = 0
        def promoTag = null
        def withVariations = false
        def facetsFilters = new HashMap<>()

        def program = getProgram()
        Map<String, Object> vpConfig = program.getConfig()
        vpConfig.put(ENABLE_SMART_PRICING, enableSmartPrice)
        program.setRedemptionOptions(getRedemptionOptions(withMaxCash))

        when:
        productServiceV3.applicationProperties = applicationProperties
        applicationProperties.getProperty("searchResultLimit") >> 100
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program
                .getProgramId(), "macbook-pro.facets") >> Arrays.asList("")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program
                .getProgramId(), SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("")

        then:
        def result = productServiceV3.getProductSearchRequestBuilder(categorySlugs, keyword, sort, sortOrder,
                locale, pointsRange, pageSize, resultOffSet, program, promoTag, withVariations,false, facetsFilters)

        expect:
        result.maxPointsPercentage == maxPointsPercentage
        result.maxCashAmount == maxCashAmount

        where:
        enableSmartPrice | withMaxCash || maxPointsPercentage || maxCashAmount
        true             | true        || Optional.of(0.50)   || Optional.of(Money.parse("USD 300"))
        true             | false       || Optional.of(0.50)   || Optional.empty()
        false            | true        || Optional.empty()    || Optional.empty()
        false            | false       || Optional.empty()    || Optional.empty()
    }

    def 'getProductSearchRequestBuilder for all accessories()'() {
        given:
        def categorySlugs = Sets.newHashSet("accessories")
        def keyword = null
        def sort = "DISPLAY_PRICE"
        def sortOrder = "ASCENDING"
        def locale = Locale.CANADA
        def pointsRange = null
        def pageSize = 12
        def resultOffSet = 0
        def program = getProgram()
        def promoTag = null
        def withVariations = false
        def facetsFilters = getFacetsFilters()

        when:
        productServiceV3.applicationProperties = applicationProperties
        applicationProperties.getProperty("searchResultLimit") >> 100
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                "accessories.facets") >> Arrays.asList("productType", "brand", "color", "headphoneStyle", "material", "storage", "printerType")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories", "macbook-pro", "watch")

        then:
        def result = productServiceV3.getProductSearchRequestBuilder(categorySlugs, keyword, sort, sortOrder,
                locale, pointsRange, pageSize, resultOffSet, program, promoTag, withVariations,withFacets,facetsFilters)

        expect:
        result.additionalFacets.isEmpty()==emptyOutput

        where:
        withFacets || emptyOutput
        true       || false
        false      || true
    }

    def 'getAppleMultiProductDetail() with service client detail returning NULL'() {

        given:
        def user = getUser()
        def psids = Arrays.asList("P1", "P2")
        def program = getProgram()
        def builder = new ProductDetailRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withPsid("psid")
                .withTargetCurrencies(Sets.newHashSet(CurrencyUnit.ofCountry("US")))
                .withVariations().withRealTimeInfo()
                .build()
        def req = Sets.newHashSet(request)

        when:
        detailTransformersHolder.getRequestTransformer().transform(_, null) >> req
        productServiceFactoryWrapper.getProductServiceClient() >> impl
        productServiceV3.applicationProperties = applicationProperties

        then:
        def products = productServiceV3.getAppleMultiProductDetail(psids, program, false, user, true,  false)

        expect:
        products != null
        products.size() == 0
    }

    def 'getAppleProductDetail() without available gift items'() {

        given:
        def user = getUser()
        user.setLocale(Locale.US)
        def program = getProgram()
        program.getConfig().put(CommonConstants.ENABLE_APPLE_CARE_SERVICE_PLAN, true)
        def builder = new ProductDetailRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withPsid("psid")
                .withTargetCurrencies(Sets.newHashSet(CurrencyUnit.ofCountry("US")))
                .withVariations().withRealTimeInfo()
                .build()
        com.b2s.rewards.apple.model.Product appleCareproduct = getMockAppleProductResponse()

        when:
        productServiceV3.productMapper = Mock(ProductMapper)
        detailTransformersHolder.getRequestTransformer().transform(_, null, program) >> Sets.newHashSet(request)
        productServiceFactoryWrapper.getProductServiceClient() >> impl
        impl.detail(_) >> new MultiProductDetailResponse(Arrays.asList(getModelProduct(false, false)), new HashMap<String,
                Throwable>())
        productServiceV3.applicationProperties = applicationProperties
        productServiceV3.varOrderManagerHolder = Mock(VarOrderManagerHolder)
        productServiceV3.productMapper.from(_, Locale.US, program, user, false, true) >>
                [PSID1: new com.b2s.rewards.apple.model.Product()]
        productServiceV3.productMapper.from(_, _, program, user, false, false) >>
                ["329393/M": appleCareproduct]
        productServiceV3.varOrderManagerHolder.getVarOrderManager("SCOTIA") >> Mock(VAROrderManagerIF)
        productServiceV3.giftPromoService.getGiftPsids(user, _) >> Collections.emptyList();

        then:
        def products = productServiceV3.getAppleProductDetail("PSID1", program, false, user, true, true, false, false)

        expect:
        products != null
        products.getAddOns().availableGiftItems == []
        products.getAddOns() != null
        products.getAddOns().getServicePlans() != []
        products.getAddOns().getServicePlans().get(0).getName() == "iphone theft" && products.getAddOns().getServicePlans().get(0).isAvailable()
    }

    private com.b2s.rewards.apple.model.Product getMockAppleProductResponse() {
        com.b2s.rewards.apple.model.Product appleProduct = new com.b2s.rewards.apple.model.Product()
        appleProduct.setName("iphone theft")
        appleProduct.setPsid("329393/M")
        appleProduct.setAvailable(true)
        return appleProduct
    }


    def 'getProductSearchRequestBuilder for search()'() {
        given:
        def categorySlugs = Sets.newHashSet("accessories")
        def keyword = null
        def sort = "DISPLAY_PRICE"
        def sortOrder = "ASCENDING"
        def locale = Locale.CANADA
        def pointsRange = null
        def pageSize = 12
        def resultOffSet = 0
        def program = getProgram()
        def promoTag = null
        def withVariations = false
        def facetsFilters = getFacetsFilters()

        when:
        productServiceV3.applicationProperties = applicationProperties
        applicationProperties.getProperty("searchResultLimit") >> 100
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SEARCH + DOT_FACETS) >> Arrays.asList("productType", "brand", "color", "headphoneStyle", "material", "storage", "printerType")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories", "macbook-pro", "watch")

        then:
        def result = productServiceV3.getProductSearchRequestBuilder(categorySlugs, keyword, sort, sortOrder,
                locale, pointsRange, pageSize, resultOffSet, program, promoTag, withVariations,withFacets, facetsFilters)

        expect:
        result.additionalFacets.isEmpty()==emptyOutput

        where:
        withFacets || emptyOutput
        true       || true
        false      || true
    }


    def 'getFacetsFilters - sorting'() {
        given:
        def builder = new ProductSearchRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withResultLimit(200)
                .withCategories(Sets.newHashSet("macbook-pro")).build()
        def sortOrderList = Arrays.asList("screenSize", "color", "processor", "touchBar")

        when:
        productServiceV3.applicationProperties = applicationProperties
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program
                .getProgramId(), "macbook-pro.facets") >> Arrays.asList("screenSize", "color", "processor", "touchBar")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories", "macbook-pro", "watch")

        then:
        def result = productServiceV3.getFacetsFilters(getProductSearchResponse(), request, program, Locale.US)

        expect:
        result.keySet().asList() == sortOrderList

    }

    def 'getFacetsFilters - test filter options sorting'() {
        given:
        def builder = new ProductSearchRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withResultLimit(200)
                .withCategories(Sets.newHashSet("macbook-pro")).build()
        def sortOrderList = Arrays.asList("screenSize", "color", "processor", "touchBar")

        when:
        productServiceV3.applicationProperties = applicationProperties
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program
                .getProgramId(), "macbook-pro.facets") >> Arrays.asList("screenSize", "color", "processor", "touchBar")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories", "macbook-pro", "watch")

        then:
        def result = productServiceV3.getFacetsFilters(getProductSearchResponse(), request, program, Locale.US)

        expect:

        result.get("color").get(0).value == "Silver";
        result.get("color").get(1).value == "Space Gray";

    }

    def 'getFacetsFilters - test filter options sorting with locale not in the white list'() {
        given:
        def builder = new ProductSearchRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withResultLimit(200)
                .withCategories(Sets.newHashSet("macbook-pro")).build()
        def sortOrderList = Arrays.asList("screenSize", "color", "processor", "touchBar")

        when:
        productServiceV3.applicationProperties = applicationProperties
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program
                .getProgramId(), "macbook-pro.facets") >> Arrays.asList("screenSize", "color", "processor", "touchBar")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories", "macbook-pro", "watch")

        then:
        def result = productServiceV3.getFacetsFilters(getProductSearchResponse(), request, program, Locale.FRENCH)

        expect:

        result.get("color").get(0).value == "Space Gray";
        result.get("color").get(1).value == "Silver";

    }

    def 'getFacetsFilters -Language Frensh - sorting'() {
        given:
        def builder = new ProductSearchRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withResultLimit(200)
                .withCategories(Sets.newHashSet("macbook-pro")).build()
        def sortOrderList = Arrays.asList("screenSize", "color", "processor", "touchBar")

        when:
        productServiceV3.applicationProperties = applicationProperties
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                "macbook-pro.facets") >> Arrays.asList("screenSize", "color", "processor", "touchBar")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories", "macbook-pro", "watch")

        then:
        def result = productServiceV3.getFacetsFilters(getProductSearchResponseForFrench(), request, program, Locale.US)

        expect:
        result.keySet().asList() == sortOrderList
        result.get("color").get(0).i18Name == "Couleur";
        result.get("screenSize").get(0).i18Name == "Taille d’écran";

    }

    def 'getAppleProductDetail() with RelatedItem'() {

        given:
        def user = getUser()
        user.setLocale(Locale.US)
        def program = getProgram()
        program.getConfig().put(CommonConstants.IS_RELATED_PRODUCTS_ENABLED, vpc)
        def builder = new ProductDetailRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withPsid("psid")
                .withTargetCurrencies(Sets.newHashSet(CurrencyUnit.ofCountry("US")))
                .withVariations().withRealTimeInfo()
                .build()
        com.b2s.rewards.apple.model.Product appleRelatedProduct = getMockAppleProductResponse()

        when:
        productServiceV3.productMapper = Mock(ProductMapper)
        detailTransformersHolder.getRequestTransformer().transform(_, null, program) >> Sets.newHashSet(request)
        productServiceFactoryWrapper.getProductServiceClient() >> impl
        impl.detail(_) >> new MultiProductDetailResponse(Arrays.asList(getModelProduct(mockRPPsids, false)), new HashMap<String,
                Throwable>())
        productServiceV3.applicationProperties = applicationProperties
        productServiceV3.varOrderManagerHolder = Mock(VarOrderManagerHolder)
        productServiceV3.productMapper.from(_, Locale.US, program, user, false, true) >>
                [PSID1: new com.b2s.rewards.apple.model.Product()]
        productServiceV3.productMapper.from(_, _, program, user, false, false) >>
                ["329393/M": appleRelatedProduct]
        productServiceV3.varOrderManagerHolder.getVarOrderManager("SCOTIA") >> Mock(VAROrderManagerIF)

        then:
        def products = productServiceV3.getAppleProductDetail("PSID1", program, false, user, true, true, false, queryParm)
        hasRPResultFlag == products.isHasRelatedProduct()
        relatedProdsResponse == org.apache.commons.collections.CollectionUtils.isNotEmpty(products.getRelatedProducts())
        where:
        queryParm | vpc   | mockRPPsids | hasRPResultFlag | relatedProdsResponse
        false     | true  | true        | true            | false
        false     | true  | false       | false           | false
        false     | false | true        | false           | false
        false     | false | false       | false           | false
        true      | true  | true        | true            | true
        true      | true  | false       | false           | false
        true      | false | true        | false           | false
        true      | false | false       | false           | false
    }

    def 'getDetailPageProduct() based on VPC'() {

        given:
        def user = getUser()
        user.setLocale(Locale.US)
        def program = getProgram()
        program.getConfig().put(CommonConstants.ENABLE_SMART_PRICING_OVERRIDE, smartPriceVPC)
        program.getConfig().put(CommonConstants.DISPLAY_PRODUCT_PAGE_CAROUSEL, pdpCarouselVPC)
        program.setRedemptionOptions(getRedemptionOptions(false))
        def builder = new ProductDetailRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withPsid("psid")
                .withTargetCurrencies(Sets.newHashSet(CurrencyUnit.ofCountry("US")))
                .withVariations().withRealTimeInfo()
                .build()
        com.b2s.rewards.apple.model.Product appleRelatedProduct = getMockAppleProductResponse()

        when:
        productServiceV3.productMapper = Mock(ProductMapper)
        detailTransformersHolder.getRequestTransformer().transform(_, null, program) >> Sets.newHashSet(request)
        productServiceFactoryWrapper.getProductServiceClient() >> impl
        impl.detail(_) >> new MultiProductDetailResponse(Arrays.asList(getModelProduct(true, true)), new HashMap<String,
                Throwable>())
        productServiceV3.applicationProperties = applicationProperties
        productServiceV3.varOrderManagerHolder = Mock(VarOrderManagerHolder)
        productServiceV3.productMapper.from(_, Locale.US, program, user, false, true) >>
                [PSID1: new com.b2s.rewards.apple.model.Product()]
        productServiceV3.productMapper.from(_, _, program, user, false, false) >>
                ["329393/M": appleRelatedProduct]
        productServiceV3.varOrderManagerHolder.getVarOrderManager("SCOTIA") >> Mock(VAROrderManagerIF)
        productCarouselImageService.getImageUrls(_, _, _, _) >> getCarouselImages()
        productServiceV3.productMapper.getSmartPrice(_, _) >> getSmartPrice()

        def cart = getCart()
        cartService.generateCartWithPriceInfo(user, program, _) >> cart

        then:
        def product = productServiceV3.getDetailPageProduct("PSID1", program, user, false, true)

        CollectionUtils.isNotEmpty(product.getCarouselImages()) == carouselImagesInResponse
        Objects.nonNull(product.getSmartPrice()) == smartPriceInResponse

        where:
        smartPriceVPC | pdpCarouselVPC || smartPriceInResponse || carouselImagesInResponse
        true          | true           || true                 || true
        true          | false          || true                 || false
        false         | true           || false                || true
        false         | false          || false                || false
    }

    def getSmartPrice() {
        def smartPrice = new SmartPrice()
        smartPrice.setAmount(179.84)
        smartPrice.setPoints(24800)
        return smartPrice
    }

    def getCarouselImages() {
        List<String> carouselImages = new ArrayList<>()
        carouselImages.add("carouselImage1")
        carouselImages.add("carouselImage2")
        return carouselImages
    }

    def getUAPayment() {
        Price cartMaxLimit = null
        Price cashMaxLimit = new Price(179.84, "USD", 24800)
        Price cashMinLimit = new Price(0, "USD", 0)
        Price pointsMaxLimit = new Price(267.84, "USD", 49600)
        Price pointsMinLimit = new Price(133.92, "USD", 24800)
        Price useMaxPoints = new Price(0, "USD", 49600)
        Price useMinPoints = new Price(179.84024999999997, "USD", 24800)

        return RedemptionPaymentLimit.builder()
                .withCartMaxLimit(cartMaxLimit)
                .withCashMaxLimit(cashMaxLimit)
                .withCashMinLimit(cashMinLimit)
                .withPointsMaxLimit(pointsMaxLimit)
                .withPointsMinLimit(pointsMinLimit)
                .withUseMaxPoints(useMaxPoints)
                .withUseMinPoints(useMinPoints)
                .build()
    }

    def getCart() {
        Cart cart = new Cart()
        List cartItems = new ArrayList<CartItem>()
        CartItem cartItem = new CartItem()
        cartItem.setSupplierId(20000)
        cartItems.add(cartItem)
        cart.setCartItems(cartItems)

        CartTotal cartTotal = new CartTotal()
        Price price = new Price(170.13, "USD", 31800);
        cartTotal.setPrice(price)
        cart.setCartTotal(cartTotal)
        cart.setConvRate(185.1851851851852)
        cart.setRedemptionPaymentLimit(getUAPayment())

        return cart;
    }

    def getRedemptionOptions(boolean withMaxCash) {
        def pointsOnlyStr = "pointsonly"
        def splitPayStr = "splitpay"
        def varId = "UA"
        def programId = "MP"
        Map<String, List<VarProgramRedemptionOption>> redemptionOptions = new HashMap<>()
        List<VarProgramRedemptionOption> varProgramRedemptionOptionList = new ArrayList<>()
        VarProgramRedemptionOption pointsOnly = getVarProgramRedemptionOption("percentage", 0, 50, pointsOnlyStr, 1, varId, programId)
        varProgramRedemptionOptionList.add(pointsOnly)
        redemptionOptions.put(pointsOnlyStr, varProgramRedemptionOptionList)

        varProgramRedemptionOptionList = new ArrayList<>()
        VarProgramRedemptionOption splitPay = getVarProgramRedemptionOption("percentage", 50, 0, splitPayStr, 2, varId, programId)
        varProgramRedemptionOptionList.add(splitPay)

        if(withMaxCash){
            splitPay = getVarProgramRedemptionOption("dollar", 300, 0, splitPayStr, 2, varId, programId)
            varProgramRedemptionOptionList.add(splitPay)
        }

        redemptionOptions.put(splitPayStr, varProgramRedemptionOptionList)
        return redemptionOptions
    }

    def getVarProgramRedemptionOption(String limitType, Integer paymentMaxLimit, Integer paymentMinLimit, String paymentOption,
                                      Integer orderBy, String programId, String varId) {
        return VarProgramRedemptionOption.builder()
                .withLimitType(limitType)
                .withPaymentMaxLimit(paymentMaxLimit)
                .withPaymentMinLimit(paymentMinLimit)
                .withPaymentOption(paymentOption)
                .withActive(true)
                .withLastUpdatedBy("updatedBy")
                .withLastUpdatedDate(new Timestamp(1476706090633))
                .withOrderBy(orderBy)
                .withProgramId(programId)
                .withVarId(varId)
                .build()
    }

    def getProductSearchResponse() {
        def List<Facet> facets = new ArrayList<>()

        def name1 = "color"
        List<FacetEntry> entries1 = new ArrayList<>()
        def facetEntry1_1 = new FacetEntry("Space Gray", 8, "color")
        def facetEntry1_2 = new FacetEntry("Silver", 6, "color")
        entries1.add(facetEntry1_1)
        entries1.add(facetEntry1_2)
        Facet facet1 = new Facet(name1, name1, entries1)

        def name2 = "processor"
        List<FacetEntry> entries2 = new ArrayList<>()
        def facetEntry2_1 = new FacetEntry("1.4 GHz quad-core (Turbo Boost up to 3.9 GHz)", 4, "processor")
        def facetEntry2_2 = new FacetEntry("2.4 GHz quad-core (Turbo Boost up to 4.1 GHz)", 4, "processor")
        def facetEntry2_3 = new FacetEntry("2.3 GHz 8‑core", 3, "processor")
        def facetEntry2_4 = new FacetEntry("2.6 GHz 6-core", 2, "processor")
        def facetEntry2_5 = new FacetEntry("2.8 GHz", 1, "processor")
        entries2.add(facetEntry2_1)
        entries2.add(facetEntry2_2)
        entries2.add(facetEntry2_3)
        entries2.add(facetEntry2_4)
        entries2.add(facetEntry2_5)
        Facet facet2 = new Facet(name2, name2, entries2)

        def name3 = "touchBar"
        List<FacetEntry> entries3 = new ArrayList<>()
        def facetEntry3_1 = new FacetEntry("Touch Bar with Touch ID", 9, "touchBar")
        entries3.add(facetEntry3_1)
        Facet facet3 = new Facet(name3, name3, entries3)

        def name4 = "screenSize"
        List<FacetEntry> entries4 = new ArrayList<>()
        def facetEntry4_1 = new FacetEntry("13-inch", 9, "screenSize")
        def facetEntry4_2 = new FacetEntry("16-inch", 5, "screenSize")
        entries4.add(facetEntry4_1)
        entries4.add(facetEntry4_2)
        Facet facet4 = new Facet(name4, name4, entries4)

        facets.add(facet1)
        facets.add(facet2)
        facets.add(facet3)
        facets.add(facet4)

        def productSearchResponse = new ProductSearchResponse(null, facets, null)
        return productSearchResponse
    }

    def getProductSearchResponseForFrench() {
        def List<Facet> facets = new ArrayList<>()

        def name1 = "color"
        List<FacetEntry> entries1 = new ArrayList<>()
        def facetEntry1_1 = new FacetEntry("Gris cosmique", 8, "color")
        def facetEntry1_2 = new FacetEntry("Argent", 6, "color")
        entries1.add(facetEntry1_1)
        entries1.add(facetEntry1_2)
        Facet facet1 = new Facet(name1, "Couleur", entries1)

        def name2 = "processor"
        List<FacetEntry> entries2 = new ArrayList<>()
        def facetEntry2_1 = new FacetEntry("1,4 GHz quadricœur (Turbo Boost jusqu’à 3,9 GHz", 4, "processor")
        def facetEntry2_2 = new FacetEntry("2,4 GHz quadricœur (Turbo Boost jusqu’à 4,1 GHz)", 4, "processor")
        def facetEntry2_3 = new FacetEntry("2,3 GHz 8 cœurs", 3, "processor")
        def facetEntry2_4 = new FacetEntry("2,6 GHz hexacœur", 2, "processor")
        def facetEntry2_5 = new FacetEntry("2,8 GHz", 1, "processor")
        entries2.add(facetEntry2_1)
        entries2.add(facetEntry2_2)
        entries2.add(facetEntry2_3)
        entries2.add(facetEntry2_4)
        entries2.add(facetEntry2_5)
        Facet facet2 = new Facet(name2, "processeur", entries2)

        def name3 = "touchBar"
        List<FacetEntry> entries3 = new ArrayList<>()
        def facetEntry3_1 = new FacetEntry("Touch Bar with Touch ID", 9, "touchBar")
        entries3.add(facetEntry3_1)
        Facet facet3 = new Facet(name3, "Touch Bar", entries3)

        def name4 = "screenSize"
        List<FacetEntry> entries4 = new ArrayList<>()
        def facetEntry4_1 = new FacetEntry("13 po", 9, "screenSize")
        def facetEntry4_2 = new FacetEntry("16 po", 5, "screenSize")
        entries4.add(facetEntry4_1)
        entries4.add(facetEntry4_2)
        Facet facet4 = new Facet(name4, "Taille d’écran", entries4)

        facets.add(facet1)
        facets.add(facet2)
        facets.add(facet3)
        facets.add(facet4)

        def productSearchResponse = new ProductSearchResponse(null, facets, null)
        return productSearchResponse
    }

    def getProgram() {
        Program program = new Program()
        program.varId = "SCOTIA"
        program.programId = "AmexROC"
        program.catalogId = "apple-ca-en"
        Map<String, Object> configs = new HashMap<>()
        configs.put("catalog_id", "apple-us-en")
        program.setConfig(configs)
        program.setTargetCurrency(CurrencyUnit.USD)
        return program
    }

    def getFacetsFilters() {
        Map facetsFiltersMap = new HashMap()
        List<Option> options1 = new ArrayList<>()
        options1.add(new Option("", "Space Gray", Optional.empty()))
        options1.add(new Option("", "Silver", Optional.empty()))
        facetsFiltersMap.put("color", options1)
        List<Option> options2 = new ArrayList<>()
        options2.add(new Option("", "1,4 GHz quadricœur (Turbo Boost jusqu’à 3,9 GHz)", Optional.empty()))
        facetsFiltersMap.put("processor", options2)
        return facetsFiltersMap
    }

    def 'appleSearch - getVariationProductByLanguagePreference'() {
        given:

        def user = getUser()
        def locale = getLocale()
        def program = getProgram_appleSearch()
        def serverRequest = ProductSearchRequest.builder()
                .withRequestContext(CatalogRequestContext.forCatalogId("apple-hk-en"))
                .withQueryPhrase("")
                .build()
        def productSearchResponse = getProductSearchResponse_appleSearch()

        when:
        productServiceFactoryWrapper.getProductServiceClient() >> impl
        impl.search(_) >> productSearchResponse

        productServiceV3.relevantLanguageDao = relevantLanguageDao
        relevantLanguageDao.getByLocale(_) >> rel_lang_entity
        productServiceV3.productMapper = productMapper
        productServiceV3.getRelatedLanguage(_) >> new ArrayList<String>()
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories", "macbook-pro", "watch")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                PRODUCT_TILES_OPTIONS) >> Arrays.asList("language", "color", "swatchImageUrl")
        categoryConfigurationService.getCategoryRepository(_) >> categoryRepository
        categoryRepository.getCategoryDetailsByHierarchy(_) >> getCategory()
        def result = productServiceV3.appleSearch(serverRequest, locale, program, user, null, true)

        then:
        result.products.get(0).name.contains(lang)

        where:/*please note that products not present for 'en_HK' locale and for 'enuk' language, refer
        getProductSearchResponse_appleSearch()*/
        rel_lang_entity                                                                          | lang
        new RelevantLanguageEntity(id: 7, locale: "en_HK", relevantLanguage: "en_US")            | "US English"
        new RelevantLanguageEntity(id: 7, locale: "en_HK", relevantLanguage: "enuk,zh_CN,en_US") | "Chinese (Pinyin)"
        new RelevantLanguageEntity(id: 7, locale: "en_HK", relevantLanguage: "enuk,ko_KR,en_CA") | "British English"

    }

    def 'appleSearch - getOptionsConfigurationData'() {
        given:

        def user = getUser()
        def locale = getLocale()
        def program = getProgram_appleSearch()
        def serverRequest = ProductSearchRequest.builder()
                .withRequestContext(CatalogRequestContext.forCatalogId("apple-hk-en"))
                .withQueryPhrase("")
                .build()
        def productSearchResponse = getProductSearchResponse_appleSearch()
        int totalOptionsSize = productSearchResponse.productSearchGroups.get('DEFAULT_GROUP').productSearchDocuments.get(0).getOptions().size()

        when:
        productServiceFactoryWrapper.getProductServiceClient() >> impl
        impl.search(_) >> productSearchResponse

        productServiceV3.relevantLanguageDao = relevantLanguageDao
        relevantLanguageDao.getByLocale(_) >> new RelevantLanguageEntity(id: 7, locale: "en_HK", relevantLanguage: "en_US")
        productServiceV3.productMapper = productMapper
        productServiceV3.getRelatedLanguage(_) >> new ArrayList<String>()
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories", "macbook-pro", "watch")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                PRODUCT_TILES_OPTIONS) >> Arrays.asList("language", "color", "swatchImageUrl")
        categoryConfigurationService.getCategoryRepository(_) >> categoryRepository
        categoryRepository.getCategoryDetailsByHierarchy(_) >> getCategory()
        def result = productServiceV3.appleSearch(serverRequest, locale, program, user, null, true)

        then:
        result.products.get(0).optionsConfigurationData != null
        result.products.get(0).optionsConfigurationData.size() == 1
        result.products.get(0).optionsConfigurationData.get("language").size() == 5
        totalOptionsSize != result.products.get(0).optionsConfigurationData.size()
    }

    def 'appleSearch - withProducts'() {
        given:

        def user = getUser()
        def locale = getLocale()
        def program = getProgram_appleSearch()
        def serverRequest = ProductSearchRequest.builder()
                .withRequestContext(CatalogRequestContext.forCatalogId("apple-hk-en"))
                .withQueryPhrase("")
                .build()
        def productSearchResponse = getProductSearchResponse_appleSearch()
        int totalOptionsSize = productSearchResponse.productSearchGroups.get('DEFAULT_GROUP').productSearchDocuments.get(0).getOptions().size()

        when:
        productServiceFactoryWrapper.getProductServiceClient() >> impl
        impl.search(_) >> productSearchResponse

        productServiceV3.relevantLanguageDao = relevantLanguageDao
        relevantLanguageDao.getByLocale(_) >> new RelevantLanguageEntity(id: 7, locale: "en_HK", relevantLanguage: "en_US")
        productServiceV3.productMapper = productMapper
        productServiceV3.getRelatedLanguage(_) >> new ArrayList<String>()
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                SLUGS_WITH_FACETS_FILTER) >> Arrays.asList("accessories", "macbook-pro", "watch")
        varProgramCatalogConfigService.getListOfValue(program.getCatalogId(), program.getVarId(), program.getProgramId(),
                PRODUCT_TILES_OPTIONS) >> Arrays.asList("language", "color", "swatchImageUrl")
        categoryConfigurationService.getCategoryRepository(_) >> categoryRepository
        categoryRepository.getCategoryDetailsByHierarchy(_) >> getCategory()
        def result = productServiceV3.appleSearch(serverRequest, locale, program, user, null, withProducts)

        then:
        result.products.size() == expectedResult

        where:
        withProducts    |   expectedResult
        true            |   1
        false           |   0
    }

    def 'giftPromoService - getGiftItem'() {

        given:
        def user = getUser()
        def qualifyingPsid = "PSID"
        def psids = Arrays.asList("P1", "P2")
        GiftItem giftItem1 = getGiftItem("P1", 50, "Percentage")
        GiftItem giftItem2 = getGiftItem("P1", 50, "Percentage")
        def giftItemList = Arrays.asList(giftItem1, giftItem2)
        def program = getProgram()
        def builder = new ProductDetailRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withPsid("psid")
                .withTargetCurrencies(Sets.newHashSet(CurrencyUnit.ofCountry("US")))
                .withVariations().withRealTimeInfo()
                .build()
        def req = Sets.newHashSet(request)

        when:
        detailTransformersHolder.getRequestTransformer().transform(_, null) >> req
        productServiceFactoryWrapper.getProductServiceClient() >> impl
        productServiceV3.applicationProperties = applicationProperties
        productServiceV3.engravingService = engravingService
        productServiceV3.giftPromoService = giftPromoService
        giftPromoService.getGiftPsids(_) >> psids
        giftPromoService.getGiftItemList(_, qualifyingPsid, program) >> giftItemList

        then:
        def products = productServiceV3.getGiftItem(user, program, qualifyingPsid, false)

        expect:
        products != null
        products.size() == 0
    }

    def 'getAppleProductDetail() gift items'() {

        given:
        def user = getUser()
        def program = getProgram()
        def builder = new ProductDetailRequest.Builder()
        def request = builder.withRequestContext(CatalogRequestContext.forCatalogId("apple-us-en"))
                .withPsid("psid")
                .withTargetCurrencies(Sets.newHashSet(CurrencyUnit.ofCountry("US")))
                .withVariations().withRealTimeInfo()
                .build()

        when:
        productServiceV3.productMapper = Mock(ProductMapper)
        detailTransformersHolder.getRequestTransformer().transform(_, null, program) >> Sets.newHashSet(request)
        productServiceFactoryWrapper.getProductServiceClient() >> impl
        impl.detail(_) >> new MultiProductDetailResponse(Arrays.asList(getModelProduct(false, false)), new HashMap<String,
                Throwable>())
        productServiceV3.applicationProperties = applicationProperties
        productServiceV3.varOrderManagerHolder = Mock(VarOrderManagerHolder)
        productServiceV3.productMapper.from(_,_, _, _, _, _) >> getProductMap()
        productServiceV3.varOrderManagerHolder.getVarOrderManager("SCOTIA") >> Mock(VAROrderManagerIF)
        productServiceV3.engravingService >> engravingService
        productServiceV3.engravingService.isEngraveEnabled(_, _, _, _) >> true
        productServiceV3.engravingService.getEngravingConfiguration(_, _, _,_) >> new Engrave()
        productServiceV3.giftPromoService >> giftPromoService
        productServiceV3.giftPromoService.getGiftItemList(_, _, _) >> Arrays.asList(
                getGiftItem("30001MWP22AM/A", 50, "Percentage"),
                getGiftItem("30001MWP22AZ/A", 75, "Percentage"))
        productServiceV3.giftPromoService.getGiftPsids(_) >> Arrays.asList("30001MWP22AM/A", "30001MWP22AZ/A")

        then:
        def products = productServiceV3.getAppleProductDetail("30001MWP22AM/A", program, false, user, true, true, true, true)

        expect:
        products != null
        products.getAddOns().availableGiftItems != null
    }

    def 'test getAffordableProductSearchRequestBuilder with Points Split pay'() {
        given:
        def ProductSearchRequest.Builder builder = new ProductSearchRequest.Builder()
        def catalofBuilder = CatalogRequestContext.builder()
        catalofBuilder.withCatalogId("catalogId")
        builder.withRequestContext(catalofBuilder.build())
        def program = getProgram()
        def pointsRange = new Integer[2]

        when:
        productServiceV3.applicationProperties = applicationProperties
        applicationProperties.getProperty("searchResultLimit") >> 100
        program.setPayments(getPaymentOptionPoints())
        program.setRedemptionOptions(getRedemptionOptionPoints())
        pointsRange[0] = 0
        pointsRange[1] = 100
        def request = productServiceV3.getAffordableProductSearchRequestBuilder(Locale.US, pointsRange, 10, 1, program).build()

        then:
        request.priceRangeFilters.size() == 1
        request.priceRangeFilters.asList().get(0).toString() == "[PNT 0..PNT 100]"
        request.getMaxPointsPercentage().get() == BigDecimal.valueOf(0.80)
        request.getPricingTier() == Optional.empty()
        request.getApplyEhf() == Optional.of(true)
    }

    def 'test getAffordableProductSearchRequestBuilder with Points Split pay and Max Cash amount and Pricing Tier'() {
        given:
        def ProductSearchRequest.Builder builder = new ProductSearchRequest.Builder()
        def catalofBuilder = CatalogRequestContext.builder()
        catalofBuilder.withCatalogId("catalogId")
        builder.withRequestContext(catalofBuilder.build())
        def program = getProgram()
        def pointsRange = new Integer[2]

        when:
        productServiceV3.applicationProperties = applicationProperties
        applicationProperties.getProperty("searchResultLimit") >> 100
        program.setPayments(getPaymentOptionPoints())
        program.setRedemptionOptions(getRedemptionOptionPointsAndMaxCash())
        program.setPricingTier("PricingTier")
        pointsRange[0] = 0
        pointsRange[1] = 100
        def request = productServiceV3.getAffordableProductSearchRequestBuilder(Locale.US, pointsRange, 10, 1, program).build()

        then:
        request.priceRangeFilters.size() == 1
        request.priceRangeFilters.asList().get(0).toString() == "[PNT 0..PNT 100]"
        request.getMaxPointsPercentage().get() == BigDecimal.valueOf(0.80)
        request.getMaxCashAmount() == Optional.of(Money.of(CurrencyUnit.USD, 300, RoundingMode.UNNECESSARY))
        request.getPricingTier() == Optional.of("PricingTier")
        request.getApplyEhf() == Optional.of(true)
    }

    def 'test getAffordableProductSearchRequestBuilder with Cash Only'() {
        given:
        def ProductSearchRequest.Builder builder = new ProductSearchRequest.Builder()
        def catalofBuilder = CatalogRequestContext.builder()
        catalofBuilder.withCatalogId("catalogId")
        builder.withRequestContext(catalofBuilder.build())
        def program = getProgram()

        when:
        productServiceV3.applicationProperties = applicationProperties
        applicationProperties.getProperty("searchResultLimit") >> 100
        program.setPayments(getPaymentOptionCash())
        def request = productServiceV3.getAffordableProductSearchRequestBuilder(Locale.US, null, 10, 1, program).build()

        then:
        request.priceRangeFilters.size() == 0
        request.getMaxPointsPercentage() == Optional.empty()
        request.getMaxCashAmount() == Optional.empty()
    }

    def getPaymentOptionPoints() {
        List<PaymentOption> paymentList = new ArrayList<>()
        PaymentOption paymentOption = new PaymentOption()
        paymentOption.setPaymentOption(CommonConstants.PaymentOption.POINTS.name())

        paymentList.add(paymentOption)
        return paymentList
    }

    def getPaymentOptionCash() {
        List<PaymentOption> paymentList = new ArrayList<>()
        PaymentOption paymentOption = new PaymentOption()
        paymentOption.setPaymentOption(CommonConstants.PaymentOption.CASH.name())

        paymentList.add(paymentOption)
        return paymentList
    }

    def getRedemptionOptionPoints() {
        Map<String, List<VarProgramRedemptionOption>> redemptionMap = new HashMap<>()

        def redemptionSplitPay = new ArrayList<>()
        redemptionSplitPay.add(VarProgramRedemptionOption.builder().withLimitType("percentage").withPaymentMaxLimit(80).build())
        redemptionMap.put(PaymentOptions.SPLITPAY.getPaymentOption(), redemptionSplitPay)

        return redemptionMap
    }

    def getRedemptionOptionPointsAndMaxCash() {
        Map<String, List<VarProgramRedemptionOption>> redemptionMap = new HashMap<>()

        def redemptionSplitPay = new ArrayList<>()
        redemptionSplitPay.add(VarProgramRedemptionOption.builder().withLimitType("percentage").withPaymentMaxLimit(80).build())
        redemptionSplitPay.add(VarProgramRedemptionOption.builder().withLimitType("dollar").withPaymentMaxLimit(300).build())
        redemptionMap.put(PaymentOptions.SPLITPAY.getPaymentOption(), redemptionSplitPay)

        return redemptionMap
    }

    def getUser() {
        def user = new User()
        user.varId = "HK"
        user.programId = "b2s_qa_only"
        user.locale = getLocale()
        return user
    }

    def getLocale() {
        return new Locale("en", "HK")
    }

    def getProgram_appleSearch() {
        Program program = new Program()
        program.varId = "HK"
        program.programId = "b2s_qa_only"
        program.catalogId = "apple-hk-en"
        Map<String, Object> configs = new HashMap<>()
        configs.put("catalog_id", "apple-hk-en")
        program.setConfig(configs)
        program.targetCurrency = CurrencyUnit.ofCountry("HK")
        return program
    }

    def getProductSearchResponse_appleSearch() {

        def document1 = getVariationSearchDocument("30001MX3L2ZA/A", "107D01B612CC49", "MX3L2ZA/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - Chinese (Zhuyin)", "zh_HK", "Chinese (Zhuyin)")

        def document2 = getVariationSearchDocument("30001MX3L2MO/A", "10FC7EDBC11A22", "MX3L2MO/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - US English", "en_US", "US English")

        def document3 = getVariationSearchDocument("30001MX3L2JX/A", "106A7DF5219589", "MX3L2JX/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - Japanese", "ja_JP", "Japanese")

        def document4 = getVariationSearchDocument("30001MX3L2CV/A", "10AA878D81C591", "MX3L2CV/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - Chinese (Pinyin)", "zh_CN", "Chinese (Pinyin)")

        def document5 = getVariationSearchDocument("30001MX3L2BX/A", "106B5940E7FF6D", "MX3L2BX/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - British English", "en_GB", "British English")

        def productSearchDocument = ProductSearchDocument.builder()
                .withPsid("30001MX3L2BX/A")
                .withBsin("106B5940E7FF6D")
                .withSku("MX3L2BX/A")
                .withName("Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - British English")
                .withSourceCountry("HKG")
                .withMerchant("apple")
                .withProductType(ProductType.MERCHANDISE)
                .withProductImageUrls(ProductImageUrls.builder().build())
                .withShippingInformation(ShippingInformation.builder().withDeliveryInformation(DeliveryInformation.builder()
                        .withSource(Source.builder().withCountry("HKG").withSupplier("apple").build()).build()).build())
                .withPricingInformation(PricingInformation.builder().withOriginalPriceInfo(OriginalPriceInfo.builder()
                        .withBasePrice(Money.of(CurrencyUnit.ofCountry("HK"), 1249.00, RoundingMode.UNNECESSARY))
                        .withShippingCost(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY))
                        .withSupplierSalesTax(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY)).build())
                        .withCalculatePriceInfos(getCalculatedPriceInfo(false)).build())
                .withAvailabilityInformation(AvailabilityInformation.builder()
                        .withAvailabilityStatus(AvailabilityStatus.IN_STOCK).withAvailabilityMessage("1 business day [24]").build())
                .withOptions(getOptionMap("en_GB", "British English", "Language"))
                .withVariations(Arrays.asList(document1, document2, document3, document4, document5))
                .withHasVariations(true)
                .withCategorySlugs(Arrays.asList("all-accessories-cases-protection"))
                .withShortDescription("Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - British English").build()

        def productSearchDocumentGroup = ProductSearchDocumentGroup.builder()
                .withProductSearchDocuments(Arrays.asList(productSearchDocument)).build()

        def productSearchResponse = new ProductSearchResponse(Collections.singletonMap("DEFAULT_GROUP",
                productSearchDocumentGroup), null, null)
        return productSearchResponse
    }

    def getVariationSearchDocument(psid, bsin, sku, name, key, value) {

        return VariationSearchDocument.builder()
                .withPsid(psid)
                .withBsin(bsin)
                .withSku(sku)
                .withName(name)
                .withProductImageUrls(ProductImageUrls.builder().build())
                .withShippingInformation(ShippingInformation.builder().withDeliveryInformation(DeliveryInformation.builder()
                        .withSource(Source.builder().withCountry("HKG").withSupplier("apple").build()).build()).build())
                .withPricingInformation(PricingInformation.builder().withOriginalPriceInfo(OriginalPriceInfo.builder()
                        .withBasePrice(Money.of(CurrencyUnit.ofCountry("HK"), 1249.00, RoundingMode.UNNECESSARY))
                        .withShippingCost(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY))
                        .withSupplierSalesTax(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY)).build())
                        .withCalculatePriceInfos(getCalculatedPriceInfo(false)).build())
                .withAvailabilityInformation(AvailabilityInformation.builder()
                        .withAvailabilityStatus(AvailabilityStatus.IN_STOCK).withAvailabilityMessage("1 business day [24]").build())
                .withOptions(getOptionMap(key, value, "Language"))
                .withShortDescription(name).build()
    }

    def getOptionMap(key, value, label) {
        def languageOption = com.b2s.service.product.common.domain.response.Option.builder()
                .withKey(key)
                .withValue(value)
                .withLabel(label).build()

        def caseSizeOption = com.b2s.service.product.common.domain.response.Option.builder()
                .withKey('caseSize')
                .withValue('Case Size')
                .withLabel('Case Size').build()

        return ['language': languageOption, 'caseSize': caseSizeOption]
    }

    def getCalculatedPriceInfo(boolean mockSplitTenderEstimate) {
        Set<CalculatedPriceInfo> calculatedPriceInfoSet = new HashSet<>()
        calculatedPriceInfoSet.add(
                CalculatedPriceInfo.builder()
                        .withDisplayPrice(Money.of(CurrencyUnit.of("HKD"), 1149.00, RoundingMode.UNNECESSARY))
                        .withBasePrice(Money.of(CurrencyUnit.of("HKD"), 1149.00, RoundingMode.UNNECESSARY))
                        .withShippingCost(Money.of(CurrencyUnit.of("HKD"), 0.00, RoundingMode.UNNECESSARY))
                        .withSupplierSalesTax(Money.of(CurrencyUnit.of("HKD"), 0.00, RoundingMode.UNNECESSARY))
                        .withUnpromotedDisplayPrice(Money.of(CurrencyUnit.of("HKD"), 1149.00, RoundingMode.UNNECESSARY))
                        .withPriceType(PriceType.SUPPLIER)
                        .build())

        CalculatedPriceInfo.Builder calculatedPriceInfoBuilder = CalculatedPriceInfo.builder()
                .withDisplayPrice(Money.of(CurrencyUnit.of("PNT"), 287300.00, RoundingMode.UNNECESSARY))
                .withBasePrice(Money.of(CurrencyUnit.of("PNT"), 287300.00, RoundingMode.UNNECESSARY))
                .withShippingCost(Money.of(CurrencyUnit.of("PNT"), 0.00, RoundingMode.UNNECESSARY))
                .withSupplierSalesTax(Money.of(CurrencyUnit.of("PNT"), 0.00, RoundingMode.UNNECESSARY))
                .withUnpromotedDisplayPrice(Money.of(CurrencyUnit.of("PNT"), 287300.00, RoundingMode.UNNECESSARY))
                .withPriceType(PriceType.SUPPLIER)

        if(mockSplitTenderEstimate){
            SplitTenderEstimate splitTenderEstimate = SplitTenderEstimate.builder()
                    .withCcBuyInPointsLimit(1234)
                    .withCcBuyInAmountLimit(Money.parse("USD 12.34"))
                    .withRemainingPointPrice(8765)
                    .build()
            calculatedPriceInfoBuilder.withSplitTenderEstimate(splitTenderEstimate)
        }
        calculatedPriceInfoSet.add(calculatedPriceInfoBuilder.build())

        return calculatedPriceInfoSet
    }

    def getModelProduct(boolean mockRelatedProducts, boolean mockSplitTenderEstimate) {
        def builder = new Product.Builder()
        List<String> relatedProductsPSids = new ArrayList<>()
        if(mockRelatedProducts) {
            relatedProductsPSids.add("30001MV7N2AM/A")
        }
        Set <ServicePlanInfo> servicePlanInfos = new HashSet<>()
        servicePlanInfos.add(ServicePlanInfo.builder()
                        .withPsid("329393/M")
                        .withName("iphone theft").build())
        return new Product(builder.withName("Product name")
                .withPsid("psid")
                .withBsin("Bsin")
                .withSku("SKU")
                .withCategories(getCategoryList())
                .withPricingInformation(PricingInformation.builder().withOriginalPriceInfo(OriginalPriceInfo.builder()
                        .withBasePrice(Money.of(CurrencyUnit.ofCountry("HK"), 1249.00, RoundingMode.UNNECESSARY))
                        .withShippingCost(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY))
                        .withSupplierSalesTax(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY)).build())
                        .withCalculatePriceInfos(getCalculatedPriceInfo(mockSplitTenderEstimate)).build())
                .withAvailabilityInformation(AvailabilityInformation.builder()
                        .withAvailabilityStatus(AvailabilityStatus.IN_STOCK)
                        .withAvailable(true)
                        .withAvailabilityMessage("1 business day [24]").build())
                .withImageUrls(ProductImageUrls.builder().build())
                .withTotalReviews(3)
                .withStatus(ProductStatus.ACTIVE)
                .withProductType(ProductType.MERCHANDISE)
                .withMerchant("")
                .withModifiedDatetime(DateTime.now())
                .withImportDatetime(DateTime.now())
                .withServicePlanInfo(servicePlanInfos)
                .withRelatedProducts(relatedProductsPSids)
                .withShippingInformation(ShippingInformation.builder().withDeliveryInformation(DeliveryInformation.builder()
                        .withSource(Source.builder().withCountry("HKG").withSupplier("apple").build()).build()).build())
        )
    }

    def getCategoryList() {
        def categoryList = new ArrayList()
        categoryList.add(getCategory())
        return categoryList
    }

    def getCategory() {
        return new Category("all-accessories-cases-protection", "Cases & Protection")
    }

    def Map<String, com.b2s.rewards.apple.model.Product> getProductMap() {
        Map<String, com.b2s.rewards.apple.model.Product> productMap = new HashMap<>()
        String psid = "30001MWP22AM/A"
        com.b2s.rewards.apple.model.Product product = new com.b2s.rewards.apple.model.Product()
        product.setPsid(psid);
        productMap.put(psid, product)
        product = new com.b2s.rewards.apple.model.Product()
        psid = "30001MWTK2LL/A"
        product.setPsid(psid)
        productMap.put(psid, product)
        return productMap;
    }

    def getGiftItem(productId, discount, discountType){
        GiftItem giftItem = new GiftItem()
        giftItem.setProductId(productId)
        giftItem.setDiscount(discount)
        giftItem.setDiscountType(discountType)
        return giftItem
    }
}
