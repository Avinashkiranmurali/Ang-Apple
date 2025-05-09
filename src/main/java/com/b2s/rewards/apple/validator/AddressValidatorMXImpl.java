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
 * Created by vprasanna on 11/17/2017.
 */
public class AddressValidatorMXImpl extends AddressValidatorImpl {

    @Override
    public boolean hasValidationError(final Address address, final MessageSource messageSource, final User user) {
        final Locale userLocale = user.getLocale();
        validateAndSetAddress(address, messageSource, userLocale);

        mandatoryNullCheck(address, address.getAddress3(),
            ServiceExceptionEnums.ADDRESS_EMPTY_EXCEPTION.getErrorMessage(), CartItemOption.ADDRESS3.getValue(),
            messageSource.getMessage(INVALID_ADDRESS_LINE_1, null, INVALID_ADDRESS_LINE_1, userLocale));
        addressPOBoxCheck(address, address.getAddress3(), CartItemOption.ADDRESS3.getValue(),
            messageSource, userLocale);

        mandatoryNullCheck(address, address.getCity(), ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage(),
            CartItemOption.CITY.getValue(), messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));


        //Sate
        mandatoryNullCheck(address, address.getState(), ServiceExceptionEnums.STATE_EMPTY_EXCEPTION.getErrorMessage(),
            CartItemOption.STATE.getValue(),
            messageSource.getMessage(STATE_PROVINCE_MISSING, null, STATE_PROVINCE_MISSING, userLocale));

        //Zip length 5
        mandatoryZipCheck(address, ZIP_LENGTH_5, messageSource, userLocale);

        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        //IgnoreSuggestedAddress - To skip Melissa address validation, in case customer chooses to ignore the recommended address sent by Melissa validation
        setAndValidateIgnoreSuggestedAddress(address, userLocale, messageSource);

        return !address.getErrorMessage().isEmpty();

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorMXImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_MX);
        sb.append('}');
        return sb.toString();
    }
}
