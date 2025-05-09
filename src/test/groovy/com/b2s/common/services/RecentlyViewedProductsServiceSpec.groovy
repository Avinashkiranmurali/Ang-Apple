package com.b2s.common.services

import com.b2s.apple.entity.RecentlyViewedProductsEntity
import com.b2s.apple.mapper.RecentlyViewedProductsMapper
import com.b2s.apple.model.CarouselConfig
import com.b2s.apple.services.RecentlyViewedProductsService
import com.b2s.common.services.productservice.ProductServiceV3
import com.b2s.rewards.apple.dao.RecentlyViewedProductsDao
import com.b2s.rewards.apple.model.Category
import com.b2s.rewards.apple.model.Product
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.model.RecentlyViewedProduct
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import org.apache.commons.collections.CollectionUtils
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

import static com.b2s.rewards.common.util.CommonConstants.BAG
import static com.b2s.rewards.common.util.CommonConstants.CarouselType.RECENTLY_VIEWED

/**
 * Created by ssundaramoorthy on 8/31/2021.
 * Unit test specifications for userRecentlyViewedProductsService.
 */
class RecentlyViewedProductsServiceSpec extends Specification {

    final recentlyViewedProductsDao = Mock(RecentlyViewedProductsDao)
    final productsMapper = new RecentlyViewedProductsMapper()
    final productServiceV3 = Mock(ProductServiceV3)
    def applicationProperties = Mock(Properties)

    @Subject
    final recentlyViewedProductsService = new RecentlyViewedProductsService(
            recentlyViewedProductsDao: recentlyViewedProductsDao,
            recentlyViewedProductsMapper: productsMapper,
            productServiceV3: productServiceV3,
            applicationProperties: applicationProperties)

    final mockMvc = MockMvcBuilders.standaloneSetup(recentlyViewedProductsService).build()
    Date currentDate = new Date()
    User user = getUser()
    Program program = getProgram()

    def 'modifyUserRecentlyViewedProducts -- Insert New Item'() {

        List<RecentlyViewedProductsEntity> resultList = new ArrayList<>()
        given:

        applicationProperties.getProperty(CommonConstants.CAROUSEL_RECENTLY_VIEWED_MAX_COUNT) >> "20"
        recentlyViewedProductsDao.getProducts(_, _, _) >> null
        recentlyViewedProductsDao.insert(_) >>
                insert(resultList, "iphone", currentDate);

        when:
        recentlyViewedProductsService.updateProducts(user, program, "iphone");

        then:
        resultList.size() == 1
        resultList.get(0).getProductId() == "iphone"
        resultList.get(0).getViewedDateTime() == currentDate

    }

    def 'modifyUserRecentlyViewedProducts -- Append new item to already existing userItem'() {

        List<RecentlyViewedProductsEntity> resultList = buildMockUserViewedEntity()
        given:

        applicationProperties.getProperty(CommonConstants.CAROUSEL_RECENTLY_VIEWED_MAX_COUNT) >> "20"
        recentlyViewedProductsDao.getProducts(_, _, _) >> null
        recentlyViewedProductsDao.insert(_) >>
                insert(resultList, "iphone", currentDate);

        when:
        recentlyViewedProductsService.updateProducts(user, program, "iphone");

        then:
        resultList.size() == 2
        resultList.get(1).getProductId() == "iphone"
        resultList.get(1).getViewedDateTime() == currentDate

    }

    def 'modifyUserRecentlyViewedProducts -- Add while Max count reached'() {

        List<RecentlyViewedProductsEntity> resultList = buildMockUserViewedEntity2()
        given:
        String psid = "airpod"
        applicationProperties.getProperty(CommonConstants.CAROUSEL_RECENTLY_VIEWED_MAX_COUNT) >> "2"
        recentlyViewedProductsDao.getProducts(_, _, _) >> buildMockUserViewedEntity2()
        recentlyViewedProductsDao.deleteProducts(_, _, _, "airtag") >> deleteMock(resultList, "airtag")
        recentlyViewedProductsDao.insert(_) >> insert(resultList, psid, currentDate)

        when:
        recentlyViewedProductsService.updateProducts(user, program, psid)

        then:
        resultList.size() == 2
        resultList.get(1).getProductId() == psid
        resultList.get(1).getViewedDateTime() == currentDate

    }

    def 'modifyUserRecentlyViewedProducts -- update Already Existing Item'() {

        List<RecentlyViewedProductsEntity> resultList = buildMockUserViewedEntity();
        given:
        applicationProperties.getProperty(CommonConstants.CAROUSEL_RECENTLY_VIEWED_MAX_COUNT) >> "20"
        recentlyViewedProductsDao.getProducts(_, _, _) >> buildMockUserViewedEntity()
        recentlyViewedProductsDao.updateProductWithCurrentTime(_, _, _, "airtag") >>
                updateUserRecentlyViewProduct(resultList, "airtag", currentDate);

        when:
        recentlyViewedProductsService.updateProducts(user, program, "airtag");

        then:
        resultList.size() == 1
        resultList.get(0).getViewedDateTime() == currentDate

    }

    def 'test getRecentlyViewedProducts'() {
        given:
        def user = new User()
        user.varId = "UA"
        user.programId = "MP"
        user.locale = new Locale("en", "US")
        def program = new Program()
        program.varId = "UA"
        program.programId = "MP"
        program.catalogId = "apple-us-en"

        def products = new ArrayList<>();
        def product = new Product()
        product.setName("iphone")
        product.setPsid("airTag")
        products.add(product)

        def recentlyViewedProductsList = new ArrayList<>()
        def recentlyViewedProduct = RecentlyViewedProduct.builder()
                .withProductId("airTag")
                .withProgramId("MP")
                .withUserId("userId")
                .withVarId("UA")
                .withViewedDateTime(new Date())
                .build();
        recentlyViewedProductsList.add(recentlyViewedProduct)

        when:
        recentlyViewedProductsDao.getProducts(_, _, _) >> buildMockUserViewedEntity()
        productsMapper.getProducts(_) >> recentlyViewedProductsList
        def result = recentlyViewedProductsService.getProducts(user)

        then:
        result != null
        result.size() != 0
        result.stream().iterator().next().productId.equalsIgnoreCase("airTag")
    }

    def 'test getCarouselProducts'() {
        given:
        def varId = 'UA'
        def programId = 'MP'
        def user = new User(userId: 'UAuser', varId: varId, programId: programId, locale: new Locale('en', 'US'))
        def program = new Program(varId: varId, programId: programId, catalogId: 'apple-us-en')

        Date date = new Date()
        date.setMonth(2)
        date.setDate(2)
        def productId = '30001MHRE3VC/A'

        when:
        recentlyViewedProductsDao.getProducts(_, _, _) >> getEntities()
        productServiceV3.getAppleMultiProductDetail(_, _, _, _, _, _) >> getProducts(productId, true)

        def result = recentlyViewedProductsService.getCarouselProducts(user, program, null)

        then:
        CollectionUtils.isNotEmpty(result)
        result.stream().iterator().next().psid.equalsIgnoreCase(productId)
    }

    def 'test getCarouselProducts scenarios'() {
        given:
        def varId = 'UA'
        def programId = 'MP'
        def user = new User(userId: 'UAuser', varId: varId, programId: programId, locale: new Locale('en', 'US'))
        def program = new Program(varId: varId, programId: programId, catalogId: 'apple-us-en')

        when:
        recentlyViewedProductsDao.getProducts(_, _, _) >> entities
        productServiceV3.getAppleMultiProductDetail(_, _, _, _, _, _) >> getProducts(psProductId, productAvailability)

        def result = recentlyViewedProductsService.getCarouselProducts(user, program, null)

        then:
        isNotEmpty == CollectionUtils.isNotEmpty(result)

        where:
        entities          | psProductId        | productAvailability || isNotEmpty
        getEntities()     | '30001MHRE3VC/A'   | true                || true
        getEntities()     | '30001MHRE3VC/A'   | false               || false
        getEntities()     | 'differentProduct' | true                || false
        new ArrayList<>() | '30001MHRE3VC/A'   | true                || false
    }

    private static void insert(List<RecentlyViewedProductsEntity> resultList, String
            productId, Date currentDate) {

        RecentlyViewedProductsEntity entity = new RecentlyViewedProductsEntity()
        User user1 = getUser()
        entity.setUserId(user1.getUserId())
        entity.setProductId(productId)
        entity.setProgramId(user1.programId)
        entity.setViewedDateTime(currentDate)
        entity.setVarId(user1.getVarId())
        resultList.add(entity)
    }

    private static int updateUserRecentlyViewProduct(List<RecentlyViewedProductsEntity> resultList, String
            productId, Date currentDate) {
        for (RecentlyViewedProductsEntity entity : resultList) {
            if (entity.getProductId().equalsIgnoreCase(productId)) {
                entity.setViewedDateTime(currentDate)
            }
        }
        return 1
    }

    private static int deleteMock(List<RecentlyViewedProductsEntity> resultList, String productId) {
        RecentlyViewedProductsEntity deleteEntity = null;
        for (RecentlyViewedProductsEntity entity : resultList) {
            if (entity.getProductId().equalsIgnoreCase(productId)) {
                deleteEntity = entity
                break
            }
        }
        resultList.remove(deleteEntity)
        return 1

    }

    private static List<RecentlyViewedProductsEntity> buildMockUserViewedEntity() {
        RecentlyViewedProductsEntity entity = new RecentlyViewedProductsEntity()
        entity.setVarId("FDR")
        entity.setUserId("userId1")
        entity.setProgramId("b2s_qa_only")
        entity.setProductId("airtag")
        Date date = new Date()
        date.setMonth(2)
        date.setDate(2)
        entity.setViewedDateTime(date)
        List<RecentlyViewedProductsEntity> entityList = new ArrayList<>()
        entityList.add(entity)
        return entityList
    }

    private static List<RecentlyViewedProductsEntity> buildMockUserViewedEntity2() {
        List<RecentlyViewedProductsEntity> entityList = new ArrayList<>()

        RecentlyViewedProductsEntity entity2 = new RecentlyViewedProductsEntity()
        entity2.setVarId("FDR")
        entity2.setUserId("userId1")
        entity2.setProgramId("b2s_qa_only")
        entity2.setProductId("i24")
        Date date2 = new Date()

        date2.setMonth(3)
        date2.setDate(4)
        entity2.setViewedDateTime(date2)
        entityList.add(entity2)

        RecentlyViewedProductsEntity entity = new RecentlyViewedProductsEntity()
        entity.setVarId("FDR")
        entity.setUserId("userId1")
        entity.setProgramId("b2s_qa_only")
        entity.setProductId("airtag")
        Date date = new Date()
        date.setMonth(2)
        date.setDate(2)
        entity.setViewedDateTime(date)
        entityList.add(entity)

        return entityList
    }

    private static User getUser() {
        User user = new User()
        user.setVarId("FDR")
        user.setUserId("userId1")
        user.setProgramId("b2s_qa_only")
        return user
    }

    def getProgram() {
        def program = new Program()
        program.setVarId("FDR")
        program.setProgramId("b2s_qa_only")
        program.setCarouselPages(["bag","pcp","pdp","store"])
        program.setCarouselConfig(getCarouselConfigs())
        return program
    }

    def getCarouselConfigs() {
        Map<String, Map<CommonConstants.CarouselType, CarouselConfig>> carouselConfigs = new HashMap<>()

        Map<CommonConstants.CarouselType, CarouselConfig> configMap = new HashMap<>()
        def carousel = CarouselConfig.builder()
                .withTemplateName("Example1")
                .withType(RECENTLY_VIEWED)
                .withDisplayPages(["bag", "pcp", "pdp"])
                .withProgramExclusion(new ArrayList<String>())
                .build()
        configMap.put(RECENTLY_VIEWED, carousel)

        carouselConfigs.put(BAG,configMap)
        return carouselConfigs
    }

    def getEntities(){
        def varId = 'UA'
        def programId = 'MP'

        Date date = new Date()
        date.setMonth(2)
        date.setDate(2)
        def productId = '30001MHRE3VC/A'

        List<RecentlyViewedProductsEntity> recentlyViewedProductEntities = new ArrayList<>()
        def entity = new RecentlyViewedProductsEntity(userId: 'UAuser', varId: varId, programId: programId,
                productId: productId, viewedDateTime: date)
        recentlyViewedProductEntities.add(entity)
        return recentlyViewedProductEntities
    }

    def getProducts(String productId, boolean available) {
        def products = new ArrayList<>()
        def categories = new ArrayList<Category>()
        def category = new Category(name: 'iPad Pro', slug: 'ipad', depth: 1)
        categories.add(category)

        products.add(new Product(name: 'airTag', psid: productId, available: available, categories: categories))
        return products
    }
}