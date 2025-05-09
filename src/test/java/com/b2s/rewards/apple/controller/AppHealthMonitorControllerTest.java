package com.b2s.rewards.apple.controller;

import com.b2s.rewards.apple.model.AppVersion;
import com.b2s.apple.services.VersionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by vkrishnan on 4/22/2019.
 */
public class AppHealthMonitorControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    AppHealthMonitorController appHealthMonitorController;

    @Mock
    private VersionService versionService;

    @Mock
    private View mockView;

    private static final int TIME_OUT = 3;
    private static final String APP_STATUS_UP = "UP";
    private static final String APP_STATUS_DOWN = "DOWN";
    private static final String EMPTY_VERSION = "NA";
    private static final String EMPTY_BUILD = "NA";


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(appHealthMonitorController).setSingleView(mockView).build();
    }

    @Test
    public void testAppHealthForUp() {
        AppVersion appHealth = getMockStatus(APP_STATUS_UP);
        when(versionService.getWebAppHealth(TIME_OUT)).thenReturn(appHealth);
        ResponseEntity<AppVersion> responseEntity = appHealthMonitorController.getApplicationHealth(TIME_OUT);
        AppVersion appHealthInfoResult = responseEntity.getBody();
        assertNotNull(appHealthInfoResult);
        assertEquals(APP_STATUS_UP, appHealthInfoResult.getStatus());
        assertNotEquals(APP_STATUS_DOWN, appHealthInfoResult.getStatus());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    public void testAppHealthForDown() {
        AppVersion appHealth = getMockStatus(APP_STATUS_DOWN);
        when(versionService.getWebAppHealth(TIME_OUT)).thenReturn(appHealth);
        ResponseEntity<AppVersion> responseEntity = appHealthMonitorController.getApplicationHealth(TIME_OUT);
        AppVersion appHealthInfoResult = responseEntity.getBody();
        assertNotNull(appHealthInfoResult);
        assertNotEquals(APP_STATUS_UP, appHealthInfoResult.getStatus());
        assertEquals(APP_STATUS_DOWN, appHealthInfoResult.getStatus());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    public void testVersionAndBuildEmpty() {
        AppVersion appVersionInfo = getMockVersion(EMPTY_VERSION, EMPTY_BUILD);
        when(versionService.getWebAppDetails()).thenReturn(appVersionInfo);
        ResponseEntity<AppVersion> responseEntity = appHealthMonitorController.getApplicationInfo();
        AppVersion appVersionInfoResult = responseEntity.getBody();
        assertNotNull(appVersionInfoResult);
        assertEquals(EMPTY_VERSION, appVersionInfoResult.getVersion());
        assertEquals(EMPTY_BUILD, appVersionInfoResult.getBuild());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    public void testVersionAndBuild() {
        String appVersion = "1.0.0";
        String appBuild = "234";
        AppVersion appVersionInfo = getMockVersion(appVersion, appBuild);
        when(versionService.getWebAppDetails()).thenReturn(appVersionInfo);
        ResponseEntity<AppVersion> responseEntity = appHealthMonitorController.getApplicationInfo();
        AppVersion appVersionInfoResult = responseEntity.getBody();
        assertNotNull(appVersionInfoResult);
        assertEquals(appVersion, appVersionInfoResult.getVersion());
        assertNotEquals(EMPTY_VERSION, appVersionInfoResult.getVersion());
        assertEquals(appBuild, appVersionInfoResult.getBuild());
        assertNotEquals(EMPTY_BUILD, appVersionInfoResult.getBuild());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    private AppVersion getMockStatus(final String status) {
        final AppVersion appVersion = new AppVersion();
        appVersion.setStatus(status);
        return appVersion;
    }

    private AppVersion getMockVersion(final String version, final String build) {
        final AppVersion appVersion = new AppVersion();
        appVersion.setVersion(version);
        appVersion.setBuild(build);
        return appVersion;
    }
}
