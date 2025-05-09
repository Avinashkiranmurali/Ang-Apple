package com.b2s.rewards.util;

import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.test.annotation.type.IntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static junit.framework.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author ewaktola Created on 9/18/2014.
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/com/b2r/account/controller/test-context.xml","/com/b2r/account/controller/test-context-products.xml","file:src/main/webapp/WEB-INF/rewardstep-servlet.xml"})
@WebAppConfiguration
@Ignore
public class AutoCompleteControllerTest {
    private MockMvc mockMvc;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    WebApplicationContext wac;
    @Autowired
    MockHttpSession mockHttpSession;
    @Autowired
    MockHttpServletRequest mockHttpServletRequest;
    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }
    @After
    public void tearDown() throws Exception {

    }
    @Test
    public void testTimeout() throws Exception {
        mockHttpSession.setAttribute(CommonConstants.USER_SESSION_OBJECT,null);
        MockHttpServletRequestBuilder requestBuilder =get("/suggest?q=").with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setSession(mockHttpSession);
                mockHttpServletRequest = request;
                request.addHeader("Accept", "application/json, text/plain, */*");
                request.addHeader("Content-Type", "application/json;charset=UTF-8");

                return request;
            }});
        MvcResult result =  mockMvc.perform(requestBuilder.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertEquals("", content);
    }
    @Test
    public void testEmptyRequest() throws Exception {
        User user = new  User();
        user.setVarId("RBC");
        user.setBalance(345690);
        mockHttpSession.setAttribute(CommonConstants.USER_SESSION_OBJECT,user);
        MockHttpServletRequestBuilder requestBuilder =get("/suggest?q=").with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setSession(mockHttpSession);
                mockHttpServletRequest = request;
                request.addHeader("Accept", "application/json, text/plain, */*");
                request.addHeader("Content-Type", "application/json;charset=UTF-8");

                return request;
            }});
        MvcResult result =  mockMvc.perform(requestBuilder.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertEquals("", content);
    }
    @Test
    public void testNonEmptyRequest() throws Exception {
        User user = new  User();
        user.setVarId("RBC");
        user.setBalance(345690);
        mockHttpSession.setAttribute(CommonConstants.USER_SESSION_OBJECT,user);
        MockHttpServletRequestBuilder requestBuilder =get("/suggest?q=ip").with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setSession(mockHttpSession);
                mockHttpServletRequest = request;
                request.addHeader("Accept", "application/json, text/plain, */*");
                request.addHeader("Content-Type", "application/json;charset=UTF-8");

                return request;
            }});
        MvcResult result =  mockMvc.perform(requestBuilder.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();

        List<String> suggestions = objectMapper.readValue(
                content,
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class, String.class));
        assertTrue(suggestions.size() > 0);
        assertNotNull(content);
    }
}
