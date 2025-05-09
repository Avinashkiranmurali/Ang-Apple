package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.MercSearchFilterDao;
import com.b2s.rewards.apple.model.MercSearchFilter;
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
import java.util.Date;


/**
 * Unit testing using H2 Database
 * H2 Embedded DB is configured in the DataSourceTestConfiguration
 * Schema changes are located in Test_schema_creation.sql
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_merc_search_filter.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class MercSearchFilterDaoTest {

    @Qualifier("mercSearchFilterDao")
    @Autowired
    private MercSearchFilterDao dao;

    private static final Logger LOGGER = LoggerFactory.getLogger(MercSearchFilterDaoTest.class);


    @Test
    public void testGetByPK()
    {
        LOGGER.info("Inside testGetByPK");

        MercSearchFilter result = dao.getByPK(null, null, null, null, "value");
        Assert.assertNotNull(result);
        Assert.assertEquals("value", result.getFilterValue());
    }

    @Test
    public void testInsert()
    {
        LOGGER.info("Inside testInsert");
        MercSearchFilter value = new MercSearchFilter();
        value.setFilterValue("newValue");
        value.setAddedDate(new Date());
        dao.add(value);

        MercSearchFilter result = dao.getByPK(null, null, null, null, "newValue");
        Assert.assertNotNull(result);
        Assert.assertEquals("newValue", result.getFilterValue());
    }

    @Test
    public void testGetById()
    {
        LOGGER.info("Inside testGetById");

        MercSearchFilter result = dao.getById(1);
        Assert.assertNotNull(result);
        Assert.assertEquals("value", result.getFilterValue());
    }

    @Test
    public void testDelete()
    {
        LOGGER.info("Inside testDelete");

        MercSearchFilter result = dao.getByPK(null, null, null, null, "value");
        Assert.assertNotNull(result);

        dao.delete(result);

        result = dao.getByPK(null, null, null, null, "value");
        Assert.assertNull(result);
    }

}

