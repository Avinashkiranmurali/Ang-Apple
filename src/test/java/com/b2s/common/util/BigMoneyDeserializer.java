package com.b2s.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;
import org.joda.money.Money;

import java.io.IOException;

public class BigMoneyDeserializer extends StdDeserializer<BigMoney> {

    public BigMoneyDeserializer() {
        super(BigMoney.class);
    }

    @Override
    public BigMoney deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException,
            JsonProcessingException {

        BigMoney result = null;

        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        result = BigMoney.parse(node.textValue());

        return result;
    }
}
