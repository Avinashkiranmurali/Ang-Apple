package com.b2s.rewards.apple.validator;

import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.common.constant.Constant;
import com.b2s.web.B2RReloadableResourceBundleMessageSource;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.MessageSource;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.b2s.rewards.apple.util.AppleUtil.getProgramConfigValueAsBoolean;
import static com.b2s.rewards.apple.util.AppleUtil.getProgramConfigValueAsList;
import static com.b2s.rewards.apple.validator.AddressValidatorConstants.*;
import static com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.NATIONAL;

/**
 * Created by rperumal on 3/11/2016.
 */
public class AddressValidatorImpl implements AddressValidatorIF {

    protected static final String EMAIL_REGEX = "^\\S+@\\S+\\.\\S+$";
    protected static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    protected static final Logger logger = LoggerFactory.getLogger(AddressValidatorImpl.class);
    public static final String IGNORE_SUGGESTED_ADDRESS = "ignoreSuggestedAddress";
    @JsonIgnore
    protected transient String phoneNumberRegEx = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
    protected transient String poBoxRegEx1 = "(^|(?:post(al)? *(?:office *)?|p[. ]*o\\.? *))box *#? *(\\w+)";
    protected transient String poBoxRegEx2 = "[pP][\\s]*[.]*[\\s]*[oO][\\s]*[.]*[\\s]*[bB][\\s]*[oO][\\s]*[xX]";
    protected transient String poBoxRegEx3 = "[pP][\\s]*[.]*[\\s]*[oO][\\s]*[.]*[\\s]*[#]*[\\s]*[\\d]";
    protected transient String numeralZipRegx = "^[0-9]{%d}";
    protected transient String zipMinMaxRegx = "^(?=[0-9]*$)(?:.{%d}|.{%d})$";
    protected transient String zip5RexEx = "^[0-9]{5}";
    protected transient String zip4RexEx = "^[0-9]{4}";
    protected transient Pattern booleanPattern = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
    private Map<String, String> errorMessage = new HashMap<>();
    private Map<String, String> warningMessage = new HashMap<>();

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

    //validate email
    @JsonIgnore
    public boolean isValidEmail(final String email) {
        final Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
        return (emailMatcher.matches() && EmailValidator.getInstance().isValid(email));
    }


    // Check if address has PO Box
    public boolean hasPoBox(final String address1, final String address2) {
        final Pattern poBoxPattern1 = Pattern.compile(poBoxRegEx1, Pattern.CASE_INSENSITIVE);
        final Pattern poBoxPattern2 = Pattern.compile(poBoxRegEx2, Pattern.CASE_INSENSITIVE);
        final Pattern poBoxPattern3 = Pattern.compile(poBoxRegEx3, Pattern.CASE_INSENSITIVE);

        if (poBoxPattern1.matcher(address1).find() ||
                poBoxPattern2.matcher(address1).find() ||
                poBoxPattern3.matcher(address1).find() ||
                poBoxPattern1.matcher(address2).find() ||
                poBoxPattern2.matcher(address2).find() ||
                poBoxPattern3.matcher(address2).find()) {
            return true;
        }
        return false;
    }

    //check of AddressLine1 has any PO box
    public boolean hasPOBoxInAddress1(final String address1) {
        final Pattern poBoxPattern1 = Pattern.compile(poBoxRegEx1, Pattern.CASE_INSENSITIVE);
        final Pattern poBoxPattern2 = Pattern.compile(poBoxRegEx2, Pattern.CASE_INSENSITIVE);
        final Pattern poBoxPattern3 = Pattern.compile(poBoxRegEx3, Pattern.CASE_INSENSITIVE);

        if (poBoxPattern1.matcher(address1).find() ||
                poBoxPattern2.matcher(address1).find() ||
                poBoxPattern3.matcher(address1).find()) {
            return true;
        }
        return false;
    }

    //check of AddressLine2 has any PO box
    public boolean hasPOBoxInAddress2(final String addressLine2) {
        final Pattern poBoxPattern1 = Pattern.compile(poBoxRegEx1, Pattern.CASE_INSENSITIVE);
        final Pattern poBoxPattern2 = Pattern.compile(poBoxRegEx2, Pattern.CASE_INSENSITIVE);
        final Pattern poBoxPattern3 = Pattern.compile(poBoxRegEx3, Pattern.CASE_INSENSITIVE);

        if (poBoxPattern1.matcher(addressLine2).find() ||
                poBoxPattern2.matcher(addressLine2).find() ||
                poBoxPattern3.matcher(addressLine2).find() ||
                poBoxPattern3.matcher(addressLine2).matches()) {
            return true;
        }
        return false;
    }

    public boolean adheresToPattern(final String matchString, final String pattern) {
        if (StringUtils.isEmpty(matchString)) {
            return false;
        }

        final Pattern matchPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        return matchPattern.matcher(matchString).matches();
    }

    public boolean find(final String matchString, final String pattern) {
        if (StringUtils.isEmpty(matchString)) {
            return false;
        }
        final Pattern matchPattern = Pattern.compile(pattern);
        return matchPattern.matcher(matchString).find();
    }

    @Override
    public boolean hasValidationError(final Address address, final MessageSource deprecatedMessageSource, final User user) {
        final Locale userLocale= user.getLocale();
        final MessageSource messageSource = getMessageSource(user.getVarId());
        boolean hasValidationError = false;

        //Replace Special characters with Empty
        replaceSpecialCharWithEmpty(address, userLocale.toString());

        //Allow only characters for First Name, Last Name & City
        checkNameWithRegExp(address, messageSource, userLocale, address.getFirstName(),
            CartItemOption.FIRST_NAME.getValue());
        checkNameWithRegExp(address, messageSource, userLocale, address.getLastName(),
            CartItemOption.LAST_NAME.getValue());

        validateAddressLines(address, messageSource, userLocale);
        validateCity(address, messageSource, userLocale, CommonConstants.CHAR_SPACE_AND_DASH_ONLY_REG_EX);

        //Zip5
        zipCodeValidation(address, messageSource, userLocale);

        //phone number
        // Convert the entered phone number to country national format
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        //Country
        mandatoryNullCheck(address, address.getCountry(),
            ServiceExceptionEnums.COUNTRY_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.COUNTRY.getValue(),
            messageSource.getMessage(COUNTRY_MISSING, null, COUNTRY_MISSING, userLocale));

        //State
        //Ship to only 50 states if country is US
        mandatoryStateCheck(address, messageSource, userLocale);

        //Email
        mandatoryEmailCheck(address, messageSource, userLocale);

        //IgnoreSuggestedAddress - To skip Melissa address validation, in case customer chooses to ignore the recommended address sent by Melissa validation
        setAndValidateIgnoreSuggestedAddress(address, userLocale, messageSource);

        if (!address.getErrorMessage().isEmpty()) {
            hasValidationError = true;
        }

        return hasValidationError;
    }

    protected void setAndValidateIgnoreSuggestedAddress(final Address address, final Locale userLocale,
        final MessageSource messageSource) {
        final String ignoreSuggestedAddress = CartItemOption.IGNORE_SUGGESTED_ADDRESS.getValue();
        address.setIgnoreSuggestedAddress(StringUtils.isBlank(address.getIgnoreSuggestedAddress()) ? "false" : address.getIgnoreSuggestedAddress());
        if (!booleanPattern.matcher(address.getIgnoreSuggestedAddress().trim()).matches()) {
            logger.error(ServiceExceptionEnums.IGNORE_SUGGESTED_ADDRESS.getErrorMessage());
            address.getErrorMessage().put(ignoreSuggestedAddress, messageSource.getMessage(
                ignoreSuggestedAddress, null, ignoreSuggestedAddress, userLocale));
        }
    }

    /**
     * Chek fieldName with reg exp.
     *
     * @param address       the address
     * @param messageSource the message source
     * @param userLocale    the user locale
     * @param fieldValue    the first fieldName
     * @param field         the value
     */
    protected void checkNameWithRegExp(Address address, MessageSource messageSource, Locale userLocale,
        String fieldValue, String field) {
        if (StringUtils.isBlank(fieldValue) ||
            !(adheresToPattern(AppleUtil.decodeSpecialChar(fieldValue), CommonConstants.RECEPIENT_NAME_REG_EX) &&
                find(fieldValue, CommonConstants.CHAR_WITH_ACCENT_ONLY_REG_EX))) {
            logger.error(ServiceExceptionEnums.ALPHA_ONLY_CHARS_EXCEPTION.getErrorMessage());
            if (!address.getErrorMessage()
                .containsValue(messageSource.getMessage(INVALID_NAME, null, INVALID_NAME, userLocale))) {
                address.getErrorMessage()
                    .put(field, messageSource.getMessage(INVALID_NAME, null, INVALID_NAME, userLocale));
            }
        }
    }

    /**
     * Mandatory check.
     *
     * @param address               the address for which validation require
     * @param fieldValue            the field name
     * @param serviceErrorMessage   the error message
     * @param field                 the field
     * @param message               the message
     */
    protected void mandatoryNullCheck(Address address, String fieldValue, String serviceErrorMessage, String field,
        String message) {
        if (StringUtils.isBlank(fieldValue)) {
            logger.error(serviceErrorMessage);
            address.getErrorMessage().put(field, message);
        }
    }

    /**
     * Mandatory check.
     *
     * @param address       the address for which validation require
     * @param messageSource the message source
     * @param userLocale    the user locale
     */
    protected void mandatoryPhoneCheckandSet(final Address address, final MessageSource messageSource,
        final Locale userLocale) {
        final String phoneNumber = address.getPhoneNumber();
        final String serviceErrorMessage = ServiceExceptionEnums.INVALID_PHONE_NUMBER.getErrorMessage();
        final String field = CartItemOption.PHONE_NUMBER.getValue();
        final String message = messageSource.getMessage(INVALID_PHONE_NUMBER, null, INVALID_PHONE_NUMBER, userLocale);
        if (StringUtils.isBlank(phoneNumber)) {
            logger.error(serviceErrorMessage,phoneNumber);
            address.getErrorMessage().put(field, message);
        }else{
            final String formattedPhone = formatPhoneNumber(phoneNumber, address.getCountry(), NATIONAL);
            if (formattedPhone == null) {
                logger.error(serviceErrorMessage,phoneNumber);
                address.getErrorMessage().put(field, message);
            }
            else{
                address.setPhoneNumber(formattedPhone);
            }
        }

    }

    /**
     * Mandatory check.
     *
     * @param address           the address for which validation require
     * @param messageSource     the messageSource to get message
     * @param userLocale        the userLocale
     */
    protected void mandatoryCityCheck(final Address address, final MessageSource messageSource,
        final Locale userLocale) {
        if (!validateCity(address.getCity(), address.getCountry())) {
            logger.error(ServiceExceptionEnums.CITY_EMPTY_EXCEPTION.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.CITY.getValue(),
                messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));
        }
    }

    /**
     * Mandatory check.
     *
     * @param address           the address for which validation require
     * @param messageSource     the messageSource to get message
     * @param userLocale        the userLocale
     */
    protected void mandatoryStateCheck(final Address address, final MessageSource messageSource,
        final Locale userLocale) {
        if (!validateState(address.getState(), address.getCountry())) {
            logger.error(ServiceExceptionEnums.STATE_EMPTY_EXCEPTION.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.STATE.getValue(), messageSource.getMessage(
                STATE_PROVINCE_MISSING, null, STATE_PROVINCE_MISSING, userLocale));
        }

    }


    /**
     * Mandatory check.
     *
     * @param address           the address for which validation require
     * @param fieldValue        the field name
     * @param field             the field
     * @param messageSource     the messageSource to get message
     * @param userLocale        the userLocale
     */
    protected void addressPOBoxCheck(final Address address, String fieldValue, String field,
        final MessageSource messageSource, final Locale userLocale) {
        if (!StringUtils.isBlank(fieldValue) && hasPOBoxInAddress2(fieldValue)) {
            logger.error(ServiceExceptionEnums.PO_BOX_NOT_ALLOWED.getErrorMessage());
            address.getErrorMessage().put(field, messageSource.getMessage(PO_BOX_NOT_ALLOWED, null,
                PO_BOX_NOT_ALLOWED, userLocale));   // Need to test both scenarios and capture ss
        }
    }

    /**
     * Mandatory check.
     *
     * @param address           the address for which validation require
     * @param zipLength         the zip code length
     * @param messageSource     the messageSource to get message
     * @param userLocale        the userLocale
     */
    protected void mandatoryZipCheck(final Address address, int zipLength, final MessageSource messageSource,
        final Locale userLocale) {
        final String postalCodeRegex = String.format(numeralZipRegx, zipLength);
        final String fieldValue = address.getZip5();
        if (StringUtils.isBlank(fieldValue) || (fieldValue.trim().length() != zipLength) || (!Pattern.compile(postalCodeRegex).matcher(fieldValue).matches())) {
            logger.error(ServiceExceptionEnums.INVALID_ZIP_CODE.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.ZIP5.getValue(), messageSource.getMessage(INVALID_ZIP_5, null, INVALID_ZIP_5, userLocale));
        }
    }

    protected void mandatoryZipCheck(final Address address, int minLength, int maxLength, String message) {
        final String postalCodeRegex = String.format(zipMinMaxRegx, minLength,maxLength);
        final String fieldValue = address.getZip5();

        if (StringUtils.isBlank(fieldValue) || ((fieldValue.trim().length() != minLength) && (fieldValue.trim().length() != maxLength)) || (!Pattern.compile(postalCodeRegex).matcher(fieldValue).matches())) {
            logger.error(ServiceExceptionEnums.INVALID_ZIP_CODE.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.ZIP5.getValue(), message);
        }
    }

    /**
     * Mandatory check.
     *
     * @param address          the address for which validation require
     * @param messageSource    the messageSource to get message
     * @param userLocale       the userLocale
     */
    protected void mandatoryEmailCheck(final Address address, final MessageSource messageSource,
        final Locale userLocale) {
        if (!isValidEmail(address.getEmail())) {
            logger.error(ServiceExceptionEnums.INVALID_EMAIL_ADDRESS.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.EMAIL.getValue(), messageSource.getMessage(INVALID_EMAIL_ADDRESS, null, INVALID_EMAIL_ADDRESS, userLocale));
        }
    }

    /**
     * Basic state validations
     * 1. check for empty string
     * 2. If US or CA, check the list of valid states
     *
     * @param state
     * @param countryCode
     * @return
     */
    protected boolean validateState(final String state, final String countryCode) {
        if(StringUtils.isBlank(state)) {
            return false;
        }

        final Map<String, String> states = Constant.getStatesByCountry(countryCode);

        if(MapUtils.isNotEmpty(states) &&
            states.keySet().stream().filter(key -> key.equalsIgnoreCase(state)).findFirst().isEmpty()) {
            return false;
        }
        return true;
    }


    protected boolean validateCity(final String city, final String countryCode) {
        boolean isValidCity = false;
        final Map<String, String> cities = Constant.getCitiesByCountry(countryCode);
        if(StringUtils.isNotEmpty(city) && MapUtils.isNotEmpty(cities)  && cities.containsKey((StringUtils.upperCase(city)))) {
            isValidCity =  true;
        }
        return isValidCity;
    }

    //check if the given phone number is valid
    @JsonIgnore
    protected String formatPhoneNumber(final String phoneNumber, final String countryCode, final
        PhoneNumberUtil.PhoneNumberFormat phoneNumberFormat) {
        return AddressMapper.formatPhoneNumber(phoneNumber, countryCode, phoneNumberFormat);

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorImpl{");
        sb.append("DEFAULT");
        sb.append('}');
        return sb.toString();
    }

    protected MessageSource getMessageSource(final String varId) {
        B2RReloadableResourceBundleMessageSource messageSource;
        try    {
            messageSource = (B2RReloadableResourceBundleMessageSource) AppContext.getApplicationContext().getBean("messageSource"+varId);
        }catch (BeansException e){
            logger.warn("No message source definition found for the given varId : {}." +
                    " Fetching default messageSource", varId);
            messageSource = (B2RReloadableResourceBundleMessageSource) AppContext.getApplicationContext().getBean("messageSource");
        }
        return messageSource;
    }

    public boolean isInvalidAddressUpdate(final Address cartAddress, final Address newShippingAddress, final Program program) {

        boolean addressLocked = getProgramConfigValueAsBoolean(program, "MercAddressLocked");
        boolean contactInfoLocked = getProgramConfigValueAsBoolean(program, "ContactInfoLocked");
        boolean shipToNameLocked = getProgramConfigValueAsBoolean(program, "ShipToNameLocked");
        boolean businessNameLocked = getProgramConfigValueAsBoolean(program, "businessNameLocked");
        List<String> contactInfoLockOverrides = getProgramConfigValueAsList(program, "ContactInfoLockOverrides");
        List<String> mercAddressLockOverrides = getProgramConfigValueAsList(program, "MercAddressLockOverrides");

        return shipToNameLockedFieldOverriddenValidation(cartAddress, newShippingAddress, shipToNameLocked) ||
            businessNameLockedFieldValidation(cartAddress, newShippingAddress, businessNameLocked) ||
            contactInfoLockedFieldValidation(cartAddress, newShippingAddress, contactInfoLocked, contactInfoLockOverrides) ||
            addressLockedFieldOverriddenValidation(cartAddress, newShippingAddress, addressLocked, mercAddressLockOverrides);
    }

    protected void validateAndSetAddress(final Address address, final MessageSource messageSource, final Locale userLocale) {
        //Replace Special characters with Empty
        replaceSpecialCharWithEmpty(address, userLocale.toString());

        //First & Last Name
        mandatoryNullCheck(address, address.getFirstName(),
            ServiceExceptionEnums.ALPHA_ONLY_CHARS_EXCEPTION.getErrorMessage(), CartItemOption.FIRST_NAME.getValue(),
            messageSource.getMessage(INVALID_NAME, null, INVALID_NAME, userLocale));
        mandatoryNullCheck(address, address.getLastName(),
            ServiceExceptionEnums.ALPHA_ONLY_CHARS_EXCEPTION.getErrorMessage(), CartItemOption.LAST_NAME.getValue(),
            messageSource.getMessage(INVALID_NAME, null, INVALID_NAME, userLocale));

        //Address1
        mandatoryNullCheck(address, address.getAddress1(),
            ServiceExceptionEnums.ADDRESS_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.ADDRESS1.getValue(),
            messageSource.getMessage(INVALID_ADDRESS_LINE_1, null, INVALID_ADDRESS_LINE_1, userLocale));
        addressPOBoxCheck(address, address.getAddress1(), CartItemOption.ADDRESS1.getValue(),
            messageSource, userLocale);

        //Country
        mandatoryNullCheck(address, address.getCountry(),
            ServiceExceptionEnums.COUNTRY_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.COUNTRY.getValue(),
            messageSource.getMessage(COUNTRY_MISSING, null, COUNTRY_MISSING, userLocale));

        mandatoryEmailCheck(address, messageSource, userLocale);
    }


    private boolean businessNameLockedFieldValidation(final Address cartAddress, final Address newShippingAddress,
        final boolean businessNameLocked) {
        return businessNameLocked &&
            isFieldOverridden(null, cartAddress.getBusinessName(), newShippingAddress.getBusinessName(), null);
    }

    private boolean contactInfoLockedFieldValidation(final Address cartAddress, final Address newShippingAddress,
        final boolean contactInfoLocked, final List<String> contactInfoLockOverrides) {
        return contactInfoLocked &&
            contactInfoLockedFieldOverriddenValidation(contactInfoLockOverrides, cartAddress, newShippingAddress);
    }

    private boolean shipToNameLockedFieldOverriddenValidation(final Address cartAddress,
        final Address newShippingAddress, final boolean shipToNameLocked) {
        return shipToNameLocked &&
            (isFieldOverridden(null, cartAddress.getFirstName(), newShippingAddress.getFirstName(), null) ||
                isFieldOverridden(null, cartAddress.getLastName(), newShippingAddress.getLastName(), null));
    }

    private boolean contactInfoLockedFieldOverriddenValidation(final List<String> contactInfoLockOverrides, final Address cartAddress,
                                                              final Address newShippingAddress) {
        if (isFieldOverridden(contactInfoLockOverrides, cartAddress.getEmail(), newShippingAddress.getEmail(), "email")) {
            return true;
        }
        if (isFieldOverridden(contactInfoLockOverrides, cartAddress.getPhoneNumber(), newShippingAddress.getPhoneNumber(), "phoneNumber")) {
            return true;
        }
        return false;
    }

    private boolean addressLockedFieldOverriddenValidation(final Address cartAddress,
        final Address newShippingAddress, final boolean addressLocked, final List<String> mercAddressLockOverrides) {
        return addressLocked &&
            (isFieldOverridden(mercAddressLockOverrides, cartAddress.getAddress1(), newShippingAddress.getAddress1(), "address1") ||
                isFieldOverridden(mercAddressLockOverrides, cartAddress.getAddress2(), newShippingAddress.getAddress2(), "address2") ||
                isFieldOverridden(mercAddressLockOverrides, cartAddress.getAddress3(), newShippingAddress.getAddress3(), "address3") ||
                isFieldOverridden(mercAddressLockOverrides, cartAddress.getSubCity(), newShippingAddress.getSubCity(), "subCity") ||
                isFieldOverridden(mercAddressLockOverrides, cartAddress.getCity(), newShippingAddress.getCity(), "city") ||
                isFieldOverridden(mercAddressLockOverrides, cartAddress.getState(), newShippingAddress.getState(), "state") ||
                isFieldOverridden(mercAddressLockOverrides, cartAddress.getZip4(), newShippingAddress.getZip4(), "zip4") ||
                isFieldOverridden(mercAddressLockOverrides, cartAddress.getZip5(), newShippingAddress.getZip5(), "zip5") ||
                isFieldOverridden(mercAddressLockOverrides, cartAddress.getCountry(), newShippingAddress.getCountry(), "country"));
    }

    private boolean isFieldOverridden(final List<String> overrides, final String originalValue,
        final String modifiedValue, final String field) {
        if (Objects.isNull(overrides) || overrides.isEmpty() || !overrides.contains(field)) {
            return !StringUtils.equals(originalValue, modifiedValue);
        } else {
            return false;
        }
    }

    private void zipCodeValidation(final Address address, final MessageSource messageSource, final Locale userLocale) {
        zipCode5LengthValidation(address, zip5RexEx,
                    messageSource, userLocale, ZIP_LENGTH_5);
        zipCode4LengthValidation(address, messageSource, userLocale);

    }

    private void zipCode5LengthValidation(final Address address, final String pattern,
                                          final MessageSource messageSource, final Locale userLocale,
                                          final int zipLength) {
        String zipCode = address.getZip5();
        if (StringUtils.isBlank(zipCode) || (zipCode.trim().length() != zipLength) || (!Pattern.compile(pattern).matcher(zipCode).matches())) {
            logger.error(ServiceExceptionEnums.INVALID_ZIP_CODE.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.ZIP5.getValue(), messageSource.getMessage(INVALID_ZIP_5, null, INVALID_ZIP_5, userLocale));
        }
    }

    protected void zipCode4LengthValidation(final Address address, final MessageSource messageSource,
                                          final Locale userLocale) {
        if (!StringUtils.isBlank(address.getZip4()) && ((address.getZip4().trim().length() != ZIP_LENGTH_4) || (!Pattern.compile(zip4RexEx).matcher(address.getZip4()).matches()))) {
            logger.error(ServiceExceptionEnums.INVALID_ZIP4_CODE.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.ZIP4.getValue(), messageSource.getMessage(INVALID_ZIP_4, null, INVALID_ZIP_4, userLocale));
        }
    }

    protected void validateAddressLines(final Address address,final MessageSource messageSource,
                                      final Locale userLocale) {

        //Address1
        if (StringUtils.isBlank(address.getAddress1())) {
            logger.error(ServiceExceptionEnums.ADDRESS_EMPTY_EXCEPTION.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.ADDRESS1.getValue(), messageSource.getMessage(INVALID_ADDRESS_LINE_1, null, INVALID_ADDRESS_LINE_1, userLocale));
        } else if (hasPOBoxInAddress1(address.getAddress1())) {
            logger.error(ServiceExceptionEnums.PO_BOX_NOT_ALLOWED.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.ADDRESS1.getValue(), messageSource.getMessage(PO_BOX_NOT_ALLOWED, null, PO_BOX_NOT_ALLOWED, userLocale));
        }

        //Address2
        if (!StringUtils.isBlank(address.getAddress2()) && hasPOBoxInAddress2(address.getAddress2())) {
            logger.error(ServiceExceptionEnums.PO_BOX_NOT_ALLOWED.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.ADDRESS2.getValue(), messageSource.getMessage(PO_BOX_NOT_ALLOWED, null, PO_BOX_NOT_ALLOWED, userLocale));
        }

    }

    protected void validateCity(final Address address, final MessageSource messageSource,
                              final Locale userLocale, final String pattern) {
        if (StringUtils.isNotBlank(address.getCity())) {

            if (!adheresToPattern(address.getCity(), pattern)) {
                logger.error(ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage());
                address.getErrorMessage().put(CartItemOption.CITY.getValue(),
                    messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));
            }

        } else {
            logger.error(ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.CITY.getValue(),
                messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));
        }
    }

    protected void replaceFirstAndLastNameSpecialCharWithEmpty(final Address address, final String locale){
        //First and Last name
        address.setFirstName(AppleUtil.replaceSpecialCharWithEmpty(address.getFirstName(), locale));
        address.setLastName(AppleUtil.replaceSpecialCharWithEmpty(address.getLastName(), locale));
    }

    protected void replaceAddressLinesSpecialCharWithEmpty(final Address address, final String locale){
        //Address1
        address.setAddress1(AppleUtil.replaceSpecialCharWithEmpty(address.getAddress1(), locale));

        //Address2
        address.setAddress2(AppleUtil.replaceSpecialCharWithEmpty(address.getAddress2(), locale));

        //Address3
        address.setAddress3(AppleUtil.replaceSpecialCharWithEmpty(address.getAddress3(), locale));
    }

    @Override
    public void replaceSpecialCharWithEmpty(final Address address, final String locale){
        //First and Last name
        replaceFirstAndLastNameSpecialCharWithEmpty(address, locale);

        //Address Lines 1, 2 and 3
        replaceAddressLinesSpecialCharWithEmpty(address, locale);

        //Business Name
        address.setBusinessName(AppleUtil.replaceSpecialCharWithEmpty(address.getBusinessName(), locale));

        //City
        address.setCity(AppleUtil.replaceSpecialCharWithEmpty(address.getCity(), locale));
    }
}