package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.PricingModelConfigurationDao;
import com.b2s.rewards.apple.model.PricingModel;
import com.b2s.rewards.apple.model.PricingModelConfiguration;
import com.b2s.apple.mapper.PricingModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by rpillai on 4/28/2017.
 */
@Service
public class PricingModelService {

    @Autowired
    private PricingModelConfigurationDao pricingModelConfigurationDao;

    @Autowired
    private PricingModelMapper pricingModelMapper;

    /**
     * Returns @List of @PricingModel objects for the given var id, program id and price type
     *
     * @param varId
     * @param programId
     * @param priceType
     * @return
     */
    public List<PricingModel> getPricingModels(final String varId, final String programId, final String priceType) {
        final List<PricingModelConfiguration> pricingModelConfigurations = pricingModelConfigurationDao.getByVarIdProgramIdPriceType(varId, programId, priceType);
        return pricingModelMapper.getPricingModels(pricingModelConfigurations);
    }
}
