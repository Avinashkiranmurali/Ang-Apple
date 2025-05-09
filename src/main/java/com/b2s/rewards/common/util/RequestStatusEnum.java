package com.b2s.rewards.common.util;

import java.util.Arrays;

/**
 * @author rjesuraj Date : 11/8/2017 Time : 4:01 PM
 */
public enum RequestStatusEnum {
    PENDING(0, "pending"),
    APPROVED(1, "approved"),
    DENIED(2, "declined"),
    PARTIALLY_APPROVED(3, "partially-approved"),
    CONFIRMATION_PENDING(-2,"confirmation-pending"),
    ORDERED(4, "ordered"),
    CANCELLED(5, "cancelled"),
    ABANDONED(6,"abandoned"),
    FAILED(7,"failed");

    private final int code;

    private final String description;

    RequestStatusEnum(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RequestStatusEnum getValue(final long code) {

        return Arrays.stream(RequestStatusEnum.values()).filter(re -> re.getCode() == code).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid RequestStatusEnum code"));
    }
}
