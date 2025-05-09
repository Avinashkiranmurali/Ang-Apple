package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.entity.MerchantEntity;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.MerchantListDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Unit testing using H2 Database
 * H2 Embedded DB is configured in the DataSourceTestConfiguration
 * Schema changes are located in Test_schema_creation.sql
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
    DataSourceTestConfiguration.class})
@SqlGroup({
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_merchant.sql"),
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class MerchantListDaoTest {

    @Qualifier("merchantListDao")
    @Autowired
    private MerchantListDao dao;
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantListDaoTest.class);


    @Test
    public void testGetAll()
    {
        LOGGER.info("Inside testGetAll");
        List<MerchantEntity> result = dao.getAll();
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void testGetMerchant()
    {
        LOGGER.info("Inside testGetMerchant");
        Integer supplierId = 200;
        Integer merchantId = 30001;
        MerchantEntity result = dao.getMerchant(supplierId, merchantId);

        Assert.assertNotNull(result);
        Assert.assertEquals("Apple", result.getName());
    }

}
