package com.b2s.common.services.awp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.StringUtils;

/**
 * Created by hranganathan on 11/21/2017.
 */
@JsonDeserialize(builder = EmployeeRequestAddress.Builder.class)
public class EmployeeRequestAddress {

    private long selectedAddressId;
    private String address1;
    private String address2;
    private String address3;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    private EmployeeRequestAddress(final Builder builder) {
        this.address1 = builder.address1;
        this.address2 = builder.address2;
        this.address3 = builder.address3;
        this.city = builder.city;
        this.state = builder.state;
        this.country = builder.country;
        this.postalCode = builder.postalCode;
        this.selectedAddressId = builder.selectedAddressId;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getAddress3() {
        return address3;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public long getSelectedAddressId() {
        return selectedAddressId;
    }

    public static class Builder {

        private String address1;
        private String address2;
        private String address3;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        private long selectedAddressId;

        public Builder withSelectedAddressId(final long selectedAddressId) {
            this.selectedAddressId = selectedAddressId;
            return this;
        }

        public Builder withAddress1(final String address1) {
            this.address1 = address1;
            return this;
        }

        public Builder withAddress2(final String address2) {
            this.address2 = address2;
            return this;
        }

        public Builder withAddress3(final String address3) {
            this.address3 = address3;
            return this;
        }

        public Builder withCity(final String city) {
            this.city = city;
            return this;
        }

        public Builder withState(final String state) {
            this.state = state;
            return this;
        }

        public Builder withCountry(final String country) {
            this.country = country;
            return this;
        }

        public Builder withPostalCode(final String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public EmployeeRequestAddress build() {
            if (StringUtils.isEmpty(address1) || StringUtils.isEmpty(city) || StringUtils.isEmpty(country) ||
                StringUtils.isEmpty(postalCode)) {
                throw new IllegalArgumentException("Request address cannot be empty");
            }
            return new EmployeeRequestAddress(this);
        }

    }
}
