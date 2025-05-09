package com.b2s.shop.common.order

import com.b2s.apple.mapper.ProductMapper
import com.b2s.apple.services.CartService
import com.b2s.apple.services.DetailService
import com.b2s.common.services.pricing.impl.LocalPricingServiceV2
import com.b2s.rewards.apple.model.CartItem
import com.b2s.rewards.apple.model.Category
import com.b2s.rewards.apple.model.GiftItem
import com.b2s.rewards.apple.model.Offer
import com.b2s.rewards.apple.model.Product
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.model.Merchant
import com.b2s.rewards.model.Supplier
import com.b2s.shop.common.User
import org.joda.money.Money
import spock.lang.Specification
import spock.lang.Subject

class GiftItemManagerSpec extends Specification {

    def localPricingServiceV2 = Mock(LocalPricingServiceV2)
    def detailService = Mock(DetailService)
    def productMapper = Mock(ProductMapper)
    def cartService = new CartService(detailService: detailService, productMapper: productMapper)

    @Subject
    private GiftItemManager giftItemManager = new GiftItemManager(localPricingServiceV2: localPricingServiceV2,
            cartService: cartService, productMapper: productMapper)

    def 'test add Gift Item Pricing Info'() {
        setup:
        def cartItem = new CartItem()
        cartItem.getSelectedAddOns().setGiftItem(new CartItem(productDetail: new Product(), discount: 100))
        def user = new User()
        def program = new Program()
        detailService.getCoreProductDetail(_,_,_,_,_) >> buildMockCoreProductDetail()
        def giftItems = getGiftItems()
        def appleProducts = new ArrayList<>()
        def abcProduct = buildMockAppleProductDetail("30001ABCDEDF/F")
        def ghiProduct = buildMockAppleProductDetail("30001GHIJKLM/N")
        appleProducts.add(abcProduct)
        appleProducts.add(ghiProduct)

        when:
        giftItemManager.addGiftItemPricingInfo(user, program, appleProducts, giftItems)
        BigDecimal discountGiftItemPromotion = abcProduct.getPromotion().get().getDiscountPercentage().get()
        Money fixedPointPricePromotion = ghiProduct.getPromotion().get().getFixedPointPrice().get()

        then:
        Objects.nonNull(discountGiftItemPromotion)
        discountGiftItemPromotion == 50.0

        Objects.nonNull(fixedPointPricePromotion)
        fixedPointPricePromotion.getAmountMajorInt() == 123
    }

    def getGiftItems(){
        def giftItemList = new ArrayList<>()
        def discountGiftItem = new GiftItem()
        discountGiftItem.setProductId("30001ABCDEDF/F")
        discountGiftItem.setDiscountType("Percentage")
        discountGiftItem.setDiscount(50)
        giftItemList.add(discountGiftItem)

        def pointsFixedGiftItem = new GiftItem()
        pointsFixedGiftItem.setProductId("30001GHIJKLM/N")
        pointsFixedGiftItem.setDiscountType("Points")
        pointsFixedGiftItem.setDiscount(new Double(123))
        giftItemList.add(pointsFixedGiftItem)
        return giftItemList
    }

    def buildMockCoreProductDetail() {
        def product = new com.b2s.rewards.model.Product()
        def offer = new com.b2s.rewards.model.Offer()
        def merchant = new com.b2s.rewards.model.Merchant()
        merchant.setMerchantId("200")
        offer.setMerchant(merchant)
        product.setOffers(Arrays.asList(offer))
        product.setSupplier(new com.b2s.rewards.model.Supplier())
        product.setProductId("30001ABCDEDF/F")
        product.setBrand("Brand test")

        return product;
    }

    def buildMockAppleProductDetail(def productId){
        def product = new Product()
        product.setPsid(productId)
        product.setName("Gift Product")
        def categories = new ArrayList<>()
        def category = new Category()
        category.setName("Gift Category")
        category.setSlug("gift-category")
        categories.add(category)
        product.setCategory(categories)

        def offer = new Offer();
        offer.setIsEligibleForPayrollDeduction(false);
        offer.setPayPeriods(0)

        Merchant merchant = new Merchant()
        merchant.setId(1)
        merchant.setMerchantId("30001")
        merchant.setSupplierId("200")
        merchant.setName("Apple")
        offer.setMerchant(merchant)
        Supplier supplier = new Supplier()
        supplier.setSupplierId(123)
        product.setSupplier(supplier)

        def offers = new ArrayList<Offer>()
        offers.add(offer)
        product.setOffers(offers);
        return product;
    }
}