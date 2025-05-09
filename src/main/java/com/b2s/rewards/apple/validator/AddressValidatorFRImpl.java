package com.b2s.rewards.apple.validator;

import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static com.b2s.rewards.apple.validator.AddressValidatorConstants.*;

/**
 * Created by Clay Xia on 11-15-2018.
 */
public class AddressValidatorFRImpl extends AddressValidatorImpl {
    @Override
    public boolean hasValidationError(final Address address, final MessageSource messageSource, final User user) {
        final Locale userLocale = user.getLocale();

        //Replace Special characters with Empty
        replaceSpecialCharWithEmpty(address, userLocale.toString());

        mandatoryNullCheck(address, address.getFirstName(),
            ServiceExceptionEnums.ALPHA_ONLY_CHARS_EXCEPTION.getErrorMessage(), CartItemOption.FIRST_NAME.getValue(),
            messageSource.getMessage(INVALID_NAME, null, INVALID_NAME, userLocale));
        mandatoryNullCheck(address, address.getLastName(),
            ServiceExceptionEnums.ALPHA_ONLY_CHARS_EXCEPTION.getErrorMessage(), CartItemOption.LAST_NAME.getValue(),
            messageSource.getMessage(INVALID_NAME, null, INVALID_NAME, userLocale));

        //City
        mandatoryNullCheck(address, address.getCity(), ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage(),
            CartItemOption.CITY.getValue(), messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));

        //Zip length 5
        mandatoryZipCheck(address, ZIP_LENGTH_5, ZIP_LENGTH_5,
            messageSource.getMessage(INVALID_ZIP_5, null, INVALID_ZIP_5, userLocale));

        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        mandatoryEmailCheck(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();

    }

    @Override
    public void replaceSpecialCharWithEmpty(final Address address, final String locale){
        //First and Last name
        replaceFirstAndLastNameSpecialCharWithEmpty(address, locale);

        //City
        address.setCity(AppleUtil.replaceSpecialCharWithEmpty(address.getCity(), locale));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorFRImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_FR);
        sb.append('}');
        return sb.toString();
    }

}
