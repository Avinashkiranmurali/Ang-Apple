package com.b2s.dao;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.dao.NaughtyWordDao;
import com.b2s.rewards.apple.model.NaughtyWord;
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
 * The method getByLocaleOrLanguage() sql is
 *
 * select *
 * from naughty_word
 * where language = '-1'
 * or locale=userLocale
 * or language=userLanguage
 * and locale=-1
 *
 * We in our test, we will have 3 locales [en_US, fr_CA, -1]
 * and 3 languages [en, fr, -1] giving us 9 rows of combination.
 * So the above query should result in
 * 3 rows for language == -1. Rows 3,6,9.
 * 3 rows for catalog == en_US but one row is shared with -1 language, so 2 new rows. Rows 1,2
 * 1 row for the language == en and locale == -1. Row no 7.
 * So total 6 rows should be returned by the function getByLocaleOrLanguage()
 * Rows not fetched are 4,5, and 8
 *
 * Row 1 -
 * insert into naughty_word(locale,word,pattern,match_whole_word,language)
 * values('en_US','en_US_en','',1,'en');
 * Row 2 -
 * insert into naughty_word(locale,word,pattern,match_whole_word,language)
 * values('en_US','en_US_fr','',1,'fr');
 * Row 3 -
 * insert into naughty_word(locale,word,pattern,match_whole_word,language)
 * values('en_US','en_US_-1','',1,'-1');
 *
 * Row 4 -
 * insert into naughty_word(locale,word,pattern,match_whole_word,language)
 * values('fr_CA','fr_CA_en','',1,'en');
 * Row 5 -
 * insert into naughty_word(locale,word,pattern,match_whole_word,language)
 * values('fr_CA','fr_CA_fr','',1,'fr');
 * Row 6 -
 * insert into naughty_word(locale,word,pattern,match_whole_word,language)
 * values('fr_CA','fr_CA_-1','',1,'-1');
 *
 * Row 7 -
 * insert into naughty_word(locale,word,pattern,match_whole_word,language)
 * values('-1','-1_en','',1,'en');
 * Row 8 -
 * insert into naughty_word(locale,word,pattern,match_whole_word,language)
 * values('-1','-1_fr','',1,'fr');
 * Row 9
 * insert into naughty_word(locale,word,pattern,match_whole_word,language)
 * values('-1','-1-1','',1,'-1');
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:scripts/data_naughty_word.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:scripts/data_cleanup.sql")
})
@WebAppConfiguration
@Transactional
public class NaughtyWordDaoTest {

    @Autowired
    private NaughtyWordDao naughtyWordDao;


    private static final Logger LOGGER = LoggerFactory.getLogger(NaughtyWordDaoTest.class);

    @Test
    public void testGetByLocaleOrLanguage() {

        LOGGER.info("Inside testGetByLocaleOrLanguage");
        List<NaughtyWord> result = naughtyWordDao.getByLocaleOrLanguage(new Locale("en", "US"), "en");
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

