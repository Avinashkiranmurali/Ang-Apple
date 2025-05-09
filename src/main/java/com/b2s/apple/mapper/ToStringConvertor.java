package com.b2s.apple.mapper;

import com.b2s.service.utils.lang.Validations;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ToStringConvertor {
    public static String createFor(final Object object) {
        try {
            return ApiObjectMapper.get().writeValueAsString(Validations.notNull(object, "object"));
        } catch (final JsonProcessingException | RuntimeException e) {
            return "<<exception: " + e.getMessage() + ">>";
        }
    }

    private ToStringConvertor() {}
}
