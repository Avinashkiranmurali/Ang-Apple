package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.VarProgramNotificationDao;
import com.b2s.rewards.apple.model.VarProgramNotification;
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


/**
 * Unit testing using H2 Database
 * H2 Embedded DB is configured in the DataSourceTestConfiguration
 * Schema changes are located in Test_schema_creation.sql
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_var_program_notification.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class VarProgramNotificationDaoTest {

    @Autowired
    private VarProgramNotificationDao dao;

    private static final Logger LOGGER = LoggerFactory.getLogger(VarProgramNotificationDaoTest.class);


    @Test
    public void testGetActiveEmailNotificationsByVarId()
    {
        LOGGER.info("Inside testGetActiveEmailNotificationsByVarId");

        List<VarProgramNotification> result =
            dao.getActiveEmailNotifications(List.of("-1", "BSWIFT"), List.of("default", "b2s_qa_only"),
                List.of("-1", "en_US"));
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("admin", result.get(0).getLastupdateUser());

        result = dao.getActiveEmailNotifications(List.of("-1", "DELTA"), List.of("default", "b2s_qa_only"),
            List.of("-1", "en_US"));
        Assert.assertEquals(0, result.size());

    }
}

