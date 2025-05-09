package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.WhiteListWordDao;
import com.b2s.rewards.apple.model.WhiteListWord;
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
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;


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
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_insert_whitelist_word.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class WhiteListWordDaoTest {

    @Autowired
    private WhiteListWordDao dao;


    private static final Logger LOGGER = LoggerFactory.getLogger(WhiteListWordDaoTest.class);

    @Test
    public void testGetWhitelistWords() {

        LOGGER.info("Inside testGetWhitelistWords");
        List<WhiteListWord> result = dao.getWhitelistWords(new Locale("en", "US"), "en");
        Assert.assertEquals(6, result.size());
        Set<String> setOfWords = result.stream()
                .map(w -> w.getWord())
                .collect(Collectors.toSet());
        Assert.assertFalse(setOfWords.contains("fr_CA_en"));  // row 4
        Assert.assertFalse(setOfWords.contains("fr_CA_fr"));  // row 5
        Assert.assertFalse(setOfWords.contains("-1_fr"));     // row 8

        Assert.assertTrue(setOfWords.contains("en_US_en"));   // row 1
        Assert.assertTrue(setOfWords.contains("en_US_fr"));   // row 2
        Assert.assertTrue(setOfWords.contains("en_US_-1"));   // row 3
        Assert.assertTrue(setOfWords.contains("fr_CA_-1"));   // row 6
        Assert.assertTrue(setOfWords.contains("-1_en"));      // row 7
        Assert.assertTrue(setOfWords.contains("-1-1"));       // row 9
    }
}