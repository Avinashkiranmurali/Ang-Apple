package com.b2s.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

import java.io.IOException;

public class CurrencyUnitKeyDeserializer extends KeyDeserializer {

    public CurrencyUnitKeyDeserializer() {
        super();
    }

    @Override
    public CurrencyUnit deserializeKey( final String key,
                                  final DeserializationContext ctxt )
            throws IOException, JsonProcessingException
    {

        return CurrencyUnit.of(key);
    }
}
