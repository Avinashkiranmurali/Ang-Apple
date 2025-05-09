package com.b2s.apple.mapper;

import com.b2s.apple.entity.MerchantEntity;
import com.b2s.apple.services.CategoryConfigurationService;
import com.b2s.apple.services.EngravingService;
import com.b2s.apple.services.VarProgramMessageService;
import com.b2s.common.CategoryInfo;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.util.CategoryRepository;
import com.b2s.common.services.util.ImageObfuscatory;
import com.b2s.common.services.util.MerchantRepositoryHolder;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.model.Category;
import com.b2s.rewards.apple.model.Product;
import com.b2s.rewards.apple.util.ShipmentQuoteUtil;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.product.client.application.detail.MultiProductDetailResponse;
import com.b2s.service.product.common.domain.*;
import com.b2s.service.product.common.domain.response.*;
import com.b2s.service.product.common.domain.response.Option;
import com.b2s.service.product.common.domain.shipping.DeliveryInformation;
import com.b2s.service.product.common.domain.shipping.ShippingInformation;
import com.b2s.service.product.common.domain.shipping.Source;
import com.b2s.shop.common.User;
import com.b2s.web.B2RReloadableResourceBundleMessageSource;
import com.google.gson.Gson;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.RoundingMode;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductMapperTest {

    @InjectMocks
    private ProductMapper productMapper;

    @Mock
    ApplicationContext applicationContext;

    @Mock
    ImageObfuscatory imageObfuscatory;

    @Mock
    OptionMapper optionMapper;

    @Mock
    VarProgramMessageService varProgramMessageService;
    @Mock
    private CategoryConfigurationService categoryConfigurationService;
    @Mock
    private EngravingService engravingService;

    @Mock
    private MerchantRepositoryHolder merchantRepositoryHolder;

    @Mock
    private Map<String, String> supplierProductMapping;

    @Mock
    private Map<String, String> legacyMerchantCodeMappings;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    MockMvc mockMvc;

    MessageSource messageSource;

    @Mock
    ShipmentQuoteUtil shipmentQuoteUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AppContext.setApplicationContext(applicationContext);
        messageSource = mock(B2RReloadableResourceBundleMessageSource.class);
        when(applicationContext.getBean(anyString())).thenReturn(messageSource);
    }

    @Test
    public void testFrom()
        throws ServiceException {
        Map<String, Category> categoryHierarchyBySlug = populateCategoryHierarchyBySlug();
        CategoryRepository categoryRepository = new CategoryRepository(new HashMap<>(),new HashMap<>(),categoryHierarchyBySlug);
        when(categoryConfigurationService.getCategoryRepository(Locale.US)).thenReturn(categoryRepository);
        CategoryConfiguration categoryConfiguration = new CategoryConfiguration();
        categoryConfiguration.setEngravable(true);
        EngraveConfiguration engraveConfiguration = new EngraveConfiguration();
        engraveConfiguration.setActive(true);
        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(anyString(), anyString()))
                .thenReturn(categoryConfiguration);
        when(engravingService.isEngraveEnabled(any(), anyString(), anyString())).thenReturn(true);
        when(engravingService.getEngraveConfiguration(any(), any())).thenReturn(engraveConfiguration);

        com.b2s.service.product.common.domain.response.Product product = getProductDetail();
        User user = getUser();
        when(merchantRepositoryHolder.getMerchantRepository()).thenReturn(getMerchantMap());
        when(supplierProductMapping.get(anyString())).thenReturn("200");
        when(legacyMerchantCodeMappings.get(anyString())).thenReturn("30002");
        when(varProgramMessageService.getMessages(any(), any(), any()))
                .thenReturn(null);
        when(messageSource.getMessage("MX3L2BX/A", null, null, Locale.US))
                .thenReturn("Shipped");
                Product result = productMapper.from(product, Locale.US, getProgram(), user, false, false);
        assertNotNull(result);
        assertEquals("ipad-keyboards", result.getCategories().get(0).getSlug());
        assertEquals(true, result.isAccessoryItem());
        assertNotNull(result.getOptionsConfigurationData());

    }

    private Map<String, Category> populateCategoryHierarchyBySlug() {
        Category parent = new Category();
        parent.setSlug("MAC");
        List<Category> parentList = new ArrayList<>();
        parentList.add(parent);
        Map<String, Category> categoryHierarchyBySlug = new HashMap<>();

        Category categoryIpadKeyboard = new Category();
        categoryIpadKeyboard.setSlug("ipad-keyboards");
        categoryIpadKeyboard.setParents(parentList);

        Category categoryMac = new Category();
        categoryMac.setSlug("macbook-pro");
        categoryMac.setParents(parentList);

        Category categoryAccessories = new Category();
        categoryAccessories.setSlug("all-accessories");
        categoryAccessories.setParents(parentList);

        categoryHierarchyBySlug.put("ipad-keyboards", categoryIpadKeyboard);
        categoryHierarchyBySlug.put("macbook-pro", categoryMac);
        categoryHierarchyBySlug.put("all-accessories", categoryAccessories);
        return categoryHierarchyBySlug;
    }

    private Map<String, List<MerchantEntity>> getMerchantMap(){
        Map<String, List<MerchantEntity>> MERCHANT_MAP = new HashMap<>();
        MerchantEntity merchantEntity = new MerchantEntity();
        merchantEntity.setId(1);
        merchantEntity.setMerchantId(30001);
        merchantEntity.setSupplierId(200);
        merchantEntity.setName("Apple");
        merchantEntity.setSimpleName("apple");
        List<MerchantEntity> merchantEntityList = new ArrayList<>();
        merchantEntityList.add(merchantEntity);
        MERCHANT_MAP.put("apple", merchantEntityList);
        return MERCHANT_MAP;
    }

    @Test
    public void testFromWithDifferentCategories()
        throws ServiceException {
        AppContext.setApplicationContext(applicationContext);
        MessageSource messageSource = mock(B2RReloadableResourceBundleMessageSource.class);
        when(applicationContext.getBean(anyString())).thenReturn(messageSource);
        CategoryRepository categoryRepositoryCanada = mock(CategoryRepository.class);
        CategoryRepository categoryRepositoryUS = mock(CategoryRepository.class);
        CategoryConfiguration categoryConfiguration = new CategoryConfiguration();
        categoryConfiguration.setEngravable(true);
        EngraveConfiguration engraveConfiguration = new EngraveConfiguration();
        engraveConfiguration.setActive(true);
        Map<String, Category> categoryHierarchyBySlug = populateCategoryHierarchyBySlug();
        CategoryRepository categoryRepository = new CategoryRepository(new HashMap<>(),new HashMap<>(),categoryHierarchyBySlug);
        when(categoryConfigurationService.getCategoryRepository(Locale.CANADA)).thenReturn(categoryRepository);
        when(categoryConfigurationService.getCategoryRepository(Locale.US)).thenReturn(categoryRepositoryUS);
        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(anyString(), anyString()))
                .thenReturn(categoryConfiguration);
        when(engravingService.isEngraveEnabled(any(), anyString(), anyString())).thenReturn(true);
        when(engravingService.getEngraveConfiguration(any(), any())).thenReturn(engraveConfiguration);
        when(categoryRepositoryCanada.getCategoryDetailsByHierarchy(anyString())).thenReturn(null);
        when(categoryRepositoryUS.getCategoryDetailsByHierarchy(anyString())).thenReturn(getCategory());

        com.b2s.service.product.common.domain.response.Product product = getProductDetail();
        User user = getUser();
        when(merchantRepositoryHolder.getMerchantRepository()).thenReturn(getMerchantMap());
        when(supplierProductMapping.get(anyString())).thenReturn("200");
        when(legacyMerchantCodeMappings.get(anyString())).thenReturn("30002");
        when(varProgramMessageService.getMessages(any(), any(), any()))
            .thenReturn(null);
        when(messageSource.getMessage("MX3L2BX/A", null, Locale.US))
            .thenReturn("Shipped");
        Program program= getProgram();
        Product result = productMapper.from(product, Locale.CANADA, program, user, false, false);
        assertNotNull(result);
        assertEquals(true,result.getAdditionalInfo().get(CommonConstants.ENGRAVABLE).equals("true"));

        program.getConfig().put(CommonConstants.ENGRAVE_DISABLED,true);
        result = productMapper.from(product, Locale.CANADA, program, user, false, false);
        assertEquals(true,result.getAdditionalInfo().get(CommonConstants.ENGRAVABLE).equals("false"));

    }

    @Test(expected = NullPointerException.class)
    public void testFromCategoryExceptionCase() {
        Category categoryTemp = getCategory();
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        try {
            when(categoryConfigurationService.getCategoryRepository(any())).thenReturn(getCategoryRepository());

            when(categoryRepository.getCategoryDetailsByHierarchy(anyString()))
                    .thenReturn(categoryTemp);

        } catch (ServiceException e) {
            e.printStackTrace();
        }

        Product result = productMapper.from(getProductSearchDetail(true), Locale.US, getProgram(), getUser(), null);


    }

    @Test
    public void testFromParentCategories() {
        AppContext.setApplicationContext(applicationContext);
        MessageSource messageSource = mock(B2RReloadableResourceBundleMessageSource.class);
        when(applicationContext.getBean(anyString())).thenReturn(messageSource);
        CategoryRepository categoryRepositoryUS = mock(CategoryRepository.class);
        CategoryConfiguration categoryConfiguration = new CategoryConfiguration();
        when(categoryConfigurationService.getCategoryRepository(Locale.US)).thenReturn(categoryRepositoryUS);
        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(anyString(), anyString()))
            .thenReturn(categoryConfiguration);
        Category category = getCategoryWithParents();
        when(categoryRepositoryUS.getCategoryDetailsByHierarchy(anyString())).thenReturn(category);
        ProductSearchDocument productSearchDocument = getProductSearchDetail(false);
        Product result = productMapper.from(productSearchDocument, Locale.US, getProgram(), getUser(), null);
        assertEquals("ipad", result.getCategories().get(0).getParents().get(0).getName());
    }

    private Category getCategoryWithParents() {
        Category category = getCategory();
        Category parentCat = new Category();
        parentCat.setName("ipad");
        List<Category> parCategories = new ArrayList<>();
        parCategories.add(parentCat);
        category.setParents(parCategories);
        return category;
    }

    // No Shipping message available for shippingMessageKey
    @Test
    public void testSetShipmentInformation_NM_for_Key() {
        Product product = getProduct();
        AvailabilityInformation info =  AvailabilityInformation.builder()
            .withAvailabilityStatus(AvailabilityStatus.IN_STOCK).withAvailabilityMessage("1 business day [24]").build();
        productMapper.setShipmentInformation(product, "MQHV2LL/A", "all-accessories-headphones" +
            "-speakers", info, Locale.US, "UA");
        assertNotNull(product.getShippingAvailabilityMessage());

    }

    // Shipping message available for shippingMessageKey
    @Test
    public void testSetShipmentInformation_for_Key() {
        Product product = getProduct();
        AvailabilityInformation info =  AvailabilityInformation.builder()
            .withAvailabilityStatus(AvailabilityStatus.IN_STOCK).withAvailabilityMessage("4-5 Weeks [300]").build();
        when(messageSource.getMessage("300", null, null, Locale.US))
            .thenReturn("4-5 Weeks");
        when(messageSource.getMessage("shipment.timeframe.key.300", null, null, Locale.ENGLISH))
            .thenReturn("5 Weeks");
        when(shipmentQuoteUtil.getShipmentQuoteDate(anyString(), any()))
            .thenReturn("2020-12-08");
        productMapper.setShipmentInformation(product, "MQHV2LL/A", "all-accessories-headphones" +
            "-speakers", info, Locale.US, "UA");
        assertNotNull(product.getShippingAvailabilityMessage());
        assertEquals("4-5 Weeks",product.getShippingAvailabilityMessage());

    }

    // No Shipping message Key available
    @Test
    public void testSetShipmentInformation_No_Key() {
        Product product = getProduct();
        AvailabilityInformation info =  AvailabilityInformation.builder()
            .withAvailabilityStatus(AvailabilityStatus.IN_STOCK).withAvailabilityMessage("4-5 Weeks").build();
        productMapper.setShipmentInformation(product, "MQHV2LL/A", "all-accessories-headphones" +
            "-speakers", info, Locale.US, "UA");
        assertNotNull(product.getShippingAvailabilityMessage());
        assertEquals("4-5 Weeks",product.getShippingAvailabilityMessage());

    }

    // No Availability Information present from PS response
    @Test
    public void testSetShipmentInformation_No_availability_info() {
        Product product = getProduct();

        when(messageSource.getMessage("400", null, null, Locale.US))
            .thenReturn("5–7 weeks");
        productMapper.setShipmentInformation(product, "MQHV2LL/A", "all-accessories-headphones" +
            "-speakers", null, Locale.US, "UA");
        assertNotNull(product.getShippingAvailabilityMessage());
        assertEquals("5–7 weeks",product.getShippingAvailabilityMessage());

    }


    @Test
    public void testFrom_multiProduct() {
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        when(categoryConfigurationService.getCategoryRepository(Locale.US)).thenReturn(categoryRepository);
        CategoryConfiguration categoryConfiguration = new CategoryConfiguration();
        EngraveConfiguration engraveConfiguration = new EngraveConfiguration();
        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(anyString(), anyString()))
                .thenReturn(categoryConfiguration);
        when(merchantRepositoryHolder.getMerchantRepository()).thenReturn(getMerchantMap());
        when(supplierProductMapping.get(anyString())).thenReturn("200");
        when(legacyMerchantCodeMappings.get(anyString())).thenReturn("30002");
        when(engravingService.isEngraveEnabled(any(), anyString(), anyString())).thenReturn(true);
        when(engravingService.getEngraveConfiguration(any(), any())).thenReturn(engraveConfiguration);
        MultiProductDetailResponse response = new MultiProductDetailResponse(Arrays.asList(getProductDetail()),
            new HashMap<String, Throwable>());

        User user = getUser();
        Map<String, Product> result = productMapper.from(response, Locale.US, getProgram(), user, false, false);
        assertNotNull(result);

    }

    @Test
    public void testFrom_variation() {
        Variation variation = Variation.builder().withPsid("30001MU9L2AM/A")
            .withSku("MU9L2AM/A")
            .withName("44mm Black Sport Band - Extra Large")
            .withBsin("106B5940E7FF6D")
            .withAvailabilityInformation(AvailabilityInformation.builder()
                .withAvailabilityStatus(AvailabilityStatus.IN_STOCK).withAvailabilityMessage("4-5 Weeks").build())
            .withImageUrls(ProductImageUrls.builder().build())
            .withAdditionalInfo(Map.of("manufacturerNote","[(1) Service coverage is available only sport band (2) Check T&C]"))
            .withPricingInformation(PricingInformation.builder().withOriginalPriceInfo(OriginalPriceInfo.builder()
                .withBasePrice(Money.of(CurrencyUnit.ofCountry("HK"), 1249.00, RoundingMode.UNNECESSARY))
                .withShippingCost(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY))
                .withSupplierSalesTax(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY)).build())
                .withCalculatePriceInfos(getCalculatedPriceInfo()).build())
            .build();
        User user = getUser();
        Product result = productMapper.from(variation, getProduct(), getProgram(), user, false,
            false);
        assertNotNull(result);
        assertEquals("30001MU9L2AM/A",result.getPsid());
        assertNotNull(result.getLearnMore());
        assertEquals("(1) Service coverage is available only sport band (2) Check T&C", result.getLearnMore());

    }


    private Product getProduct(){
        Product product = new Product();
        product.setPsid("30001MK0C2ZA/A");
        product.setName("Dummy product name");

        com.b2s.rewards.apple.model.Offer offer = new com.b2s.rewards.apple.model.Offer();
        offer.setIsEligibleForPayrollDeduction(false);
        List<com.b2s.rewards.apple.model.Offer> offers = new ArrayList<>();
        offers.add(offer);
        product.setOffers(offers);
        return product;
    }

    private com.b2s.service.product.common.domain.response.Product getProductDetail() {
        String large = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MWVG2?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1566852616078";
        String medium = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MWVG2?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1566852616078&wid=150&hei=150";
        String small = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MWVG2?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1566852616078&wid=75&hei=75";
        String thumbnail = "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MWVG2?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1566852616078&wid=30&hei=30";

        com.b2s.service.product.common.domain.response.Product product_detail =
                com.b2s.service.product.common.domain.response.Product.builder()
                        .withPsid("30001MX3L2BX/A")
                        .withBsin("106B5940E7FF6D")
                        .withSku("MX3L2BX/A")
                        .withName("Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - British English")
                        .withAverageRating(4f)
                        .withStatus(ProductStatus.ACTIVE)
                        .withMerchant("apple")
                        .withProductType(ProductType.MERCHANDISE)
                        .withImageUrls(ProductImageUrls.builder()
                                .withLarge(large)
                                .withMedium(medium)
                                .withSmall(small)
                                .withThumbnail(thumbnail)
                                .build())
                        .withImportDatetime(new DateTime())
                        .withModifiedDatetime(new DateTime())
                        .withAdditionalInfo(Map.of("varAnalyticKey","WATC","whatsInTheBox","Apple Watch Sport Band " +
                        "(includes band that can be configured for either S/M or M/L length, or band that can be configured for either M/L or L/XL length)\""))
                        .withShippingInformation(ShippingInformation.builder().withDeliveryInformation(DeliveryInformation.builder()
                                .withSource(Source.builder().withCountry("HKG").withSupplier("apple").build()).build()).build())
                        .withPricingInformation(PricingInformation.builder().withOriginalPriceInfo(OriginalPriceInfo.builder()
                                .withBasePrice(Money.of(CurrencyUnit.ofCountry("HK"), 1249.00, RoundingMode.UNNECESSARY))
                                .withShippingCost(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY))
                                .withSupplierSalesTax(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY)).build())
                                .withCalculatePriceInfos(getCalculatedPriceInfo()).build())
                        .withAvailabilityInformation(AvailabilityInformation.builder()
                                .withAvailabilityStatus(AvailabilityStatus.IN_STOCK).withAvailabilityMessage("1 business day [24]").build())
                        .withOptions(getOptionMap("en_GB", "British English", "Language"))
                        .withHasVariations(true)
                        .withUpc("UPC")
                        .withBrand("apple")
                        .withTotalReviews(4)
                        .withCategories(getCategories())
                        .withLongDescription("Smart Keyboard for iPad (7th generation) and iPad Smart Keyboard for iPad (7th generation) and iPad Smart Keyboard for iPad (7th generation) and iPad ")
                        .withShortDescription("Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - British English").build();
        return product_detail;
    }

    private ProductSearchDocument getProductSearchDetail(boolean hasVariation) {
        VariationSearchDocument document1 = getVariationSearchDocument("30001MX3L2ZA/A", "107D01B612CC49", "MX3L2ZA/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - Chinese (Zhuyin)", "zh_HK", "Chinese (Zhuyin)");

        VariationSearchDocument document2 = getVariationSearchDocument("30001MX3L2MO/A", "10FC7EDBC11A22", "MX3L2MO/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - US English", "en_US", "US English");

        VariationSearchDocument document3 = getVariationSearchDocument("30001MX3L2JX/A", "106A7DF5219589", "MX3L2JX/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - Japanese", "ja_JP", "Japanese");

        VariationSearchDocument document4 = getVariationSearchDocument("30001MX3L2CV/A", "10AA878D81C591", "MX3L2CV/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - Chinese (Pinyin)", "zh_CN", "Chinese (Pinyin)");

        VariationSearchDocument document5 = getVariationSearchDocument("30001MX3L2BX/A", "106B5940E7FF6D", "MX3L2BX/A",
                "Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - British English", "en_GB", "British English");

        return ProductSearchDocument.builder()
                .withPsid("30001MX3L2BX/A")
                .withBsin("106B5940E7FF6D")
                .withSku("MX3L2BX/A")
                .withName("Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - British English")
                .withSourceCountry("HKG")
                .withMerchant("apple")
                .withProductType(ProductType.MERCHANDISE)
                .withProductImageUrls(ProductImageUrls.builder().build())
                .withShippingInformation(ShippingInformation.builder().withDeliveryInformation(DeliveryInformation.builder()
                        .withSource(Source.builder().withCountry("HKG").withSupplier("apple").build()).build()).build())
                .withPricingInformation(PricingInformation.builder().withOriginalPriceInfo(OriginalPriceInfo.builder()
                        .withBasePrice(Money.of(CurrencyUnit.ofCountry("HK"), 1249.00, RoundingMode.UNNECESSARY))
                        .withShippingCost(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY))
                        .withSupplierSalesTax(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY)).build())
                        .withCalculatePriceInfos(getCalculatedPriceInfo()).build())
                .withAvailabilityInformation(AvailabilityInformation.builder()
                        .withAvailabilityStatus(AvailabilityStatus.IN_STOCK).withAvailabilityMessage("1 business day [24]").build())
                .withOptions(getOptionMap("en_GB", "British English", "Language"))
                .withVariations(Arrays.asList(document1, document2, document3, document4, document5))
                .withHasVariations(hasVariation)
                .withCategorySlugs(Arrays.asList("all-accessories-cases-protection"))
                .withShortDescription("Smart Keyboard for iPad (7th generation) and iPad Air (3rd generation) - British English").build();
    }

    private Set getCalculatedPriceInfo() {
        Set<CalculatedPriceInfo> calculatedPriceInfoSet = new HashSet<>();
        SplitTenderEstimate splitTenderEstimate = SplitTenderEstimate.builder()
                .withCcBuyInAmountLimit(Money.of(CurrencyUnit.of("HKD"), 263.20, RoundingMode.UNNECESSARY))
                .withCcBuyInPointsLimit(40492)
                .withRemainingPointPrice(10123)
                .build();

        calculatedPriceInfoSet.add(
                CalculatedPriceInfo.builder()
                        .withDisplayPrice(Money.of(CurrencyUnit.of("HKD"), 1149.00, RoundingMode.UNNECESSARY))
                        .withBasePrice(Money.of(CurrencyUnit.of("HKD"), 1149.00, RoundingMode.UNNECESSARY))
                        .withShippingCost(Money.of(CurrencyUnit.of("HKD"), 0.00, RoundingMode.UNNECESSARY))
                        .withSupplierSalesTax(Money.of(CurrencyUnit.of("HKD"), 0.00, RoundingMode.UNNECESSARY))
                        .withUnpromotedDisplayPrice(Money.of(CurrencyUnit.of("HKD"), 1149.00, RoundingMode.UNNECESSARY))
                        .withSplitTenderEstimate(splitTenderEstimate)
                        .withPriceType(PriceType.SUPPLIER)
                        .build());
        calculatedPriceInfoSet.add(
                CalculatedPriceInfo.builder()
                        .withDisplayPrice(Money.of(CurrencyUnit.of("PNT"), 287300.00, RoundingMode.UNNECESSARY))
                        .withBasePrice(Money.of(CurrencyUnit.of("PNT"), 287300.00, RoundingMode.UNNECESSARY))
                        .withShippingCost(Money.of(CurrencyUnit.of("PNT"), 0.00, RoundingMode.UNNECESSARY))
                        .withSupplierSalesTax(Money.of(CurrencyUnit.of("PNT"), 0.00, RoundingMode.UNNECESSARY))
                        .withUnpromotedDisplayPrice(Money.of(CurrencyUnit.of("PNT"), 287300.00, RoundingMode.UNNECESSARY))
                        .withSplitTenderEstimate(splitTenderEstimate)
                        .withPriceType(PriceType.SUPPLIER)
                        .build());

        return calculatedPriceInfoSet;
    }


    private Category getCategory() {
        Category category = new Category();
        category.setSlug("ABCDEDF/F");
        return category;
    }

    private CategoryRepository getCategoryRepository() throws ServiceException {
        Map<String, CategoryInfo> categoriesBySlug = new HashMap<String, CategoryInfo>();
        Map<String, CategoryInfo> categoriesByHierarchyFromRootNode = new HashMap<String, CategoryInfo>();
        Map<String, Category> categoryHierarchyBySlug = new HashMap<String, Category>();
        categoryHierarchyBySlug.put("ABCDEDF/F", getCategory());
        CategoryRepository cr =
                new CategoryRepository(categoriesBySlug, categoriesByHierarchyFromRootNode, categoryHierarchyBySlug);
        return cr;
    }

    private VariationSearchDocument getVariationSearchDocument(String psid, String bsin, String sku, String name, String key, String value) {

        return VariationSearchDocument.builder()
                .withPsid(psid)
                .withBsin(bsin)
                .withSku(sku)
                .withName(name)
                .withProductImageUrls(ProductImageUrls.builder().build())
                .withShippingInformation(ShippingInformation.builder().withDeliveryInformation(DeliveryInformation.builder()
                        .withSource(Source.builder().withCountry("HKG").withSupplier("apple").build()).build()).build())
                .withPricingInformation(PricingInformation.builder().withOriginalPriceInfo(OriginalPriceInfo.builder()
                        .withBasePrice(Money.of(CurrencyUnit.ofCountry("HK"), 1249.00, RoundingMode.UNNECESSARY))
                        .withShippingCost(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY))
                        .withSupplierSalesTax(Money.of(CurrencyUnit.ofCountry("HK"), 0.00, RoundingMode.UNNECESSARY)).build())
                        .withCalculatePriceInfos(getCalculatedPriceInfo()).build())
                .withAvailabilityInformation(AvailabilityInformation.builder()
                        .withAvailabilityStatus(AvailabilityStatus.IN_STOCK).withAvailabilityMessage("1 business day [24]").build())
                .withOptions(getOptionMap(key, value, "Language"))
                .withShortDescription(name).build();

    }

    private Map<String, Option> getOptionMap(String key, String value, String label) {
        Option languageOption = Option.builder()
                .withKey(key)
                .withValue(value)
                .withLabel(label).build();

        Option caseSizeOption = Option.builder()
                .withKey("caseSize")
                .withValue("Case Size")
                .withLabel("Case Size").build();
        Map<String, Option> options =
                new HashMap<String, Option>();
        options.put("language", languageOption);
        options.put("caseSize", caseSizeOption);
        return options;

    }

    private Program getProgram() {
        Program program = new Program();
        program.setVarId("SCOTIA");
        program.setProgramId("AmexROC");
        program.setCatalogId("apple-ca-en");
        Map<String, Object> configs = new HashMap<>();
        configs.put("catalog_id", "apple-us-en");
        configs.put("epp", "false");
        program.setConfig(configs);
        program.setTargetCurrency(CurrencyUnit.of("PNT"));
        return program;
    }

    User getUser() {
        User user = new User();
        user.setVarId("HK");
        user.setProgramId("b2s_qa_only");
        user.setLocale(new Locale("en", "HK"));
        return user;
    }

    private com.b2s.service.product.common.domain.Category fromJson(String str){
        Gson gson=new Gson();
        return gson.fromJson(str,com.b2s.service.product.common.domain.Category.class);
    }

    private List<com.b2s.service.product.common.domain.Category> getCategories() {
        List<com.b2s.service.product.common.domain.Category> categoryList = new ArrayList<>();

        com.b2s.service.product.common.domain.Category parentCategory = com.b2s.service.product.common.domain.Category.builder()
                .withName("Mac Accessories")
                .withDepth((short)1)
                .withSlug("mac-accessories")
                .withAncestors(new ArrayList<>())
                .build();
        com.b2s.service.product.common.domain.Category category = com.b2s.service.product.common.domain.Category.builder()
                .withName("Displays & Mounts")
                .withDepth((short)2)
                .withSlug("mac-displays-mounts")
                .withAncestors(List.of(parentCategory))
                .build();
        categoryList.add(category);

        categoryList.add(
            fromJson("{\"imageUrl\":\"\",\"i18nName\":\"Ipad Keyboard\",\"slug\":\"ipad-keyboards\"," +
                "\"name\":\"Ipad Keyboards\",\"templateType\":\"LIST/GRID\",\"defaultImage\":\"http://store" +
                ".storeimages" +
                ".cdn-apple.com/4572/as-images.apple.com/is/image/AppleInc/aos/published/images/M/E8/ME864" +
                "/ME864\",\"summaryIconImage\":\"\",\"displayOrder\":3,\"engraveBgImageLocation\":null," +
                "\"subCategories\":[],\"parents\":[],\"products\":[],\"images\":{\"small\":\"https://store.storeimages" +
                ".cdn-apple.com/4982/as-images.apple.com/is/mbp16touch-space-select-201911?wid=1200&hei=1200&fmt=jpeg&qlt" +
                "=80&op_usm=0.5,0.5&.v=1572825197207&wid=75&hei=75\",\"thumbnail\":\"https://store.storeimages.cdn-apple" +
                ".com/4982/as-images.apple.com/is/mbp16touch-space-select-201911?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm" +
                "=0.5,0.5&.v=1572825197207&wid=30&hei=30\",\"large\":\"https://store.storeimages.cdn-apple" +
                ".com/4982/as-images.apple.com/is/mbp16touch-space-select-201911?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm" +
                "=0.5,0.5&.v=1572825197207\",\"medium\":\"https://store.storeimages.cdn-apple.com/4982/as-images.apple" +
                ".com/is/mbp16touch-space-select-201911?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&" +
                ".v=1572825197207&wid=150&hei=150\"},\"psid\":null,\"depth\":1,\"categoryVarPrograms\":null," +
                "\"supportedLocales\":[\"fr_CA\",\"en_US\",\"en_CA\"],\"new\":false,\"configurable\":false," +
                "\"active\":true}"));

        categoryList.add(
            fromJson("{\"imageUrl\":\"\",\"i18nName\":\"Ipad Keyboard\",\"slug\":\"ipad-accessories\"," +
                "\"name\":\"Ipad accessories\",\"templateType\":\"LIST/GRID\",\"defaultImage\":\"http://store" +
                ".storeimages" +
                ".cdn-apple.com/4572/as-images.apple.com/is/image/AppleInc/aos/published/images/M/E8/ME864" +
                "/ME864\",\"summaryIconImage\":\"\",\"displayOrder\":3,\"engraveBgImageLocation\":null," +
                "\"subCategories\":[],\"parents\":[],\"products\":[],\"images\":{\"small\":\"https://store.storeimages" +
                ".cdn-apple.com/4982/as-images.apple.com/is/mbp16touch-space-select-201911?wid=1200&hei=1200&fmt=jpeg&qlt" +
                "=80&op_usm=0.5,0.5&.v=1572825197207&wid=75&hei=75\",\"thumbnail\":\"https://store.storeimages.cdn-apple" +
                ".com/4982/as-images.apple.com/is/mbp16touch-space-select-201911?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm" +
                "=0.5,0.5&.v=1572825197207&wid=30&hei=30\",\"large\":\"https://store.storeimages.cdn-apple" +
                ".com/4982/as-images.apple.com/is/mbp16touch-space-select-201911?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm" +
                "=0.5,0.5&.v=1572825197207\",\"medium\":\"https://store.storeimages.cdn-apple.com/4982/as-images.apple" +
                ".com/is/mbp16touch-space-select-201911?wid=1200&hei=1200&fmt=jpeg&qlt=80&op_usm=0.5,0.5&" +
                ".v=1572825197207&wid=150&hei=150\"},\"psid\":null,\"depth\":1,\"categoryVarPrograms\":null," +
                "\"supportedLocales\":[\"fr_CA\",\"en_US\",\"en_CA\"],\"new\":false,\"configurable\":false," +
                "\"active\":true}"));
        return categoryList;
    }
}