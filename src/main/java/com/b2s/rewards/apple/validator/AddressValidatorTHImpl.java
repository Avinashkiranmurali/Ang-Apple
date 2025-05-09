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
 * Created by rpillai on 01/31/2018.
 */
public class AddressValidatorTHImpl extends AddressValidatorImpl {

    @Override
    public boolean hasValidationError(final Address address, final MessageSource messageSource, final User user) {
        final Locale userLocale = user.getLocale();
        validateAndSetAddress(address, messageSource, userLocale);

        addressPOBoxCheck(address, address.getAddress2(), CartItemOption.ADDRESS2.getValue(),
            messageSource, userLocale);

        mandatoryNullCheck(address, address.getCity(), ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage(),
            CartItemOption.CITY.getValue(), messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));

        mandatoryNullCheck(address, address.getSubCity(), ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage(),
            CartItemOption.SUBCITY.getValue(),
            messageSource.getMessage(INVALID_DISTRICT, null, INVALID_DISTRICT, userLocale));

        //State
        mandatoryNullCheck(address, address.getState(), ServiceExceptionEnums.STATE_EMPTY_EXCEPTION.getErrorMessage(),
            CartItemOption.STATE.getValue(),
            messageSource.getMessage(STATE_PROVINCE_MISSING, null, STATE_PROVINCE_MISSING, userLocale));

        //Zip length 4
        mandatoryZipCheck(address, ZIP_LENGTH_5, messageSource, userLocale);

        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();

    }

    @Override
    public void replaceSpecialCharWithEmpty(final Address address, final String locale){
        //First and Last name
        replaceFirstAndLastNameSpecialCharWithEmpty(address, locale);

        //Address Line 1, 2 and 3
        replaceAddressLinesSpecialCharWithEmpty(address, locale);

        //Business Name
        address.setBusinessName(AppleUtil.replaceSpecialCharWithEmpty(address.getBusinessName(), locale));

        //City
        address.setCity(AppleUtil.replaceSpecialCharWithEmpty(address.getCity(), locale));

        //Sub City
        address.setSubCity(AppleUtil.replaceSpecialCharWithEmpty(address.getSubCity(), locale));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorTHImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_TH);
        sb.append('}');
        return sb.toString();
    }
}
