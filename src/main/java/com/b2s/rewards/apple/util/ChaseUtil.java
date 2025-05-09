package com.b2s.rewards.apple.util;

import com.b2s.shop.common.order.var.UserChase;
import com.b2s.common.services.model.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rkumar 2019-02-27
 */
public class ChaseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChaseUtil.class);

    public static String zipValidation(final String zip) {
        if (zip.length() == 5) {
            return zip;
        }
        if (zip.length() == 9) {
            return zip.substring(0, 5) + "-" + zip.substring(5);
        }
        if (zip.length() == 10 && zip.contains("-")) {
            final String[] zipArr = zip.split("-");
            if (zipArr[0].length() == 5 && zipArr[1].length() == 4) {
                return zip;
            }
        }
        LOGGER.error("ZIP CODE length should be 9 or 5 digits");
        throw new IllegalArgumentException("ZIP CODE length should be 9 or 5 digits");

    }

    public static List<Address> getAddressList(final UserChase user) {
        final List<Address> addresses = new ArrayList<>();
        final String[] postalCode = zipValidation(user.getZip()).split("-");
        final Address.AddressBuilder addressBuilder = Address.builder()
            .withAddressId(1)
            .withAddress1(user.getAddr1())
            .withAddress2(user.getAddr2())
            .withCity(user.getCity())
            .withState(user.getState())
            .withCountry(user.getCountry())
            .withPhoneNumber(user.getPhone());
        if (postalCode.length == 2) {
            addressBuilder.withZip5(postalCode[0]);
            addressBuilder.withZip4(postalCode[1]);
        } else {
            addressBuilder.withZip5(postalCode[0]);
        }
        addresses.add(addressBuilder.build());
        return addresses;
    }

}
