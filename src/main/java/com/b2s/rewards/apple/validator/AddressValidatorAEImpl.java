package com.b2s.rewards.apple.validator;

import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static com.b2s.rewards.apple.validator.AddressValidatorConstants.*;

/**
 * @author rjesuraj Date : 1/22/2018 Time : 6:54 PM
 */
public class AddressValidatorAEImpl extends AddressValidatorImpl {

    @Override
    public boolean hasValidationError(final Address address, final MessageSource messageSource, final User user) {
        final Locale userLocale = user.getLocale();
        validateAndSetAddress(address, messageSource, userLocale);

        addressPOBoxCheck(address, address.getAddress2(), CartItemOption.ADDRESS2.getValue(),
            messageSource, userLocale);

        //Address3
        addressPOBoxCheck(address, address.getAddress3(), CartItemOption.ADDRESS3.getValue(), messageSource,
            userLocale);
        if (StringUtils.isNotEmpty(address.getAddress3()) && !adheresToPattern(address.getAddress3(),
            CommonConstants.CHAR_SPACE_AND_DASH_ONLY_REG_EX)) {
                address.getErrorMessage().put(CartItemOption.ADDRESS3.getValue(),
                    messageSource.getMessage(INVALID_TOWN, null, INVALID_TOWN, userLocale));
        }

        mandatoryNullCheck(address, address.getCity(), ServiceExceptionEnums.CITY_EMPTY_EXCEPTION.getErrorMessage(),
            CartItemOption.CITY.getValue(), messageSource.getMessage(INVALID_CITY, null, INVALID_CITY, userLocale));
        mandatoryCityCheck(address, messageSource, userLocale);


        //phone number check and set it to national format.
        mandatoryPhoneCheckandSet(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressValidatorAEImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_AE);
        sb.append('}');
        return sb.toString();
    }
}
