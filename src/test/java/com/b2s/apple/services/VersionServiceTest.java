package com.b2s.apple.services;

import com.b2s.apple.config.ApplicationConfig;
import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.model.AppVersion;
import com.b2s.rewards.common.util.CommonConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
    DataSourceTestConfiguration.class})
@WebAppConfiguration
@Transactional
public class VersionServiceTest {

    @Autowired
    private VersionService versionService;

    @Test
    public void testAppHealthForUp() {
        AppVersion appVersion = versionService.getWebAppHealth(3);

        Assert.assertNotNull(appVersion);
        Assert.assertEquals(CommonConstants.APP_UP, appVersion.getStatus());
    }

    @Test
    public void testWebAppDetails() {
        AppVersion appDetails = versionService.getWebAppDetails();
        Assert.assertNotNull(appDetails);
    }
}
