package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.SearchRedirectService;
import com.b2s.rewards.apple.dao.MercSearchFilterDao;
import com.b2s.rewards.apple.model.Category;
import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.model.ProductResponse;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.apple.services.CategoryConfigurationService;
import com.b2s.apple.services.DetailService;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.common.services.util.CategoryRepository;
import com.b2s.common.services.util.CategoryRepositoryHolder;
import com.b2s.service.product.client.application.search.ProductSearchRequest;
import com.b2s.service.product.client.common.CatalogRequestContext;
import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by rperumal on 5/18/2015.
 */

public class ProductsControllerTest {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private MockMvc mockMvc;

    final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8")
    );


    @InjectMocks
    private ProductsController productsController;


    @Mock
    private DetailService detailService;

    @Mock
    @Qualifier("productServiceV3Service")
    ProductServiceV3 productServiceV3;

    @Mock
    CategoryConfigurationService categoryConfigurationService;

    @Mock
    private MercSearchFilterDao mercSearchFilterDao;

    @Mock
    private CategoryRepositoryHolder categoryRepositoryHolder;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    SearchRedirectService searchRedirectService;

    @Mock
    private AppSessionInfo appSessionInfo;


    private MockHttpSession session=new MockHttpSession();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(productsController).build();

        User user = new User();
        user.setUserId("raji");
        user.setVarId("1");
        user.setProgramId("1");
        user.setState("GA");
        user.setCountry("US");
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);

        Program program = new Program();
        Map<String,Object> mapConfig = new HashMap<>();
        mapConfig.put(CommonConstants.CONFIG_CATALOG_ID,CommonConstants.US_CATALOG_ID);
        program.setConfig(mapConfig);
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Get all Categories
     */
    @Test
    public void testGetCategories() throws Exception {

        List<Category> categories=new ArrayList<>();
        categories.add(new Category());

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(categoryConfigurationService.getParentCategories(anyObject(),anyObject())).thenReturn(categories);

        MvcResult result = mockMvc.perform(get("/categories").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Category[] category = new Gson().fromJson(content, (Type) Category[].class);
        assertTrue("Category list is empty in testGetCategories() ", category.length > 0);

    }

    /**
     * This will return all Attribute Configuration, if Slug is not provided
     */
    // // Database entities are returned from the REST call, causing the infinite recursion
    @Test
    public void testGetAllProductAttributeConfiguration() throws Exception {

        mockMvc.perform(get("/productAttributeConfiguration").session(session))
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * This will return all Attribute Configuration, if Slug is not provided
     */
    // Database entities are returned from the REST call, causing the infinite recursion
    @Test

    public void testGetAllCategoryConfigurations() throws Exception {

         mockMvc.perform(get("/categoryConfigurations").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();

    }
}
