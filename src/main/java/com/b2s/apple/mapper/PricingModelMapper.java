package com.b2s.apple.mapper;

import com.b2s.rewards.apple.model.PricingModel;
import com.b2s.rewards.apple.model.PricingModelConfiguration;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rpillai on 4/28/2017.
 */
@Component
public class PricingModelMapper {

    /**
     * Converts @List of @PricingModelConfiguration objects to @List of @PricingModel objects
     *
     * @param pricingModelConfigurations
     * @return
     */
    public List<PricingModel> getPricingModels(final List<PricingModelConfiguration> pricingModelConfigurations) {
        List<PricingModel> pricingModels = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(pricingModelConfigurations)) {
            for(PricingModelConfiguration pricingModelConfiguration : pricingModelConfigurations) {
                PricingModel pricingModel = this.getPricingModel(pricingModelConfiguration);
                if(pricingModel != null) {
                    pricingModels.add(pricingModel);
                }
            }
        }
        return pricingModels;
    }

    /**
     * Converts @PricingModelConfiguration object to @PricingModel object
     *
     * @param pricingModelConfiguration
     * @return
     */
    private PricingModel getPricingModel(final PricingModelConfiguration pricingModelConfiguration) {
        PricingModel pricingModel = null;
        if(pricingModelConfiguration != null) {
            pricingModel = new PricingModel();
            pricingModel.setPriceType(pricingModelConfiguration.getPriceType());
            pricingModel.setPriceKey(pricingModelConfiguration.getPriceKey());
            pricingModel.setPaymentValue(pricingModelConfiguration.getPaymentValue());
            pricingModel.setPaymentValuePoints(pricingModelConfiguration.getPaymentValuePoints());
        }
        return pricingModel;
    }
}
