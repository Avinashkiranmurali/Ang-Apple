package com.b2s.common.services

import com.b2s.apple.entity.CarouselEntity
import com.b2s.apple.entity.CarouselTemplateEntity
import com.b2s.apple.model.CarouselConfig
import com.b2s.apple.services.AppSessionInfo
import com.b2s.apple.services.CarouselHolder
import com.b2s.apple.services.CarouselService
import com.b2s.apple.services.RecentlyViewedProductsService
import com.b2s.common.services.exception.ServiceException
import com.b2s.rewards.apple.dao.CarouselDao
import com.b2s.rewards.apple.dao.CarouselTemplateDao
import com.b2s.rewards.apple.model.Cart
import com.b2s.rewards.apple.model.CartItem
import com.b2s.rewards.apple.model.Category
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.model.Product
import com.b2s.shop.common.User
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.MapUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static com.b2s.rewards.common.util.CommonConstants.CarouselType
import static com.b2s.rewards.common.util.CommonConstants.CarouselType.RECENTLY_VIEWED
import static com.b2s.rewards.common.util.CommonConstants.CarouselType.AFFORDABLE_PRODUCT
import static com.b2s.rewards.common.util.CommonConstants.SHOW_ONLY_ON_EMPTY_CART

@Unroll
class CarouselServiceSpec extends Specification {

    final carouselDao = Mock(CarouselDao)
    final carouselTemplateDao = Mock(CarouselTemplateDao)
    final recentlyViewedProductsService = Mock(RecentlyViewedProductsService)
    final carouselHolder = Mock(CarouselHolder)
    final appSessionInfo = Mock(AppSessionInfo)

    @Subject
    final carouselService = new CarouselService(carouselDao: carouselDao, carouselTemplateDao: carouselTemplateDao,
            carouselHolder: carouselHolder, appSessionInfo: appSessionInfo)


    def 'test setProgramCarouselConfig with Var Program combination'() {
        given:
        def program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)
        carouselDao.getActiveCarouselEntities(_, _) >> getCarouselEntities(varId, programId)

        when:
        carouselService.setProgramCarouselConfig(program)

        then:
        def configs = program.getCarouselConfig()
        def config = getCarouselConfig(configs, page, type)
        def pages = program.getCarouselPages()
        config.templateName == templateName
        Objects.nonNull(pages)

        where:
        varId | programId     || page    || type               || templateName
        'UA'  | 'MP'          || 'bag'   || RECENTLY_VIEWED    || 'defRVbag_pdpTemplate'
        'UA'  | 'MP'          || 'pdp'   || RECENTLY_VIEWED    || 'defRVbag_pdpTemplate'
        'UA'  | 'MP'          || 'pcp'   || RECENTLY_VIEWED    || 'defRVpcpTemplate'
        'UA'  | 'MP'          || 'pcp'   || AFFORDABLE_PRODUCT || 'defAPpcpTemplate'
        'UA'  | 'MP'          || 'store' || AFFORDABLE_PRODUCT || 'defAPstoreTemplate'

        'RBC' | 'AIB'         || 'bag'   || RECENTLY_VIEWED    || 'varRVbag_pcpTemplate'
        'RBC' | 'AIB'         || 'pcp'   || RECENTLY_VIEWED    || 'varRVbag_pcpTemplate'
        'RBC' | 'AIB'         || 'pdp'   || RECENTLY_VIEWED    || 'varRVpdpTemplate'
        'RBC' | 'AIB'         || 'pcp'   || AFFORDABLE_PRODUCT || 'varAPpcpTemplate'
        'RBC' | 'AIB'         || 'pdp'   || AFFORDABLE_PRODUCT || 'varAPpdpTemplate'

        'RBC' | 'b2s_qa_only' || 'bag'   || RECENTLY_VIEWED    || 'pgmRVbag_storeTemplate'
        'RBC' | 'b2s_qa_only' || 'store' || RECENTLY_VIEWED    || 'pgmRVbag_storeTemplate'
        'RBC' | 'b2s_qa_only' || 'pdp'   || RECENTLY_VIEWED    || 'pgmRVpdpTemplate'
        'RBC' | 'b2s_qa_only' || 'pcp'   || AFFORDABLE_PRODUCT || 'pgmAPpcpTemplate'
        'RBC' | 'b2s_qa_only' || 'rev'   || AFFORDABLE_PRODUCT || 'pgmAPrevTemplate'

        'RBC' | 'b2s_qa_only' || 'pcp'   || RECENTLY_VIEWED    || 'varRVbag_pcpTemplate'
        'RBC' | 'b2s_qa_only' || 'store' || AFFORDABLE_PRODUCT || 'defAPstoreTemplate'
    }

    def getCarouselEntities(String varId, String programId){
        List<CarouselEntity> matchingConfigs = new ArrayList<>()
        for (CarouselEntity config : getAllEntities()) {
            if (config.getProgramId().equalsIgnoreCase(programId)) {
                if (config.getVarId().equalsIgnoreCase(varId)) {
                    matchingConfigs.add(config)
                } else if ("-1".equalsIgnoreCase(config.getVarId())) {
                    matchingConfigs.add(config)
                }
            }else if ("-1".equalsIgnoreCase(config.getProgramId())) {
                if (config.getVarId().equalsIgnoreCase(varId)) {
                    matchingConfigs.add(config)
                } else if ("-1".equalsIgnoreCase(config.getVarId())) {
                    matchingConfigs.add(config)
                }
            }
        }
        return matchingConfigs
    }

    List<CarouselEntity> getAllEntities(){
        def entities = new ArrayList()
        entities.add(getCarouselEntity('-1', '-1', 'recentlyViewed', 'bag,pdp', 'excludeProgram', 'defRVbag_pdpTemplate'))
        entities.add(getCarouselEntity('-1', '-1', 'recentlyViewed', 'pcp', 'b2s_qa_only', 'defRVpcpTemplate'))
        entities.add(getCarouselEntity('-1', '-1', 'affordableProduct', 'pcp', 'excludeProgram', 'defAPpcpTemplate'))
        entities.add(getCarouselEntity('-1', '-1', 'affordableProduct', 'store', 'excludeProgram', 'defAPstoreTemplate'))

        entities.add(getCarouselEntity('RBC', '-1', 'recentlyViewed', 'bag,pcp', null, 'varRVbag_pcpTemplate'))
        entities.add(getCarouselEntity('RBC', '-1', 'recentlyViewed', 'pdp', null, 'varRVpdpTemplate'))
        entities.add(getCarouselEntity('RBC', '-1', 'affordableProduct', 'pcp', null, 'varAPpcpTemplate'))
        entities.add(getCarouselEntity('RBC', '-1', 'affordableProduct', 'pdp', null, 'varAPpdpTemplate'))

        entities.add(getCarouselEntity('RBC', 'b2s_qa_only', 'recentlyViewed', 'bag,store', null, 'pgmRVbag_storeTemplate'))
        entities.add(getCarouselEntity('RBC', 'b2s_qa_only', 'recentlyViewed', 'pdp', null, 'pgmRVpdpTemplate'))
        entities.add(getCarouselEntity('RBC', 'b2s_qa_only', 'affordableProduct', 'pcp', null, 'pgmAPpcpTemplate'))
        entities.add(getCarouselEntity('RBC', 'b2s_qa_only', 'affordableProduct', 'rev', null, 'pgmAPrevTemplate'))
        return entities
    }

    def 'test getCarouselResponse with showOnlyOnEmptyCart'() {
        given:
        def templateName = 'RVdefTemplate'
        def varId = 'RBC'
        def programId = 'b2s_qa_only'
        def userId = '123'

        carouselTemplateDao.getCarouselTemplates(templateName) >> getCarouselTemplateEntitiesBasedOnShowOnlyOnEmptyCart(showOnlyOnEmptyCart)
        def user = new User()
        user.setVarId(varId)
        user.setProgramId(programId)
        user.setUserId(userId)
        def program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)
        program.setCarouselConfig(getCarouselByPage(page, templateName, RECENTLY_VIEWED))
        carouselHolder.getCarouselService(_) >> recentlyViewedProductsService
        recentlyViewedProductsService.getCarouselProducts(user, program, null) >> getMultiProduct()
        carouselService.carouselService = carouselService
        appSessionInfo.getSessionCart() >> cartItems

        when:
        def result = carouselService.getCarouselResponse(user, program, page)

        then:
        Objects.nonNull(result)
        result.size() == 1
        result.getAt(0).getName() == RECENTLY_VIEWED.getValue()
        showCarousel == CollectionUtils.isNotEmpty(result.getAt(0).getProducts())

        where:
        showOnlyOnEmptyCart | cartItems           | page  || showCarousel
        'true'              | containsCartItems() | 'bag' || false
        'true'              | getEmptyCart()      | 'bag' || true
        'true'              | containsCartItems() | 'pcp' || true
        'true'              | getEmptyCart()      | 'pcp' || true

        'false'             | containsCartItems() | 'bag' || true
        'false'             | getEmptyCart()      | 'bag' || true
        'inActive'          | containsCartItems() | 'bag' || true
        'inActive'          | getEmptyCart()      | 'bag' || true
    }

    def getEmptyCart(){
        return new Cart()
    }

    def containsCartItems(){
        def cart = new Cart()
        cart.cartItems.add(new CartItem())
        return cart
    }

    def getCarouselTemplateEntitiesBasedOnShowOnlyOnEmptyCart(String showOnlyOnEmptyCart) {
        def entities = getCarouselTemplateEntities()
        if ("true".equalsIgnoreCase(showOnlyOnEmptyCart)) {
            entities.add(getCarouselTemplateEntity('RVdefTemplate', SHOW_ONLY_ON_EMPTY_CART, 'true'))
        }
        if ("false".equalsIgnoreCase(showOnlyOnEmptyCart)) {
            entities.add(getCarouselTemplateEntity('RVdefTemplate', SHOW_ONLY_ON_EMPTY_CART, 'false'))
        }
        return entities
    }

    def 'test setProgramCarouselConfig with different Entities combination'() {
        given:
        carouselDao.getActiveCarouselEntities(_, _) >> carouselEntities
        def program = new Program()
        program.setVarId('RBC')
        program.setProgramId('b2s_qa_only')

        when:
        carouselService.setProgramCarouselConfig(program)

        then:
        def configs = program.getCarouselConfig()
        def config = getCarouselConfig(configs, page, type)
        def pages = program.getCarouselPages()
        config.getProgramExclusion().size() == programExclusionSize
        config.getDisplayPages().size() == displayPagesSize
        config.templateName == templateName
        Objects.nonNull(pages)

        where:
        carouselEntities               | page  | type               || programExclusionSize || displayPagesSize || templateName
        onlyDefaultCarousels()         | 'pcp' | AFFORDABLE_PRODUCT || 1                    || 1                || 'APdefTemplate'
        onlyDefaultCarousels()         | 'pcp' | RECENTLY_VIEWED    || 1                    || 1                || 'RVdefTemplate'
        onlyVARGenericCarousels()      | 'pcp' | AFFORDABLE_PRODUCT || 0                    || 2                || 'APvarTemplate'
        onlyVARGenericCarousels()      | 'pcp' | RECENTLY_VIEWED    || 0                    || 2                || 'RVvarTemplate'
        onlyProgramSpecificCarousels() | 'pdp' | RECENTLY_VIEWED    || 0                    || 3                || 'RVpgmTemplate'
        onlyProgramSpecificCarousels() | 'pdp' | AFFORDABLE_PRODUCT || 0                    || 4                || 'APpgmTemplate'
        varGenericCarousels()          | 'pcp' | RECENTLY_VIEWED    || 0                    || 2                || 'RVvarTemplate'
        varGenericCarousels()          | 'pcp' | AFFORDABLE_PRODUCT || 0                    || 2                || 'APvarTemplate'
        varProgramCarousels()          | 'pdp' | RECENTLY_VIEWED    || 0                    || 3                || 'RVpgmTemplate'
        varProgramCarousels()          | 'pdp' | AFFORDABLE_PRODUCT || 0                    || 4                || 'APpgmTemplate'
        allCarousels()                 | 'pdp' | RECENTLY_VIEWED    || 0                    || 3                || 'RVpgmTemplate'
        allCarousels()                 | 'pdp' | AFFORDABLE_PRODUCT || 0                    || 4                || 'APpgmTemplate'
    }

    def 'test setProgramCarouselConfig Empty'() {
        given:
        carouselDao.getActiveCarouselEntities(_, _) >> []
        def program = new Program()
        program.setVarId('UA')
        program.setProgramId('MP')

        when:
        carouselService.setProgramCarouselConfig(program)

        then:
        MapUtils.isEmpty(program.getCarouselConfig())
        CollectionUtils.isEmpty(program.getCarouselPages())
    }

    def 'test getCarouselResponse'() {
        given:
        def templateName = 'RVdefTemplate'
        def varId = 'RBC'
        def programId = 'b2s_qa_only'
        def userId = '123'
        def page = 'pcp'
        carouselTemplateDao.getCarouselTemplates(templateName) >> getCarouselTemplateEntities()
        def user = new User()
        user.setVarId(varId)
        user.setProgramId(programId)
        user.setUserId(userId)
        def program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)
        program.setCarouselConfig(getCarouselByPage(page, templateName, RECENTLY_VIEWED))
        carouselHolder.getCarouselService(_) >> recentlyViewedProductsService
        recentlyViewedProductsService.getCarouselProducts(user, program, null) >> getMultiProduct()
        carouselService.carouselService = carouselService

        when:
        def result = carouselService.getCarouselResponse(user, program, page)

        then:
        Objects.nonNull(result)
        result.size() == 1
        result.getAt(0).getName() == RECENTLY_VIEWED.getValue()
        Objects.nonNull(result.getAt(0).getProducts())
        result.getAt(0).getProducts().size() == 1
        result.getAt(0).getProducts().get(0).getName() == 'Product name'
        Objects.nonNull(result.getAt(0).getConfig())
        result.getAt(0).getConfig().size() == 1
    }

    def 'test getCarouselResponse without Carousel template'() {
        given:
        def templateName = 'RVdefTemplate'
        def varId = 'RBC'
        def programId = 'b2s_qa_only'
        def userId = '123'
        def page = 'pcp'
        carouselTemplateDao.getCarouselTemplates(templateName) >> []
        def user = new User()
        user.setVarId(varId)
        user.setProgramId(programId)
        user.setUserId(userId)
        def program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)
        program.setCarouselConfig(getCarouselByPage(page, templateName, RECENTLY_VIEWED))
        carouselHolder.getCarouselService(_) >> recentlyViewedProductsService
        recentlyViewedProductsService.getCarouselProducts(user, program, null) >> getMultiProduct()
        carouselService.carouselService = carouselService

        when:
        def result = carouselService.getCarouselResponse(user, program, page)

        then:
        Objects.nonNull(result)
        result.size() == 1
        result.getAt(0).getName() == RECENTLY_VIEWED.getValue()
        Objects.nonNull(result.getAt(0).getProducts())
        result.getAt(0).getProducts().size() == 1
        result.getAt(0).getProducts().getAt(0).getName() == 'Product name'
        MapUtils.isEmpty(result.getAt(0).getConfig())
    }

    def 'test getCarouselResponse without products'() {
        given:
        def templateName = 'RVdefTemplate'
        def varId = 'RBC'
        def programId = 'b2s_qa_only'
        def userId = '123'
        def page = 'pcp'
        carouselTemplateDao.getCarouselTemplates(templateName) >> getCarouselTemplateEntities()
        def user = new User()
        user.setVarId(varId)
        user.setProgramId(programId)
        user.setUserId(userId)
        def program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)
        program.setCarouselConfig(getCarouselByPage(page, templateName, RECENTLY_VIEWED))
        carouselHolder.getCarouselService(RECENTLY_VIEWED).getCarouselProducts(user, program) >> getMultiProduct()
        carouselService.carouselService = carouselService

        when:
        def result = carouselService.getCarouselResponse(user, program, page)

        then:
        Objects.nonNull(result)
        result.size() == 1
        result.getAt(0).getName() == RECENTLY_VIEWED.getValue()
        Objects.nonNull(result.getAt(0).getConfig())
        result.getAt(0).getConfig().size() == 1
        CollectionUtils.isEmpty(result.getAt(0).getProducts())
    }

    def 'test getCarouselResponse handle buildCarouselResponse Exception'() {
        given:
        def templateName = 'RVdefTemplate'
        def varId = 'RBC'
        def programId = 'b2s_qa_only'
        def userId = '123'
        def page = 'pcp'
        carouselTemplateDao.getCarouselTemplates(templateName) >> getCarouselTemplateEntities()
        def user = new User()
        user.setVarId(varId)
        user.setProgramId(programId)
        user.setUserId(userId)
        def program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)
        program.setCarouselConfig(getCarouselByPage(page, templateName, null))

        when:
        def result = carouselService.getCarouselResponse(user, program, page)

        then:
        CollectionUtils.isEmpty(result)
    }

    def 'test getCarouselResponse handle Exception'() {
        given:
        def templateName = 'RVdefTemplate'
        def varId = 'RBC'
        def programId = 'b2s_qa_only'
        def userId = '123'
        def page = 'pcp'
        carouselTemplateDao.getCarouselTemplates(templateName) >> getCarouselTemplateEntities()
        def user = new User()
        user.setVarId(varId)
        user.setProgramId(programId)
        user.setUserId(userId)
        def program = new Program()
        program.setVarId(varId)
        program.setProgramId(programId)
        program.setCarouselConfig(null)
        carouselHolder.getCarouselService(RECENTLY_VIEWED).getCarouselProducts(user, program) >> getMultiProduct()

        when:
        def result = carouselService.getCarouselResponse(user, program, page)

        then:
        ServiceException ex = thrown()
        ex.message.contains('Service execution exception')
    }

    def onlyDefaultCarousels(){
        return [getDefaultRecentlyViewedCarouselEntity(),
                getDefaultSuggestedProductCarouselEntity()]
    }

    def onlyVARGenericCarousels(){
        return [getVarGenericSuggestedProductCarouselEntity(),
                getVarGenericRecentlyViewedCarouselEntity()]
    }

    def onlyProgramSpecificCarousels(){
        return [getVarProgramSpecificRecentlyViewedCarouselEntity(),
                getVarProgramSpecificSuggestedProductCarouselEntity()]
    }

    def varGenericCarousels(){
        return [getVarGenericRecentlyViewedCarouselEntity(), getDefaultRecentlyViewedCarouselEntity(),
                getDefaultSuggestedProductCarouselEntity(), getVarGenericSuggestedProductCarouselEntity()]
    }

    def varProgramCarousels(){
        return [getVarGenericRecentlyViewedCarouselEntity(),
                getVarProgramSpecificSuggestedProductCarouselEntity(),
                getVarProgramSpecificRecentlyViewedCarouselEntity(),
                getVarGenericSuggestedProductCarouselEntity()]
    }

    def allCarousels(){
        return [getVarProgramSpecificRecentlyViewedCarouselEntity(),
                getVarGenericRecentlyViewedCarouselEntity(),
                getDefaultRecentlyViewedCarouselEntity(),
                getDefaultSuggestedProductCarouselEntity(),
                getVarGenericSuggestedProductCarouselEntity(),
                getVarProgramSpecificSuggestedProductCarouselEntity()]
    }

    def getDefaultRecentlyViewedCarouselEntity() {
        return getCarouselEntity('-1', '-1', 'recentlyViewed', 'pcp', 'excludeProgram', 'RVdefTemplate')
    }

    def getDefaultSuggestedProductCarouselEntity() {
        return getCarouselEntity('-1', '-1', 'affordableProduct', 'pcp', 'excludeProgram', 'APdefTemplate')
    }

    def getVarGenericRecentlyViewedCarouselEntity() {
        return getCarouselEntity('RBC', '-1', 'recentlyViewed', 'store,pcp', null, 'RVvarTemplate')
    }

    def getVarGenericSuggestedProductCarouselEntity() {
        return getCarouselEntity('RBC', '-1', 'affordableProduct', 'store,pcp', null, 'APvarTemplate')
    }

    def getVarProgramSpecificRecentlyViewedCarouselEntity() {
        return getCarouselEntity('RBC', 'b2s_qa_only', 'recentlyViewed', 'bag,pdp,pcp', null, 'RVpgmTemplate')
    }

    def getVarProgramSpecificSuggestedProductCarouselEntity() {
        return getCarouselEntity('RBC', 'b2s_qa_only', 'affordableProduct', 'bag,pdp,pcp,store', null, 'APpgmTemplate')
    }

    def getCarouselConfig(Map<String, Map<CarouselType, CarouselConfig>> carouselConfigMap, String page, CarouselType carouselType){
        if(MapUtils.isNotEmpty(carouselConfigMap)){
            Map<CarouselType, CarouselConfig> carouselConfigs = carouselConfigMap.get(page)
            if(MapUtils.isNotEmpty(carouselConfigs)){
                return carouselConfigs.get(carouselType)
            }
        }
    }

    def getCarouselTemplateEntities() {
        def carouselTemplateEntities = new ArrayList<CarouselTemplateEntity>()
        def carouselTemplateEntity = getCarouselTemplateEntity('RVdefTemplate', 'backgroundColor', '#F3F4F5')
        carouselTemplateEntities.add(carouselTemplateEntity)
        return carouselTemplateEntities
    }

    def getCarouselTemplateEntity(String templateName, String name, String value) {
        return new CarouselTemplateEntity(templateName: templateName, name: name, value: value, active: true,
                modifiedBy: 'User', modifiedDate: new Date())
    }

    def getCarouselEntity(String varId, String programId, String type, String displayPages, String programExclusion,
                          String templateName) {
        return new CarouselEntity(varId: varId, programId: programId, type: type, displayPages: displayPages,
                programExclusion: programExclusion, templateName: templateName, active: true, modifiedBy: 'User',
                modifiedDate: new Date())
    }

    def getCarouselByPage(String page, String templateName, CarouselType carouselType){
        Map<String, Map<CarouselType, CarouselConfig>> carouselConfigsBasedOnPage = new HashMap<>()
        Map<CarouselType, CarouselConfig> carouselConfigs = new HashMap<>()
        carouselConfigs.put(carouselType, getCarouselConfig(page, templateName, carouselType))
        carouselConfigsBasedOnPage.put(page, carouselConfigs)
        return carouselConfigsBasedOnPage
    }

    def getCarouselConfig(String page, String templateName, CarouselType carouselType){
        return CarouselConfig.builder()
                .withTemplateName(templateName)
                .withPage(page)
                .withType(carouselType)
                .withProgramExclusion(['excludeProgram'])
                .withDisplayPages(['bag',page,'pcp','store']).build()
    }

    def getMultiProduct() {
        return [getProduct()]
    }

    def getProduct(){
        def product = new Product()
        product.psid = '30001MHRE3VC/A'
        product.name = 'Product name'
        product.available = true
        def categories = new ArrayList<Category>()
        def category = new Category()
        category.depth = 1
        category.slug = "ipad"
        category.name = "iPad Pro"
        categories.add(category)
        product.setCategory(categories)
        return product
    }
}