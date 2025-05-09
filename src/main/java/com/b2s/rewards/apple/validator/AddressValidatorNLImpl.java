package com.b2s.rewards.apple.validator;

import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.regex.Pattern;

import static com.b2s.rewards.apple.validator.AddressValidatorConstants.*;

/**
 * Created by skathirvel on 26-03-2019.
 */
public class AddressValidatorNLImpl extends AddressValidatorImpl {


    private transient String nlPostalCodeRegEx = "^[A-Za-z0-9 ]{7}";

    @Override
    public boolean hasValidationError(final Address address, final MessageSource messageSource, final User user) {
        final Locale userLocale = user.getLocale();

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

        //City
        mandatoryNullCheck(address, address.getCity(), ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage(),
            CartItemOption.CITY.getValue(), messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));

        //Zip length 7
        mandatoryZipCheckForNL(address, address.getZip5(), 7, ServiceExceptionEnums.INVALID_ZIP_CODE.getErrorMessage(),
            CartItemOption.ZIP5.getValue(), messageSource.getMessage(INVALID_ZIP_7, null, INVALID_ZIP_7, userLocale));

        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        //Country
        mandatoryNullCheck(address, address.getCountry(),
            ServiceExceptionEnums.COUNTRY_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.COUNTRY.getValue(),
            messageSource.getMessage(COUNTRY_MISSING, null, COUNTRY_MISSING, userLocale));

        mandatoryEmailCheck(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();

    }

    private void mandatoryZipCheckForNL(Address address, String fieldValue, int zipLength, String serviceErrorMessage,
        String field, String message) {
        final String postalCodeRegex = String.format(nlPostalCodeRegEx, zipLength);

        if (StringUtils.isBlank(fieldValue) || (fieldValue.trim().length() != zipLength) ||
            (!Pattern.compile(postalCodeRegex).matcher(fieldValue).matches())) {
            logger.error(serviceErrorMessage);
            address.getErrorMessage().put(field, message);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorNLImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_NL);
        sb.append('}');
        return sb.toString();
    }
}
