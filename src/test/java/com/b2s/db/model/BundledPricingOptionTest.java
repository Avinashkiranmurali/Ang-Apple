package com.b2s.db.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by cborn on 3/4/14.
 */
public class BundledPricingOptionTest {

    @Test
    public void testFromDatabaseValueN() {
        assertEquals("N is wrong", BundledPricingOption.BUNDLED, BundledPricingOption.fromDatabaseValue("N"));
    }

    @Test
    public void testFromDatabaseValueY() {
        assertEquals("Y is wrong", BundledPricingOption.UNBUNDLED_DETAIL_PAGE_BUNDLED_CHECKOUT, BundledPricingOption.fromDatabaseValue("Y"));
    }

    @Test
    public void testFromDatabaseValueA() {
        assertEquals("A is wrong", BundledPricingOption.BASE_PRICE_SHOPPING_UNBUNDLED_CHECKOUT, BundledPricingOption.fromDatabaseValue("A"));
    }
}
