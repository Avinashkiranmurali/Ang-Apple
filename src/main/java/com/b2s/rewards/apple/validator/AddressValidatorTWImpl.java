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
public class AddressValidatorTWImpl extends AddressValidatorImpl {

    @Override
    public boolean hasValidationError(final Address address, final MessageSource messageSource, final User user) {
        final Locale userLocale = user.getLocale();
        validateAndSetAddress(address, messageSource, userLocale);

        addressPOBoxCheck(address, address.getAddress2(), CartItemOption.ADDRESS2.getValue(),
            messageSource, userLocale);

        mandatoryNullCheck(address, address.getCity(), ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage(),
            CartItemOption.CITY.getValue(), messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));

        //Zip length 5
        mandatoryZipCheck(address, ZIP_MIN_LENGTH_3, ZIP_LENGTH_5,
            messageSource.getMessage(INVALID_ZIP_5, null, INVALID_ZIP_5, userLocale));

        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorTWImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_TW);
        sb.append('}');
        return sb.toString();
    }
}
