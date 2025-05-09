package com.b2s.rewards.merchandise.action;

import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.common.util.CommonConstants;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: jshue
 * Date: 10/6/11
 * Time: 9:22 AM
 */
public class CartCalculationUtil {

    /**
     * Rounds the dollar value with more than 2 decimal places to the ceiling value
     * of 2 decimal values. No rounding done if the decimal places are just 2.
     * @param value
     * @return
     */
    public static double roundDollar(final double value){
        final BigDecimal scaledAndRounded= new BigDecimal(Double.toString(value))
                .setScale(3, BigDecimal.ROUND_FLOOR)
                .setScale(2, BigDecimal.ROUND_CEILING);
        return scaledAndRounded.doubleValue();
    }

    public static java.util.Date convertTimeZone(Date date, TimeZone toTZ)    {
        TimeZone fromTZ = TimeZone.getTimeZone(CommonConstants.SERVER_TIME_ZONE);
        long fromTZDst = 0;
        if(fromTZ.inDaylightTime(date)){
            fromTZDst = fromTZ.getDSTSavings();
        }
        long fromTZOffset = fromTZ.getRawOffset() + fromTZDst;
        long toTZDst = 0;
        if(toTZ.inDaylightTime(date)){
            toTZDst = toTZ.getDSTSavings();
        }
        long toTZOffset = toTZ.getRawOffset() + toTZDst;
        return new Date(date.getTime() + (toTZOffset - fromTZOffset));
    }

    public static double getCartTotalAmount(final Cart cart) {
        double cartTotalAmount = 0.0d;
        if(cart != null && cart.getCartTotal() != null && cart.getCartTotal().getPrice() != null && cart.getCartTotal().getPrice().getAmount() != null) {
            cartTotalAmount = cart.getCartTotal().getPrice().getAmount();
        }
        return cartTotalAmount;
    }

    public static double getDisplayCartTotalAmount(final Cart cart) {
        double cartTotalAmount = 0.0d;
        if(cart != null && cart.getDisplayCartTotal() != null && cart.getDisplayCartTotal().getPrice() != null && cart.getDisplayCartTotal().getPrice().getAmount() != null) {
            cartTotalAmount = cart.getDisplayCartTotal().getPrice().getAmount();
        }
        return cartTotalAmount;
    }

    public static boolean isCartTotalModified(final double initialCartTotal, final double updatedCartTotal) {
        boolean isCartTotalModified = false;
        if(initialCartTotal != 0.0d && updatedCartTotal != 0.0d && initialCartTotal != updatedCartTotal) {
            isCartTotalModified =  true;
        }
        return isCartTotalModified;
    }


}
