package com.b2s.apple.model;

/**
 * @author rkumar 2019-09-11
 */
public enum FeeValue {
    EHF("ehfFee");

    public final String displayName;

    FeeValue(final String displayName) {
        this.displayName = displayName;
    }
}
