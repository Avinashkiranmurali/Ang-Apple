package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.services.SearchRedirectService;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.model.SearchRedirect;
import com.b2s.shop.common.User;
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

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Unit testing SearchRedirectDao using H2 Database
 * H2 Embedded DB is configured in the DataSourceTestConfiguration
 * Schema changes are located in Test_schema_creation.sql
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_search_redirect.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class SearchRedirectDaoTest {

    @Autowired
    private SearchRedirectService searchRedirectService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchRedirectDaoTest.class);

    @Test
    public void testVarProgramSpecificALTERNATESearchResults() {

        LOGGER.info("Inside testVarProgramSpecificALTERNATESearchResults");
        User user =  getUser("UA", "MP", Locale.US, "fivebox");
        final String catalogId = "apple-us-en";
        final String keyword = "Adaptor";
        SearchRedirect searchRedirect = searchRedirectService.getSearchRedirect(user, catalogId, keyword);
        Assert.assertNotNull(searchRedirect);
        Assert.assertEquals("adapter", searchRedirect.getValue());
    }

    @Test
    public void testProgramGenericREDIRECTSearchResults() {

        LOGGER.info("Inside testProgramGenericREDIRECTSearchResults");
        User user =  getUser("UA", "MP", Locale.US, "fivebox");
        final String catalogId = "apple-us-en";
        final String keyword = "airpod pro";
        SearchRedirect searchRedirect = searchRedirectService.getSearchRedirect(user, catalogId, keyword);
        Assert.assertNotNull(searchRedirect);
        Assert.assertEquals("#/store/browse/music/music-airpods", searchRedirect.getValue());
    }

    @Test
    public void testVarGenericREDIRECT_ON_NO_RESULTSearchResults() {

        LOGGER.info("Inside testVarGenericREDIRECT_ON_NO_RESULTSearchResults");
        User user =  getUser("UA", "MP", Locale.US, "fivebox");
        final String catalogId = "apple-us-en";
        final String keyword = "AiRFly";
        SearchRedirect searchRedirect = searchRedirectService.getSearchRedirect(user, catalogId, keyword);
        Assert.assertNotNull(searchRedirect);
        Assert.assertEquals("#/store/curated/accessories/all-accessories/all-accessories-wireless-headphones",
            searchRedirect.getValue());
    }

    @Test
    public void testCatalogGenericALTERNATESearchResults() {

        LOGGER.info("Inside testCatalogGenericALTERNATESearchResults");
        User user =  getUser("UA", "MP", Locale.US, "fivebox");
        final String catalogId = "apple-us-en";
        final String keyword = "airfly next";
        SearchRedirect searchRedirect = searchRedirectService.getSearchRedirect(user, catalogId, keyword);
        Assert.assertNotNull(searchRedirect);
        Assert.assertEquals("butterfly", searchRedirect.getValue());
    }

    User getUser(String varId, String programId, Locale locale, String loginType) {
        User user = new User();
        user.setVarId(varId);
        user.setProgramId(programId);
        user.setLocale(locale);
        user.setLoginType(loginType);
        return user;
    }
}

