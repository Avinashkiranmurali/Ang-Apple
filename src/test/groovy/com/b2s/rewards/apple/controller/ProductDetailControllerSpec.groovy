package com.b2s.rewards.apple.controller

import com.b2s.apple.services.AppSessionInfo
import com.b2s.apple.services.GiftPromoService
import com.b2s.common.services.productservice.ProductServiceV3
import com.b2s.rewards.apple.model.*
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification
import spock.lang.Subject

class ProductDetailControllerSpec extends Specification{

    def productServiceV3 = Mock(ProductServiceV3)
    def giftPromoService = Mock(GiftPromoService)
    final request = new MockHttpServletRequest()
    final session = new MockHttpSession()
    def appSessionInfo=Mock(AppSessionInfo)

    @Subject
    final productDetailController = new ProductDetailController(productServiceV3: productServiceV3,
            giftPromoService: giftPromoService,
            appSessionInfo: appSessionInfo)

    def 'test getGiftProducts()'(){

        given:
        String qualifyingPsid = "30001MWV72LL/A"
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, new User())
        appSessionInfo.currentUser() >> new User()
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, new Program())
        request.setSession(session)
        productServiceV3.getGiftItem( _, _ , _ , _ ) >> getProducts()

        when:
        final response = productDetailController.getGiftProducts(request, qualifyingPsid)

        then:
        response!=null && response.statusCodeValue == 200 && response.getBody()!=null
        List list = response.getBody()
        list != null && list.size() == 2
    }

    def 'test getGiftProducts PS issue()'(){

        given:
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, new User())
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, new Program())
        request.setSession(session)
        String qualifyingPsid = "30001MWV72LL/A"

        when:
        final response = productDetailController.getGiftProducts(request, "30001MWV72LL/A")

        then:
        response.statusCodeValue == 204
        String msg = response.getBody()
        msg == "Gift Items not found for the given Psid "+qualifyingPsid
    }

    List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        product.setPsid("30001MWP22AM/A");
        products.add(product);
        product = new Product();
        product.setPsid("30001MWTK2LL/A");
        products.add(product);
        return products;
    }
}
