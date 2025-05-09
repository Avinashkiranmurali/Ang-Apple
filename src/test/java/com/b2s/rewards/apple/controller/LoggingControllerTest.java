package com.b2s.rewards.apple.controller;

import com.b2s.rewards.apple.model.UIErrors;
import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ssrinivasan on 3/25/2015.
 */


public class LoggingControllerTest {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private MockMvc mockMvc;


    @InjectMocks
    private LoggingController loggingController;



    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(loggingController).build();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore
    public void testLogErrors() throws Exception {
        UIErrors uiErrors= new UIErrors();
        uiErrors.setBrowserInfo("Test Browser");
        List<String> stackTrace = new ArrayList();
        stackTrace.add("Error One");
        stackTrace.add("Error Two");
        stackTrace.add("Error Three");
        uiErrors.setCause("cause two");
        uiErrors.setErrorMessage("Error Message One");
        uiErrors.setStackTrace(stackTrace);
        Gson gson = new Gson();
        String json = gson.toJson(uiErrors);
        MvcResult mvcResult = mockMvc.perform(post("/log/errors").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andReturn();
        assert(mvcResult.getResponse().getContentAsString().equals("true"));
    }
}
