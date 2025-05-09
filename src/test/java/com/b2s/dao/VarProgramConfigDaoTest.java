package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.VarProgramConfigDao;
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
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_var_program_config.sql"),
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class VarProgramConfigDaoTest {

    @Autowired
    private VarProgramConfigDao dao;
    private static final Logger LOGGER = LoggerFactory.getLogger(VarProgramConfigDaoTest.class);


    @Test
    public void testGetVarProgramConfig()
    {
        LOGGER.info("Inside testGetVarProgramConfig");
        String varId = "UA";
        String programId = "MP";

        List<VarProgramConfigEntity> result = dao.getVarProgramConfig(varId, programId);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testGetVarProgramConfigByVarAndName()
    {
        LOGGER.info("Inside testGetVarProgramConfigByVarAndName");
        String varId = "PNC";
        String name = "showDollars";
        List<VarProgramConfigEntity> result = dao.getVarProgramConfigByVarAndName(varId, name);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testGetVarProgramConfigByVarProgramName()
    {
        LOGGER.info("Inside testGetVarProgramConfigByVarProgramName");
        String varId = "RBC";
        String programId = "b2s_qa_only";
        String name = "catalog_id";
        VarProgramConfigEntity result = dao.getVarProgramConfigByVarProgramName(varId, programId, name);

        Assert.assertNotNull(result);
        Assert.assertEquals("apple-ca-en", result.getValue());
    }

}
