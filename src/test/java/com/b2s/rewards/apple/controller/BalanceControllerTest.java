package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.JwtService;
import com.b2s.rewards.apple.model.BalanceTokenResponse;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BalanceControllerTest {
    private MockMvc mockMvc;
    private MockHttpSession session=new MockHttpSession();
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private BalanceController controller;

    @Mock
    private JwtService jwtService;

    @Mock
    private View mockView;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).setSingleView(mockView).build();

        User user = new User();
        user.setCsid("");
        user.setBalance(99);
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);
    }

    @Test
    public void testGetToken() throws Exception {
        when(jwtService.generateKeystoneToken(99, "")).thenReturn(new BalanceTokenResponse("NEW_TOKEN", "apple-gr"));

        final MvcResult mvcResult = mockMvc.perform(get("/participant/balanceToken").session(session))
            .andExpect(status().isOk())
            .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        final BalanceTokenResponse balanceToken = new Gson().fromJson(content, BalanceTokenResponse.class);
        assertEquals(balanceToken.getToken(), "NEW_TOKEN");
        assertEquals(balanceToken.getKeyId(), "apple-gr");
    }
}
