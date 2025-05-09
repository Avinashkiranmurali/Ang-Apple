package com.b2s.rewards.apple.validator;

import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.model.Program;
import com.b2s.shop.common.User;
import org.springframework.context.MessageSource;

import java.util.Map;

/**
 * Created by rperumal on 3/11/2016.
 */
public interface AddressValidatorIF {

    Map<String, String> getErrorMessage();

    void setErrorMessage(Map<String, String> errorMessage);

    Map<String, String> getWarningMessage();

    void setWarningMessage(Map<String, String> warningMessage);

    boolean isValidEmail(String email);

    // Check if address has PO Box
    boolean hasPoBox(String address1, String address2);

    //check of AddressLine1 has any PO box
    boolean hasPOBoxInAddress1(String address1);

    //check of AddressLine2 has any PO box
    boolean hasPOBoxInAddress2(String addressLine2);

    boolean adheresToPattern(String matchString, String pattern);

    boolean hasValidationError(Address address, MessageSource messageSource, User user);

    boolean isInvalidAddressUpdate(final Address cartAddress, final Address newShippingAddress, final Program program);

    void replaceSpecialCharWithEmpty(final Address address, final String locale);
}
