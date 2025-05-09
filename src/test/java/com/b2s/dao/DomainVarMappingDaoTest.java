package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.DomainVarMappingDao;
import com.b2s.rewards.apple.model.DomainVarMapping;
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
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_domain_var_mapping.sql"),
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class DomainVarMappingDaoTest {

    @Qualifier("OtpDomainVarDao")
    @Autowired
    private DomainVarMappingDao dao;
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainVarMappingDaoTest.class);

    @Test
    public void testFindByDomain()
    {
        LOGGER.info("Inside testFindByDomain");
        String domain = "bakkt.com";
        String loginType = "otp";

        DomainVarMapping result = dao.findByDomain(domain, loginType);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getId());

        loginType = "url";
        result = dao.findByDomain(domain, loginType);
        Assert.assertNull(result);
    }

    @Test
    public void testIsDomainMappingExist()
    {
        LOGGER.info("Inside testIsDomainMappingExist");
        String domain = "bakkt.com";
        String loginType = "otp";
        boolean result = dao.isDomainMappingExist(domain, loginType);
        Assert.assertTrue(result);

        domain = "bridge2solutions.com";
        result = dao.isDomainMappingExist(domain, loginType);
        Assert.assertFalse(result);
    }

    @Test
    public void testGetDomainByLoginType(){
        LOGGER.info("Inside testGetDomainByLoginType");
        String loginType = "otp";
        List<DomainVarMapping> result = dao.getDomainByLoginType(loginType);

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }
}
