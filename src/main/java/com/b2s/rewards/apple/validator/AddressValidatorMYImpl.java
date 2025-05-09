package com.b2s.rewards.apple.validator;

import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static com.b2s.rewards.apple.validator.AddressValidatorConstants.INVALID_ADDRESS_LINE_1;
import static com.b2s.rewards.apple.validator.AddressValidatorConstants.INVALID_CITY;
import static com.b2s.rewards.apple.validator.AddressValidatorConstants.STATE_PROVINCE_MISSING;
import static com.b2s.rewards.apple.validator.AddressValidatorConstants.ZIP_LENGTH_5;

public class AddressValidatorMYImpl extends AddressValidatorImpl {

    @Override
    public boolean hasValidationError(final Address address, final MessageSource messageSource, final User user) {
        final Locale userLocale = user.getLocale();
        validateAndSetAddress(address, messageSource, userLocale);

        mandatoryNullCheck(address, address.getAddress1(),
            ServiceExceptionEnums.ADDRESS_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.ADDRESS2.getValue(),
            messageSource.getMessage(INVALID_ADDRESS_LINE_1, null, INVALID_ADDRESS_LINE_1, userLocale));
        addressPOBoxCheck(address, address.getAddress2(), CartItemOption.ADDRESS2.getValue(),
            messageSource, userLocale);

        mandatoryNullCheck(address, address.getCity(), ServiceExceptionEnums.CITY_EMPTY_EXCEPTION.getErrorMessage(),
            CartItemOption.CITY.getValue(), messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));

        //State
        mandatoryNullCheck(address, address.getState(), ServiceExceptionEnums.STATE_EMPTY_EXCEPTION.getErrorMessage(),
            CartItemOption.STATE.getValue(),
            messageSource.getMessage(STATE_PROVINCE_MISSING, null, STATE_PROVINCE_MISSING, userLocale));

        //Zip length 5
        mandatoryZipCheck(address, ZIP_LENGTH_5, messageSource, userLocale);

        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorMYImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_MY);
        sb.append('}');
        return sb.toString();
    }
}

