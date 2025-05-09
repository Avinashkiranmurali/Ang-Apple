package com.b2s.rewards.apple.validator;

import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static com.b2s.rewards.apple.validator.AddressValidatorConstants.*;

/**
 * Created by rpillai on 01/11/2018.
 */
public class AddressValidatorAUImpl extends AddressValidatorImpl {

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
        addressPOBoxCheck(address, address.getAddress1(), CartItemOption.ADDRESS1.getValue(),
            messageSource, userLocale);

        //Address2
        addressPOBoxCheck(address, address.getAddress2(), CartItemOption.ADDRESS2.getValue(),
            messageSource, userLocale);

        //City
        mandatoryNullCheck(address, address.getCity(), ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage(),
            CartItemOption.CITY.getValue(), messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));

        //State
        mandatoryStateCheck(address, messageSource, userLocale);

        //Zip length 4
        mandatoryZipCheck(address, ZIP_LENGTH_4, messageSource, userLocale);

        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        //Country
        mandatoryNullCheck(address, address.getCountry(),
            ServiceExceptionEnums.COUNTRY_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.COUNTRY.getValue(),
            messageSource.getMessage(COUNTRY_MISSING, null, COUNTRY_MISSING, userLocale));

        mandatoryEmailCheck(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorAUImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_AU);
        sb.append('}');
        return sb.toString();
    }
}
