package com.b2s.rewards.apple.integration.model;

/**
 * Created by rperumal on 8/28/2015.
 */
public class Address {
    private String line1;
    private String line2;
    private String line3;
    private String city;
    private String stateCode;
    private String postalCode;
    private String countryCode;

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getLine3() {
        return line3;
    }

    public void setLine3(String line3) {
        this.line3 = line3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    @Override
    public String toString() {
        return "Address{" +
                "line1='" + line1 + '\'' +
                ", line2=" + line2 +
                ", line3=" + line3 +
                ", city=" + city +
                ", stateCode=" + stateCode +
                ", postalCode=" + postalCode +
                ", countryCode=" + countryCode +
                '}';
    }
}
