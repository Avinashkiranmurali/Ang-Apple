package com.b2s.shop.common.order.var;

import com.b2s.shop.common.User;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VAROrderManagerDeltaTest {

    private VAROrderManagerDelta varOrderManagerDelta = new VAROrderManagerDelta();

    @Test
    public void verifyUSLocale()  throws Exception {
        User user = new User();
        user.setLocale(LocaleUtils.toLocale("en_US"));
        user.setCountry("US");
        boolean localeFlag = varOrderManagerDelta.verifyLocaleWithCountry(user);
        assertTrue(localeFlag);
    }

    @Test
    public void verifyCALocale()  throws Exception {
        User user = new User();
        user.setLocale(LocaleUtils.toLocale("en_CA"));
        user.setCountry("US");
        boolean localeFlag = varOrderManagerDelta.verifyLocaleWithCountry(user);
        assertFalse(localeFlag);
    }
}
