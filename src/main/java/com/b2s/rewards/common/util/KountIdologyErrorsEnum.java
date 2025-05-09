package com.b2s.rewards.common.util;

/**
 * @author rjesuraj Date : 10/9/2019 Time : 7:23 PM
 */
public enum KountIdologyErrorsEnum {
    KOUNT_IDOLOGY_ERROR_CODE("B28376"),
    KOUNT_ERROR_CODE("B28374"),
    IDOLOGY_ERROR_CODE("B28375");

    private final String value;
    KountIdologyErrorsEnum(final String value) {
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
