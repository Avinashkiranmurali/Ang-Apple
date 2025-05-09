package com.b2s.common.services

import com.b2s.apple.services.AffordabilityService
import com.b2s.common.services.productservice.ProductServiceV3
import com.b2s.rewards.apple.model.Category
import com.b2s.rewards.apple.model.Product
import com.b2s.rewards.apple.model.ProductResponse
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.service.product.client.application.search.ProductSearchRequest
import com.b2s.shop.common.User
import org.apache.commons.collections.CollectionUtils
import spock.lang.Specification
import spock.lang.Subject

class AffordabilityServiceSpec extends Specification {

    def productServiceV3 = Mock(ProductServiceV3)
    def applicationProperties = Mock(Properties)

    @Subject
    final affordabilityService = new AffordabilityService(
            productServiceV3: productServiceV3,
            applicationProperties: applicationProperties)

    def 'test getCarouselProducts'() {
        given:
        def varId = 'UA'
        def programId = 'MP'
        def user = new User(userId: 'UAuser', varId: varId, programId: programId, locale: new Locale('en', 'US'),
                balance: 500)
        def program = new Program(varId: varId, programId: programId, catalogId: 'apple-us-en')

        Date date = new Date()
        date.setMonth(2)
        date.setDate(2)
        def productId = '30001MHRE3VC/A'

        when:
        applicationProperties.getProperty(CommonConstants.CAROUSEL_AFFORDABILITY_MAX_COUNT) >> "20"
        productServiceV3.getProductSearchRequestBuilder(_, _, _, _, _, _, _, _, _, _, _, _, _) >> Mock(ProductSearchRequest.Builder.class)
        productServiceV3.getProducts(_,_,_,_,_) >> getProductResponse(productId, true)

        def result = affordabilityService.getCarouselProducts(user, program, 20)

        then:
        CollectionUtils.isNotEmpty(result)
    }

    def 'test getCarouselProducts throws Exception'() {
        given:
        def varId = 'UA'
        def programId = 'MP'
        def user = new User(userId: 'UAuser', varId: varId, programId: programId, locale: new Locale('en', 'US'),
                balance: 500)
        def program = new Program(varId: varId, programId: programId, catalogId: 'apple-us-en')

        Date date = new Date()
        date.setMonth(2)
        date.setDate(2)
        def productId = '30001MHRE3VC/A'

        when:
        productServiceV3.getProductSearchRequestBuilder(_, _, _, _, _, _, _, _, _, _, _, _, _) >> Mock(ProductSearchRequest.Builder.class)
        productServiceV3.getProducts(_,_,_,_,_) >> getProductResponse(productId, true)

        def result = affordabilityService.getCarouselProducts(user, program, 20)

        then:
        RuntimeException ex = thrown()
    }

    def getProductResponse(String productId, boolean available) {
        List<Product> products = new ArrayList<>()
        def categories = new ArrayList<Category>()
        def category = new Category(name: 'iPad Pro', slug: 'ipad', depth: 1)
        categories.add(category)

        products.add(new Product(name: 'airTag', psid: productId, available: available, categories: categories))
        return new ProductResponse(products: products)
    }
}