package com.b2s.rewards.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum YesOrNo {
    YES("Y"),
    NO("N");

    private String value;

    YesOrNo(final String value) {
        this.value = value;
    }

    public static List<String> valuesAsList() {
        return Arrays.asList(YesOrNo.values()).stream().map(c -> c.value).collect(Collectors.toList());
    }

    public String getValue() {

        return value;
    }
}
