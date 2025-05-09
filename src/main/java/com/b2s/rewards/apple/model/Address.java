package com.b2s.rewards.apple.model;

import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static com.b2s.rewards.apple.util.AppleUtil.isStringsEqualIgnoreCase;

/** xxxx
 * Created by rperumal on 6/26/2015.
 */
public class Address implements Serializable {

    private static final long serialVersionUID = 6634472031405814330L;
    private transient Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String businessName;
    private String address1;
    private String address2;
    private String address3 = "";
    private String subCity;
    private String city;
    private String state;
    private String zip5;
    private String zip4;
    private String country;
    private String phoneNumber;
    private String faxNumber;
    private transient Integer version;
    private String email;
    private Map<String, String> errorMessage = new HashMap<>();
    private Map<String, String> warningMessage = new HashMap<>();
    private String ignoreSuggestedAddress;
    private Boolean isValidAddress = false;
    private boolean cartTotalModified;
    private long selectedAddressId;
    private String addressModified;

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return AppleUtil.replaceNull(firstName);
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return AppleUtil.replaceNull(middleName);
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return AppleUtil.replaceNull(lastName);
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getBusinessName() {
        return AppleUtil.replaceNull(businessName);
    }

    public void setBusinessName(final String businessName) {
        this.businessName = businessName;
    }

    public String getAddress1() {
        return AppleUtil.replaceNull(address1);
    }

    public void setAddress1(final String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return AppleUtil.replaceNull(address2);
    }

    public void setAddress2(final String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getSubCity() {
        return subCity;
    }

    public void setSubCity(String subCity) {
        this.subCity = subCity;
    }

    public String getCity() {
        return AppleUtil.replaceNull(city);
    }

    public void setCity(final String city) {
        this.city = city;
    }


    public String getState() {
        return AppleUtil.replaceNull(state);
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getZip5() {
        return AppleUtil.replaceNull(zip5);
    }

    public void setZip5(final String zip5) {
        this.zip5 = zip5;
    }

    public String getZip4() {
        return AppleUtil.replaceNull(zip4);
    }

    public void setZip4(final String zip4) {
        this.zip4 = zip4;
    }

    public String getCountry() {
        return AppleUtil.replaceNull(country);
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getPhoneNumber() {
        return AppleUtil.replaceNull(phoneNumber);
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return AppleUtil.replaceNull(faxNumber);
    }

    public void setFaxNumber(final String faxNumber) {
        this.faxNumber = faxNumber;
    }

    @JsonIgnore
    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public String getEmail() {
        return AppleUtil.replaceNull(email);
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public Map<String, String> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final Map<String, String> errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, String> getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(final Map<String, String> warningMessage) {
        this.warningMessage = warningMessage;
    }

    public String getIgnoreSuggestedAddress() {
        return ignoreSuggestedAddress;
    }

    public void setIgnoreSuggestedAddress(final String ignoreSuggestedAddress) {
        this.ignoreSuggestedAddress = ignoreSuggestedAddress;
    }

    @JsonIgnore
    public boolean canCalculateWithFeesAndTaxes(final Boolean addressCheckNeeded) {
        if (addressCheckNeeded && CommonConstants.COUNTRIES_WITH_NO_STATE.contains(this.getCountry())) {
            return isAllMandatoryFieldsAvailable();
        }

        return canCalculateWithFeesAndTaxes();
    }

    @JsonIgnore
    public boolean canCalculateWithFeesAndTaxes() {
        if (CommonConstants.COUNTRIES_WITH_NO_STATE.contains(this.getCountry())) {
            return true;
        }

        //Rest of the countries.
        return (isNotBlank(this.getState()) && isAllMandatoryFieldsAvailable());
    }

    private boolean isAllMandatoryFieldsAvailable() {
        return isNotBlank(this.getZip5()) &&
                isNotBlank(this.city) &&
                isNotBlank(this.address1) &&
                isNotBlank(this.getEmail()) &&
                isNotBlank(this.getPhoneNumber()) &&
                isNotBlank(this.getFirstName()) &&
                isNotBlank(this.getLastName());
    }

    public Boolean isValidAddress() {
        return isValidAddress;
    }

    public void setIsValidAddress(final Boolean isValidAddress) {
        this.isValidAddress = isValidAddress;
    }

    public boolean isCartTotalModified() {
        return cartTotalModified;
    }

    public void setCartTotalModified(boolean cartTotalModified) {
        this.cartTotalModified = cartTotalModified;
    }

    public long getSelectedAddressId() {
        return selectedAddressId;
    }

    public void setSelectedAddressId(final long selectedAddressId) {
        this.selectedAddressId = selectedAddressId;
    }

    public String getAddressModified() {
        return addressModified;
    }

    public void setAddressModified(String addressModified) {
        this.addressModified = addressModified;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Address address = (Address) o;

        if (phoneNumber != null ? address.phoneNumber != null &&
            !CommonConstants.REGEX_NOT_NUMERIC.matcher(phoneNumber).replaceAll("")
                .equalsIgnoreCase(CommonConstants.REGEX_NOT_NUMERIC.matcher(address.phoneNumber).replaceAll("")) :
            address.phoneNumber != null) {
            return false;
        }

        return isStringsEqualIgnoreCase(firstName, address.firstName) &&
            isStringsEqualIgnoreCase(middleName, address.middleName) &&
            isStringsEqualIgnoreCase(lastName, address.lastName) &&
            isStringsEqualIgnoreCase(businessName, address.businessName) &&
            isStringsEqualIgnoreCase(address1, address.address1) &&
            isStringsEqualIgnoreCase(address2, address.address2) &&
            isStringsEqualIgnoreCase(address3, address.address3) &&
            isStringsEqualIgnoreCase(subCity, address.subCity) &&
            isStringsEqualIgnoreCase(city, address.city) &&
            isStringsEqualIgnoreCase(state, address.state) &&
            isStringsEqualIgnoreCase(zip5, address.zip5) &&
            isStringsEqualIgnoreCase(country, address.country) &&
            isStringsEqualIgnoreCase(faxNumber, address.faxNumber) &&
            isStringsEqualIgnoreCase(email, address.email);

    }

    @Override
    public int hashCode() {
        return Objects
            .hash(firstName, middleName, lastName, businessName, address1, address2, address3, subCity, city, state,
                zip5, zip4, country, phoneNumber, faxNumber, email);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", businessName='" + businessName + '\'' +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", address3='" + address3 + '\'' +
                ", subCity='" + subCity + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip5='" + zip5 + '\'' +
                ", zip4='" + zip4 + '\'' +
                ", country='" + country + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", faxNumber='" + faxNumber + '\'' +
                ", version=" + version +
                ", email='" + email + '\'' +
                '}';
    }
}
