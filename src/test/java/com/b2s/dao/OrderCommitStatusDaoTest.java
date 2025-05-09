package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.entity.OrderCommitStatusEntity;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.OrderCommitStatusDao;
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
 *
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_order_commit_status.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class OrderCommitStatusDaoTest {

    @Autowired
    private OrderCommitStatusDao orderCommitStatusDao;


    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCommitStatusDaoTest.class);

    @Test
    public void testFindByVarIdAndProgramIdAndUserId() {

        LOGGER.info("Inside testFindByVarIdAndProgramIdAndUserId");
        List<OrderCommitStatusEntity> result = orderCommitStatusDao.findByVarIdAndProgramIdAndUserId("varid1", "default", "user1");
        Assert.assertEquals(1, result.size());
        result = orderCommitStatusDao.findByVarIdAndProgramIdAndUserId("varid2", "default", "user1");
        Assert.assertEquals(2, result.size());
        result = orderCommitStatusDao.findByVarIdAndProgramIdAndUserId("varid3", "default", "user1");
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void testDeleteByVarIdAndProgramIdAndUserIdAndOrderHashCode() {

        LOGGER.info("Inside testDeleteByVarIdAndProgramIdAndUserIdAndOrderHashCode");
        orderCommitStatusDao.deleteByVarIdAndProgramIdAndUserIdAndOrderHashCode("varid1", "default", "user1", 1);

        List<OrderCommitStatusEntity> result = orderCommitStatusDao.findByVarIdAndProgramIdAndUserId("varid1", "default", "user1");
        Assert.assertEquals(0, result.size());
    }

}

