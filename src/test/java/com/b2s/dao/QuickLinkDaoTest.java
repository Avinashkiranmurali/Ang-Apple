package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.services.QuickLinkService;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.model.QuickLink;
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
 * Unit testing QuickLinkDao using H2 Database
 * H2 Embedded DB is configured in the DataSourceTestConfiguration
 * Schema changes are located in Test_schema_creation.sql
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_quick_link.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class QuickLinkDaoTest {

    @Autowired
    private QuickLinkService quickLinkService;

    @Autowired
    private HttpServletRequest request;

    private static final Logger LOGGER = LoggerFactory.getLogger(QuickLinkDaoTest.class);

    @Test
    public void testUAwithMPvarProgram() {

        LOGGER.info("Inside testUAwithMPvarProgram");
        User user =  getUser("UA", "MP", Locale.US, "fivebox");
        request.getSession().setAttribute("USER",user);
        List<QuickLink> result = quickLinkService.getByVarIdProgramIdLocale(user);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(result, getListOfUaAndDefaultQuickLinks());
    }

    @Test
    public void testPncWithDefaultVarProgram() {

        LOGGER.info("Inside testPNCwithDefaultVarProgram");
        User user = getUser("PNC", "default", Locale.US, "fivebox");
        request.getSession().setAttribute("USER", user);
        List<QuickLink> result = quickLinkService.getByVarIdProgramIdLocale(user);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(result, getListOfPncAndDefaultQuickLinks());
    }

    @Test
    public void testPncWithDefaultVarProgramForUnauthenticatedUser() {

        LOGGER.info("Inside testPNCwithDefaultVarProgram");
        User user = getUser("PNC", "default", Locale.US, "anonymous");
        request.getSession().setAttribute("USER", user);
        List<QuickLink> result = quickLinkService.getByVarIdProgramIdLocale(user);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(result, getListOfPncAndDefaultWithUnauthenticatedUser());
    }

    @Test
    public void testRbcPbaWithDisplayFalse() {

        LOGGER.info("Inside testPNCwithDefaultVarProgram");
        User user = getUser("RBC", "PBA", Locale.CANADA, "fivebox");
        request.getSession().setAttribute("USER", user);
        List result = quickLinkService.getByVarIdProgramIdLocale(user);
        Assert.assertNull(result);

    }


    @Test
    public void testUaAnonymousWithUnauthenticatedFalse() {

        LOGGER.info("Inside testUaAnonymousWithUnauthenticatedFalse");
        User user = getUser("UA", "anonymous", Locale.US, "anonymous");
        request.getSession().setAttribute("USER", user);
        List result = quickLinkService.getByVarIdProgramIdLocale(user);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(result, getListOfUaAnonymousWithDisplayFalse());
    }

    User getUser(String varId, String programId, Locale locale, String loginType) {
        User user = new User();
        user.setVarId(varId);
        user.setProgramId(programId);
        user.setLocale(locale);
        user.setLoginType(loginType);
        if(loginType.equalsIgnoreCase("anonymous")){
            user.setAnonymous(true);
        }
        return user;
    }

    List<QuickLink> getListOfUaAndDefaultQuickLinks() {
        List<QuickLink> theList = new ArrayList<>();
        theList.add(getQuickLink("-1", "default", "en_US", "airpods", "AirPods", "/browse/music/music-airpods/", 10, true, true));
        theList.add(getQuickLink("-1", "default", "en_US", "applePencil", "Apple Pencil", "/browse/ipad/apple-pencil/", 20, true, true));
        theList.add(getQuickLink("UA", "default", "en_US", "orderHistory", "Order History", "/order-history/", 40, false, true));
        theList.add(getQuickLink("-1", "default", "en_US", "affordableInBalance", "Items within your Point Balance", "/browse/affordableTBD", 50, false, true));
        return theList;
    }

    List<QuickLink> getListOfPncAndDefaultQuickLinks() {
        List<QuickLink> theList = new ArrayList<>();
        theList.add(getQuickLink("-1", "default", "en_US", "airpods", "AirPods", "/browse/music/music-airpods/", 10, true, true));
        theList.add(getQuickLink("-1", "default", "en_US", "affordableInBalance", "Items within your Point Balance", "/browse/affordableTBD", 50, false, true));
        return theList;
    }

    List<QuickLink> getListOfPncAndDefaultWithUnauthenticatedUser() {
        List<QuickLink> theList = new ArrayList<>();
        theList.add(getQuickLink("-1", "default", "en_US", "airpods", "AirPods", "/browse/music/music-airpods/", 10, true, true));
        return theList;
    }


    List<QuickLink> getListOfUaAnonymousWithDisplayFalse() {
        List<QuickLink> theList = new ArrayList<>();
        theList.add(getQuickLink("-1", "default", "en_US", "airpods", "AirPods", "/browse/music/music-airpods/", 10, true, true));
        theList.add(getQuickLink("-1", "default", "en_US", "applePencil", "Apple Pencil", "/browse/ipad/apple-pencil/", 20, true, true));
        return theList;
    }

    QuickLink getQuickLink(String varId, String programId, String locale, String linkCode, String linkText,
                           String linkUrl, int priority, boolean showUnauthenticated, boolean display) {
        return QuickLink.builder()
                .withLocale(locale)
                .withLinkCode(linkCode)
                .withVarId(varId)
                .withProgramId(programId)
                .withLinkText(linkText)
                .withLinkUrl(linkUrl)
                .withOrder(priority)
                .withDisplay(display)
                .withShowUnauthenticated(showUnauthenticated).build();
    }

}

