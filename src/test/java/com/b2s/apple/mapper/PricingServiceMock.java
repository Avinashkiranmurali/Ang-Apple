package com.b2s.apple.mapper;

import com.b2s.common.util.BigMoneyDeserializer;
import com.b2s.common.util.CurrencyUnitDeserializer;
import com.b2s.common.util.CurrencyUnitKeyDeserializer;
import com.b2s.common.util.MoneyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class PricingServiceMock {

    /**
     * Creates an Object Mapper with custom modules for deserialization
     *
     * @return ObjectMapper objMapper
     */
    public ObjectMapper setObjectMapper() {

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Money.class, new MoneyDeserializer());
        module.addDeserializer(CurrencyUnit.class, new CurrencyUnitDeserializer());
        module.addDeserializer(BigMoney.class, new BigMoneyDeserializer());
        module.addKeyDeserializer(CurrencyUnit.class, new CurrencyUnitKeyDeserializer());

        ObjectMapper objMapper = new ObjectMapper().registerModules(new GuavaModule(), new JodaModule(), module);

        return objMapper;
    }
}
