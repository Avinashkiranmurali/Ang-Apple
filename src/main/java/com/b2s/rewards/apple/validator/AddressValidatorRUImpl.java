package com.b2s.rewards.apple.validator;

import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static com.b2s.rewards.apple.validator.AddressValidatorConstants.*;
import static com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL;

/**
 * Created by Cong Xia on 11-09-2018.
 */
public class AddressValidatorRUImpl extends AddressValidatorImpl {

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

        //Zip length 6
        mandatoryZipCheck(address, ZIP_LENGTH_6, ZIP_LENGTH_6,
            messageSource.getMessage(INVALID_ZIP_6, null, INVALID_ZIP_5, userLocale));

        //phone number check and set it to international format.
        mandatoryRUPhoneCheckandSet(address, messageSource, userLocale);

        mandatoryEmailCheck(address, messageSource, userLocale);

        return !address.getErrorMessage().isEmpty();

    }

    /**
     * Mandatory check.
     *
     * @param address      the address for which validation require
     * @param messageSource the message source
     * @param userLocale    the user locale
     */
    private void mandatoryRUPhoneCheckandSet(final Address address, final MessageSource messageSource,
        final Locale userLocale) {
        final String phoneNumber = address.getPhoneNumber();
        final String serviceErrorMessage = ServiceExceptionEnums.INVALID_PHONE_NUMBER.getErrorMessage();
        final String field = CartItemOption.PHONE_NUMBER.getValue();
        final String message = messageSource.getMessage(INVALID_PHONE_NUMBER, null, INVALID_PHONE_NUMBER, userLocale);
        if (StringUtils.isBlank(phoneNumber)) {
            logger.error(serviceErrorMessage,phoneNumber);
            address.getErrorMessage().put(field, message);
        }else{
            final String formattedPhone = formatPhoneNumber(phoneNumber, address.getCountry(), INTERNATIONAL);
            if (formattedPhone == null) {
                logger.error(serviceErrorMessage,phoneNumber);
                address.getErrorMessage().put(field, message);
            }
            else{
                address.setPhoneNumber(formattedPhone);
            }
        }

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
        final StringBuilder sb = new StringBuilder("AddressValidatorRUImpl{");
        sb.append(CommonConstants.COUNTRY_CODE_ZA);
        sb.append('}');
        return sb.toString();
    }

}
