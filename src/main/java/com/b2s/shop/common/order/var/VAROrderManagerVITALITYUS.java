package com.b2s.shop.common.order.var;

import com.b2s.rewards.apple.model.PricingModelConfiguration;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Component("varOrderManagerVitalityUS")
public class VAROrderManagerVITALITYUS extends VAROrderManagerVITALITY {

    @Override
    protected String getVARId() {
        return CommonConstants.VAR_VITALITYUS;
    }

    @Override
    protected String getLocale() {
        return CommonConstants.LOCALE_EN_US;
    }

    @Override
    public boolean isValidLogin(final User user, final Program program) {
        return isValidProgramAndPricingModel(user, program); //// This may not be needed with new NPI changes (series1 & series2)
    }

    private boolean isValidProgramAndPricingModel(final User user, final Program program) {
        boolean success = true;
        if (user != null) {
            if (program == null) {
                success = false;
            } else{
                if (isProgramTVGCorporate(program.getProgramId())) {
                    success = processTVGCorporateProgram(user);
                }
            }
        }
        return success;
    }

    private boolean processTVGCorporateProgram(User user) {
        boolean success=true;
        if (MapUtils.isNotEmpty(user.getAdditionalInfo()) &&
                (user.getAdditionalInfo().containsKey(CommonConstants.PRICING_ID))) {
            final Map<String, String> additionalInfo = user.getAdditionalInfo();
            final String pricingId = additionalInfo.get(CommonConstants.PRICING_ID);
            if (isNotBlank(pricingId)) {
                List<PricingModelConfiguration> pricingModelConfigurations  = pricingModelConfigurationDao
                        .getAllSubsidiesByVarIdProgramIdPriceKey(user.getVarId(), user.getProgramId(), pricingId);
                if (pricingModelConfigurations == null) {
                    success = false;
                }
            } else {
                success = false;
            }
        } else {
            success = false;
        }
        return success;
    }

}