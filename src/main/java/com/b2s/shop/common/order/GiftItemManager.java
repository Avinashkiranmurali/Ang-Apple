package com.b2s.shop.common.order;

import com.b2s.apple.mapper.ProductMapper;
import com.b2s.apple.services.CartService;
import com.b2s.common.services.pricing.impl.LocalPricingServiceV2;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.validator.AddressMapper;
import com.b2s.shop.common.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GiftItemManager {

    @Autowired
    private LocalPricingServiceV2 localPricingServiceV2;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductMapper productMapper;

    /**
     * Creates Gift Cart and sets updated Pricing details for DGwP items
     *
     * @param user
     * @param program
     * @param products
     * @param giftItems
     */
    public void addGiftItemPricingInfo(final User user, final Program program, final List<Product> products,
        final List<GiftItem> giftItems) {

        final Cart giftCart = new Cart();
        final List<CartItem> giftCartItems = new ArrayList<>();
        for (final Product product : products) {
            for (final GiftItem giftItem : giftItems) {
                if (product.getPsid().equalsIgnoreCase(giftItem.getProductId())) {
                    //Create Gift Cart Item from Product
                    final CartItem giftCartItem = new CartItem();
                    giftCartItem.setDiscount(giftItem.getDiscount());
                    giftCartItem.setDiscountType(giftItem.getDiscountType());
                    productMapper.productName2CartName(product);
                    giftCartItem.setProductDetail(product);
                    giftCartItem.applyDiscountedGwpPromotion();

                    //Update Gift Cart Item from Core Product Detail
                    giftCartItem.setProductId(giftItem.getProductId());
                    cartService.setCartItemFromCoreProductDetail(giftCartItem, product);
                    giftCartItems.add(giftCartItem);
                    break;
                }
            }
        }

        giftCart.setShippingAddress(AddressMapper.getAddress(user, program));
        giftCart.setCartItems(giftCartItems);

        //Make P$ call with the updated Gift Cart Items
        localPricingServiceV2.calculateCartPrice(giftCart, user, program);
    }
}
