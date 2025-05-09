package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.entity.VarProgramCatalogConfigEntity;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.VarProgramCatalogConfigDao;
import org.junit.Assert;
import org.junit.Before;
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
import java.util.ArrayList;
import java.util.List;

import static com.b2s.rewards.common.util.CommonConstants.*;


/**
 * Unit testing VarProgramCatalogConfigDaoTest using H2 Database
 * H2 Embedded DB is configured in the DataSourceTestConfiguration
 * Schema changes are located in Test_schema_creation.sql
 * The testing will verify the following 6 level hierarchy.
 *
 * catalog/var/program
 *
 * catalog/var/-1
 *
 * catalog/-1/-1
 *
 * catalog/-1/program - should not happen as program cannot exist without a var
 *
 * -1/var/program - need Catalog ID as well
 *
 * -1/var/-1 - need Catalog ID as well
 *
 * -1/-1/program - should not happen as program cannot exist without a var.
 *
 * -1/-1/-1 - global level. 
 *
 * 8 level hierarchy but 2 are not valid in our case. So result is 6 level.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_vpcc.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class VarProgramCatalogConfigDaoTest {

    @Autowired
    private VarProgramCatalogConfigDao varProgramCatalogConfigDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(VarProgramCatalogConfigDaoTest.class);

    final List<String> catalogIds = new ArrayList<>();
    final List<String> varIds = new ArrayList<>();
    final List<String> programIds = new ArrayList<>();

    @Before
    public void setup() {
        catalogIds.add(DEFAULT_CATALOG_ID);
        varIds.add(DEFAULT_VAR_PROGRAM);
        programIds.add(DEFAULT_PROGRAM_KEY);
    }

    @Test
    public void testCatalogVarProgramEmpty() {
        LOGGER.info("Inside catalog/var/program");
        String catalogId = "apple-us-en";
        catalogIds.add(catalogId);
        String varId = "PNC";
        varIds.add(varId);
        String programId = "Onyx";
        programIds.add(programId);
        List<VarProgramCatalogConfigEntity> varProgramCatalogConfigEntities =
            varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, "test");
        Assert.assertTrue(varProgramCatalogConfigEntities.isEmpty());
    }

    /**
     * catalog/var/program
     */
    @Test
    public void testCatalogVarProgram() {

        LOGGER.info("Inside catalog/var/program");
        String catalogId = "apple-us-en";
        catalogIds.add(catalogId);
        String varId = "PNC";
        varIds.add(varId);
        String programId = "Onyx";
        programIds.add(programId);
        List<VarProgramCatalogConfigEntity> varProgramCatalogConfigEntities =
            varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, EXCLUDE_CATEGORY);
        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = getVarProgramCatalogConfigEntity(catalogId, varId,
            programId, varProgramCatalogConfigEntities);
        Assert.assertEquals("PhotographyapplePNCOnyx", varProgramCatalogConfigEntity.getValue());
    }

    /**
     * catalog/var/-1
     */
    @Test
    public void testCatalogVarDefault() {

        LOGGER.info("Inside catalog/var/-1");
        String catalogId = "apple-us-en";
        catalogIds.add(catalogId);
        String varId = "PNC";
        varIds.add(varId);
        String programId = "TVG";
        programIds.add(programId);
        List<VarProgramCatalogConfigEntity> varProgramCatalogConfigEntities =
            varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, EXCLUDE_CATEGORY);
        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = getVarProgramCatalogConfigEntity(catalogId, varId,
            programId, varProgramCatalogConfigEntities);
        Assert.assertEquals("PhotographyapplePNC-1", varProgramCatalogConfigEntity.getValue());
    }

    /**
     * catalog/-1/-1
     */
    @Test
    public void testCatalogDefaultDefault() {

        LOGGER.info("Inside catalog/-1/-1");
        String catalogId = "apple-us-en";
        catalogIds.add(catalogId);
        String varId = "UA";
        varIds.add(varId);
        String programId = "MP";
        programIds.add(programId);
        List<VarProgramCatalogConfigEntity> varProgramCatalogConfigEntities =
            varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, EXCLUDE_CATEGORY);
        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = getVarProgramCatalogConfigEntity(catalogId, varId,
            programId, varProgramCatalogConfigEntities);
        Assert.assertEquals("Photographyapple-1-1", varProgramCatalogConfigEntity.getValue());
    }

    /**
     * -1/-1/-1
     */
    @Test
    public void testDefaultDefaultDefault() {

        LOGGER.info("Inside -1/-1/-1");
        String catalogId = "apple-ca-en";
        catalogIds.add(catalogId);
        String varId = "Scotia";
        varIds.add(varId);
        String programId = "Amex";
        programIds.add(programId);
        List<VarProgramCatalogConfigEntity> varProgramCatalogConfigEntities =
            varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, EXCLUDE_CATEGORY);
        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = getVarProgramCatalogConfigEntity(catalogId, varId,
            programId, varProgramCatalogConfigEntities);
        Assert.assertEquals("Photography-1-1-1", varProgramCatalogConfigEntity.getValue());
    }

    /**
     * Retrieves relatively best match @VarProgramCatalogConfigEntity object from the List of varProgramCatalogConfigEntities based on override precedence
     * override precedence ==> higher overrideValue overrides lower overrideValue
     * overrideValue = 3 ==> Specific Catalog, Var and Program Ids
     * overrideValue = 2 ==> Specific Catalog and Var Ids, Generic Program Id
     * overrideValue = 1 ==> Specific Catalog Id, Generic Var and Program Ids
     * overrideValue = 0 ==> Generic Catalog, Var and Program Ids
     *
     * @param catalogId
     * @param varId
     * @param programId
     * @param varProgramCatalogConfigEntities
     * @return varProgramCatalogConfigEntity object or null
     */
    public VarProgramCatalogConfigEntity getVarProgramCatalogConfigEntity(final String catalogId, final String varId,
        final String programId, final List<VarProgramCatalogConfigEntity> varProgramCatalogConfigEntities) {

        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = null;
        int overrideValue = -1;

        for (VarProgramCatalogConfigEntity tempVarProgramCatalogConfigEntity : varProgramCatalogConfigEntities) {
            if (tempVarProgramCatalogConfigEntity.getCatalogId().equalsIgnoreCase(catalogId)) {
                if (tempVarProgramCatalogConfigEntity.getVarId().equalsIgnoreCase(varId)) {
                    if (tempVarProgramCatalogConfigEntity.getProgramId().equalsIgnoreCase(programId)) {
                        //overrideValue = 3;    //highest precedence as Catalog, Var and Program Ids matches
                        return tempVarProgramCatalogConfigEntity;
                    } else {
                        overrideValue = 2;  //higher precedence as matching Specific Catalog and Var Ids
                        varProgramCatalogConfigEntity = tempVarProgramCatalogConfigEntity;
                    }
                } else if (overrideValue < 1) {
                    overrideValue = 1;  //average precedence as only Catalog Id matches
                    varProgramCatalogConfigEntity = tempVarProgramCatalogConfigEntity;
                }
            } else if (overrideValue < 0) {
                overrideValue = 0;      //should be default configuration
                varProgramCatalogConfigEntity = tempVarProgramCatalogConfigEntity;
            }
        }

        return varProgramCatalogConfigEntity;
    }
}