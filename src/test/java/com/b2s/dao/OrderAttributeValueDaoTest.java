package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.OrderAttributeValueDao;
import com.b2s.rewards.apple.model.OrderAttributeValue;
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
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_order_attribute.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class OrderAttributeValueDaoTest {

    @Qualifier("orderAttributeValueDao")
    @Autowired
    private OrderAttributeValueDao dao;


    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAttributeValueDaoTest.class);


    @Test
    public void testInsert()
    {
        LOGGER.info("Inside testInsert");
        OrderAttributeValue value = new OrderAttributeValue();
        value.setOrderId(4L);
        value.setName("name");
        value.setValue("value");

        dao.insert(value);

        List<OrderAttributeValue> result = dao.getByOrder(4L);
        Assert.assertNotNull(result);
        Assert.assertEquals("value", result.get(0).getValue());
    }

    @Test
    public void testFindByOrderId()
    {
        LOGGER.info("Inside testFindByOrderId");

        List<OrderAttributeValue> result = dao.getByOrder(1L);
        Assert.assertEquals(1, result.size());

        result = dao.getByOrder(2L);
        Assert.assertEquals(2, result.size());

        result = dao.getByOrder(3L);
        Assert.assertEquals(3, result.size());
    }

}

