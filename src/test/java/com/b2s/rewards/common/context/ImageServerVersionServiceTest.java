package com.b2s.rewards.common.context;

import com.b2s.apple.services.AsyncNetCall;
import com.b2s.apple.services.ImageServerVersionService;
import com.b2s.rewards.apple.model.AppDetails;
import com.b2s.rewards.apple.model.AppVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ImageServerVersionServiceTest {
    @InjectMocks
    @Qualifier("ImageServerVersionService")
    private ImageServerVersionService versionCheck;

    @Mock
    private Properties applicationProperties;

    @Mock
    private AsyncNetCall asyncNetCall;

    private CompletableFuture<AppDetails> appDetailsResponse;
    private String versionWithWhiteSpaces = " 2.0 \r\n";
    private AppDetails appDetails = new AppDetails();
    private AppVersion appVersion = new AppVersion();


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        appVersion.setVersion(versionWithWhiteSpaces);
        appDetails.setApplicationInfo(appVersion);
        appDetailsResponse = CompletableFuture.completedFuture(appDetails);
    }

    @Test
    public void testCheckVersion()  {
        when(applicationProperties.getProperty(anyString())).thenReturn("");
        Mockito.doReturn(appDetailsResponse).when(asyncNetCall).getVersion(anyString(), anyString(), anyInt());
        String version = versionCheck.getVersion();
        Assert.assertNotEquals("1.0", version);
    }

    @Test
    public void testWhiteSpaceTrimmedOut()  {
        when(applicationProperties.getProperty(anyString())).thenReturn("");
        Mockito.doReturn(appDetailsResponse).when(asyncNetCall).getVersion(anyString(), anyString(), anyInt());
        String version = versionCheck.getVersion();
        Assert.assertNotEquals(versionWithWhiteSpaces, version);
        Assert.assertEquals(versionWithWhiteSpaces.trim(), version);
    }

    @Test
    public void testException()  {
        when(applicationProperties.getProperty(anyString())).thenReturn("");
        Mockito.doThrow(new RuntimeException("run time exception")).when(asyncNetCall).getVersion(anyString(), anyString(), anyInt());
        String version = versionCheck.getVersion();
        Assert.assertEquals("1.0", version);
    }
}
