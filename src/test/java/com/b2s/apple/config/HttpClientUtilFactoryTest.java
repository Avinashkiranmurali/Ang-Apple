package com.b2s.apple.config;

import com.b2s.apple.spring.DataSourceTestConfiguration;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.shop.common.order.var.VarIntegrationServiceRemoteImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@WebAppConfiguration
public class HttpClientUtilFactoryTest {
    @Autowired @Qualifier("varIntegrationServiceRemoteImpl")
    VarIntegrationServiceRemoteImpl remote;

    @Test
    public void testDeltaHasDefaultTimeoutValue()  {
        int i = HttpClientUtilFactory.getTimeoutValue("delta");
        Assert.assertEquals(60000, i);
    }

    @Test
    public void testUAHasTimeoutValue()  {
        int i = HttpClientUtilFactory.getTimeoutValue("ua");
        Assert.assertEquals(700000, i);
    }

    @Test
    public void testHttpClients()  {
        HttpClientUtil deltaUtil = HttpClientUtilFactory.getCustomHttpClientUtil("delta");
        HttpClientUtil uaUtil = HttpClientUtilFactory.getCustomHttpClientUtil("ua");
        HttpClientUtil chaseUtil = HttpClientUtilFactory.getCustomHttpClientUtil("chase");
        Assert.assertEquals(chaseUtil, deltaUtil);
        Assert.assertNotEquals(chaseUtil, uaUtil);
    }

    @Test
    public void testVarIntegrationServiceRemoteImplUnknown()  {
        HttpClientUtil unknownUtil = remote.setHttpClientUtil("unknown");
        HttpClientUtil demoUtil = HttpClientUtilFactory.getCustomHttpClientUtil("demo");
        Assert.assertEquals(demoUtil, unknownUtil);
    }

    @Test
    public void testVarIntegrationServiceRemoteImplUA()  {
        HttpClientUtil uaUtil = remote.setHttpClientUtil("ua");
        HttpClientUtil defaultUtil = HttpClientUtilFactory.getCustomHttpClientUtil("wf");
        Assert.assertNotEquals(defaultUtil, uaUtil);
    }
}
