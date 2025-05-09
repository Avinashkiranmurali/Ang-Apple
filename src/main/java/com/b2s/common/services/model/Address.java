package com.b2s.common.services.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.io.Serializable;

/**
 * Created by hranganathan on 10/16/2017.
 */
@JsonDeserialize(builder = Address.AddressBuilder.class)
public class Address implements Serializable {

    private static final long serialVersionUID = 3833878407248544199L;
    private final long addressId;
    private final String addressName;
    private final String phoneNumber;
    private final boolean active;
    private final String address1;
    private final String address2;
    private final String address3;
    private final String city;
    private final String state;
    private final String postalCode;
    private final String country;
    private final String zip5;
    private final String zip4;

    private Address(final AddressBuilder addressResponseBuilder) {
        this.addressId = addressResponseBuilder.addressId;
        this.addressName = addressResponseBuilder.addressName;
        this.phoneNumber = addressResponseBuilder.phoneNumber;
        this.active = addressResponseBuilder.active;
        this.address1 = addressResponseBuilder.address1;
        this.address2 = addressResponseBuilder.address2;
        this.city = addressResponseBuilder.city;
        this.state = addressResponseBuilder.state;
        this.postalCode = addressResponseBuilder.postalCode;
        this.address3 = addressResponseBuilder.address3;
        this.country = addressResponseBuilder.country;
        this.zip5 = addressResponseBuilder.zip5;
        this.zip4 = addressResponseBuilder.zip4;
    }

    public long getAddressId() {
        return addressId;
    }

    public String getAddressName() {
        return addressName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getAddress3() {
        return address3;
    }

    public String getCountry() {
        return country;
    }

    public String getZip5() {
        return zip5;
    }

    public String getZip4() {
        return zip4;
    }

    public static AddressBuilder builder() {
        return new AddressBuilder();
    }

    @JsonPOJOBuilder(buildMethodName = "build")
    public static class AddressBuilder {

        private long addressId;
        private String addressName;
        private String phoneNumber;
        private boolean active;
        private String address1;
        private String address2;
        private String address3;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String zip5;
        private String zip4;

        public AddressBuilder withAddressId(final long addressId) {
            this.addressId = addressId;
            return this;
        }

        public AddressBuilder withAddressName(final String addressName) {
            this.addressName = addressName;
            return this;
        }

        public AddressBuilder withPhoneNumber(final String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public AddressBuilder withActive(final boolean active) {
            this.active = active;
            return this;
        }

        public AddressBuilder withAddress1(final String address1) {
            this.address1 = address1;
            return this;
        }

        public AddressBuilder withAddress2(final String address2) {
            this.address2 = address2;
            return this;
        }

        public AddressBuilder withAddress3(final String address3) {
            this.address3 = address3;
            return this;
        }

        public AddressBuilder withCity(final String city) {
            this.city = city;
            return this;
        }

        public AddressBuilder withState(final String state) {
            this.state = state;
            return this;
        }

        public AddressBuilder withPostalCode(final String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public AddressBuilder withCountry(final String country) {
            this.country = country;
            return this;
        }

        public AddressBuilder withZip5(final String zip5) {
            this.zip5 = zip5;
            return this;
        }

        public AddressBuilder withZip4(final String zip4) {
            this.zip4 = zip4;
            return this;
        }

        public Address build() {
            return new Address(this);
        }

    }

}
