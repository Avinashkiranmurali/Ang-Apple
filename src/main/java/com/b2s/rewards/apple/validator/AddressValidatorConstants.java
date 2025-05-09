package com.b2s.rewards.apple.validator;

import java.util.regex.Pattern;

public class AddressValidatorConstants {

    private AddressValidatorConstants() {
        // This is a utility class, it cannot be instantiated
    }

    public static final String INVALID_CITY = "invalidCity";
    public static final String INVALID_NAME = "invalidName";
    public static final String INVALID_EMAIL_ADDRESS = "invalidEmailAddress";
    public static final String COUNTRY_MISSING = "countryMissing";
    public static final String STATE_PROVINCE_MISSING = "stateProvinceMissing";
    public static final String INVALID_PHONE_NUMBER = "invalidPhoneNumber";
    public static final int ZIP_MIN_LENGTH_3 = 3;
    public static final int ZIP_LENGTH_4 = 4;
    public static final int ZIP_LENGTH_5 = 5;
    public static final int ZIP_LENGTH_6 = 6;
    public static final String INVALID_ZIP_4 = "invalidZip4";
    public static final String INVALID_ZIP_5 = "invalidZip5";
    public static final String INVALID_ZIP_6 = "invalidZip6";
    public static final String INVALID_ZIP_7 = "invalidZip7";
    public static final String EMAIL_REGEX = "^\\S+@\\S+\\.\\S+$";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    public static final String INVALID_ADDRESS_LINE_1 = "invalidAddressLine1";
    public static final String INVALID_ADDRESS_LINE_3 = "invalidAddressLine3";
    public static final String INVALID_EMAIL = "invalidEmail";
    public static final String PO_BOX_NOT_ALLOWED = "poBoxNotAllowed";
    public static final String INVALID_POSTAL_CODE = "invalidPostalCode";
    public static final String INVALID_DISTRICT = "invalidDistrict";
    public static final String INVALID_TOWN = "invalidTown";

}
