package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.ProductCarouselImageService;
import com.b2s.apple.services.RecentlyViewedProductsService;
import com.b2s.rewards.apple.dao.PricingModelConfigurationDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.common.services.discountservice.CouponCodeValidator;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by rperumal on 4/27/2015.
 */


public class ProductDetailControllerTest {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private MockMvc mockMvc;

    @Mock
    private View mockView;

    final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8")
    );

    @InjectMocks
    private ProductDetailController productDetailController;

    @Mock
    ApplicationContext applicationContext;

    @Mock
    private ProductServiceV3 productServiceV3;

    @Mock
    private PricingModelConfigurationDao pricingModelConfigurationDao;

    @Mock
    private RecentlyViewedProductsService recentlyViewedProductsService;

    @Mock
    private CouponCodeValidator couponCodeValidator;

    @Mock
    private AppSessionInfo appSessionInfo;

    @Mock
    private ProductCarouselImageService productCarouselImageService;

    MockHttpSession session=new MockHttpSession();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(productDetailController).setSingleView(mockView).build();
        User user = new User();
        user.setUserId("TestUser");
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);

        Program program = new Program();
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);
    }

    /**
     * Get product details for valid product
     */
    @Test
    public void testGetProductDetail() throws Exception {

        AppContext.setApplicationContext(applicationContext);
        when(applicationContext.getBean(any(String.class))).thenReturn(productServiceV3);
        Product pd=new Product();
        pd.setPsid("1");
        final List<Category> categories = new ArrayList<>();
        Category category =new Category();
        category.setSlug("test");
        categories.add(category);
        pd.setCategory(categories);
        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(productServiceV3.getDetailPageProduct(anyString(), anyObject(), anyObject(), anyBoolean(), anyBoolean()))
                .thenReturn(pd);
        List<DiscountCode> discountCodes = new ArrayList<>();
        when(couponCodeValidator.removeInvalidDiscount(anyObject(), anyObject())).thenReturn(discountCodes);
        when(productCarouselImageService.getImageUrls(any(),any(),any(),anyList())).thenReturn(new ArrayList<>());

        MvcResult result = mockMvc.perform(get("/detail/30001MGL12LL").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Product appleProduct = gson.fromJson(content, Product.class);
        assertNotNull("Product Not found", appleProduct.getPsid());

    }

    /**
     * test for Missing PSID
     */
    @Test
    public void testProductDetailMissingPsid() throws Exception {

        List<DiscountCode> discountCodes = new ArrayList<>();
        when(couponCodeValidator.removeInvalidDiscount(anyObject(), anyObject())).thenReturn(discountCodes);
        when(appSessionInfo.currentUser()).thenReturn(new User());

        MvcResult result = mockMvc.perform(get("/detail").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        content = content;
    }

    /**
     * Get product details for valid product
     */
    @Test
    public void testProductDetailServerError() throws Exception {

        try {
            when(appSessionInfo.currentUser()).thenReturn(new User());
            MvcResult result = mockMvc.perform(get("/detail/30001MGXA2LL/A").session(session))
                    .andExpect(status().isNoContent())
                    .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andReturn();
            String content = result.getResponse().getContentAsString();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get product details for valid product
     */
    @Test
    public void testGetPriceModelsByVarProgram() throws Exception {

        when(pricingModelConfigurationDao.getByVarIdProgramId("VitalityUS","TVG")).thenReturn(getPricingModels());

        mockMvc.perform(get("/priceModelsByVarProgram?var=VitalityUS&program=TVG").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();

    }

    @Test
    public void testGetPriceModelsByVarProgramBadRequest() throws Exception {

        when(pricingModelConfigurationDao.getByVarIdProgramId("VitalityUS","TVG")).thenReturn(getPricingModels());

        mockMvc.perform(get("/priceModelsByVarProgram").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();

    }

    private List<PricingModelConfiguration> getPricingModels() {
        List<PricingModelConfiguration> pricingModelConfigurations = new ArrayList<>();
        PricingModelConfiguration pricingModelConfiguration = new PricingModelConfiguration();
        pricingModelConfiguration.setPriceKey("ZeroEmployerSubsidy");
        pricingModelConfigurations.add(pricingModelConfiguration);
        return pricingModelConfigurations;
    }
}
