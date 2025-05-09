package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.PricingModelConfigurationDao;
import com.b2s.rewards.apple.model.PricingModelConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_pricing_model_configuration.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class PricingModelConfigurationDaoTest {

    @Autowired
    private PricingModelConfigurationDao pricingModelConfigurationDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(PricingModelConfigurationDaoTest.class);

    @Test
    public void getByVarIdProgramId() {
        LOGGER.info("Inside getByVarIdProgramId");
        List<PricingModelConfiguration> result = pricingModelConfigurationDao.getByVarIdProgramId("VitalityUS", "JohnHancock");
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getByVarIdProgramIdPriceType() {
        LOGGER.info("Inside getByVarIdProgramIdPriceType");
        List<PricingModelConfiguration> result = pricingModelConfigurationDao.getByVarIdProgramIdPriceType("VitalityUS", "JohnHancock", "Subsidy");
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getSubsidyByVarIdProgramIdPriceKey() {
        LOGGER.info("Inside getSubsidyByVarIdProgramIdPriceKey");
        PricingModelConfiguration result = pricingModelConfigurationDao.getSubsidyByVarIdProgramIdPriceKey("VitalityUS", "JohnHancock", "apple-watch-nikeplus-series5|44mm");
        Assert.assertNotNull(result);
        Assert.assertEquals("Subsidy", result.getPriceType());
    }

    @Test
    public void getAllSubsidiesByVarIdProgramIdPriceKey() {
        LOGGER.info("Inside getAllSubsidiesByVarIdProgramIdPriceKey");
        List<PricingModelConfiguration> result = pricingModelConfigurationDao.getAllSubsidiesByVarIdProgramIdPriceKey("VitalityUS", "JohnHancock", "apple-watch-");
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }
}
