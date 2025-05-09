package com.b2s.rewards.security.controller;

import com.b2s.rewards.apple.util.CitiUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Properties;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ppalpandi on 11/27/2017.
 */
public class CitiLoginControllerTest {

    @InjectMocks
    private CitiLoginController controller;

    @Mock
    private Properties applicationProperties;

    private CitiUtil citiUtil;

    private MockMvc mockMvc;

    private static final String IDP_URL = "https://pat.cbgrus.uatglobalrewards.com/loginSeamless.htm";
    private static final String REDIRECT_URL = "https://pat.cbgrus.uatglobalrewards.com/loginSeamless.htm?partnerCode=FV_B2SAPP&relayState=https%3A%2F%2Fcatalog.pat.cbgrus.uatglobalrewards.com%2Fapple-gr%2Fmerchandise%2Flanding.jsp%23%2Fstore%2F&sessionState=A";

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        StandaloneMockMvcBuilder mvcBuilder = MockMvcBuilders.standaloneSetup(controller);
        mvcBuilder.setUseSuffixPatternMatch(true);
        this.mockMvc = mvcBuilder.build();
        citiUtil = new CitiUtil();
        Whitebox.setInternalState(controller,"citiUtil",citiUtil);
        Whitebox.setInternalState(citiUtil,"applicationProperties",applicationProperties);
    }

    @Test
    public void testInitSAMLFromEpsilon() throws Exception {
        mockMvc.perform(get("/citi/AppleStore.htm?env=PAT")).andExpect(status().is3xxRedirection());
    }

    @Test
    public void shouldTakeanyValuesAsEnvParam() throws Exception {
        mockMvc.perform(get("/citi/AppleStore.htm?env=XXXX")).andExpect(status().is3xxRedirection());
    }
    @Test
    public void testCategoryDeepLinkURL() throws Exception {
        final MvcResult result = mockMvc.perform(get("/citi/AppleStore.htm?env=PAT&CATEGORY=iphone&src=GRAUENG")).andReturn();
        Assert.assertTrue(result.getResponse().getHeader("Location").contains("iphone"));
    }
    @Test
    public void testProductDetailDeepLinkURL() throws Exception {
        final MvcResult result = mockMvc.perform(get("/citi/AppleStore.htm?env=PAT&itemId=30001MU7F2X-A&src=GRAUENG")).andReturn();
        Assert.assertTrue(result.getResponse().getHeader("Location").contains("30001MU7F2X-A"));
    }
}