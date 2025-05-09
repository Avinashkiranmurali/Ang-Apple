package com.b2s.rewards.apple.validator;

import com.b2r.util.address.StreetAddress;
import com.b2r.util.address.melissadata.GlobalAddress;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.ContextUtil;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.var.UserCiti;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.b2s.rewards.apple.util.AppleUtil.replaceNullString;
import static com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.NATIONAL;

/**
 * Created by rperumal on 3/14/2016.
 * Modified by vprasanna on 11/17/2017
 * The type Address mapper.
 */
public class AddressMapper {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AddressMapper.class);
    private static final Map<String, AddressValidatorIF> ADDRESS_VALIDATOR_IF_MAP = new HashMap<>();

    static {
        init();
    }

    private AddressMapper() {
    }

    private static final void init() {
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_DEFAULT, new AddressValidatorImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_SG, new AddressValidatorSGImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_HK, new AddressValidatorHKImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_MX, new AddressValidatorMXImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_TW, new AddressValidatorTWImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_PH, new AddressValidatorPHImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_AU, new AddressValidatorAUImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_AE, new AddressValidatorAEImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_TH, new AddressValidatorTHImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_MY, new AddressValidatorMYImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_ZA, new AddressValidatorZAImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_RU, new AddressValidatorRUImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_FR, new AddressValidatorFRImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_CH, new AddressValidatorCHImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_NL, new AddressValidatorNLImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_CA, new AddressValidatorCAImpl());
        ADDRESS_VALIDATOR_IF_MAP.put(CommonConstants.COUNTRY_CODE_GB, new AddressValidatorGBImpl());
    }

    /**
     * Gets validator for country. If this does not find a country specific
     * implementation then default is returned.
     *
     * @param countryCode the country code
     * @return the validator for country
     */
    public static AddressValidatorIF getValidatorForCountry(String countryCode) {
        logger.debug("Looking for a validator with countryCode {} " , countryCode);
        //Look for any country specific validator
        AddressValidatorIF validator = ADDRESS_VALIDATOR_IF_MAP.get(countryCode);
        logger.debug("Found validator: {}", validator);
        //If you don't have one please use the default
        if (null == validator) {
            validator = ADDRESS_VALIDATOR_IF_MAP.get(CommonConstants.COUNTRY_CODE_DEFAULT);
        }
        logger.info("Returning validator: {}", validator);
        return validator;
    }

    /**
     * Get user address
     * @param user
     * @return
     */
    public static Address getAddress(final User user, final Program program) {
        // Address page.
        final Address address = new Address();
        address.setFirstName(replaceNullString(user.getFirstName()));
        address.setMiddleName(""); // middle name not in User
        address.setLastName(replaceNullString(user.getLastName()));
        address.setMiddleName("");
        address.setBusinessName(replaceNullString(user.getBusinessName()));
        address.setEmail(replaceNullString(user.getEmail()));
        address.setIgnoreSuggestedAddress("false");
        address.setFaxNumber("");
        if (CollectionUtils.isEmpty(user.getAddresses())) {
            address.setAddress1(replaceNullString(user.getAddr1()));
            address.setAddress2(replaceNullString(user.getAddr2()));
            address.setCity(replaceNullString(user.getCity()));
            address.setState(replaceNullString(user.getState()));
            address.setCountry(replaceNullString(user.getCountry()));
            setZipCode(user.getCountry(), user.getZip(), address);
            address.setPhoneNumber(getFormattedPhone(user.getPhone(), address.getCountry()));
        } else {
            final com.b2s.common.services.model.Address primaryAddress = user.getAddresses().get(0);
            address.setSelectedAddressId(primaryAddress.getAddressId());
            address.setAddress1(replaceNullString(primaryAddress.getAddress1()));
            address.setAddress2(replaceNullString(primaryAddress.getAddress2()));
            address.setCity(replaceNullString(primaryAddress.getCity()));
            address.setState(replaceNullString(primaryAddress.getState()));
            address.setCountry(replaceNullString(primaryAddress.getCountry()));
            address.setZip5(replaceNullString(primaryAddress.getZip5()));
            address.setZip4(replaceNullString(primaryAddress.getZip4()));
            address.setPhoneNumber(getFormattedPhone(primaryAddress.getPhoneNumber(), address.getCountry()));
        }

        if ( address.canCalculateWithFeesAndTaxes(isAddressCheckNeeded(program))  && StringUtils.isNotEmpty(address.getFirstName()) && StringUtils.isNotEmpty(address.getLastName())) {
            address.setIsValidAddress(true);
        }
        //this block captures unhandled address validation for invalidAddress flow
        if (handleInvalidAddress(address,user,program)){
            handleInvalidAddressErrorMessages(address,user);
        }

        //validate name length for shipping purpose
        limitRecipientNameSize(address);
        if (user instanceof UserCiti){
            transformCitiUserAddress(address,user);
        }
        return address;
    }

    private static String getFormattedPhone(final String phoneNumber, final String country) {
        String formattedPhone = formatPhoneNumber(phoneNumber, country, NATIONAL);
        formattedPhone = formattedPhone != null ? formattedPhone : phoneNumber;
        return replaceNullString(formattedPhone);
    }

    private static void setZipCode(final String country, final String zip, final Address address) {
        if (StringUtils.isNotBlank(country) && country.equals(CommonConstants.COUNTRY_CODE_US)) {
            if (StringUtils.isNotBlank(replaceNullString(zip))) {
                final String[] zipArr = zip.split("-");
                if (zipArr.length == 2) {
                    address.setZip5(zipArr[0]);
                    address.setZip4(zipArr[1]);
                } else {
                    address.setZip5(zip);
                    address.setZip4("");  //No zip4 data
                }
            } else {
                address.setZip5("");
                address.setZip4("");
            }
        } else {
            address.setZip5(replaceNullString(zip));
            address.setZip4("");  //TODO:  zip4 not in User
        }
    }

    /**
     * Transform GlobalAddress to Address
     * @param globalAddress
     * @param address
     */
    public static void transformGlobalAddress(final GlobalAddress globalAddress, final Address address) {
        if(globalAddress != null && address != null && StringUtils.isNotBlank(globalAddress.getCountryISO3166_1_Alpha2())) {
            transformGlobalAddressLines(globalAddress, address);
        }
    }

    /**
     * Transform GlobalAddress to Address
     * @param globalAddress
     * @param address
     */
    private static void transformGlobalAddressLines(final GlobalAddress globalAddress, final Address address) {
        //Below are common Mappings
        if(!CommonConstants.COUNTRY_CODE_TW.equals(globalAddress.getCountryISO3166_1_Alpha2())) {
            address.setAddress1(globalAddress.getAddressLine1());
            if(!globalAddress.getAddressLine2().equalsIgnoreCase(address.getAddress3())){
                address.setAddress2(globalAddress.getAddressLine2());
            }
        }else{
            transformTWAddressLine(globalAddress, address);
        }

        address.setCity(globalAddress.getLocality());
        address.setZip5(globalAddress.getPostalCode());
        address.setCountry(globalAddress.getCountryISO3166_1_Alpha2());

        if(!CommonConstants.COUNTRIES_WITH_NO_STATE.contains(globalAddress.getCountryISO3166_1_Alpha2())) {
            address.setState(globalAddress.getAdministrativeArea());
        }
        setAddressForSpecificCountries(globalAddress, address);

    }

    private static void setAddressForSpecificCountries(final GlobalAddress globalAddress, final Address address) {
        //Below are country specific overrides.
        switch(globalAddress.getCountryISO3166_1_Alpha2()) {
            case CommonConstants.COUNTRY_CODE_CH:
                address.setAddress3(globalAddress.getAddressLine3());
                address.setAddress2(globalAddress.getAddressLine2());
                break;
            case CommonConstants.COUNTRY_CODE_GB:
                transformAddressGB(globalAddress, address);
                break;
            case CommonConstants.COUNTRY_CODE_ZA:
                address.setAddress3(globalAddress.getDependentLocality());
                break;
            case CommonConstants.COUNTRY_CODE_MX:
                // Mexico has a different mapping for the address.
                // Please check with Ranjith or Venkat before making changes to the below mapping
                // Sub Administrative Area --> City
                // Address Line 3 (Colonia) --> Locality
                address.setCity(globalAddress.getSubAdministrativeArea());
                address.setAddress3(globalAddress.getLocality());
                break;
            case CommonConstants.COUNTRY_CODE_TH:
                address.setSubCity(globalAddress.getDependentLocality());
                break;
            case CommonConstants.COUNTRY_CODE_MY:
                address.setAddress3(globalAddress.getAddressLine3());
                break;
            case CommonConstants.COUNTRY_CODE_NL:
                address.setAddress2(globalAddress.getSubAdministrativeArea());
                address.setAddress3(globalAddress.getAdministrativeArea());
                break;
            default:
                break;
        }
    }

    private static void transformAddressGB(final GlobalAddress globalAddress, final Address address) {
    /*
  GB address has maximum 5 lines including the zip code as explained by RoyalMail
  https://personal.help.royalmail.com/app/answers/detail/a_id/81
  So copying the 1st line,
  2nd and 3rd lines are conditional so that the locality is not repeated,
  4th line is locality which is copied to city and needs to be upper case.
  Melissa is returning lowercase locality but uppercase in 'formatted address'.
  5th line is postal code which also needs to be upper case and Melissa is returning uppercase.
  GB does not have states
 */
        address.setAddress1(globalAddress.getAddressLine1());
        if (!(globalAddress.getLocality().equalsIgnoreCase(globalAddress.getAddressLine2()))) {
            address.setAddress2(globalAddress.getAddressLine2());
        } else {
            address.setAddress2("");
        }
        if (!(globalAddress.getLocality().equalsIgnoreCase(globalAddress.getAddressLine3()))) {
            address.setAddress3(globalAddress.getAddressLine3());
        } else {
            address.setAddress3("");
        }
        address.setCity(globalAddress.getLocality().toUpperCase());
        address.setState("");
    }

    /**
     * Transform Address to GlobalAddress
     * @param globalAddress
     * @param address
     */
    public static void transformAddress(final Address address, final GlobalAddress globalAddress) {
        if(globalAddress != null && address != null && StringUtils.isNotBlank(address.getCountry())) {
            transformAddressLines(address, globalAddress);
        }
    }

    /**
     * Transform Address to GlobalAddress
     * @param globalAddress
     * @param address
     */
    public static void transformAddressLines(final Address address, final GlobalAddress globalAddress) {
        //Below are the common mappings
        globalAddress.setOrganization(address.getBusinessName());
        globalAddress.setAddressLine1(address.getAddress1());
        if(!CommonConstants.COUNTRY_CODE_NL.equals(address.getCountry())) {
            globalAddress.setAddressLine2(address.getAddress2());
        }
        globalAddress.setPostalCode(address.getZip5());
        globalAddress.setCountryISO3166_1_Alpha2(address.getCountry());
        globalAddress.setLocality(address.getCity());
        globalAddress.setAdministrativeArea(address.getState());

        //Below are country specific overrides.
        switch (address.getCountry()) {
            case CommonConstants.COUNTRY_CODE_GB:
                globalAddress.setAdministrativeArea(address.getAddress3());
                break;
            case CommonConstants.COUNTRY_CODE_ZA:
                globalAddress.setDependentLocality(address.getAddress3());
                if (StringUtils.isBlank(address.getAddress2())) {
                    globalAddress.setAddressLine2(address.getAddress3());
                }
                break;
            case CommonConstants.COUNTRY_CODE_MX:
                // Mexico has a different mapping for the address.
                // Please check with Ranjith or Venkat before making changes to the below mapping
                // City --> Sub Administrative Area
                // Locality --> Address Line 3 (Colonia)
                globalAddress.setSubAdministrativeArea(address.getCity());
                globalAddress.setLocality(address.getAddress3());
                break;
            case CommonConstants.COUNTRY_CODE_TH:
                globalAddress.setDependentLocality(address.getSubCity());
                break;
            case CommonConstants.COUNTRY_CODE_MY:
                globalAddress.setAddressLine3(address.getAddress3());
                break;
            case CommonConstants.COUNTRY_CODE_NL:
                globalAddress.setSubAdministrativeArea(address.getAddress2());
                globalAddress.setAdministrativeArea(address.getAddress3());
                break;
            case CommonConstants.COUNTRY_CODE_CH:
                globalAddress.setAdministrativeArea(null);
                globalAddress.setSubAdministrativeArea(null);
                globalAddress.setSubNationalArea(null);
                break;
            default:
                break;

        }
    }

    /**
     * Tranform Street address to Apple Address
     * @param address
     * @param streetAddress
     * @return
     */
    public static void transformStreetAddress(StreetAddress streetAddress, Address address) {
        try {
            //set address data
            BeanUtils.copyProperties(address, streetAddress);
            address.setBusinessName(streetAddress.getCompany());
            address.setZip5(streetAddress.getZip());
            address.setZip4(streetAddress.getPlus4());

            // Melissa Address validation, moves SUITE information from Address2 to StreetAddress.Suite. This need to be set back
            if (!StringUtils.isEmpty(streetAddress.getSuite()) ) {
                address.setAddress2(streetAddress.getSuite());
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Problem in transforming StreetAddress...", e);
        }
    }

    /**
     * Tranform Apple address to Street Address
     * @param address
     * @param streetAddress
     * @return
     */
    public static void transformAddress( Address address, StreetAddress streetAddress) {

        try {
            //set address data
            BeanUtils.copyProperties(streetAddress, address);
            streetAddress.setCompany(address.getBusinessName());
            streetAddress.setZip(address.getZip5());
            streetAddress.setPlus4(address.getZip4());

        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Problem in transforming Address...", e);
        }

    }

    /**
     * Receipients Full Name cannot exceed  CommonConstants.RECEPIENT_FULL_NAME_LENGTH_MAX.
     * Otherwise limit first name & last name length to CommonConstants.RECEPIENT_FIRST_LAST_NAME_LENGTH_MAX, each
     *
     * @param address
     */
    public static void limitRecipientNameSize(Address address) {
        String firstName = (StringUtils.isEmpty(address.getFirstName()) ? "" : address.getFirstName().trim());
        String lastName = (StringUtils.isEmpty(address.getLastName()) ? "" : address.getLastName().trim());
        StringBuilder sb = new StringBuilder().append(firstName).append(lastName);
        if (sb.toString().length() > CommonConstants.RECEPIENT_FULL_NAME_LENGTH_MAX) {
            address.setFirstName(StringUtils.substring(firstName, 0, CommonConstants.RECEPIENT_FIRST_LAST_NAME_LENGTH_MAX));
            address.setLastName(StringUtils.substring(lastName, 0, CommonConstants.RECEPIENT_FIRST_LAST_NAME_LENGTH_MAX));
        }
    }

    protected static String formatPhoneNumber(final String phoneNumber, final String countryCode, final
    PhoneNumberUtil.PhoneNumberFormat phoneNumberFormat) {
        final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        final Phonenumber.PhoneNumber numberProto;
        try {
            numberProto = phoneUtil.parse(phoneNumber, countryCode);
            if (!phoneUtil.isValidNumber(numberProto)) {
                return null;
            } else {
                return phoneUtil.format(numberProto, phoneNumberFormat);
            }
        } catch (final NumberParseException e) {
            return null;
        }

    }

    public static void transformCitiUserAddress(final Address address, final User user){
        final UserCiti userCiti = (UserCiti)user;

        switch (user.getVarId()){
            case CommonConstants.COUNTRY_CODE_HK:
            case CommonConstants.COUNTRY_CODE_SG:
            case CommonConstants.COUNTRY_CODE_TW:
                final String delimitedAddressLine2 = Stream
                    .of(userCiti.getAddr2(),userCiti.getAddr3(), userCiti .getAddr4(),userCiti.getAddr5(), userCiti.getAddr6()).
                    filter(s -> StringUtils.isNotEmpty(s)).collect(Collectors.joining(", "));
                address.setAddress2(replaceNullString(delimitedAddressLine2));
                break;
            case CommonConstants.COUNTRY_CODE_TH:
                final String delimitedAddressLine3TH = Stream.of(userCiti.getAddr3(), userCiti.getAddr5(), userCiti.getAddr6()).
                    filter(s -> StringUtils.isNotEmpty(s)).collect(Collectors.joining(", "));
                address.setAddress3(replaceNullString(delimitedAddressLine3TH));
                address.setSubCity(userCiti.getAddr4());
                break;
            default:
                final String delimitedAddressLine3 = Stream.of(userCiti.getAddr3(), userCiti.getAddr4(),userCiti.getAddr5(), userCiti.getAddr6()).
                    filter(s -> StringUtils.isNotEmpty(s)).collect(Collectors.joining(", "));
                address.setAddress3(replaceNullString(delimitedAddressLine3));
                break;

        }
    }

    private static void handleInvalidAddressErrorMessages(final Address address, final User user){
        final ApplicationContext context = AppContext.getApplicationContext();
        final ContextUtil contextUtil = (ContextUtil) context.getBean("contextUtil");
        final AddressValidatorIF addressValidator = com.b2s.rewards.apple.validator.AddressMapper.getValidatorForCountry(address.getCountry());
        addressValidator.hasValidationError(address,contextUtil.getMessageSource(user.getVarId()),user);
    }

    private static boolean handleInvalidAddress(final Address address, final User user, final Program program){
        //User address should be validated only if IGNORE_PROFILE_ADDRESS is false for Citi VARs
        return !address.isValidAddress() && CommonConstants.APPLE_GR_ADDRESS_SUPPORTED_COUNTRIES.contains(user.getCountry())
            && (! (Boolean) program.getConfig().getOrDefault(CommonConstants.IGNORE_PROFILE_ADDRESS, Boolean .FALSE));
    }

    public static boolean isAddressCheckNeeded(final Program program){
        boolean ignoreProfileAddress = (Boolean) program.getConfig().getOrDefault(CommonConstants.IGNORE_PROFILE_ADDRESS, Boolean.FALSE);
        return CommonConstants.AWP_PROGRAM_DRP.equalsIgnoreCase((String)program.getConfig().get(CommonConstants.SHOP_EXPERIENCE) )||  ignoreProfileAddress;
    }

    private static boolean checkTWAddressLine1(final GlobalAddress globalAddress, final Address address) {
        boolean result = true;
        if(globalAddress.getAddressLine1().length() < 13){
            address.setAddress1(globalAddress.getAddressLine1());
            address.setAddress2(globalAddress.getAddressLine2());
            result = false;
        } else {
            String address1 = globalAddress.getAddressLine1().replaceAll("\\s+", "");
            if (address1.length() < 13) {
                address.setAddress1(address1);
                address.setAddress2(globalAddress.getAddressLine2());
                result = false;
            }
        }
        return result;
    }

    private static boolean setAddressPremises(final GlobalAddress globalAddress, final Address address) {
        boolean result;
        if (globalAddress.getThoroughfare().length() + globalAddress.getPremisesNumber().length() + globalAddress.getSubPremises().length() < 13) {
            address.setAddress1(globalAddress.getPremisesNumber() + globalAddress.getThoroughfare() + globalAddress.getSubPremises());
            address.setAddress2(globalAddress.getAddressLine2());
            result = false;
        } else if (globalAddress.getThoroughfare().length() + globalAddress.getPremisesNumber().length() < 13) {
            address.setAddress1(globalAddress.getPremisesNumber() + globalAddress.getThoroughfare());
            address.setAddress2(globalAddress.getSubPremises());
            address.setAddress3(globalAddress.getAddressLine2());
            result = false;
        } else if (globalAddress.getThoroughfare().length() + globalAddress.getSubPremises().length() < 13) {
            address.setAddress1(globalAddress.getPremisesNumber());
            address.setAddress2(globalAddress.getThoroughfare() + globalAddress.getSubPremises());
            address.setAddress3(globalAddress.getAddressLine2());
            result = false;
        } else if (globalAddress.getThoroughfare().length() < 13) {
            address.setAddress1(globalAddress.getPremisesNumber() + globalAddress.getSubPremises());
            address.setAddress2(globalAddress.getThoroughfare());
            address.setAddress3(globalAddress.getAddressLine2());
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    private static boolean checkTWThoroughfarePremisesSubPremises(final GlobalAddress globalAddress, final Address address) {
        boolean result = true;
        if (globalAddress.getThoroughfare() != null && globalAddress.getPremisesNumber() != null &&
            globalAddress.getSubPremises() != null
            && globalAddress.getAddressLine1().contains(globalAddress.getThoroughfare()) &&
            globalAddress.getAddressLine1().contains(globalAddress.getPremisesNumber()) &&
            globalAddress.getAddressLine1().contains(globalAddress.getSubPremises())) {
            result = setAddressPremises(globalAddress, address);
        }
        return result;
    }
    /* For TW an address Line can have max 12 characters*/
    private static void transformTWAddressLine(final GlobalAddress globalAddress, final Address address) {
        if (checkTWAddressLine1(globalAddress, address) &&
            checkTWThoroughfarePremisesSubPremises(globalAddress, address)) {
            String[] address1Array = globalAddress.getAddressLine1().split("\\s+", 2);
            if ((address1Array.length > 1) && (address1Array[0].length() < 13) && (address1Array[1].length() < 13)) {
                address.setAddress1(address1Array[0]);
                address.setAddress2(address1Array[1]);
                address.setAddress3(globalAddress.getAddressLine2());
            } else {
                address.setAddress1(globalAddress.getAddressLine1().substring(0, 12));
                address.setAddress2(globalAddress.getAddressLine1().substring(12));
                address.setAddress3(globalAddress.getAddressLine2());
            }

        }
    }

    public static void setZipCode(final String country, final String zip, final com.b2s.common.services.model.Address.AddressBuilder address) {
        if (StringUtils.isNotBlank(country) && country.equals(CommonConstants.COUNTRY_CODE_US)) {
            if (StringUtils.isNotBlank(replaceNullString(zip))) {
                final String[] zipArr = zip.split("-");
                if (zipArr.length == 2) {
                    address.withZip5(zipArr[0]);
                    address.withZip4(zipArr[1]);
                } else {
                    address.withZip5(zip);
                    address.withZip4("");
                }
            } else {
                address.withZip5("");
                address.withZip4("");
            }
        } else {
            address.withZip5(replaceNullString(zip));
            address.withZip4("");
        }
    }
}
