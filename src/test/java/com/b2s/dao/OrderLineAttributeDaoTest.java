package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.entity.OrderLineAttributeEntity;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.OrderLineAttributeDao;
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
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_ol_attribute.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class OrderLineAttributeDaoTest {

    @Autowired
    private OrderLineAttributeDao dao;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderLineAttributeDaoTest.class);

    @Test
    public void testFindByOrderIdAndLineNumAndName() {

        LOGGER.info("Inside testFindByOrderIdAndLineNumAndName");
        List<OrderLineAttributeEntity> result = dao.findByOrderIdAndLineNumAndName(1L, 1, "shippingAvailablity");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(null, result.get(0).getValue());

        result = dao.findByOrderIdAndLineNumAndName(2L, 1, "shippingAvailablity");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("2 weeks", result.get(0).getValue());

        result = dao.findByOrderIdAndLineNumAndName(3L, 1, "shippingAvailablity");
        Assert.assertEquals(3, result.size());
    }

    /**
     * The Repeated verifies that order_id which is auto incremented is starting with 1 by the data_cleanup.sql script.
     */
    @Test
    public void testFindByOrderIdAndLineNumAndNameRepeated() {

        LOGGER.info("Inside testFindByOrderIdAndLineNumAndNameRepeated");
        List<OrderLineAttributeEntity> result = dao.findByOrderIdAndLineNumAndName(1L, 1, "shippingAvailablity");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(null, result.get(0).getValue());

        result = dao.findByOrderIdAndLineNumAndName(2L, 1, "shippingAvailablity");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("2 weeks", result.get(0).getValue());

        result = dao.findByOrderIdAndLineNumAndName(3L, 1, "shippingAvailablity");
        Assert.assertEquals(3, result.size());
    }

}

