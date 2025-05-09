package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.VarProgramDomainUserRestrictionDao;
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
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_var_program_domain_user_restriction.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class VarProgramDomainUserRestrictionDaoTest {

    @Autowired
    private VarProgramDomainUserRestrictionDao dao;


    private static final Logger LOGGER = LoggerFactory.getLogger(VarProgramDomainUserRestrictionDaoTest.class);

    @Test
    public void testIsUserOfAuthType() {

        LOGGER.info("Inside testIsUserOfAuthType");
        boolean result = dao.isUserOfAuthType("user1@gmail.com", "var1", "EPP", "otp", "blacklist");
        Assert.assertTrue(result); // active user

        result = dao.isUserOfAuthType("user2@gmail.com", "var2", "EPP", "otp", "blacklist");
        Assert.assertFalse(result); // inactive user

        result = dao.isUserOfAuthType("user3@gmail.com", "var1", "EPP", "otp", "blacklist");
        Assert.assertFalse(result); // user does not exist

        result = dao.isUserOfAuthType("user1@gmail.com", "varX", "EPP", "otp", "blacklist");
        Assert.assertFalse(result); // varX does not exist in the database

        result = dao.isUserOfAuthType("user1@gmail.com", "var1", "EPP", "otp", "list");
        Assert.assertFalse(result); // auth type = list not there in database

        result = dao.isUserOfAuthType("user1@gmail.com", "var1", "EPP", "saml", "blacklist");
        Assert.assertFalse(result); // login type saml not there in databse
    }
}

