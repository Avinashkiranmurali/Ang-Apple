package com.b2s.shop.common.order.supplier;

public enum SupplierStatus {
    SUCCESS(1),
    FAIL(2),
    ERROR(3),
    NLA(4);

    private int value;

    SupplierStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
