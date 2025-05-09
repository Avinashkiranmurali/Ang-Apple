package com.b2s.rewards.apple.model;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ShoppingCartItemTest {

    @Test
    public void testConvertToMap() {
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        final String optionsXml =
            "<xml><options><name>gift</name><value>{\"giftWrapPoints\":100}</value></options></xml>";
        Map<String, String> map = shoppingCartItem.convertToMap(optionsXml);
        assertNotNull(map);
        assertEquals("{\"giftWrapPoints\":100}", map.get("gift"));
    }

    @Test
    public void testConvertToMapWithEngrave() {
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        final String optionsXml =
            "<xml><options><name>gift</name><value>{\"giftWrapPoints\":100}</value><name>engrave" +
                "</name><value>{\"linel\":\"Line 1\",\"line2\":\"\",\"font\":\"Helvetica Neuen," +
                "\"fontCode\":*EHVO77N\"," +
                "\"maxCharsPerLinen:\"35 Eng\",wwidthDimension\":\"45mm\"}</value></options></xml>";
        Map<String, String> map = shoppingCartItem.convertToMap(optionsXml);
        assertNotNull(map);
        assertEquals("{\"giftWrapPoints\":100}", map.get("gift"));
        assertEquals("{\"linel\":\"Line 1\",\"line2\":\"\",\"font\":\"Helvetica Neuen," +
            "\"fontCode\":*EHVO77N\",\"maxCharsPerLinen:\"35 Eng\",wwidthDimension\":\"45mm\"}", map.get("engrave"));
    }

    @Test
    public void testConvertToMapWithFreeGiftItems() {
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        final String optionsXml =
            "<xml><options><name>gift</name><value>{\"giftWrapPoints\":100}</value><name>engrave" +
                "</name><value>{\"linel\":\"Line 1\",\"line2\":\"\",\"font\":\"Helvetica Neuen," +
                "\"fontCode\":*EHVO77N\"," + "\"maxCharsPerLinen:\"35 Eng\"," +
                "wwidthDimension\":\"45mm\"}</value><name>giftItem</name><value" +
                ">{\"productId\": \"30001ABCDEDF/F\", \"engrave\":{\"linel\":\"Line 1,\"line2\":\"\"," +
                "\"font\":\"Helvetica Neue\",\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\"," +
                "\"widthDimension\":\"45mm\"}}</value>" +
                "</options></xml>";
        Map<String, String> map = shoppingCartItem.convertToMap(optionsXml);
        assertNotNull(map);
        assertEquals("{\"giftWrapPoints\":100}", map.get("gift"));
        assertEquals(map.get("engrave"), "{\"linel\":\"Line 1\",\"line2\":\"\",\"font\":\"Helvetica Neuen," +
            "\"fontCode\":*EHVO77N\",\"maxCharsPerLinen:\"35 Eng\",wwidthDimension\":\"45mm\"}");
        assertEquals(map.get("giftItem"), "{\"productId\": \"30001ABCDEDF/F\", \"engrave\":{\"linel\":\"Line 1," +
            "\"line2\":\"\",\"font\":\"Helvetica Neue\",\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\"," +
            "\"widthDimension\":\"45mm\"}}");
    }

    @Test
    public void testConvertToOptionsXml() {
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        Map<String, String> map = Map.of("gift", "{griftWrap:10}");
        final String optionsXml = shoppingCartItem.convertToOptionsXml(map);
        assertNotNull(optionsXml);
        assertEquals("<xml><options><name>gift</name><value>{griftWrap:10}</value></options></xml>", optionsXml);
    }
}
