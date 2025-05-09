package com.b2s.apple.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class ApiObjectMapper {

    private static final ObjectMapper INSTANCE;

    public static ObjectMapper get() {
        return INSTANCE;
    }

    private ApiObjectMapper() {}

    static {
        INSTANCE = new ObjectMapper();
        INSTANCE.registerModule(new Jdk8Module());
        INSTANCE.registerModule(new JavaTimeModule());
        INSTANCE.registerModule(new GuavaModule());
        INSTANCE.registerModule(createModule());

        INSTANCE.enable(JsonParser.Feature.ALLOW_COMMENTS); // allow Java-style comments in JSON
        INSTANCE.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        INSTANCE.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        INSTANCE.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        INSTANCE.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // ignore unrecognized fields
        INSTANCE.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }

    private static Module createModule() {
        final SimpleModule module =
            new SimpleModule(
                "serviceModule",
                new Version(1, 0, 0, null, "com.fasterxml.jackson.core", "jackson-databind"));

        // JodaMoney serialization
        module.addDeserializer(Money.class, new MoneyDeserializer());
        module.addSerializer(Money.class, new ToStringSerializer());
        module.addKeyDeserializer(Money.class, new MoneyKeyDeserializer());
        module.addDeserializer(BigMoney.class, new BigMoneyDeserializer());
        module.addSerializer(BigMoney.class, new ToStringSerializer());
        module.addKeyDeserializer(BigMoney.class, new BigMoneyKeyDeserializer());
        module.addDeserializer(CurrencyUnit.class, new CurrencyUnitDeserializer());
        module.addSerializer(CurrencyUnit.class, new ToStringSerializer());
        module.addKeyDeserializer(CurrencyUnit.class, new CurrencyUnitKeyDeserializer());
        module.addKeyDeserializer(CurrencyUnit.class, new CurrencyUnitKeyDeserializer());

        return module;
    }

    private static class MoneyDeserializer extends FromStringDeserializer<Money> {
        private static final long serialVersionUID = -9126923845485726950L;

        private MoneyDeserializer() {
            super(Money.class);
        }

        @Override
        protected Money _deserialize(final String value, final DeserializationContext ctxt) {
            return Money.parse(value);
        }
    }

    private static class BigMoneyDeserializer extends FromStringDeserializer<BigMoney> {
        private static final long serialVersionUID = -3329263887756698087L;

        private BigMoneyDeserializer() {
            super(BigMoney.class);
        }

        @Override
        protected BigMoney _deserialize(final String value, final DeserializationContext ctxt) {
            return BigMoney.parse(value);
        }
    }

    private static class CurrencyUnitDeserializer extends FromStringDeserializer<CurrencyUnit> {

        private static final long serialVersionUID = -6001785302042395279L;

        private CurrencyUnitDeserializer() {
            super(CurrencyUnit.class);
        }

        @Override
        protected CurrencyUnit _deserialize(final String value, final DeserializationContext ctxt) {
            return CurrencyUnit.of(value);
        }
    }

    private static class CurrencyUnitKeyDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(final String key, final DeserializationContext ctxt) {
            return CurrencyUnit.of(key);
        }
    }

    private static class MoneyKeyDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(final String key, final DeserializationContext ctxt) {
            return Money.parse(key);
        }
    }

    private static class BigMoneyKeyDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(final String key, final DeserializationContext ctxt) {
            return BigMoney.parse(key);
        }
    }
}
