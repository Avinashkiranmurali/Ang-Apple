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

import static com.b2s.rewards.apple.validator.AddressValidatorConstants.COUNTRY_MISSING;
import static com.b2s.rewards.apple.validator.AddressValidatorConstants.INVALID_POSTAL_CODE;
import static com.b2s.rewards.common.util.CommonConstants.COUNTRY_CODE_GB;

public class AddressValidatorGBImpl extends AddressValidatorImpl {
    private static final String GB_POSTAL_CODE_REG_EX = "[a-zA-Z0-9 ]+";

    @Override
    public boolean hasValidationError(final Address address, final MessageSource deprecatedMessageSource, final User user) {
        final Locale userLocale= user.getLocale();
        final MessageSource messageSource = getMessageSource(user.getVarId());
        boolean hasValidationError = false;

        replaceSpecialCharWithEmpty(address, userLocale.toString());

        //Allow only characters for First Name, Last Name & City
        checkNameWithRegExp(address, messageSource, userLocale, address.getFirstName(),
            CartItemOption.FIRST_NAME.getValue());
        checkNameWithRegExp(address, messageSource, userLocale, address.getLastName(),
            CartItemOption.LAST_NAME.getValue());

        validateAddressLines(address, messageSource, userLocale);
        validateCity(address, messageSource, userLocale, CommonConstants.CHAR_SPACE_AND_DASH_ONLY_REG_EX);

        //Zip5
        if (StringUtils.isBlank(address.getZip5()) || (address.getZip5().trim().length() > 8) || (!Pattern.compile(
            GB_POSTAL_CODE_REG_EX).matcher(address.getZip5()).matches())) {
            logger.error(ServiceExceptionEnums.INVALID_POSTAL_CODE.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.ZIP5.getValue(), messageSource.getMessage(INVALID_POSTAL_CODE, null, INVALID_POSTAL_CODE, userLocale));
        }
        zipCode4LengthValidation(address, messageSource, userLocale);

        //phone number
        // Convert the entered phone number to country national format
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        //Country
        mandatoryNullCheck(address, address.getCountry(),
            ServiceExceptionEnums.COUNTRY_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.COUNTRY.getValue(),
            messageSource.getMessage(COUNTRY_MISSING, null, COUNTRY_MISSING, userLocale));

        //State validation is not needed for GB, as it doesn't have states

        //Email
        mandatoryEmailCheck(address, messageSource, userLocale);

        //IgnoreSuggestedAddress - To skip Melissa address validation, in case customer chooses to ignore the recommended address sent by Melissa validation
        setAndValidateIgnoreSuggestedAddress(address, userLocale, messageSource);

        if (!address.getErrorMessage().isEmpty()) {
            hasValidationError = true;
        }

        return hasValidationError;
    }

    @Override
    public String toString() {
        return "AddressValidatorGBImpl{" + COUNTRY_CODE_GB + "}";
    }

}