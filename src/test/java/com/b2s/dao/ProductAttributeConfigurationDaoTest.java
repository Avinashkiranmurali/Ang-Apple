package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.ProductAttributeConfigurationDao;
import com.b2s.rewards.apple.model.CategoryConfiguration;
import com.b2s.rewards.apple.model.ProductAttributeConfiguration;
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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_product_attribute_configuration.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class ProductAttributeConfigurationDaoTest {

    @Autowired
    private ProductAttributeConfigurationDao productAttributeConfigurationDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductAttributeConfigurationDaoTest.class);

    @Test
    public void getByCategoryConfiguration() {

        LOGGER.info("Inside getByCategoryConfiguration");
        List<ProductAttributeConfiguration> result = productAttributeConfigurationDao.findByCategoryConfiguration(getCategoryConfiguration());
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getByCategoryConfigurationAndCategorySlugIsNotNullAndDetails() {

        LOGGER.info("Inside getByCategoryConfigurationAndCategorySlugNullAndDetails");
        List<ProductAttributeConfiguration> result = productAttributeConfigurationDao.findByCategoryConfigurationAndCategorySlugNullAndDetails(null);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getByCategoryConfigurationAndCategorySlugNullAndDetails() {

        LOGGER.info("Inside getByCategoryConfigurationAndCategorySlugNullAndDetails");
        List<ProductAttributeConfiguration> result = productAttributeConfigurationDao.findByCategoryConfigurationAndCategorySlugNullAndDetails(getCategoryConfiguration());
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    private CategoryConfiguration getCategoryConfiguration() {
        return new CategoryConfiguration(1288, "macbook-air");
    }

    @Test
    public void testCacheConfiguration() {
        List<ProductAttributeConfiguration> result =
            productAttributeConfigurationDao.findByCategoryConfiguration(getCategoryConfiguration());
        List<ProductAttributeConfiguration> resultNew =
            productAttributeConfigurationDao.findByCategoryConfiguration(getCategoryConfiguration());

        if(cacheEnabled()){
            Assert.assertSame(result, resultNew);
            Assert.assertEquals(result, resultNew);
        }else{
            Assert.assertNotSame(result, resultNew);
            Assert.assertEquals(result, resultNew);
        }

    }

    public boolean cacheEnabled(){
        final String cacheVMOption = "net.sf.ehcache.disabled";
        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        final List<String> vmArgs = runtimeMXBean.getInputArguments();
        for(final String vmArg: vmArgs){
            if(vmArg.contains(cacheVMOption)){
                if(vmArg.contains("false")){
                    return true;    //cacheEnabled
                }else{
                    return false;    //cacheDisabled
                }
            }
        }
        return true;    //cacheEnabled by default
    }
}
