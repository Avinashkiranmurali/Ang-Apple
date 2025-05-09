package com.b2s.rewards.common.context;


import com.b2s.rewards.common.exception.B2RException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DBVersionCheckTest {


    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    @Qualifier("dbVersionCheck")
    private DBVersionCheck dbVersionCheck;


    @Before
    public void initMocks() throws IOException {
            MockitoAnnotations.initMocks(this);

    }

    @Value("${db.version}")
    private String dbVersion;


    @Test
    public void testCheckDbVersion() throws Exception {

        final Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("version", "6.5.1");
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(resultMap);

        final Field field = dbVersionCheck.getClass().getDeclaredField("dbVersion");
        field.setAccessible(true);
        field.set(dbVersionCheck, "6.5.1");
        final boolean versionMatch = dbVersionCheck.checkDbVersion();
        Assert.assertTrue(versionMatch);
    }

    @Test(expected = B2RException.class)
    public void testCheckDbVersionMismatch() throws Exception {
        final Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("version", "6.5.1");
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(resultMap);

        final Field field = dbVersionCheck.getClass().getDeclaredField("dbVersion");
        field.setAccessible(true);
        field.set(dbVersionCheck, "6.5.2");

        dbVersionCheck.checkDbVersion();
    }

    @Test(expected = B2RException.class)
    public void testCheckDbVersionDBException() throws Exception {

        when(jdbcTemplate.queryForMap(anyString())).thenThrow(RuntimeException.class);
        dbVersionCheck.checkDbVersion();
    }

    @Test(expected = B2RException.class)
    public void testCheckDbVersionEmpty() throws Exception {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(resultMap);
        dbVersionCheck.checkDbVersion();

    }

    @Test
    public void testCheckDbIgnoreMinorVersion() throws Exception {

        final Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("version", "6.5.1");
        when(jdbcTemplate.queryForMap(anyString())).thenReturn(resultMap);

        final Field field = dbVersionCheck.getClass().getDeclaredField("dbVersion");
        field.setAccessible(true);
        field.set(dbVersionCheck, "6.5.2");

        final Field field2 = dbVersionCheck.getClass().getDeclaredField("ignoreDbMinorVersion");
        field2.setAccessible(true);
        field2.set(dbVersionCheck, "true");

        boolean versionMatch = dbVersionCheck.checkDbVersion();
        Assert.assertTrue(versionMatch);
    }



}
