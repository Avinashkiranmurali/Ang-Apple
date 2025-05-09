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
 * Created by srukmagathan on 06-06-2018.
 */
public class AddressValidatorZAImpl extends AddressValidatorImpl {

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

        //Address1
        mandatoryNullCheck(address, address.getAddress1(),
            ServiceExceptionEnums.ADDRESS_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.ADDRESS1.getValue(),
            messageSource.getMessage(INVALID_ADDRESS_LINE_1, null, INVALID_ADDRESS_LINE_1, userLocale));
        addressPOBoxCheck(address, address.getAddress1(), CartItemOption.ADDRESS1.getValue(),
            messageSource, userLocale);
        address.setAddress1(AppleUtil.replaceSpecialCharWithEmpty(address.getAddress1()));

        //Address2
        addressPOBoxCheck(address, address.getAddress2(), CartItemOption.ADDRESS2.getValue(),
            messageSource, userLocale);
        address.setAddress2(AppleUtil.replaceSpecialCharWithEmpty(address.getAddress2()));

        //Address3
        mandatoryNullCheck(address, address.getAddress3(),
            ServiceExceptionEnums.ADDRESS_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.ADDRESS3.getValue(),
            messageSource.getMessage(INVALID_ADDRESS_LINE_3, null, INVALID_ADDRESS_LINE_3, userLocale));

        //City
        mandatoryNullCheck(address, address.getCity(), ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage(),
            CartItemOption.CITY.getValue(), messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));

        //Zip length 5
        mandatoryZipCheck(address, ZIP_LENGTH_4,
            ZIP_LENGTH_4, messageSource.getMessage(INVALID_ZIP_5, null, INVALID_ZIP_5, userLocale));

        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        mandatoryEmailCheck(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();

    }

    @Override
    public void replaceSpecialCharWithEmpty(final Address address, final String locale){
        //First and Last name
        replaceFirstAndLastNameSpecialCharWithEmpty(address,locale);

        //Address Line 1, 2 and 3
        replaceAddressLinesSpecialCharWithEmpty(address, locale);

        //City
        address.setCity(AppleUtil.replaceSpecialCharWithEmpty(address.getCity(), locale));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorZAImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_ZA);
        sb.append('}');
        return sb.toString();
    }

}
