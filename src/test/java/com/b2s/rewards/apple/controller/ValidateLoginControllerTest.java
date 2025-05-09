package com.b2s.rewards.apple.controller;

import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.apple.services.*;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.dao.VarProgramConfigDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.controller.ValidateLoginController;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.service.product.client.application.search.ProductSearchRequest;
import com.b2s.service.product.client.common.CatalogRequestContext;
import com.b2s.service.product.common.domain.SpellCheckInfo;
import com.b2s.service.product.common.domain.Suggestion;
import com.b2s.service.product.common.domain.response.Facet;
import com.b2s.service.product.common.domain.response.ProductSearchDocumentGroup;
import com.b2s.service.product.common.domain.response.ProductSearchResponse;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.var.VAROrderManagerUA;
import com.b2s.shop.common.order.var.VarOrderManagerHolder;
import org.junit.*;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by rpillai on 6/16/2017.
 */
public class ValidateLoginControllerTest {


    private static final String REDITECT_LOCATION = "https://varsignout.com/logout";
    public static final String LOGIN_VIEW = "/pages/login.jsp?returnTest=signOutBack";

    private MockMvc mockMvc;

    private MockHttpSession session=new MockHttpSession();


    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ValidateLoginController controller;

    @Mock
    private ProgramService programService;

    @Mock
    private VarProgramMessageService varProgramMessageService;

    @Mock
    private DomainVarMappingService domainVarMappingService;

    @Mock
    private VarOrderManagerHolder varOrderManagerHolder;

    @Mock
    private Properties applicationProperties;

    @Mock
    private CategoryConfigurationService categoryConfigurationService;

    @Mock
    private ProductServiceV3 productServiceV3;

    @Mock
    private ServletContext servletContext;

    @Mock
    private VarProgramConfigDao varProgramConfigDao;

    @Mock
    private ImageServerVersionService imageServerVersionService;

    @Mock
    private MaintenanceMessageService maintenanceMessageService;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        StandaloneMockMvcBuilder mvcBuilder = MockMvcBuilders.standaloneSetup(controller);
        mvcBuilder.setUseSuffixPatternMatch(true);
        this.mockMvc = mvcBuilder.build();
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram("UA",
            "MP"));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAnonymousLogin() throws Exception {

        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class))).thenReturn(Optional.of("true"));

        MvcResult mvcResult = mockMvc.perform(get("/AnonLogin?v=EPP&p=b2s_qa_only"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));
        Assert.assertTrue(mvcResult.getResponse().getHeader("Location").startsWith("/login.do?varid=EPP&programid=b2s_qa_only&locale=en_US&userid=Anonymous"));
    }

    @Test
    public void testAnonymousLoginNonDefaultLocale() throws Exception {

        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class))).thenReturn(Optional.of("true"));

        MvcResult mvcResult = mockMvc.perform(get("/AnonLogin?v=EPP&p=b2s_qa_only&l=en_CA"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));
        Assert.assertTrue(mvcResult.getResponse().getHeader("Location").startsWith("/login.do?varid=EPP&programid=b2s_qa_only&locale=en_CA&userid=Anonymous"));
    }

    @Test
    public void testAnonymousLoginWithDiscountCode() throws Exception {

        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class))).thenReturn(Optional.of("true"));

        MvcResult mvcResult = mockMvc.perform(get("/AnonLogin?v=EPP&p=b2s_qa_only&l=en_CA&c=DISCOUNTCODE1"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));
        Assert.assertTrue(mvcResult.getResponse().getHeader("Location").startsWith("/login.do?varid=EPP&programid=b2s_qa_only&locale=en_CA&c=DISCOUNTCODE1&userid=Anonymous"));
    }

    @Test
    public void testAnonymousLoginMissingProgramId() throws Exception {

        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class))).thenReturn(Optional.of("true"));

        MvcResult mvcResult = mockMvc.perform(get("/AnonLogin?v=EPP"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));
        Assert.assertEquals("/ui/login-error", mvcResult.getResponse().getHeader("Location"));
    }

    @Test
    public void testAnonymousLoginMissingVarId() throws Exception {

        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class))).thenReturn(Optional.of("true"));

        MvcResult mvcResult = mockMvc.perform(get("/AnonLogin?p=b2s_qa_only"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));
        Assert.assertEquals("/ui/login-error", mvcResult.getResponse().getHeader("Location"));
    }

    @Test
    public void testAnonymousLoginMissingVarIdAndProgramId() throws Exception {

        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class))).thenReturn(Optional.of("true"));

        MvcResult mvcResult = mockMvc.perform(get("/AnonLogin"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));
        Assert.assertEquals("/ui/login-error", mvcResult.getResponse().getHeader("Location"));
    }

    @Test
    public void testAnonymousLoginForAnonymousPurchaseNotEnabledProgram() throws Exception {

        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class))).thenReturn(Optional.empty());

        MvcResult mvcResult = mockMvc.perform(get("/AnonLogin?v=EPP&p=apple_qa"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));
        Assert.assertEquals("/ui/login-error", mvcResult.getResponse().getHeader("Location"));
    }

    @Test
    public void testGetProgramWithTransientContent() throws Exception {
        // To verify the assignment to the transient field is present in json string

        when(servletContext.getAttribute("B2S-Version")).thenReturn("12");
        when(servletContext.getAttribute("Build-Number")).thenReturn("3456");
        when(applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY)).thenReturn("https://localhost/imageserver");
        when(imageServerVersionService.getVersionWtihoutNetworkCall()).thenReturn("1234");

        MockHttpSession session=new MockHttpSession();
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram("UA", "MP"));
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, getUser());

        MvcResult mvcResult = mockMvc.perform(get("/program").session(session))
            .andExpect(status().isOk())
            .andReturn();
        Assert.assertEquals(true,
            mvcResult.getResponse().getContentAsString().contains("\"config\":{\"catalog_id\":\"apple\"," +
                "\"defaultPSprogram\":\"MP\"}"));
        Assert.assertEquals(true,
                mvcResult.getResponse().getContentAsString().
                        contains("\"sessionConfig\":{\"imageServer\":\"https://localhost/imageserver\"," +
                                "\"imageServerBuildNumber\":\"1234\",\"buildId\":\"12-3456\"}"));
    }

    @Test
    public void testDomainLogin()
        throws Exception {
        VAROrderManagerUA vAROrderManagerUA = mock(VAROrderManagerUA.class);
        User user = new User();
        user.setProgramId("Anonymous");
        user.setVarId("UA");
        when(vAROrderManagerUA.selectUser(any())).thenReturn(user);
        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class)))
            .thenReturn(Optional.empty());
        when(domainVarMappingService.findDomainByPattern("url", "localhost"))
            .thenReturn(getDomainVarMapping("UA", "Anonymous"));
        when(varOrderManagerHolder.getVarOrderManager("UA")).thenReturn(vAROrderManagerUA);
        when(applicationProperties.getProperty("PS3_DEFAULT_CATALOG")).thenReturn("apple");
        when(programService.getProgram(any(), any(), any())).thenReturn(getProgram("UA", "Anonymous"));
        final Properties properties = new Properties();
        properties.setProperty(CommonConstants.FIVE9_TITLE_KEY, "SkyMiles Marketplace Chat");
        when(varProgramMessageService.getMessages(Optional.of("UA"), Optional.of("Anonymous"), "en_US"))
            .thenReturn(properties);
        MvcResult mvcResult = mockMvc.perform(get("/DomainLogin"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(mvcResult.getResponse().containsHeader("Location"), true);

        mvcResult = mockMvc.perform(get(mvcResult.getResponse().getHeader("Location")).session(session))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));

        Assert.assertEquals("/ui/store", mvcResult.getResponse().getHeader("Location"));
    }

    @Test
    public void testDomainLoginWithDeepLinking()
        throws Exception {
        VAROrderManagerUA vAROrderManagerUA = mock(VAROrderManagerUA.class);
        User user = new User();
        user.setProgramId("Anonymous");
        user.setVarId("UA");
        when(vAROrderManagerUA.selectUser(any())).thenReturn(user);
        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class)))
            .thenReturn(Optional.empty());
        when(domainVarMappingService.findDomainByPattern("url", "localhost")).thenReturn(getDomainVarMapping("UA",
            "Anonymous"));
        when(varOrderManagerHolder.getVarOrderManager("UA")).thenReturn(vAROrderManagerUA);
        ProductSearchRequest.Builder pb = mock(ProductSearchRequest.Builder.class);
        when(productServiceV3.getProductSearchRequestBuilder(any(), any(), any(), any(), any(), any(), any(), any(),
            any(), any(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyBoolean(), any()))
            .thenReturn(pb);
        when(productServiceV3.searchProducts(any())).thenReturn(
            new ProductSearchResponse(getProductSearchDocumentGroupMap(), new ArrayList<Facet>(),
                new SpellCheckInfo(true, new ArrayList<Suggestion>())));
        when(applicationProperties.getProperty("PS3_DEFAULT_CATALOG")).thenReturn("apple");
        when(programService.getProgram("UA", "Anonymous", Locale.US)).thenReturn(getProgram("UA", "Anonymous"));
        when(categoryConfigurationService.getCategoryConfiguration(any()))
            .thenReturn(getCategoryConfigurationWithDeepLinking("ipad-pro", "/configure/ipad/ipad-pro/"));
        final Properties properties = new Properties();
        properties.setProperty(CommonConstants.FIVE9_TITLE_KEY, "SkyMiles Marketplace Chat");
        when(varProgramMessageService.getMessages(Optional.of("UA"), Optional.of("Anonymous"), Locale.US.toString()))
            .thenReturn(properties);
        MvcResult mvcResult = mockMvc.perform(get("/DomainLogin?product=ipad-pro"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));

        mvcResult = mockMvc.perform(get(mvcResult.getResponse().getHeader("Location")).session(session))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));

        Assert.assertEquals("/ui/store/configure/ipad/ipad-pro/", mvcResult.getResponse().getHeader("Location"));
    }

    @Test
    public void testItemIdWithDeepLinking() throws Exception {
        // Macbook-pro item
        VAROrderManagerUA vAROrderManagerUA= mock(VAROrderManagerUA.class);
        User user = new User();
        user.setProgramId("MP");
        user.setVarId("UA");
        when(vAROrderManagerUA.selectUser(any())).thenReturn(user);
        when(domainVarMappingService.findDomainByPattern("url", "localhost")).thenReturn(getDomainVarMapping("UA", "MP"));
        when(varOrderManagerHolder.getVarOrderManager("UA")).thenReturn(vAROrderManagerUA);
        when(applicationProperties.getProperty("PS3_DEFAULT_CATALOG")).thenReturn("apple");
        when(applicationProperties.getProperty("ua.navigateBackUrl")).thenReturn("ua-navigateurl");
        when(programService.getProgram(any(), any(), any())).thenReturn(getProgram("UA", "MP"));
        when(productServiceV3.getDetailPageProduct(any(String.class),any(Program.class),any(User.class),any(Boolean.class),any(Boolean.class)))
                .thenReturn(getProductInDetail());
        final Properties properties = new Properties();
        properties.setProperty(CommonConstants.FIVE9_TITLE_KEY, "SkyMiles Marketplace Chat");
        when(varProgramMessageService.getMessages(Optional.of("UA"),Optional.of("MP"), "en_US")).thenReturn(properties);
        when(categoryConfigurationService.getCategoryConfiguration(any())).thenReturn(getCategoryConfigurationWithDeepLinking("macbook-pro","/browse/mac/macbook-pro/"));


        MvcResult mvcResult = mockMvc.perform(get("/DomainLogin?itemId=MXK32LL/A"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(mvcResult.getResponse().containsHeader("Location"), true);

        mvcResult = mockMvc.perform(get(mvcResult.getResponse().getHeader("Location")).session(session))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));

        Assert.assertEquals("/ui/store/browse/mac/macbook-pro/30001MXK32LL-A", mvcResult.getResponse().getHeader("Location"));
    }

    @Test
    public void testDomainLoginWithDeeplinkConfigurePage() throws Exception {
        // Macbook-pro item
        VAROrderManagerUA vAROrderManagerUA= mock(VAROrderManagerUA.class);
        User user = new User();
        user.setProgramId("MP");
        user.setVarId("UA");
        when(vAROrderManagerUA.selectUser(any())).thenReturn(user);
        when(domainVarMappingService.findDomainByPattern("url", "localhost")).thenReturn(getDomainVarMapping("UA", "MP"));
        when(varOrderManagerHolder.getVarOrderManager("UA")).thenReturn(vAROrderManagerUA);
        when(applicationProperties.getProperty("PS3_DEFAULT_CATALOG")).thenReturn("apple");
        when(applicationProperties.getProperty("ua.navigateBackUrl")).thenReturn("ua-navigateurl");
        when(programService.getProgram(any(), any(), any())).thenReturn(getProgram("UA", "MP"));
        ProductSearchRequest.Builder pb=mock(ProductSearchRequest.Builder.class);
        when(productServiceV3.getProductSearchRequestBuilder(any(),any(), any(), any(), any(), any(), any(), any(),
            any(), any(), ArgumentMatchers.anyBoolean(),ArgumentMatchers.anyBoolean(), any()))
            .thenReturn(pb);
        when(productServiceV3.searchProducts(any())).thenReturn(new ProductSearchResponse(getProductSearchDocumentGroupMap(), null, new SpellCheckInfo(true, new ArrayList<Suggestion>())));
        final Properties properties = new Properties();
        properties.setProperty(CommonConstants.FIVE9_TITLE_KEY, "SkyMiles Marketplace Chat");
        when(varProgramMessageService.getMessages(Optional.of("UA"),Optional.of("MP"), "en_US")).thenReturn(properties);
        when(categoryConfigurationService.getCategoryConfiguration(any())).thenReturn(getCategoryConfigurationWithDeepLinking("ipad-pro", "/configure/ipad/ipad-pro/"));


        MvcResult mvcResult = mockMvc.perform(get("/DomainLogin?product=ipad-pro"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(mvcResult.getResponse().containsHeader("Location"), true);

        mvcResult = mockMvc.perform(get(mvcResult.getResponse().getHeader("Location")).session(session))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));

        Assert.assertEquals("/ui/store/configure/ipad/ipad-pro/", mvcResult.getResponse().getHeader("Location"));

    }


    @Test
    public void testDomainLoginWithDeepLinkingInvalidProduct() throws Exception {
        VAROrderManagerUA vAROrderManagerUA= mock(VAROrderManagerUA.class);
        User user = new User();
        user.setProgramId("MP");
        user.setVarId("UA");
        Program program=new Program();
        when(vAROrderManagerUA.selectUser(any())).thenReturn(user);
        //when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class))).thenReturn("test");
        when(domainVarMappingService.findDomainByPattern("url", "localhost")).thenReturn(getDomainVarMapping("UA", "Anonymous"));
        when(varOrderManagerHolder.getVarOrderManager("UA")).thenReturn(vAROrderManagerUA);
        when(applicationProperties.getProperty("PS3_DEFAULT_CATALOG")).thenReturn("apple");
        when(applicationProperties.getProperty("ua.navigateBackUrl")).thenReturn("ua-navigateurl");
        when(programService.getProgram(any(), any(),any())).thenReturn(getProgram("UA", "Anonymous"));
        when(categoryConfigurationService.getCategoryConfiguration("product-x")).thenReturn(null);
        final Properties properties = new Properties();
        properties.setProperty(CommonConstants.FIVE9_TITLE_KEY, "SkyMiles Marketplace Chat");
        when(varProgramMessageService.getMessages(any(),any(),any())).thenReturn(properties);
        MvcResult mvcResult = mockMvc.perform(get("/DomainLogin?product=product-x"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(mvcResult.getResponse().containsHeader("Location"), true);

        mvcResult = mockMvc.perform(get(mvcResult.getResponse().getHeader("Location")).session(session))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        Assert.assertEquals(true, mvcResult.getResponse().containsHeader("Location"));

        Assert.assertEquals("/ui/store", mvcResult.getResponse().getHeader("Location"));
    }

    private User getUser() {
        User user = new User();
        user.setVarId("UA");
        user.setProgramId("MP");
        user.setLocale(Locale.US);
        return user;
    }

    private DomainVarMapping getDomainVarMapping(String varId, String programId) {
        final DomainVarMapping domainVarMapping = new DomainVarMapping();
        domainVarMapping.setVarId(varId);
        domainVarMapping.setProgramId(programId);
        domainVarMapping.setDomain("localhost");
        return domainVarMapping;
    }

    private Program getProgram(final String varId, final String programId) {
        final Program program = new Program();
        program.setVarId(varId);
        program.setProgramId(programId);
        final Map<String, Object> config = new HashMap<>();
        config.put("catalog_id", "apple");
        config.put("defaultPSprogram", "MP");
        program.setConfig(config);
        return program;
    }

    private CategoryConfiguration getCategoryConfigurationWithDeepLinking(String categoryName, String deepLinkUrl) {
        CategoryConfiguration categoryConfiguration = new CategoryConfiguration();
        categoryConfiguration.setCategoryName(categoryName);
        categoryConfiguration.setDeepLinkUrl(deepLinkUrl);
        return categoryConfiguration;
    }

    private Map<String, ProductSearchDocumentGroup> getProductSearchDocumentGroupMap() {
        Map<String, ProductSearchDocumentGroup> productSearchDocumentGroupMap = new HashMap<>();
        productSearchDocumentGroupMap.put("DEFAULT_GROUP", ProductSearchDocumentGroup.builder().withTotalFound(1).build());
        return productSearchDocumentGroupMap;
    }

    private Product getProductInDetail(){
        Product product = new Product();
        List<Category> categories = new ArrayList<Category>();
        List<Category> parents = new ArrayList<>();
        Category category = new Category();
        category.setSlug("macbook-pro");
        Category parentCategory = new Category();
        parentCategory.setSlug("mac");
        parents.add(parentCategory);
        category.setParents(parents);
        categories.add(category);
        product.setCategory(categories);
        return product;
    }

    private Set<String> getStrings(){
        Set<String> strings = new HashSet<>();
        strings.add(any(String.class));
        return strings;
    }

    @Test
    public void testgetIPAddressFromRemoteClient()throws Exception {
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        Assert.assertEquals("IpAdresses are not Equal","127.0.0.1", AppleUtil.getClientIpAddress(httpServletRequest));

    }

    @Test
    public void testgetIPAddressFromProxy() throws Exception {
        when(httpServletRequest.getHeader("X-FORWARDED-FOR")).thenReturn("4.31.207.82,108.162.237.244");
        Assert.assertEquals("IpAddresses are not equal","4.31.207.82",AppleUtil.getClientIpAddress(httpServletRequest));
    }

    @Test
    public void shouldRedirectToLoginFail()
        throws Exception {
        mockMvc.perform(get("/signOutPage.htm").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", LOGIN_VIEW));
    }

    @Test
    public void shouldRedirectToLoginRedirect()
        throws Exception {
        setupURLForRedirect();
        mockMvc.perform(get("/signOutPage.htm").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", REDITECT_LOCATION));
    }

    private void setupURLForRedirect() {
        final Map<String, String> urlMap = new HashMap<>();
        urlMap.put(ExternalUrlConstants.SIGN_OUT_URL, REDITECT_LOCATION);
        session.setAttribute(ExternalUrlConstants.EXTERNAL_URLS, urlMap);
    }

}
