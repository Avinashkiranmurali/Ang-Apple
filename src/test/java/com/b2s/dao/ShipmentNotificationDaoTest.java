package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.ShipmentNotificationDao;
import com.b2s.rewards.apple.model.OrderLineShipmentNotification;
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

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_olsn.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class ShipmentNotificationDaoTest {

    @Autowired
    private ShipmentNotificationDao dao;


    private static final Logger LOGGER = LoggerFactory.getLogger(ShipmentNotificationDaoTest.class);

    @Test
    public void testGetShipmentNotification() {

        LOGGER.info("Inside testGetShipmentNotification");
        OrderLineShipmentNotification result = dao.getShipmentNotification(1L, 1);
        Assert.assertNotNull(result);

        result = dao.getShipmentNotification(2L, 1);
        Assert.assertNotNull(result);

        result = dao.getShipmentNotification(3L, 1);
        Assert.assertNotNull(result);

        result = dao.getShipmentNotification(3L, 2);
        Assert.assertNull(result);

        result = dao.getShipmentNotification(4L, 1);
        Assert.assertNull(result);


    }
}
