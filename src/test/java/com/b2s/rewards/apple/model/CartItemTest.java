package com.b2s.rewards.apple.model;

import com.b2s.shop.common.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CartItemTest {

    @Test
    public void testTransform() {
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        final String optionsXml = "<xml><options><name>gift</name><value>{\"giftWrapPoints\":100}</value><name" +
            ">giftItem</name><value>{\"productId\": \"30001ABCDEDF/F\", \"engrave\":{\"line1\":\"Line 1\"," +
            "\"line2\":\"\",\"font\":\"Helvetica Neue\",\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\"," +
            "\"widthDimension\":\"45mm\"}}</value></options></xml>";
        shoppingCartItem.setOptionsXml(optionsXml);
        final CartItem cartItem = CartItem.transform(shoppingCartItem, new User(), new Program());
        assertNotNull(cartItem);
        assertNotNull(cartItem.getSelectedAddOns().getGiftItem());
        assertEquals("30001ABCDEDF/F", cartItem.getSelectedAddOns().getGiftItem().getProductId());
        assertEquals("Line 1", cartItem.getSelectedAddOns().getGiftItem().getEngrave().getLine1());

    }
}
