package com.b2s.rewards.apple.validator;

import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.shop.common.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.regex.Pattern;

import static com.b2s.rewards.apple.validator.AddressValidatorConstants.COUNTRY_MISSING;
import static com.b2s.rewards.apple.validator.AddressValidatorConstants.INVALID_POSTAL_CODE;
import static com.b2s.rewards.common.util.CommonConstants.CITY_CA_FRENCH_REG_EX;
import static com.b2s.rewards.common.util.CommonConstants.COUNTRY_CODE_CA;

public class AddressValidatorCAImpl extends AddressValidatorImpl {
    private static final String CA_POSTAL_CODE_REG_EX = "^(?!.*[DFIOQU])[a-vxyA-VXY][0-9][a-zA-Z] ?[0-9][a-zA-Z][0-9]$";

    @Override
    public boolean hasValidationError(final Address address, final MessageSource deprecatedMessageSource,
        final User user) {
        final Locale userLocale = user.getLocale();
        final MessageSource messageSource = getMessageSource(user.getVarId());
        boolean hasValidationError = false;
        replaceSpecialCharWithEmpty(address, userLocale.toString());

        //Allow only characters for First Name, Last Name & City
        checkNameWithRegExp(address, messageSource, userLocale, address.getFirstName(),
            CartItemOption.FIRST_NAME.getValue());

        checkNameWithRegExp(address, messageSource, userLocale, address.getLastName(),
            CartItemOption.LAST_NAME.getValue());

        validateCity(address, messageSource, userLocale, CITY_CA_FRENCH_REG_EX);

        validateAddressLines(address, messageSource, userLocale);

        //Zip5
        if (StringUtils.isBlank(address.getZip5()) ||
            (address.getZip5().trim().length() != 7 && address.getZip5().trim().length() != 6) ||
            (!Pattern.compile(CA_POSTAL_CODE_REG_EX).matcher(address.getZip5()).matches())) {
            logger.error(ServiceExceptionEnums.INVALID_POSTAL_CODE.getErrorMessage());
            address.getErrorMessage().put(CartItemOption.ZIP5.getValue(),
                messageSource.getMessage(INVALID_POSTAL_CODE, null, INVALID_POSTAL_CODE, userLocale));
        }

        zipCode4LengthValidation(address, messageSource, userLocale);

        //phone number
        // Convert the entered phone number to country national format
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        //Country
        mandatoryNullCheck(address, address.getCountry(),
            ServiceExceptionEnums.COUNTRY_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.COUNTRY.getValue(),
            messageSource.getMessage(COUNTRY_MISSING, null, COUNTRY_MISSING, userLocale));

        //State
        mandatoryStateCheck(address, messageSource, userLocale);

        //Email
        mandatoryEmailCheck(address, messageSource, userLocale);

        //IgnoreSuggestedAddress - To skip Melissa address validation, in case customer chooses to ignore the
        // recommended address sent by Melissa validation
        setAndValidateIgnoreSuggestedAddress(address, userLocale, messageSource);

        if (!address.getErrorMessage().isEmpty()) {
            hasValidationError = true;
        }

        return hasValidationError;
    }

    @Override
    public String toString() {
        return "AddressValidatorCAImpl{" + COUNTRY_CODE_CA + "}";
    }
}