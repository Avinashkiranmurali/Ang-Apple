package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.PricingModelConfigurationDao;
import com.b2s.rewards.apple.model.PricingModel;
import com.b2s.rewards.apple.model.PricingModelConfiguration;
import com.b2s.apple.mapper.PricingModelMapper;
import com.b2s.common.services.exception.ServiceException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * Created by rpillai on 5/3/2017.
 */
public class PricingModelServiceTest {

    @InjectMocks
    private PricingModelService pricingModelService;

    @Mock
    private PricingModelConfigurationDao pricingModelConfigurationDao;

    @Spy
    private PricingModelMapper pricingModelMapper = new PricingModelMapper();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPricingModels() throws ServiceException {
        when(pricingModelConfigurationDao.getByVarIdProgramIdPriceType("RBC", "PBA", "UserCost")).thenReturn(getPricingModelConfgurations("RBC", "PBA", "UserCost", new String[]{"apple-watch-series1", "iphone-se"}));
        List<PricingModel> pricingModels =  pricingModelService.getPricingModels("RBC", "PBA", "UserCost");
        assertNotNull(pricingModels);
        assertEquals(2, pricingModels.size());
        for(PricingModel pricingModel: pricingModels) {
            assertNotNull(pricingModels);
            if(pricingModel.getPriceKey().equals("apple-watch-series1")) {
                assertEquals(new Double(0), pricingModel.getPaymentValue());
            }
        }
    }

    @Test
    public void testGetPricingModelsWithEmpty() throws ServiceException {
        when(pricingModelConfigurationDao.getByVarIdProgramIdPriceType("RBC", "GCP", "UserCost")).thenReturn(null);
        List<PricingModel> pricingModels =  pricingModelService.getPricingModels("RBC", "GCP", "UserCost");
        assertNotNull(pricingModels);
        assertEquals(0, pricingModels.size());
    }

    private List<PricingModelConfiguration> getPricingModelConfgurations(final String varId, final String programId, final String priceType, final String[] priceKeys) {
        List<PricingModelConfiguration> pricingModelConfigurations = new ArrayList<>();
        for(String priceKey : priceKeys) {
            PricingModelConfiguration pricingModelConfiguration = new PricingModelConfiguration();
            pricingModelConfiguration.setVarId(varId);
            pricingModelConfiguration.setProgramId(programId);
            pricingModelConfiguration.setPriceType(priceType);
            pricingModelConfiguration.setPriceKey(priceKey);
            if(priceKey.equals("apple-watch-series1")) {
                pricingModelConfiguration.setPaymentValue(0d);
            }
            if(priceKey.equals("iphone-se")) {
                pricingModelConfiguration.setPaymentValue(79d);
            }
            pricingModelConfigurations.add(pricingModelConfiguration);
        }
        return pricingModelConfigurations;
    }
}
