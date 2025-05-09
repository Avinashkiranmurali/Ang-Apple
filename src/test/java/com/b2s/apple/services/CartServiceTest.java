
package com.b2s.apple.services;

import com.b2s.apple.mapper.ProductMapper;
import com.b2s.common.services.discountservice.CouponCodeValidator;
import com.b2s.common.services.exception.DataException;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.pricing.impl.LocalPricingServiceV2;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.dao.NaughtyWordDao;
import com.b2s.rewards.apple.dao.ShoppingCartDao;
import com.b2s.rewards.apple.dao.ShoppingCartItemDao;
import com.b2s.rewards.apple.dao.WhiteListWordDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.model.Merchant;
import com.b2s.rewards.model.Supplier;
import com.b2s.shop.common.User;
import com.b2s.shop.util.VarProgramConfigHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.ENABLE_SMART_PRICING;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * Created by rramasundaram on 4/23/2019.
 */


public class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private ShoppingCartDao appleShoppingCartDao;

    @Mock
    private WhiteListWordDao whiteListWordDao;

    @Mock
    private NaughtyWordDao naughtyWordDao;

    @Mock
    private CategoryConfigurationService categoryConfigurationService;

    @Mock
    private ShoppingCartItemDao appleShoppingCartItemDao;

    @Mock
    private CouponCodeValidator couponCodeValidator;

    @Mock
    private ProductServiceV3 productServiceV3;

    @Mock
    private LocalPricingServiceV2 pricingServiceV2;

    @Mock
    private GiftPromoService giftPromoService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private EngravingService engravingService;

    @Mock
    private VarProgramConfigHelper varProgramConfigHelper;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    MockMvc mockMvc;

    @Captor
    ArgumentCaptor<ShoppingCartItem> argumentCaptor;
    Cart cart;
    Program program;
    User user;

    @Before
    public void setup()
        throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(cartService).build();
    }

    @Test
    public void testCheckForCharset() {
        String test1 = cartService.checkForCharset("Raゑをん39น้ำ", "zh_HK");
        String test2 = cartService.checkForCharset("Orange", "zh_HK");
        String test3 = cartService.checkForCharset("Sample", "en_US");

        assertNotNull(test1);
        assertNull(test2);
        assertNull(test3);
    }

    @Test
    public void testGetCartWithEngraveAndGiftItem()
        throws ServiceException {
        User user = getUser();
        Program program = new Program();

        final String optionsXml = "<xml><options>" +
            "<name>engrave</name><value>{\"line1\":\"Product Line 1\",\"line2\":\"\",\"font\":\"Helvetica Neue\"," +
            "\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\",\"widthDimension\":\"45mm\"}</value>" +
            "<name>giftItem</name><value>{\"productId\": \"30001ABCDEDF/F\", \"engrave\":{\"line1\":\"Gift Line 1\"," +
            "\"line2\":\"\",\"font\":\"Helvetica Neue\",\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\"," +
            "\"widthDimension\":\"45mm\"}}</value>" +
            "</options></xml>";
        ShoppingCartItem shoppingCartItem = getShoppingCartItem(optionsXml);
        ShoppingCart shoppingCart = getShoppingCart();
        final Product product = getProduct("30001ABCDEDF/F");

        when(appleShoppingCartDao.get("Delta", "b2s_qa", "test")).thenReturn(shoppingCart);
        when(appleShoppingCartItemDao.getShoppingCartItems(shoppingCart)).thenReturn(Arrays.asList(shoppingCartItem));
        when(productServiceV3
            .getAppleProductDetail(nullable(String.class), any(Program.class), anyBoolean(), any(User.class),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
            .thenReturn(product);
        when(productMapper.productName2CartNameForGiftCard(product)).thenReturn("");
        when(giftPromoService.getGiftPsids(any()) ).thenReturn(List.of("30001ABCDEDF/F"));
        List<GiftItem> giftItemList = new ArrayList<>();
        GiftItem giftItem = new GiftItem();
        giftItem.setProductId("30001ABCDEDF/F");
        giftItem.setDiscount(80.00);
        giftItem.setDiscountType("percentage");
        giftItemList.add(giftItem);
        when(giftPromoService.getGiftItemList(any(), any(), any())).thenReturn(giftItemList);

        Cart result = cartService.getCart(user, null, program);
        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());
        assertNotNull(result.getCartItems().get(0).getSelectedAddOns().getGiftItem());
        assertEquals(Double.valueOf("80.00"), result.getCartItems().get(0).getSelectedAddOns().getGiftItem().getDiscount());
        assertEquals("Product Line 1", result.getCartItems().get(0).getEngrave().getLine1());
        assertEquals(result.getCartItems().get(0).getSelectedAddOns().getGiftItem().getQuantity(), Integer.valueOf(1));
        assertEquals("Gift Line 1", result.getCartItems().get(0).getSelectedAddOns().getGiftItem().getEngrave().getLine1());
    }

    @Test
    public void testGetCartWithExpiringGiftPromotionInCart()
        throws ServiceException {
        User user = getUser();
        Program program = new Program();
        program.setConfig(Map.of("paymentType", "PAYMENT"));

        final String optionsXml = "<xml><options>" +
            "<name>engrave</name><value>{\"line1\":\"Product Line 1\",\"line2\":\"\",\"font\":\"Helvetica Neue\"," +
            "\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\",\"widthDimension\":\"45mm\"}</value>" +
            "<name>giftItem</name><value>{\"productId\": \"30001ABCDEDF/F\", \"engrave\":{\"line1\":\"Gift Line 1\"," +
            "\"line2\":\"\",\"font\":\"Helvetica Neue\",\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\"," +
            "\"widthDimension\":\"45mm\"}}</value>" +
            "</options></xml>";
        ShoppingCartItem shoppingCartItem = getShoppingCartItem(optionsXml);
        ShoppingCart shoppingCart = getShoppingCart();
        final Product product = getProduct("30001ABCDEDF/F");

        when(appleShoppingCartDao.get("Delta", "b2s_qa", "test")).thenReturn(shoppingCart);
        when(appleShoppingCartItemDao.getShoppingCartItems(shoppingCart)).thenReturn(Arrays.asList(shoppingCartItem));
        when(productServiceV3
            .getAppleProductDetail(null, program, false, user, true, true, false, false))
            .thenReturn(product);
        when(productServiceV3
            .getAppleProductDetail(null, program, false, user, false, true, false, false))
            .thenReturn(product);

        when(productMapper.productName2CartNameForGiftCard(product)).thenReturn("");
        Cart result = cartService.getCart(user, null, program);

        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());
        assertNull(result.getCartItems().get(0).getSelectedAddOns().getGiftItem());
    }

    @Test
    public void testGetCartWithEngraveAndGiftItemWithListOfGiftsNull()
            throws ServiceException {
        User user = getUser();
        Program program = new Program();

        final String optionsXml = "<xml><options>" +
                "<name>engrave</name><value>{\"line1\":\"Product Line 1\",\"line2\":\"\",\"font\":\"Helvetica Neue\"," +
                "\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\",\"widthDimension\":\"45mm\"}</value>" +
                "<name>giftItem</name><value>{\"productId\": \"30001ABCDEDF/F\", \"engrave\":{\"line1\":\"Gift Line 1\"," +
                "\"line2\":\"\",\"font\":\"Helvetica Neue\",\"fontCode\":\"EHVO77N\",\"maxCharsPerLine\":\"35 Eng\"," +
                "\"widthDimension\":\"45mm\"}}</value>" +
                "</options></xml>";
        ShoppingCartItem shoppingCartItem = getShoppingCartItem(optionsXml);
        ShoppingCart shoppingCart = getShoppingCart();
        final Product product = getProduct("30001ABCDEDF/F");

        when(appleShoppingCartDao.get("Delta", "b2s_qa", "test")).thenReturn(shoppingCart);
        when(appleShoppingCartItemDao.getShoppingCartItems(shoppingCart)).thenReturn(Arrays.asList(shoppingCartItem));
        when(productServiceV3
            .getAppleProductDetail(null, program, false, user, true, true, false, false))
            .thenReturn(product);
        when(productServiceV3
            .getAppleProductDetail(null, program, false, user, false, true, false, false))
            .thenReturn(product);

        when(productMapper.productName2CartNameForGiftCard(product)).thenReturn("");
        Cart result = cartService.getCart(user, null, program);

        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());

    }

    @Test
    public void testModifyCartWithQualifyingEngravableAndGiftNonEngravable() throws ServiceException, DataException {
        final String optionsXml = "<xml><options>" +
            "<name>engrave</name><value>{\"line1\":\"Product Line 1\",\"line2\":\"\"}</value>" +
            "<name>giftItem</name><value>{\"productId\":\"30001ABCDEDF/F\"}</value>" +
            "</options></xml>";
        setupMocks(optionsXml, true, false);
        when(giftPromoService.getGiftPsids(any()) ).thenReturn(List.of("30001ABCDEDF/F"));
        mockGiftPromoService("30001ABCDEDF/F");

        Map request = new HashMap<>();
        request.put(CartItemOption.ENGRAVE.getValue(), Map.of("line1", "Engrave Line 1", "line2", "Engrave Line 2"));

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml = "<xml><options><name>engrave</name><value>{\"line1\":\"Engrave Line 1\"," +
            "\"line2\":\"Engrave Line 2\",\"font\":\"\",\"fontCode\":\"\",\"maxCharsPerLine\":\"\"," +
            "\"widthDimension\":\"\",\"noOfLines\":0,\"isSkuBasedEngraving\":false,\"isPreview\":false," +
            "\"isDefaultPreviewEnabled\":false,\"isUpperCaseEnabled\":false}</value><name>giftItem</name><value" +
            ">{\"productId\":\"30001ABCDEDF/F\"}</value></options></xml>";
        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartWithQualifyingEngravableAndGiftEngravable() throws ServiceException, DataException {
        final String optionsXml = "<xml><options><name>engrave</name><value>{\"line1\":\"Product Line 1\"," +
            "\"line2\":\"\"}</value><name>giftItem</name><value>{\"productId\":\"30001ABCDEDF/F\",\"discount\":100.0," +
            "\"discountType\":\"Percentage\"}</value>" +
            "</options></xml>";
        setupMocks(optionsXml, true, true);


        when(giftPromoService.getGiftPsids(any()) ).thenReturn(List.of("30001ABCDEDF/F"));
        mockGiftPromoService("30001ABCDEDF/F");

        Map request = new HashMap<>();
        request.put(CartItemOption.ENGRAVE.getValue(), Map.of("line1", "Engrave Line 1", "line2", "Engrave Line 2"));
        request.put(CartItemOption.GIFT_ITEM.getValue(), Map.of("productId", "30001ABCDEDF/F", "engrave", Map.of("line1", "Gift Engrave Line 1", "line2", "Gift Engrave Line 2")));

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml = "<xml><options><name>engrave</name><value>{\"line1\":\"Engrave Line 1\"," +
            "\"line2\":\"Engrave Line 2\",\"font\":\"\",\"fontCode\":\"\",\"maxCharsPerLine\":\"\"," +
            "\"widthDimension\":\"\",\"noOfLines\":0,\"isSkuBasedEngraving\":false,\"isPreview\":false," +
            "\"isDefaultPreviewEnabled\":false,\"isUpperCaseEnabled\":false}</value><name>giftItem</name><value" +
            ">{\"productId\":\"30001ABCDEDF/F\",\"engrave\":{\"line1\":\"Gift Engrave Line 1\",\"line2\":\"Gift " +
            "Engrave Line 2\",\"font\":\"\",\"fontCode\":\"\",\"maxCharsPerLine\":\"\",\"widthDimension\":\"\"," +
            "\"noOfLines\":0,\"isSkuBasedEngraving\":false,\"isPreview\":false,\"isDefaultPreviewEnabled\":false," +
            "\"isUpperCaseEnabled\":false}}</value></options></xml>";
        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartWithQualifyingEngravableAndEmptyEngraveRequest() throws ServiceException, DataException {
        final String optionsXml = "<xml><options>" +
            "<name>engrave</name><value>{\"line1\":\"Product Line 1\",\"line2\":\"\"}</value>" +
            "</options></xml>";
        setupMocks(optionsXml, true, true);

        Map request = new HashMap<>();
        request.put(CartItemOption.ENGRAVE.getValue(), new HashMap<>());

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml = "<xml><options/></xml>";
        assertNotNull(result);
        assertEquals(argument.getOptionsXml(), persistedOptionsXml);
    }

    @Test
    public void testModifyCartWithQualifyingNonEngravableAndGiftEngravable() throws ServiceException, DataException {
        final String optionsXml = "<xml><options>" +
            "<name>giftItem</name><value>{\"productId\": \"30001ABCDEDF/F\"}</value>" +
            "</options></xml>";
        setupMocks(optionsXml, false, true);

        when(giftPromoService.getGiftPsids(any()) ).thenReturn(List.of("30001ABCDEDF/F"));
        mockGiftPromoService("30001ABCDEDF/F");

        Map request = new HashMap<>();
        request.put(CartItemOption.GIFT_ITEM.getValue(), Map.of("productId", "30001MXK32LL/A", "engrave", Map.of("line1", "Gift Engrave Line 1", "line2", "Gift Engrave Line 2")));

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml = "<xml><options><name>giftItem</name><value>{\"productId\":\"30001ABCDEDF/F" +
            "\",\"engrave\":{\"line1\":\"Gift Engrave Line 1\",\"line2\":\"Gift Engrave Line 2\",\"font\":\"\"," +
            "\"fontCode\":\"\",\"maxCharsPerLine\":\"\",\"widthDimension\":\"\",\"noOfLines\":0," +
            "\"isSkuBasedEngraving\":false,\"isPreview\":false,\"isDefaultPreviewEnabled\":false," +
            "\"isUpperCaseEnabled\":false}}</value></options></xml>";
        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartWithQualifyingNonEngravableAndGiftNonEngravable() throws ServiceException, DataException {
        final String optionsXml = "<xml><options>" +
            "<name>giftItem</name><value>{\"productId\":\"30001ABCDEDF/F\"}</value>" +
            "</options></xml>";
        setupMocks(optionsXml, false, false);
        when(giftPromoService.getGiftPsids(any()) ).thenReturn(List.of("30001ABCDEDF/F"));
        mockGiftPromoService("30001ABCDEDF/F");

        Map request = new HashMap<>();
        request.put(CartItemOption.GIFT_ITEM.getValue(), Map.of("productId", "30001ABCDEDF/F"));

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml = "<xml><options><name>giftItem</name><value>{\"productId\":\"30001ABCDEDF/F" +
            "\"}</value></options></xml>";
        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    private List<GiftItem> mockGiftPromoService(String giftProductId)
        throws ServiceException {
        List<GiftItem> giftItemList = new ArrayList<>();
        GiftItem giftItem = new GiftItem();
        giftItem.setProductId(giftProductId);
        giftItemList.add(giftItem);
        when(giftPromoService.getGiftItemList(any(), any(), any())).thenReturn(giftItemList);
        when(giftPromoService.getGiftItem(any(), any(), any(), any())).thenReturn(Optional.of(giftItem));
        return giftItemList;
    }

    @Test
    public void testModifyCartRemoveGiftWithQualifyingEngravable() throws ServiceException, DataException {
        final String optionsXml = "<xml><options>" +
            "<name>engrave</name><value>{\"line1\":\"Product Line 1\",\"line2\":\"\"}</value>" +
            "<name>giftItem</name><value>{\"productId\": \"30001ABCDEDF/F\"}</value>" +
            "</options></xml>";
        setupMocks(optionsXml, true, true);

        Map request = new HashMap<>();
        request.put(CartItemOption.ENGRAVE.getValue(), Map.of("line1", "Engrave Line 1", "line2", "Engrave Line 2"));
        request.put(CartItemOption.GIFT_ITEM.getValue(), new HashMap<>());

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml = "<xml><options><name>engrave</name><value>{\"line1\":\"Engrave Line 1\"," +
            "\"line2\":\"Engrave Line 2\",\"font\":\"\",\"fontCode\":\"\",\"maxCharsPerLine\":\"\"," +
            "\"widthDimension\":\"\",\"noOfLines\":0,\"isSkuBasedEngraving\":false,\"isPreview\":false," +
            "\"isDefaultPreviewEnabled\":false,\"isUpperCaseEnabled\":false}</value></options></xml>";
        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartWithEngravingTextContainsNaughtyWord() throws ServiceException, DataException {
        final String optionsXml = "<xml><options/></xml>";
        setupMocks(optionsXml, true, false);

        NaughtyWord naughtyWord = new NaughtyWord();
        naughtyWord.setWord("naughty");
        naughtyWord.setPattern("naughty");
        naughtyWord.setMatchWholeWord(1);
        naughtyWord.setLanguage("en");
        naughtyWord.setLocale("en_US");
        when(naughtyWordDao.getByLocaleOrLanguage(user.getLocale(), user.getLocale().getLanguage())).thenReturn(Arrays.asList(naughtyWord));

        Map request = new HashMap<>();
        request.put(CartItemOption.ENGRAVE.getValue(), Map.of("line1", "naughty naughty naughty", "line2", "Engrave Line 2"));

        // The method must throw a DataException exception
        thrown.expect(DataException.class);

        cartService.modifyCart(cart, user, 1L, request, program);
    }

    @Test
    public void testVerifyIsCartItemsStillAvailableWithQualifyingProductNotAvailable() {

        cart = getCart();
        program = new Program();
        user = getUser();

        final String optionsXml = "<xml><options>" +
            "<name>giftItem</name><value>{\"productId\": \"30001ABCDEDF/F\"}</value>" +
            "</options></xml>";
        ShoppingCartItem shoppingCartItem = getShoppingCartItem(optionsXml);
        shoppingCartItem.setId(1L);
        shoppingCartItem.setProductId("30001MXK3999/F");
        List<ShoppingCartItem> dbShoppingCartItems = new ArrayList<>();
        dbShoppingCartItems.add(shoppingCartItem);

        final Product product = getProduct("30001MXK3999/F");
        product.setAvailable(false);
        when(productServiceV3
            .getAppleProductDetail(null, program, false, user, false, true, false, false))
            .thenReturn(product);

        cartService.verifyIsCartItemsStillAvailable(dbShoppingCartItems, cart, user, program);

        verify(appleShoppingCartItemDao, times(1)).delete(argumentCaptor.capture());

        assertEquals(1, cart.getItemsNoLongerAvailable().size());
    }

    @Test
    public void testVerifyIsCartItemsStillAvailableWithGiftItemNotAvailable() {

        cart = getCart();
        program = new Program();
        user = getUser();

        final String optionsXml = "<xml><options>" +
            "<name>giftItem</name><value>{\"productId\": \"30001ABCDEDF/F\"}</value>" +
            "</options></xml>";
        ShoppingCartItem shoppingCartItem = getShoppingCartItem(optionsXml);
        shoppingCartItem.setId(1L);
        shoppingCartItem.setProductId("30001MXK3999/F");
        List<ShoppingCartItem> dbShoppingCartItems = new ArrayList<>();
        dbShoppingCartItems.add(shoppingCartItem);

        final String psid = "30001MXK3999/F";
        final Product product = getProduct(psid);
        product.setAvailable(true);
        when(productServiceV3
            .getAppleProductDetail(psid, program, false, user, false, true, false, false))
            .thenReturn(product);

        final Product giftProduct = getProduct(psid);
        giftProduct.setAvailable(false);
        when(productServiceV3
            .getAppleProductDetail(psid, program, false, user, false, true, false, false))
            .thenReturn(product);

        cartService.verifyIsCartItemsStillAvailable(dbShoppingCartItems, cart, user, program);

        verify(appleShoppingCartItemDao, times(1)).delete(argumentCaptor.capture());

        assertEquals(1, cart.getItemsNoLongerAvailable().size());
    }

    @Test
    public void testModifyCartWithDeleteSubscriptionRequest() throws ServiceException, DataException {
        Map request = new HashMap<>();
        final Map<String, Object> subscriptionMap = new HashMap<>();
        subscriptionMap.put(CommonConstants.ITEM_ID , "amp_news");
        subscriptionMap.put(CommonConstants.QUANTITY , 0);
        request.put(CartItemOption.SUBSCRIPTIONS.getValue(), subscriptionMap);

        setupMocksSubscription(subscriptionMap);
        Cart result1 = cartService.modifyCart(cart, user, 1L, request, program);

        assertNotNull(result1);
        assertEquals(result1.getCartItemsTotalCount(),0);
    }

    @Test
    public void testModifyCartWithAddSubscriptionRequest() throws ServiceException, DataException {
        Map request = new HashMap<>();
        final Map<String, Object> subscriptionMap = new HashMap<>();
        subscriptionMap.put(CommonConstants.ITEM_ID , "amp_news");
        subscriptionMap.put(CommonConstants.QUANTITY , 1);
        request.put(CartItemOption.SUBSCRIPTIONS.getValue(), subscriptionMap);

        setupMocksSubscription(subscriptionMap);
        Cart result1 = cartService.modifyCart(cart, user, 1L, request, program);

        assertNotNull(result1);
    }

    @Test
    public void testModifyCartWithCartItemQualifyingServicePlan()
        throws ServiceException, DataException {
        final String optionsXml = "<xml><options><name>gift</name><value>{\"giftWrapPoints\":100}</value></options" +
            "></xml>";
        setupMocksForServicePlans(optionsXml);
        Map request = new HashMap<>();
        request.put(CartItemOption.SERVICE_PLAN.getValue(), "30001S8275LL/A");

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml = "<xml><options><name>gift</name><value>{\"giftWrapPoints\":100}</value" +
            "><name>servicePlan</name><value>30001S8275LL/A</value></options></xml>";
        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartRemoveServicePlanFromCartItem()
        throws ServiceException, DataException {
        final String optionsXml = "<xml><options><name>gift</name><value>{\"giftWrapPoints\":100}</value" +
            "><name>servicePlan</name><value>30001S8275LL/A</value></options></xml>";

        setupMocksForServicePlans(optionsXml);

        Map request = new HashMap<>();
        request.put(CartItemOption.SERVICE_PLAN.getValue(), "");

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml =
            "<xml><options><name>gift</name><value>{\"giftWrapPoints\":100}</value></options" +
                "></xml>";
        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartWithGiftItemQualifyingServicePlan()
        throws ServiceException, DataException {
        final String optionsXml = "<xml><options>" +
            "<name>giftItem</name><value>{\"productId\": \"30001GIFTITEM/F\"}</value>" +
            "</options></xml>";
        setupMocksForServicePlans(optionsXml);
        when(giftPromoService.getGiftPsids(any())).thenReturn(List.of("30001GIFTITEM/F"));
        mockGiftPromoService("30001GIFTITEM/F");

        Map request = new HashMap<>();
        request.put(CartItemOption.GIFT_ITEM.getValue(), Map.of("productId", "30001GIFTITEM/F", "servicePlan", "30001S8275LL/A"));

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml =
            "<xml><options><name>giftItem</name><value>{\"productId\":\"30001GIFTITEM/F\"," +
                "\"servicePlan\":\"30001S8275LL/A\"}</value></options></xml>";
        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartRemoveServicePlanFromGiftItem()
        throws ServiceException, DataException {

        final String optionsXml =
            "<xml><options><name>giftItem</name><value>{\"productId\":\"30001GIFTITEM/F\"," +
                "\"servicePlan\":\"30001S8275LL/A\"}</value></options></xml>";

        setupMocksForServicePlans(optionsXml);
        when(giftPromoService.getGiftPsids(any())).thenReturn(List.of("30001GIFTITEM/F"));
        mockGiftPromoService("30001GIFTITEM/F");

        Map request = new HashMap<>();
        request.put(CartItemOption.GIFT_ITEM.getValue(), Map.of("productId", "30001GIFTITEM/F", "servicePlan", ""));

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml = "<xml><options>" +
            "<name>giftItem</name><value>{\"productId\":\"30001GIFTITEM/F\"}</value>" +
            "</options></xml>";
        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartWithGiftItemQualifyingServiceAndEngrave()
        throws ServiceException, DataException {

        final String optionsXml =
            "<xml><options><name>giftItem</name><value>{\"productId\":\"30001GIFTITEM/F\"," +
                "\"servicePlan\":\"30001S8275LL/A\"}</value></options></xml>";

        setupMocksForServicePlans(optionsXml);
        when(giftPromoService.getGiftPsids(any())).thenReturn(List.of("30001GIFTITEM/F"));
        mockGiftPromoService("30001GIFTITEM/F");

        Map request = new HashMap<>();
        request.put(CartItemOption.GIFT_ITEM.getValue(), Map.of("productId", "30001GIFTITEM/F", "engrave",
            Map.of("line1", "Engrave Line 1", "line2", "Engrave Line 2")));

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml = "<xml><options><name>giftItem</name><value>{\"productId\":\"30001GIFTITEM" +
            "/F\",\"engrave\":{\"line1\":\"Engrave Line 1\",\"line2\":\"Engrave Line 2\",\"font\":\"\"," +
            "\"fontCode\":\"\",\"maxCharsPerLine\":\"\",\"widthDimension\":\"\",\"noOfLines\":0," +
            "\"isSkuBasedEngraving\":false,\"isPreview\":false,\"isDefaultPreviewEnabled\":false," +
            "\"isUpperCaseEnabled\":false},\"servicePlan\":\"30001S8275LL/A\"}</value></options></xml>";

        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartWithGiftItemQualifyingServiceAndEngraveRemoved()
        throws ServiceException, DataException {

        final String optionsXml =
            "<xml><options><name>giftItem</name><value>{\"productId\":\"30001GIFTITEM/F\"," +
                "\"servicePlan\":\"30001S8275LL/A\"}</value></options></xml>";

        setupMocksForServicePlans(optionsXml);
        when(giftPromoService.getGiftPsids(any())).thenReturn(List.of("30001GIFTITEM/F"));
        mockGiftPromoService("30001GIFTITEM/F");

        Map request = new HashMap<>();
        request.put(CartItemOption.GIFT_ITEM.getValue(), Map.of("productId", "30001GIFTITEM/F", "engrave", Map.of("line1", "", "line2", "")));

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml =
            "<xml><options><name>giftItem</name><value>{\"productId\":\"30001GIFTITEM/F\"," +
                "\"servicePlan\":\"30001S8275LL/A\"}</value></options></xml>";

        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test
    public void testModifyCartWithSingleItemPurchase() throws ServiceException, DataException {
        final String optionsXml = "";
        setupMocks(optionsXml, false, false);

        when(varProgramConfigHelper
                .getValue(user.getVarId(), user.getProgramId(), CommonConstants.SINGLE_ITEM_PURCHASE)).thenReturn("true");

        Map requestMap = new HashMap<>();
        requestMap.put(CartItemOption.QUANTITY.getValue(), 1);

        cart.getCartItems().get(0).setQuantity(5);

        cartService.modifyCart(cart, user, 1L, requestMap, program);

        assertNotNull(cart);
        assertNotNull(cart.getCartItems());
        assertTrue(Long.valueOf(cart.getCartItems().get(0).getQuantity())==1);
    }

    @Test(expected = ServiceException.class)
    public void testModifyCartWithSingleItemPurchaseServiceException() throws ServiceException, DataException {
        final String optionsXml = "";
        setupMocks(optionsXml, false, false);

        when(varProgramConfigHelper
                .getValue(user.getVarId(), user.getProgramId(), CommonConstants.SINGLE_ITEM_PURCHASE)).thenReturn("true");

        Map requestMap = new HashMap<>();
        requestMap.put(CartItemOption.QUANTITY.getValue(), 100);

        cart.getCartItems().get(0).setQuantity(5);

        cartService.modifyCart(cart, user, 1L, requestMap, program);
    }

    @Test
    public void testModifyCartWithDRPSingleItemPurchase() throws ServiceException, DataException {
        final String optionsXml = "";
        setupMocks(optionsXml, false, false);

        when(varProgramConfigHelper
                .getValue(user.getVarId(), user.getProgramId(), CommonConstants.SINGLE_ITEM_PURCHASE)).thenReturn("true");
        program.getConfig().put(CommonConstants.SHOP_EXPERIENCE, CommonConstants.EXPERIENCE_DRP);

        Map requestMap = new HashMap<>();
        requestMap.put(CartItemOption.QUANTITY.getValue(), 1);

        cart.getCartItems().get(0).setQuantity(5);

        cartService.modifyCart(cart, user, 1L, requestMap, program);

        assertNotNull(cart);
        assertNotNull(cart.getCartItems());
        assertTrue(Long.valueOf(cart.getCartItems().get(0).getQuantity())==1);
    }

    @Test(expected = ServiceException.class)
    public void testModifyCartWithDRPSingleItemPurchaseServiceException() throws ServiceException, DataException {
        final String optionsXml = "";
        setupMocks(optionsXml, false, false);

        when(varProgramConfigHelper
                .getValue(user.getVarId(), user.getProgramId(), CommonConstants.SINGLE_ITEM_PURCHASE)).thenReturn("true");
        program.getConfig().put(CommonConstants.SHOP_EXPERIENCE, CommonConstants.EXPERIENCE_DRP);
        program.getConfig().put(CommonConstants.LIMIT_MAX_QUANTITY, "2");

        Map requestMap = new HashMap<>();
        requestMap.put(CartItemOption.QUANTITY.getValue(), 100);

        cart.getCartItems().get(0).setQuantity(5);

        cartService.modifyCart(cart, user, 1L, requestMap, program);
    }

    @Test
    public void testModifyCartWithQuantityZero() throws ServiceException, DataException {
        final String optionsXml = "";
        setupMocks(optionsXml, false, false);

        Map requestMap = new HashMap<>();
        requestMap.put(CartItemOption.QUANTITY.getValue(), 0);

        cartService.modifyCart(cart, user, 1L, requestMap, program);

        assertNotNull(cart);
        assertNotNull(cart.getCartItems());
    }

    @Test(expected = ServiceException.class)
    public void testModifyCartWithQuantityThrowServiceException() throws ServiceException, DataException {
        final String optionsXml = "";
        setupMocks(optionsXml, false, false);

        Map requestMap = new HashMap<>();
        requestMap.put(CartItemOption.QUANTITY.getValue(), -1);

        cartService.modifyCart(cart, user, 1L, requestMap, program);
    }

    @Test(expected = ServiceException.class)
    public void testEngraveWithMultipleQuantityThrowServiceException() throws ServiceException, DataException {
        final String optionsXml = "<xml><options>" +
                "<name>engrave</name><value>{\"line1\":\"Product Line 1\",\"line2\":\"\"}</value>" +
                "</options></xml>";
        setupMocks(optionsXml, false, false);

        Map requestMap = new HashMap<>();
        requestMap.put(CartItemOption.QUANTITY.getValue(), 5);

        cartService.modifyCart(cart, user, 1L, requestMap, program);
    }

    @Test
    public void testModifyCartWithGift() throws ServiceException, DataException {
        final String optionsXml = "<xml><options/></xml>";
        setupMocks(optionsXml, false, false);

        Map request = new HashMap<>();
        request.put(CartItemOption.GIFT.getValue(), Map.of("message1", "testing message1"));

        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        verify(appleShoppingCartItemDao, times(1)).update(argumentCaptor.capture());
        ShoppingCartItem argument = argumentCaptor.getValue();
        final String persistedOptionsXml =
                "<xml><options><name>gift</name><value>{\"giftWrapPoints\":100,\"message1\":\"testing message1\"}</value></options></xml>";

        assertNotNull(result);
        assertEquals(persistedOptionsXml, argument.getOptionsXml());
    }

    @Test(expected = ServiceException.class)
    public void testModifyCartWithGiftThrowException() throws ServiceException, DataException {
        final String optionsXml = "<xml><options/></xml>";
        setupMocks(optionsXml, true, false);

        Map request = new HashMap<>();
        request.put(CartItemOption.GIFT.getValue(), Map.of("message1", "testing message1",
                "message2", "testing message2", "message3", "testing message3", "message4", "testing message4",
                "message5", "testing message with more and more data to throw exception"));

        cartService.modifyCart(cart, user, 1L, request, program);
    }

    @Test
    public void testMaxCartTotalExceeded() throws ServiceException, DataException {
        final String optionsXml = "";
        setupMocks(optionsXml, true, false);

        Map request = new HashMap<>();

        cart.setMaxCartTotalExceeded(true);
        Cart result = cartService.modifyCart(cart, user, 1L, request, program);

        assertNotNull(result);
        assertTrue(result.isMaxCartTotalExceeded());
    }

    private void setupMocksForServicePlans(final String optionsXml)
        throws ServiceException {
        setupMocks(optionsXml, false, false);
        program.getConfig().put(CommonConstants.ENABLE_APPLE_CARE_SERVICE_PLAN, true);
        Product product = getProduct("30001ABCDEDF/F");
        Product servicePlanProduct = getProduct("30001S8275LL/A");
        product.getAddOns().getServicePlans().add(servicePlanProduct);
        when(productServiceV3
            .getAppleProductDetail(any(), any(), anyBoolean(), any(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
            .thenReturn(product);
    }

    private void setupMocksSubscription(final Map message)
    {
        cart = getCart();
        program = new Program();
        user = getUser();
        final ShoppingCart shoppingCart = getAMPShoppingCart();
        List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
        shoppingCartItems.add(getAMPShoppingCartItem(shoppingCart, message));
        when(appleShoppingCartDao.get("Delta", "b2s_qa", "test")).thenReturn(shoppingCart);
        when(appleShoppingCartItemDao.getShoppingCartItems(shoppingCart)).thenReturn(shoppingCartItems);
            when(productServiceV3
            .getAppleProductDetail(any(), any(), anyBoolean(), any(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
            .thenReturn(getProduct("30001ABCDEDF/F"));
        when(whiteListWordDao.getWhitelistWords(user.getLocale(), user.getLocale().getLanguage())).thenReturn(Collections.emptyList());
        when(naughtyWordDao.getByLocaleOrLanguage(user.getLocale(), user.getLocale().getLanguage())).thenReturn(Collections.emptyList());

    }
    private void setupMocks(final String optionsXml, final boolean isQualifyingEngravable, final boolean isGiftEngravable)
        throws ServiceException {
        cart = getCart();
        program = new Program();
        program.getConfig().put(ENABLE_SMART_PRICING, true);
        program.setPayments(new ArrayList<>());

        user = getUser();
        final ShoppingCart shoppingCart = getShoppingCart();
        when(appleShoppingCartDao.get("Delta", "b2s_qa", "test")).thenReturn(shoppingCart);
        when(appleShoppingCartItemDao.getShoppingCartItems(shoppingCart)).thenReturn(Arrays.asList(getShoppingCartItem(optionsXml)));
        when(productServiceV3
            .getAppleProductDetail(any(), any(), anyBoolean(), any(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
            .thenReturn(getProduct("30001ABCDEDF/F"));
        when(whiteListWordDao.getWhitelistWords(user.getLocale(), user.getLocale().getLanguage())).thenReturn(Collections.emptyList());
        when(naughtyWordDao.getByLocaleOrLanguage(user.getLocale(), user.getLocale().getLanguage())).thenReturn(Collections.emptyList());

        CategoryConfiguration qualifyingCategoryConfiguration = new CategoryConfiguration();
        qualifyingCategoryConfiguration.setEngravable(isQualifyingEngravable);
        when(categoryConfigurationService.getCategoryConfigurationByCategoryName("", null)).thenReturn(qualifyingCategoryConfiguration);
        CategoryConfiguration giftCategoryConfiguration = new CategoryConfiguration();
        giftCategoryConfiguration.setEngravable(isGiftEngravable);
        when(categoryConfigurationService.getCategoryConfigurationByCategoryName(any(), any())).thenReturn(giftCategoryConfiguration);
        when(giftPromoService.getGiftItem(any(), any(), any(), any())).thenReturn(Optional.of(new GiftItem()));
    }

    private User getUser() {
        User user = new User();
        user.setVarId("Delta");
        user.setProgramId("b2s_qa");
        user.setUserId("test");
        user.setLocale(Locale.ENGLISH);
        user.setAmexInstallment(true);

        return user;
    }

    private ShoppingCart getShoppingCart() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(1L);
        return shoppingCart;
    }

    private Product getProduct(final String productId) {
        Product product = new Product();
        product.setProductId(productId);
        product.setPsid(productId);
        Offer offer = new Offer();
        Merchant merchant = new Merchant();
        merchant.setId(1);
        merchant.setMerchantId("30001");
        merchant.setSupplierId("200");
        merchant.setName("Apple");
        offer.setMerchant(merchant);
        offer.setB2sItemPrice(new Price(1.1, "", 1));
        offer.setPayPeriods(12);
        offer.setB2sItemPrice(new Price(12d, "USD", 1000));

        product.setOffers(Arrays.asList(offer));

        Supplier supplier = new Supplier();
        supplier.setSupplierId(200);
        product.setSupplier(supplier);
        product.setAvailable(true);
        return product;
    }

    private ShoppingCartItem getShoppingCartItem(final String optionsXml) {
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        shoppingCartItem.setId(1L);
        shoppingCartItem.setQuantity(5);
        shoppingCartItem.setOptionsXml(optionsXml);
        shoppingCartItem.setAddedDate(new Date());
        shoppingCartItem.setSupplierId(CommonConstants.APPLE_SUPPLIER_ID);

        return shoppingCartItem;
    }

    private Cart getCart() {
        Cart cart = new Cart();
        cart.setUserId("test");

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setAddedDate(new Date());

        cart.setRedemptionPaymentLimit(getRedemptionPaymentLimit());
        CartTotal cartTotal = new CartTotal();
        cartTotal.setPrice(new Price(267.84, "USD", 49600));
        cart.setCartTotal(cartTotal);
        cart.setDisplayCartTotal(cartTotal);
        cart.setCartItems(new ArrayList<>(Arrays.asList(cartItem)));

        return cart;
    }

    private RedemptionPaymentLimit getRedemptionPaymentLimit(){
        Price cartMaxLimit = null;
        Price cashMaxLimit = new Price(179.84, "USD", 24800);
        Price cashMinLimit = new Price(0d, "USD", 0);
        Price pointsMaxLimit = new Price(267.84, "USD", 49600);
        Price pointsMinLimit = new Price(133.92, "USD", 24800);
        Price useMaxPoints = new Price(0d, "USD", 49600);
        Price useMinPoints = new Price(179.84024999999997, "USD", 24800);

        return RedemptionPaymentLimit.builder()
                .withCartMaxLimit(cartMaxLimit)
                .withCashMaxLimit(cashMaxLimit)
                .withCashMinLimit(cashMinLimit)
                .withPointsMaxLimit(pointsMaxLimit)
                .withPointsMinLimit(pointsMinLimit)
                .withUseMaxPoints(useMaxPoints)
                .withUseMinPoints(useMinPoints)
                .build();
    }

    private ShoppingCartItem getAMPShoppingCartItem(final ShoppingCart shoppingCart, final Map modifyMessage) {
        ShoppingCartItem ampSubscriptionItem=new ShoppingCartItem();
        String productId = String.valueOf(modifyMessage.get(CommonConstants.ITEM_ID));
        ampSubscriptionItem.setAddedDate(new Date());
        ampSubscriptionItem.setMerchantId(CommonConstants.APPLE_MERCHANT_ID);
        ampSubscriptionItem.setProductId(productId);
        ampSubscriptionItem.setProductName(productId);
        ampSubscriptionItem.setQuantity((Integer)modifyMessage.get(CommonConstants.QUANTITY));
        ampSubscriptionItem.setSupplierId(CommonConstants.AMP_SUPPLIER_ID);
        ampSubscriptionItem.setShoppingCart(shoppingCart);
        return ampSubscriptionItem;
    }

    private ShoppingCart getAMPShoppingCart() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(1L);
        shoppingCart.setShoppingCartItems(new ArrayList<>());
        return shoppingCart;
    }
}