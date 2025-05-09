package com.b2s.rewards.apple.util;

/**
 * This will have modify elements JSON for modify cart
 *
 * Created by rperumal on 6/24/2015.
 */
public enum CartItemOption {
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    QUANTITY("quantity"),
    ENGRAVE("engrave"),
    ENGRAVE_LINE1("line1"), // Line1 Message
    ENGRAVE_LINE2("line2"), // Line2 Message
    GIFT("gift"),
    GIFT_ITEM("giftItem"),
    GIFT_WRAP("giftWrap"),
    GIFT_WRAP_FREE("freeGiftWrap"),
    GIFT_MESSAGE("message"),
    ADDRESS1("address1"),
    ADDRESS2("address2"),
    ADDRESS3("address3"),
    CITY("city"),
    SUBCITY("subCity"),
    STATE("state"),
    ZIP5("zip5"),
    ZIP4("zip4"),
    COUNTRY("country"),
    PHONE_NUMBER("phoneNumber"),
    EMAIL("email"),
    SHIPPING_ADDRESS("shippingAddress"),
    IGNORE_SUGGESTED_ADDRESS("ignoreSuggestedAddress"),
    PAYMENT_OPTION("selectedPaymentOption"),
    SELECTED_REDEMPTION_OPTION("selectedRedemptionOption"),
    PAYMENT_INSTALMENT("noOfInstalment"),
    SUBSCRIPTIONS("subscriptions"),
    SERVICE_PLAN("servicePlan");

    private String value;

    CartItemOption(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CartItemOption getItemValue(String value) {
        try {
            return CartItemOption.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }
}
