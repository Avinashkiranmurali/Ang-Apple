package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.VarProgramPaymentOptionDao;
import com.b2s.rewards.apple.model.VarProgramPaymentOption;
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
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_var_program_payment_option.sql"),
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class VarProgramPaymentOptionDaoTest {

    @Autowired
    private VarProgramPaymentOptionDao dao;
    private static final Logger LOGGER = LoggerFactory.getLogger(VarProgramPaymentOptionDaoTest.class);

    @Test
    public void testGetVarProgramPaymentOption()
    {
        LOGGER.info("Inside testGetVarProgramPaymentOption");
        String varId = "UA";
        String programId = "MP";

        List<VarProgramPaymentOption> result = dao.getVarProgramPaymentOption(varId, programId);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

}
