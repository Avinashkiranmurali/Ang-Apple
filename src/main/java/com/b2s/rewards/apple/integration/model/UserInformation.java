package com.b2s.rewards.apple.integration.model;

import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.b2s.rewards.apple.validator.AddressMapper.setZipCode;

/**
 * Created by rpillai on 2/19/2016.
 */
public class UserInformation {

    private Map<String, String> additionalInfo;
    private Address address;
    private String dateOfBirth;
    private EmailAddress[] emailAddresses = null;
    private String firstName;
    private String lastName;
    private String middleName;
    private PhoneNumber[] phoneNumbers = null;
    private String title;
    private boolean deceased;
    private List<Address> additionalAddresses;

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public EmailAddress[] getEmailAddresses() {
        if (emailAddresses != null) {
            return Arrays.copyOf(emailAddresses, emailAddresses.length);
        }else {
            return null;
        }
    }

    public void setEmailAddresses(final EmailAddress[] emailAddresses) {
        if (emailAddresses != null) {
            this.emailAddresses = Arrays.copyOf(emailAddresses, emailAddresses.length);
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    public PhoneNumber[] getPhoneNumbers() {
        if (phoneNumbers != null) {
            return Arrays.copyOf(phoneNumbers, phoneNumbers.length);
        } else {
            return null;
        }
    }

    public void setPhoneNumbers(final PhoneNumber[] phoneNumbers) {
        if (phoneNumbers != null) {
            this.phoneNumbers = Arrays.copyOf(phoneNumbers, phoneNumbers.length);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public boolean isDeceased() {
        return deceased;
    }

    public void setDeceased(final boolean deceased) {
        this.deceased = deceased;
    }

    public List<Address> getAdditionalAddresses() {
        return additionalAddresses;
    }

    public void setAdditionalAddresses(final List<Address> additionalAddresses) {
        this.additionalAddresses = additionalAddresses;
    }

    public void copyDataToUser(final User user) {
            this.copyDataToUser(user, false);
    }


    public void copyDataToUser(final User user, final boolean disableAdditionalAddresses) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        if(ArrayUtils.isNotEmpty(emailAddresses)) {
            user.setEmail(emailAddresses[0] != null ? emailAddresses[0].getEmail() : "");
        }
        if(address != null) {
            user.setAddr1(address.getLine1());
            user.setAddr2(address.getLine2());
            user.setCity(address.getCity());
            user.setState(address.getStateCode());
            user.setZip(address.getPostalCode());
            user.setCountry(address.getCountryCode());
        }
        if(ArrayUtils.isNotEmpty(phoneNumbers)) {
            user.setPhone(phoneNumbers[0] != null ? phoneNumbers[0].getNumber() : "");
        }

        if(MapUtils.isNotEmpty(user.getAdditionalInfo())){
            if(MapUtils.isNotEmpty(additionalInfo)){
                user.getAdditionalInfo().putAll(additionalInfo);
            }
        }else{
            user.setAdditionalInfo(additionalInfo);
        }

        user.setDeceased(deceased);

        if (!disableAdditionalAddresses && CollectionUtils.isNotEmpty(additionalAddresses)) {
            final AtomicLong id = new AtomicLong(1);
            final List<com.b2s.common.services.model.Address> additionalAddressInfo = new ArrayList<>();

            if (Objects.nonNull(address)) {
                final com.b2s.common.services.model.Address.AddressBuilder primaryAddress =
                        addressFrom(address, id.getAndIncrement(), user.getPhone());
                setZipCode(address.getCountryCode(), address.getPostalCode(), primaryAddress);

                additionalAddressInfo.add(primaryAddress.build());
            }

            this.additionalAddresses.stream()
                    .map(address -> {
                        final com.b2s.common.services.model.Address.AddressBuilder additionalAddress =
                                addressFrom(address, id.getAndIncrement(), user.getPhone());
                        setZipCode(address.getCountryCode(), address.getPostalCode(), additionalAddress);

                        return additionalAddress.build();
                    })
                    .forEach(additionalAddressInfo::add);

            user.setAddresses(additionalAddressInfo);
        }
    }

    private com.b2s.common.services.model.Address.AddressBuilder addressFrom(
        final Address addressInfo,
        final long id,
        final String phone) {

        return com.b2s.common.services.model.Address.builder()
            .withAddressId(id)
            .withAddress1(addressInfo.getLine1())
            .withAddress2(addressInfo.getLine2())
            .withCity(addressInfo.getCity())
            .withState(addressInfo.getStateCode())
            .withPostalCode(addressInfo.getPostalCode())
            .withCountry(addressInfo.getCountryCode())
            .withPhoneNumber(phone);
    }
}
