package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.services.GiftPromoService;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.model.Program;
import com.b2s.shop.common.User;
import org.joda.money.CurrencyUnit;
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
import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.ENABLE_SMART_PRICING;

/**
 * Unit testing GiftPromoDao using H2 Database
 * H2 Embedded DB is configured in the DataSourceTestConfiguration
 * Schema changes are located in Test_schema_creation.sql
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_var_program_gift_promo.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class GiftPromoDaoTest {

    @Autowired
    private GiftPromoService giftPromoService;

    @Autowired
    private ProductServiceV3 productServiceV3;

    private static final Logger LOGGER = LoggerFactory.getLogger(GiftPromoDaoTest.class);

    /*
    * Validate Locale, Var & Program combination
    * Validate Start & End promo combination
    * Active & Not Active combination
    *
    * */

    @Test
    public void testGiftPromo() {
        LOGGER.info("Inside testVarProgramSpecificGiftPromo");
        User user =  getUser("Delta", "b2s_qa_only", Locale.US, "fivebox");
        final String qualifyingSku = "30001MWYK2ZM/A";
        List<String> giftPromoEntities = giftPromoService.getGiftPsids(user, qualifyingSku, new Program());
        Assert.assertNotNull(giftPromoEntities);
        Assert.assertEquals(2, giftPromoEntities.size());
        Assert.assertEquals("30001MVH22LL/A", giftPromoEntities.get(0));
        Assert.assertEquals("30001MWP22AM/A", giftPromoEntities.get(1));
    }

    @Test
    public void testDisableGiftPromo() {
        LOGGER.info("Inside testVarProgramSpecificGiftPromo");
        User user =  getUser("Delta", "b2s_qa_only", Locale.US, "fivebox");
        final String qualifyingSku = "30001MWYK2ZM/A";
        Program program = new Program();

        program.setConfig(Map.of("disableGwp", true));
        List<String> giftPromoEntities = giftPromoService.getGiftPsids(user, qualifyingSku, program);
        Assert.assertNotNull(giftPromoEntities);
        Assert.assertEquals(0, giftPromoEntities.size());
    }

    @Test
    public void testVarProgramSpecificGiftPromo() {
        LOGGER.info("Inside testVarProgramSpecificGiftPromo");
        User user =  getUser("Delta", "b2s_qa_only", Locale.US, "fivebox");
        final String qualifyingSku = "30001MWV72LL/A";
        List<String> giftPromoEntities = giftPromoService.getGiftPsids(user, qualifyingSku, new Program());
        Assert.assertNotNull(giftPromoEntities);
        Assert.assertEquals(2, giftPromoEntities.size());
        Assert.assertEquals("30001MWT92LL/A", giftPromoEntities.get(0));
        Assert.assertEquals("30001MY252LL/A", giftPromoEntities.get(1));
    }

    @Test
    public void testProgramGenericGiftPromo() {
        LOGGER.info("Inside testProgramGenericGiftPromo");
        User user =  getUser("Delta", "Demo", Locale.US, "fivebox");
        final String qualifyingSku = "30001MWV72LL/A";
        List<String> giftPromoEntities = giftPromoService.getGiftPsids(user, qualifyingSku, new Program());
        Assert.assertNotNull(giftPromoEntities);
        Assert.assertEquals(2, giftPromoEntities.size());
        Assert.assertEquals("30001MU8F2AM/A", giftPromoEntities.get(0));
        Assert.assertEquals("30001MWT82LL/A", giftPromoEntities.get(1));
    }

    @Test
    public void testCatalogsGenericGiftPromo() {
        LOGGER.info("Inside testCatalogsGenericGiftPromo");
        User user =  getUser("UA", "MP", Locale.US, "fivebox");
        final String qualifyingSku = "30001MWV72LL/A";
        List<String> giftPromoEntities = giftPromoService.getGiftPsids(user, qualifyingSku, new Program());
        Assert.assertNotNull(giftPromoEntities);
        Assert.assertEquals(2, giftPromoEntities.size());
        Assert.assertEquals("30001MVH22LL/A", giftPromoEntities.get(0));
        Assert.assertEquals("30001MWTJ2LL/A", giftPromoEntities.get(1));
    }

    @Test
    public void testVarGenericGiftPromo() {
        LOGGER.info("Inside testVarGenericGiftPromo");
        User user =  getUser("Chase", "b2s_qa_only", Locale.US, "fivebox");
        final String qualifyingSku = "30001MWV72LL/A";
        List<String> giftPromoEntities = giftPromoService.getGiftPsids(user, qualifyingSku, new Program());
        Assert.assertNotNull(giftPromoEntities);
        Assert.assertEquals(2, giftPromoEntities.size());
        Assert.assertEquals("30001MVH52LL/A", giftPromoEntities.get(0));
        Assert.assertEquals("30001MWP42LL/A", giftPromoEntities.get(1));
    }

    @Test
    public void testCatalogGenericGiftPromo() {
        LOGGER.info("Inside testCatalogGenericGiftPromo");
        User user =  getUser("RBC", "b2s_qa_only", Locale.CANADA_FRENCH, "fivebox");
        final String qualifyingSku = "30001MWV72LL/A";
        List<String> giftPromoEntities = giftPromoService.getGiftPsids(user, qualifyingSku, new Program());
        Assert.assertNotNull(giftPromoEntities);
        Assert.assertEquals(2, giftPromoEntities.size());
        Assert.assertEquals("30001MWP22AM/A", giftPromoEntities.get(0));
        Assert.assertEquals("30001MWTK2LL/A", giftPromoEntities.get(1));
    }

    @Test
    public void testGetGiftItem() {
        LOGGER.info("Inside testGetGiftItem");
        User user =  getUser("RBC", "b2s_qa_only", Locale.CANADA_FRENCH, "fivebox");
        Program program =  getProgram("RBC", "b2s_qa_only", "apple-ca-fr");
        final String qualifyingSku = "30001MWV72LL/A";
        List<String> giftPromoEntities = giftPromoService.getGiftPsids(user, qualifyingSku, new Program());
        List<Product> giftProducts = productServiceV3.getAppleMultiProductDetail(giftPromoEntities, program, false,
            user, true, false);
        Assert.assertNotNull(giftProducts);
        Assert.assertEquals(0, giftProducts.size());
    }

    List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        product.setPsid("30001MWP22AM/A");
        products.add(product);
        product = new Product();
        product.setPsid("30001MWTK2LL/A");
        products.add(product);
        return products;
    }

    User getUser(String varId, String programId, Locale locale, String loginType) {
        User user = new User();
        user.setVarId(varId);
        user.setProgramId(programId);
        user.setLocale(locale);
        user.setLoginType(loginType);
        return user;
    }

    Program getProgram(String varId, String programId, String catalogId) {
        Program program = new Program();
        program.setVarId(varId);
        program.setProgramId(programId);
        program.setCatalogId(catalogId);
        program.setTargetCurrency(CurrencyUnit.of("USD"));

        Map<String, Object> configs = new HashMap<>();
        configs.put(ENABLE_SMART_PRICING, true);
        configs.put("catalog_id", "apple-us-en");
        program.setConfig(configs);
        return program;
    }
}