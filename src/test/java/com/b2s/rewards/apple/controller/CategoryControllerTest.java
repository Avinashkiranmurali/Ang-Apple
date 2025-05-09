package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.CategoryConfigurationService;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

import java.nio.charset.Charset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by rpillai on 8/21/2015.
 */

public class CategoryControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryControllerTest.class);

    private MockMvc mockMvc;

    @Mock
    private View mockView;

    private MockHttpSession session =new MockHttpSession();

    @InjectMocks
    private CategoryController controller;

    @Mock
    private CategoryConfigurationService categoryConfigurationService;

    @Mock
    @Qualifier("productServiceV3Service")
    private ProductServiceV3 productServiceV3;

    @Value("${jobs.categorySync.disable}")
    private boolean categorySyncDisable;

    final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8")
    );

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).setSingleView(mockView).build();
        User user = new User();
        user.setUserId("ranjith");
        user.setFirstName("Ranjith");
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testReloadCategories() throws Exception{

        mockMvc.perform(get("/category/reloadRepository").param("locale","en")
            .session(session))
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void testReloadCategoriesBlankLocale() throws Exception{

        mockMvc.perform(get("/category/reloadRepository").param("locale","")
            .session(session))
            .andExpect(status().isOk())
            .andReturn();

    }



    @Test
    public void testReloadCategoriesServiceException() throws Exception{

        mockMvc.perform(get("/category/reloadRepository").param("locale","en_ca")
            .session(session))
            .andExpect(status().isNoContent())
            .andReturn();

    }

}
