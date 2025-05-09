package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.ShoppingCartDao;
import com.b2s.rewards.apple.model.ShoppingCart;
import org.junit.Assert;
import org.junit.Ignore;
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

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_shopping_cart.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class ShoppingCartDaoTest {

    @Autowired
    @Qualifier("appleShoppingCartDao")
    private ShoppingCartDao appleShoppingCartDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingCartDaoTest.class);

    @Ignore
    @Test
    public void getShoppingCart() {

        LOGGER.info("Inside getShoppingCart");
        ShoppingCart result = appleShoppingCartDao.get("UA", "b2s_qa_only", "user");
        Assert.assertNotNull(result);
    }

    @Test
    public void createShoppingCart() {

        LOGGER.info("Inside getShoppingCart");
        List<ShoppingCart> beforeInsert = appleShoppingCartDao.findAll();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setVarId("RBC");
        shoppingCart.setProgramId("b2s_qa_only");
        shoppingCart.setUserId("user");
        appleShoppingCartDao.save(shoppingCart);
        List<ShoppingCart> afterInsert = appleShoppingCartDao.findAll();
        Assert.assertNotEquals(beforeInsert.size(), afterInsert.size());
    }


}

