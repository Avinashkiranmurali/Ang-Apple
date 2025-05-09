package com.b2s.apple.services;

import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.common.services.util.CategoryRepository;
import com.b2s.common.services.util.CategoryRepositoryHolder;
import com.b2s.rewards.apple.integration.model.ps.CategoryResponse;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.product.client.application.search.ProductSearchRequest;
import com.b2s.service.product.client.common.CatalogRequestContext;
import com.b2s.service.product.client.domain.Audience;
import com.b2s.service.product.client.domain.CategoryNode;
import com.b2s.service.product.common.domain.SpellCheckInfo;
import com.b2s.service.product.common.domain.Suggestion;
import com.b2s.service.product.common.domain.response.Facet;
import com.b2s.service.product.common.domain.response.FacetEntry;
import com.b2s.service.product.common.domain.response.ProductSearchDocumentGroup;
import com.b2s.service.product.common.domain.response.ProductSearchResponse;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rpillai on 8/20/2015.
 */

public class CategoryConfigurationServiceTest {

    @InjectMocks
    private CategoryConfigurationService categoryConfigurationService;

    @Mock
    private ProductServiceV3 productServiceV3;

    @Mock
    private Properties applicationProperties;

    @Mock
    private CategoryRepositoryHolder categoryRepositoryHolder;


    @Mock
    MockHttpSession httpSession;

    MockMvc mockMvc;

    @Mock
    HttpClientUtil httpClientUtil;


    @Before
    public void setup() throws Exception {
        List<String> accString= new ArrayList<>();
        accString.add("tv-accessories");
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(categoryConfigurationService, "accessoriesSlugs", accString);
        this.mockMvc = MockMvcBuilders.standaloneSetup(categoryConfigurationService).build();
    }

    @Test
    public void testGetParentCategoriesFilterByBrand()
        throws ServiceException, B2RException {

        Program program=new Program();
        Locale locale=Locale.US;
        CategoryRepository categoryRepository=mock(CategoryRepository.class);
        CategoryResponse categoryResponse=mock(CategoryResponse.class);

        when(applicationProperties.getProperty(any())).thenReturn("mock");
        when(categoryRepositoryHolder.getCategoryRepository(locale)).thenReturn(categoryRepository);
        doReturn(getCategories()).when(categoryRepository).getParentCategories(any(),any(),any(),any());
        when(httpClientUtil.getHttpResponse(any(),any(),any(),any())).thenReturn(categoryResponse);
        ProductSearchRequest.Builder pb=mock(ProductSearchRequest.Builder.class);
        when(productServiceV3.getProductSearchRequestBuilder(any(),any(), any(), any(), any(), any(), any(), any(),
            any(), any(), ArgumentMatchers.anyBoolean(),ArgumentMatchers.anyBoolean(), any()))
            .thenReturn(pb);

        FacetEntry facetEntry1=new FacetEntry("2/tv/tv-accessories/tv-accessories-audio-music",3,"categories");
        FacetEntry facetEntry2=new FacetEntry("2/tv/tv-accessories/tv-accessories-headphones-speakers",3,"categories");
        List<FacetEntry> entries=new ArrayList<>();
        entries.add(facetEntry1);
        entries.add(facetEntry2);
        Facet facet=new Facet("categories","categories",entries);
        List<Facet> facets=new ArrayList<>();
        facets.add(facet);

        when(productServiceV3.searchProducts(any())).thenReturn(new ProductSearchResponse(getProductSearchDocumentGroupMap(), facets, new SpellCheckInfo(true, new ArrayList<Suggestion>())));
        List<CategoryNode> categoryNodes=new ArrayList<>();
        categoryNodes.add(getCategoryNode());
        doReturn(categoryNodes).when(categoryResponse).getResponse();

        List<Category> categoryList= categoryConfigurationService.getParentCategories(program,locale);

        assertNotNull(categoryList);
        assertNotEquals(getCategories().get(0).getSubCategories().get(2).getSubCategories().size(), categoryList.get(0).getSubCategories().get(0).getSubCategories().size());
        assertEquals(2, categoryList.get(0).getSubCategories().get(0).getSubCategories().size());
        // final list should not contain "tv-accessories-gaming" which is not Apple® brand.
        assertEquals(false,
            categoryList.get(0).getSubCategories().get(0).getSubCategories().stream().anyMatch(category -> category.getSlug().equalsIgnoreCase("tv-accessories-gaming")));
    }

    /**
     * In this test we will be removing a node at depth 1 and another at depth 2.
     * It will also try to remove the node at depth 0 but won't succeed
     * The nodes removed don't have subcategories.
     */
    @Test
    public void testExcludeCategory() {
        List<Category> originalCategoryList = getCategories();
        List<Category> categoryList = getCategories();
        List<String> excludedCategories = new ArrayList<>();
        excludedCategories.add("tv-accessories-gaming");
        excludedCategories.add("apple-tv-apple-tv");
        excludedCategories.add("tv");

        categoryList = categoryConfigurationService.excludeCategories(categoryList, excludedCategories);

        assertNotNull(categoryList);
        assertNotEquals(originalCategoryList.get(0).getSubCategories().size(), categoryList.get(0).getSubCategories().size());
        assertNotEquals(originalCategoryList.get(0).getSubCategories().get(2).getSubCategories().size(), categoryList.get(0).getSubCategories().get(1).getSubCategories().size());
        assertEquals(2, categoryList.get(0).getSubCategories().get(1).getSubCategories().size());
        // final list should not contain "tv-accessories-gaming" as it has been excluded
        assertEquals(false,
            categoryList.get(0).getSubCategories().get(1).getSubCategories().stream().anyMatch(category -> category.getSlug().equalsIgnoreCase("tv-accessories-gaming")));
    }

    /**
     * In this test, the node at depth 1 will be removed which has subcategories.
     */
    @Test
    public void testExcludeCategoryWithSubCategory() {
        List<Category> originalCategoryList = getCategories();
        List<Category> categoryList = getCategories();
        List<String> excludedCategories = new ArrayList<>();
        excludedCategories.add("tv-accessories-gaming");
        excludedCategories.add("tv-accessories");
        excludedCategories.add("tv");

        categoryList = categoryConfigurationService.excludeCategories(categoryList, excludedCategories);

        assertNotNull(categoryList);
        assertNotEquals(originalCategoryList.get(0).getSubCategories().size(), categoryList.get(0).getSubCategories().size());
        // final list should not contain "tv-accessories" as it has been excluded
        assertEquals(false,
            categoryList.get(0).getSubCategories().stream().anyMatch(category -> category.getSlug().equalsIgnoreCase("tv-accessories")));
    }

    private CategoryConfiguration getCategoryConfiguration(String categoryName, Set<ProductAttributeConfiguration> productAttributes) {
        CategoryConfiguration categoryConfiguration = new CategoryConfiguration();
        categoryConfiguration.setCategoryName(categoryName);
        categoryConfiguration.setIsConfigurable(true);
        categoryConfiguration.setProductAttributes(productAttributes);
        return categoryConfiguration;
    }

    private Set<ProductAttributeConfiguration> getProductAttributeConfigurations() {
        Set<ProductAttributeConfiguration> productAttributes = new HashSet<>();
        productAttributes.add(getProductAttributeConfiguration("options", "color", "silver,gold,space gray", "test"));
        productAttributes.add(getProductAttributeConfiguration("options", "storage", "16GB,64GB,128GB", "test"));
        productAttributes.add(getProductAttributeConfiguration("options", "communication", "Wi-FI,Wi-Fi+Cellular", "test"));
        return productAttributes;
    }

    private ProductAttributeConfiguration getProductAttributeConfiguration(String attributeType, String attributeName, String attributeValue, String attributeImageUrl) {
        ProductAttributeConfiguration productAttributeConfiguration = new ProductAttributeConfiguration();
        productAttributeConfiguration.setAttributeType(attributeType);
        productAttributeConfiguration.setAttributeName(attributeName);
        return productAttributeConfiguration;
    }


    private ProductResponse getProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product());
        ProductResponse productResponse = new ProductResponse();
        productResponse.setProducts(products);
        return productResponse;
    }

    private ProductSearchRequest getProductSearchRequest(String catalogId, String categoryName) {
        return ProductSearchRequest.builder()
                .withRequestContext(CatalogRequestContext.builder()
                        .withCatalogId(catalogId)
                        .withAudience(Audience.builder()
                                .withVarIdAndProgramId(CommonConstants.DEFAULT_VAR_ID, CommonConstants.DEFAULT_PROGRAM_ID).build()).build())
//                .withVariations(true)
                .withCategory(categoryName).build();
    }



    private List<CategoryConfiguration> generateCategoryConfigurations() {
        List<CategoryConfiguration> categoryConfigurations = new ArrayList<>();
        categoryConfigurations.add(createCategoryConfiguration("ipad", true));
        categoryConfigurations.add(createCategoryConfiguration("ipad-mini-2", true));
        categoryConfigurations.add(createCategoryConfiguration("ipad-mini-3", true));
        categoryConfigurations.add(createCategoryConfiguration("ipad-air-2", true));
        categoryConfigurations.add(createCategoryConfiguration("ipad-applecare", true));
        categoryConfigurations.add(createCategoryConfiguration("ipad-headphones", true));
        categoryConfigurations.add(createCategoryConfiguration("ipad-accessories", true));
        categoryConfigurations.add(createCategoryConfiguration("mac", true));
        categoryConfigurations.add(createCategoryConfiguration("imac", true));
        categoryConfigurations.add(createCategoryConfiguration("macbook-air", true));
        categoryConfigurations.add(createCategoryConfiguration("ipod", true));
        categoryConfigurations.add(createCategoryConfiguration("ipod-nano", true));
        categoryConfigurations.add(createCategoryConfiguration("ipod-shuffle", true));
        categoryConfigurations.add(createCategoryConfiguration("ipod-touch", true));
        return categoryConfigurations;
    }

    private CategoryConfiguration createCategoryConfiguration(String categoryName, boolean isActive) {
        CategoryConfiguration categoryConfiguration = new CategoryConfiguration();
        categoryConfiguration.setCategoryName(categoryName);
        return categoryConfiguration;
    }

    private Collection<CategoryNode> generateUSAEnglishCategoryTaxonomy(){

        final List<CategoryNode> rootNodes = new ArrayList<>();

        // First root category
        final CategoryNode level211 = buildCategoryNode("ipad-applecare", "AppleCare+ for iPad", "211", 2, null);
        final CategoryNode level221 = buildCategoryNode("ipad-headphones", "Headphones", "221", 2, null);
        final ImmutableSet.Builder<CategoryNode>  level111NodesBuilder = ImmutableSet.builder();
        level111NodesBuilder.add(level211);
        level111NodesBuilder.add(level221);
        final CategoryNode level111 = buildCategoryNode("ipad-accessories", "iPad Accessories","111",1,level111NodesBuilder.build());
        final CategoryNode level121 = buildCategoryNode("ipad-air-2", "iPad Air 2","121", 1, null);
        final CategoryNode level131 = buildCategoryNode("ipad-mini-3", "iPad mini 3","131", 1, null);
        final ImmutableSet.Builder<CategoryNode>  level011NodesBuilder = ImmutableSet.builder();
        level011NodesBuilder.add(level111);
        level011NodesBuilder.add(level121);
        level011NodesBuilder.add(level131);
        final CategoryNode level011 = buildCategoryNode("ipad", "iPad", "011", 0, level011NodesBuilder.build());

        //Second root category
        final CategoryNode level112 = buildCategoryNode("imac", "iMac","112", 1, null);
        final CategoryNode level122 = buildCategoryNode("macbook-air", "MacBook Air","122", 1, null);
        final ImmutableSet.Builder<CategoryNode>  level012NodesBuilder = ImmutableSet.builder();
        level012NodesBuilder.add(level112);
        level012NodesBuilder.add(level122);
        final CategoryNode level012 = buildCategoryNode("mac", "Mac", "012", 0, level012NodesBuilder.build());

        // Third root category
        final CategoryNode level113 = buildCategoryNode("ipod-nano", "iPod nano", "113", 1, null);
        final CategoryNode level123 = buildCategoryNode("ipod-shuffle", "iPod shuffle", "123", 1, null);
        final CategoryNode level133 = buildCategoryNode("ipod-touch", "iPod touch", "133", 1, null);
        final ImmutableSet.Builder<CategoryNode>  level013NodesBuilder = ImmutableSet.builder();
        level013NodesBuilder.add(level113);
        level013NodesBuilder.add(level123);
        level013NodesBuilder.add(level133);
        final CategoryNode level013 = buildCategoryNode("ipod", "iPod", "013", 0, level013NodesBuilder.build());

        rootNodes.add(level011);
        rootNodes.add(level012);
        rootNodes.add(level013);
        return rootNodes;
    }

    private CategoryNode buildCategoryNode(java.lang.String slug, java.lang.String name, java.lang.String browseNodeId,
                                           int depth, java.util.Set<com.b2s.service.product.client.domain.CategoryNode> children){

        CategoryNode categoryNode = createNiceMock(CategoryNode.class);

        expect(categoryNode.getSlug()).andReturn(slug).anyTimes();
        expect(categoryNode.getName()).andReturn(name).anyTimes();
        expect(categoryNode.getBrowseNodeId()).andReturn(Optional.ofNullable(browseNodeId)).anyTimes();
        expect(categoryNode.getDepth()).andReturn(depth).anyTimes();
        expect(categoryNode.getChildren()).andReturn((ImmutableSet<CategoryNode>) children).anyTimes();
        replay(categoryNode);

        return categoryNode;
    }

    /**
     * @return Returns the following structure
     * tv
     *      apple-tv-apple-tv
     *      apple-tv-apple-tv-4k
     *      tv-accessories
     * 			tv-accessories-audio-music
     * 			tv-accessories-gaming
     * 			tv-accessories-headphones-speakers
     */
    private ArrayList<Category> getCategories(){
        ArrayList<Category> categoryList = new ArrayList<>();
        categoryList.add(fromJson("{\"imageUrl\":\"\",\"i18nName\":\"TV\",\"slug\":\"tv\",\"name\":\"TV\"," +
            "\"isConfigurable\":false,\"templateType\":\"LANDING\",\"defaultImage\":\"\",\"summaryIconImage\":\"\"," +
            "\"displayOrder\":5,\"isMultilineEngravable\":false,\"isActive\":false," +
            "\"subCategories\":[{\"imageUrl\":\"\",\"i18nName\":\"Apple TV\",\"slug\":\"apple-tv-apple-tv\"," +
            "\"name\":\"Apple TV\",\"isConfigurable\":true,\"templateType\":\"LIST/GRID\"," +
            "\"defaultImage\":\"apple-gr/assets/img/customizable/apple-tv.jpeg\",\"summaryIconImage\":\"\"," +
            "\"displayOrder\":2,\"engraveBgImageLocation\":\"\",\"isMultilineEngravable\":false,\"isActive\":true," +
            "\"subCategories\":[],\"parents\":[],\"products\":[],\"images\":{\"small\":\"https://store.storeimages" +
            ".cdn-apple.com/4982/as-images.apple.com/is/apple-tv-hero-select-201510?wid\\u003d1200\\u0026hei" +
            "\\u003d1200\\u0026fmt\\u003djpeg\\u0026qlt\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1503607253099\\u0026wid\\u003d75\\u0026hei\\u003d75\",\"thumbnail\":\"https://store.storeimages" +
            ".cdn-apple.com/4982/as-images.apple.com/is/apple-tv-hero-select-201510?wid\\u003d1200\\u0026hei" +
            "\\u003d1200\\u0026fmt\\u003djpeg\\u0026qlt\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1503607253099\\u0026wid\\u003d30\\u0026hei\\u003d30\",\"large\":\"https://store.storeimages" +
            ".cdn-apple.com/4982/as-images.apple.com/is/apple-tv-hero-select-201510?wid\\u003d1200\\u0026hei" +
            "\\u003d1200\\u0026fmt\\u003djpeg\\u0026qlt\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1503607253099\",\"medium\":\"https://store.storeimages.cdn-apple.com/4982/as-images.apple" +
            ".com/is/apple-tv-hero-select-201510?wid\\u003d1200\\u0026hei\\u003d1200\\u0026fmt\\u003djpeg\\u0026qlt" +
            "\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1503607253099\\u0026wid\\u003d150\\u0026hei\\u003d150\"},\"isNew\":false,\"depth\":1}," +
            "{\"imageUrl\":\"\",\"i18nName\":\"Apple TV 4K\",\"slug\":\"apple-tv-apple-tv-4k\",\"name\":\"Apple TV " +
            "4K\",\"isConfigurable\":true,\"templateType\":\"LIST/GRID\"," +
            "\"defaultImage\":\"apple-gr/assets/img/customizable/apple-tv-4k.jpeg\",\"summaryIconImage\":\"\"," +
            "\"displayOrder\":1,\"engraveBgImageLocation\":\"\",\"isMultilineEngravable\":false,\"isActive\":true," +
            "\"subCategories\":[],\"parents\":[],\"products\":[],\"images\":{\"small\":\"https://store.storeimages" +
            ".cdn-apple.com/4982/as-images.apple.com/is/apple-tv-hero-select-201709?wid\\u003d1200\\u0026hei" +
            "\\u003d1200\\u0026fmt\\u003djpeg\\u0026qlt\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1504814112595\\u0026wid\\u003d75\\u0026hei\\u003d75\",\"thumbnail\":\"https://store.storeimages" +
            ".cdn-apple.com/4982/as-images.apple.com/is/apple-tv-hero-select-201709?wid\\u003d1200\\u0026hei" +
            "\\u003d1200\\u0026fmt\\u003djpeg\\u0026qlt\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1504814112595\\u0026wid\\u003d30\\u0026hei\\u003d30\",\"large\":\"https://store.storeimages" +
            ".cdn-apple.com/4982/as-images.apple.com/is/apple-tv-hero-select-201709?wid\\u003d1200\\u0026hei" +
            "\\u003d1200\\u0026fmt\\u003djpeg\\u0026qlt\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1504814112595\",\"medium\":\"https://store.storeimages.cdn-apple.com/4982/as-images.apple" +
            ".com/is/apple-tv-hero-select-201709?wid\\u003d1200\\u0026hei\\u003d1200\\u0026fmt\\u003djpeg\\u0026qlt" +
            "\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1504814112595\\u0026wid\\u003d150\\u0026hei\\u003d150\"},\"isNew\":false,\"depth\":1}," +
            "{\"imageUrl\":\"\",\"i18nName\":\"Apple TV Accessories\",\"slug\":\"tv-accessories\",\"name\":\"Apple TV" +
            " Accessories\",\"isConfigurable\":false,\"templateType\":\"CATEGORYLIST\",\"defaultImage\":\"\"," +
            "\"summaryIconImage\":\"\",\"displayOrder\":3,\"isMultilineEngravable\":false,\"isActive\":true," +
            "\"subCategories\":[{\"imageUrl\":\"\",\"i18nName\":\"Audio \\u0026 Music\"," +
            "\"slug\":\"tv-accessories-audio-music\",\"name\":\"Audio \\u0026 Music\",\"isConfigurable\":false," +
            "\"templateType\":\"CATEGORYLIST\",\"defaultImage\":\"\",\"summaryIconImage\":\"\",\"displayOrder\":99," +
            "\"isMultilineEngravable\":false,\"isActive\":true,\"subCategories\":[],\"parents\":[],\"products\":[]," +
            "\"images\":{},\"isNew\":false,\"depth\":2},{\"imageUrl\":\"\",\"i18nName\":\"Headphones " +
            "\\u0026 Speakers\",\"slug\":\"tv-accessories-headphones-speakers\",\"name\":\"Headphones " +
            "\\u0026 Speakers\",\"isConfigurable\":false,\"templateType\":\"CATEGORYLIST\",\"defaultImage\":\"\"," +
            "\"summaryIconImage\":\"\",\"displayOrder\":99,\"isMultilineEngravable\":false,\"isActive\":true," +
            "\"subCategories\":[],\"parents\":[],\"products\":[],\"images\":{},\"isNew\":false,\"depth\":2}," +
            "{\"imageUrl\":\"\",\"i18nName\":\"Gaming\"," +
            "\"slug\":\"tv-accessories-gaming\",\"name\":\"Gaming\"," +
            "\"isConfigurable\":false,\"templateType\":\"CATEGORYLIST\",\"defaultImage\":\"\"," +
            "\"summaryIconImage\":\"\",\"displayOrder\":99,\"isMultilineEngravable\":false,\"isActive\":true," +
            "\"subCategories\":[],\"parents\":[],\"products\":[],\"images\":{},\"isNew\":false,\"depth\":2}]," +
            "\"parents\":[],\"products\":[],\"images\":{\"small\":\"https://store.storeimages.cdn-apple" +
            ".com/4982/as-images.apple.com/is/MWP22?wid\\u003d1200\\u0026hei\\u003d1200\\u0026fmt\\u003djpeg" +
            "\\u0026qlt\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1572990352299\\u0026wid\\u003d75\\u0026hei\\u003d75\",\"thumbnail\":\"https://store.storeimages" +
            ".cdn-apple.com/4982/as-images.apple.com/is/MWP22?wid\\u003d1200\\u0026hei\\u003d1200\\u0026fmt" +
            "\\u003djpeg\\u0026qlt\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026" +
            ".v\\u003d1572990352299\\u0026wid\\u003d30\\u0026hei\\u003d30\",\"large\":\"https://store.storeimages" +
            ".cdn-apple.com/4982/as-images.apple.com/is/MWP22?wid\\u003d1200\\u0026hei\\u003d1200\\u0026fmt" +
            "\\u003djpeg\\u0026qlt\\u003d80\\u0026op_usm\\u003d0.5,0.5\\u0026.v\\u003d1572990352299\"," +
            "\"medium\":\"https://store.storeimages.cdn-apple.com/4982/as-images.apple" +
            ".com/is/MWP22?wid\\u003d1200\\u0026hei\\u003d1200\\u0026fmt\\u003djpeg\\u0026qlt\\u003d80\\u0026op_usm" +
            "\\u003d0.5,0.5\\u0026.v\\u003d1572990352299\\u0026wid\\u003d150\\u0026hei\\u003d150\"},\"isNew\":false," +
            "\"depth\":1}],\"parents\":[],\"products\":[],\"images\":{},\"isNew\":false,\"depth\":0}"));

        return categoryList;
    }

    private Category fromJson(String str){
        Gson gson=new Gson();
        return gson.fromJson(str,Category.class);
    }

    private Map<String, ProductSearchDocumentGroup> getProductSearchDocumentGroupMap() {
        Map<String, ProductSearchDocumentGroup> productSearchDocumentGroupMap = new HashMap<>();
        productSearchDocumentGroupMap.put("DEFAULT_GROUP", ProductSearchDocumentGroup.builder().withTotalFound(1).build());
        return productSearchDocumentGroupMap;
    }

    private CategoryNode getCategoryNode(){

        CategoryNode categoryNodeAcc1=CategoryNode.builder()
            .withSlug("tv-accessories-headphones-speakers")
            .withName("Headphones & Speakers")
            .withDepth(2).build();

        CategoryNode categoryNodeAcc2=CategoryNode.builder()
            .withSlug("tv-accessories-gaming")
            .withName("Gaming")
            .withDepth(2).build();
        CategoryNode categoryNodeAcc3=CategoryNode.builder()
            .withSlug("tv-accessories-audio-music")
            .withName("Audio & Music")
            .withDepth(2).build();

        Set<CategoryNode> nodeList = new HashSet<>();
        nodeList.add(categoryNodeAcc1);
        nodeList.add(categoryNodeAcc2);
        nodeList.add(categoryNodeAcc3);

        CategoryNode categoryNodeAcc=CategoryNode.builder()
            .withSlug("tv-accessories")
            .withName("Apple TV Accessories")
            .withDepth(1)
            .withChildren(nodeList)
            .build();

        Set<CategoryNode> nodes = new HashSet<>();
        nodes.add(categoryNodeAcc);

        return CategoryNode.builder()
            .withSlug("tv")
            .withName("TV")
            .withDepth(0)
            .withChildren(nodes).build();

    }
}
