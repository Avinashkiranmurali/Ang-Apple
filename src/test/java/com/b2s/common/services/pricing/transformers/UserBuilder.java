package com.b2s.common.services.pricing.transformers;

import com.b2s.shop.common.User;

import java.util.Locale;

/**
 * @author jkattookaren Created on 9/23/2014.
 */
public class UserBuilder {
    public static final User GENERIC_USER = builder()
            .withCountry("XX")
            .withState("YY")
            .withVarId("varId")
            .withProgramId("programId")
            .withConversionRate(1.00)
            .withLocale(Locale.getDefault()).build();

    public static final User GENERIC_CA_USER = builder()
            .withCountry("CA")
            .withState("QC")
            .withVarId("varId")
            .withProgramId("programId")
            .withConversionRate(1.00)
            .withLocale(Locale.CANADA).build();

    public static final User GENERIC_US_USER = builder()
            .withCountry("US")
            .withState("GA")
            .withVarId("varId")
            .withProgramId("programId")
            .withConversionRate(1.00)
            .withLocale(Locale.US).build();

    private String country;
    private String state;
    private String varId;
    private String programId;
    private Locale locale;
    private double conversionRate;

    private UserBuilder() {
    }

    public static com.b2s.common.services.pricing.transformers.UserBuilder builder() {
        return new com.b2s.common.services.pricing.transformers.UserBuilder();
    }

    public User build() {
        final User user = new User();
        user.setCountry(this.country);
        user.setState(this.state);
        user.setVarId(this.varId);
        user.setProgramId(this.programId);
        user.setLocale(this.locale);
        return user;
    }

    public com.b2s.common.services.pricing.transformers.UserBuilder withCountry(final String countryFrom) {
        this.country = countryFrom;
        return this;
    }

    public com.b2s.common.services.pricing.transformers.UserBuilder withState(final String stateFrom) {
        this.state = stateFrom;
        return this;
    }

    public com.b2s.common.services.pricing.transformers.UserBuilder withVarId(final String varIdFrom) {
        this.varId = varIdFrom;
        return this;
    }

    public com.b2s.common.services.pricing.transformers.UserBuilder withProgramId(final String programIdFrom) {
        this.programId = programIdFrom;
        return this;
    }

    public com.b2s.common.services.pricing.transformers.UserBuilder withLocale(final Locale localeFrom) {
        this.locale = localeFrom;
        return this;
    }

    public com.b2s.common.services.pricing.transformers.UserBuilder withConversionRate(final double conversionRateFrom) {
        this.conversionRate = conversionRateFrom;
        return this;
    }

}
