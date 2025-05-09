package com.b2s.rewards.apple.validator;

import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * Created by vprasanna on 11/17/2017.
 */
public class AddressValidatorHKImpl extends AddressValidatorImpl {
    @Override
    public boolean hasValidationError(final Address address, final MessageSource messageSource, final User user) {
        final Locale userLocale = user.getLocale();
        validateAndSetAddress(address, messageSource, userLocale);

        addressPOBoxCheck(address, address.getAddress2(), CartItemOption.ADDRESS2.getValue(),
            messageSource, userLocale);

        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorHKImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_HK);
        sb.append('}');
        return sb.toString();
    }
}
