package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.VarProgramFinanceOptionDao;
import com.b2s.rewards.apple.model.VarProgramFinanceOption;
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

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase;

/**
 * Unit testing VarProgramFinanceOptionDao using H2 Database
 * H2 Embedded DB is configured in the DataSourceTestConfiguration
 * Schema changes are located in Test_schema_creation.sql
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
    DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_finance_options.sql"),
        @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class VarProgramFinanceOptionDaoTest {

    @Autowired
    private VarProgramFinanceOptionDao varProgramFinanceOptionDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(VarProgramFinanceOptionDaoTest.class);

    /**
     * Testing insert for var_program_finance_option table
     */
    @Test
    public void testInsertVarProgramFinanceOption() throws Exception {

        LOGGER.info("Inside testInsertVarProgramFinanceOption");

        VarProgramFinanceOption varProgramFinanceOption = new VarProgramFinanceOption();
        varProgramFinanceOption.setVarId("Amex");
        varProgramFinanceOption.setProgramId("b2s_qa_only");
        varProgramFinanceOption.setInstallment(3);
        varProgramFinanceOption.setInstallmentPeriod("month");
        varProgramFinanceOption.setMessageCode("finance.month.3.text");
        varProgramFinanceOption.setActive(true);
        varProgramFinanceOption.setOrderBy(4);
        varProgramFinanceOptionDao.save(varProgramFinanceOption);

        List<VarProgramFinanceOption> varProgramFinanceOptionList = varProgramFinanceOptionDao.getVarProgramFinanceOption("Amex", "b2s_qa_only");
        LOGGER.debug("varProgramFinanceOptionList Size " + varProgramFinanceOptionList.size());

        // Count is asserted to 4 records (3 got inserted via SQL query and 1 via Hibernate DAO)
        Assert.assertEquals(4, varProgramFinanceOptionList.stream().count());
        Assert.assertNotNull(varProgramFinanceOptionList.stream().filter(varProgramFinanceOpt -> varProgramFinanceOpt.getInstallment().equals(3)).findAny().get());
        Assert.assertEquals("finance.month.6.text", varProgramFinanceOptionList.stream().filter(varProgramFinanceOpt -> varProgramFinanceOpt.getInstallment().equals(6)).findFirst().get().getMessageCode());

    }

    /**
     * Testing GET var_program_finance_option table
     */
    @Test
    public void testGetVarProgramFinanceOption() throws Exception {

        LOGGER.info("Inside testGetVarProgramFinanceOption");

        List<VarProgramFinanceOption> varProgramFinanceOptionList = varProgramFinanceOptionDao.getVarProgramFinanceOption("AmexAU", "default");
        LOGGER.debug("varProgramFinanceOptionList Size " + varProgramFinanceOptionList.size());

        // Count is asserted to 5 records (all of it got inserted via SQL query)
        Assert.assertEquals(5, varProgramFinanceOptionList.stream().count());
        Assert.assertNotNull(varProgramFinanceOptionList.stream().filter(varProgramFinanceOpt -> varProgramFinanceOpt.getInstallment().equals(10)).findAny().get());
        Assert.assertEquals("finance.month.10.text", varProgramFinanceOptionList.stream().filter(varProgramFinanceOpt -> varProgramFinanceOpt.getInstallment().equals(10)).findFirst().get().getMessageCode());
        Assert.assertEquals(5, varProgramFinanceOptionList.stream().filter(varProgramFinanceOpt -> varProgramFinanceOpt.getInstallment().equals(10)).findFirst().get().getEstablishmentFeeRate().floatValue(), 0);

    }
}
