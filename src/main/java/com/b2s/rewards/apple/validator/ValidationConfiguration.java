package com.b2s.rewards.apple.validator;

import com.b2s.rewards.common.util.CommonConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by vprasanna on 11/16/17.
 */
public enum ValidationConfiguration {

    FIRST_NAME(Arrays.asList(Country.SG, Country.MY)),
    LAST_NAME(Arrays.asList(Country.SG, Country.MY)),
    ADDRESS1(Arrays.asList(Country.SG, Country.MY)),
    ADDRESS2(Arrays.asList(Country.SG, Country.MY)),
    ADDRESS3(Arrays.asList(Country.SG, Country.MY)),
    ADDRESS4(Arrays.asList(Country.SG, Country.MY)),
    CITY(Arrays.asList(Country.SG, Country.MY)),
    STATE(Arrays.asList(Country.SG, Country.MY)),
    POSTAL_CODE(Arrays.asList(Country.SG, Country.MY)),
    COUNTRY(Arrays.asList(Country.SG, Country.MY)),
    TELEPHONE1(Arrays.asList(Country.SG, Country.MY)),
    TELEPHONE2(Arrays.asList(Country.SG, Country.MY));

    private List<Country> countries;

    ValidationConfiguration(List<Country> countries) {
        this.countries = countries;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public Country findCountry(final String countryCode) {
        Optional<Country> foundCountry = Arrays.asList(Country.values()).stream()
                .filter(country -> country.toString().equalsIgnoreCase(countryCode))
                .findFirst();

        //If we have a match send the country instance else default
        if (foundCountry.isPresent()) {
            return foundCountry.get();
        } else {
            return Country.DEFAULT;
        }
    }

    public enum Country {
        SG("firstName", "lastName", "phoneNumber", "postalCode"),
        MY("firstName", "lastName", "phoneNumber", "postalCode"),
        DEFAULT(CommonConstants.RECEPIENT_NAME_REG_EX, CommonConstants.RECEPIENT_NAME_REG_EX, "", "");
        String firstName;
        String lastName;
        String phoneNumber;
        String postalCode;

        Country(String firstName,
                String lastName,
                String phoneNumber,
                String postalCode) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
            this.postalCode = postalCode;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getPostalCode() {
            return postalCode;
        }
    }
}
