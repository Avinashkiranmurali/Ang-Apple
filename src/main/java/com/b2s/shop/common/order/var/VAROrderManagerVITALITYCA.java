package com.b2s.shop.common.order.var;

import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.springframework.stereotype.Component;

/**
 * Created by srukmagathan on 23-02-2017.
 */
@Component("varOrderManagerVitalityCA")
public class VAROrderManagerVITALITYCA extends VAROrderManagerVITALITY {

    private static final String LOCALE_STRING = "en_CA";

    @Override
    protected String getVARId() {
        return CommonConstants.VAR_VITALITYCA;
    }

    @Override
    protected String getLocale() {
        return LOCALE_STRING;
    }

    @Override
    public boolean isValidLogin(final User user, final Program program) {
        return isValidProgramAndPricingModel(user, program); //// This may not be needed with new NPI changes (series1 & series2)
    }

    private boolean isValidProgramAndPricingModel(final User user, final Program program) {
        boolean success = true;
        if (user != null && program == null) {
                success = false;
        }
        return success;
    }

    @Override
    public void setCountry(final User user){
        user.setCountry(CommonConstants.COUNTRY_CODE_CA);
    }

}
