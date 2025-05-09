package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.entity.DemoUserEntity;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.DemoUserDao;
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

/**
 * Unit testing using H2 Database
 * H2 Embedded DB is configured in the DataSourceTestConfiguration
 * Schema changes are located in Test_schema_creation.sql
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
    DataSourceTestConfiguration.class})
@SqlGroup({
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_demo_user.sql"),
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class DemoUserDaoTest {

    @Qualifier("demoUserDao")
    @Autowired
    private DemoUserDao dao;
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoUserDaoTest.class);


    @Test
    public void testFindByDemoUserIdAndPassword()
    {
        LOGGER.info("Inside testFindByDemoUserIdAndPassword");
        final DemoUserEntity.DemoUserId demoUserId = demoUserId();

        DemoUserEntity result = dao.findByDemoUserIdAndPassword(demoUserId, "YYY");
        Assert.assertNotNull(result);
        Assert.assertEquals("xxyympua@bakkt.com", result.getEmail());
    }

    @Test
    public void testGet()
    {
        LOGGER.info("Inside testGet");
        final DemoUserEntity.DemoUserId demoUserId = demoUserId();
        DemoUserEntity result = dao.get(demoUserId);

        Assert.assertNotNull(result);
        Assert.assertEquals("xxyympua@bakkt.com", result.getEmail());
    }

    private DemoUserEntity.DemoUserId demoUserId(){
        final DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId();
        demoUserId.setProgramId("MP");
        demoUserId.setVarId("UA");
        demoUserId.setUserId("XXX");
        return demoUserId;
    }

}
