package com.b2s.common.services.discountservice;

import com.b2s.rewards.apple.model.DiscountCode;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hranganathan on 4/6/2017.
 */
@Component
public class CouponCodeValidator {

    @Autowired
    private DiscountServiceClient discountServiceClient;


    public List<DiscountCode> removeInvalidDiscount(final User user, final List<DiscountCode> discountCodes) {
        //To remove the discount code if it is already applied for this user.
        final List<DiscountCode> validDiscountCodes = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(discountCodes)) {
            discountCodes.stream().forEach(discountCode -> {
                final CouponDetails couponDetails = discountServiceClient.getValidDiscountCode(discountCode.getDiscountCode(), user);
                if (couponDetails != null && couponDetails.isValid()) {
                    validDiscountCodes.add(discountCode);
                }
            });
        }
        return validDiscountCodes;
    }

}
