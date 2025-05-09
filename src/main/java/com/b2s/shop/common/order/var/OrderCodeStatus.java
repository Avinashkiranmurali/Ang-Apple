package com.b2s.shop.common.order.var;

public enum OrderCodeStatus {
    SUCCESS(1),
    FAIL(2),
    NOT_ENOUGH_POINTS(3),
    HAS_PENDING_ORDER(4);

    private int value;

    OrderCodeStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
