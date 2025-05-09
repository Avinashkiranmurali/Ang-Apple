package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.apple.services.CartService;
import com.b2s.common.services.exception.DataException;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.pricing.impl.LocalPricingServiceV2;
import com.b2s.common.services.productservice.ProductServiceV3;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.model.ShoppingCart;
import com.b2s.service.product.client.exception.EntityNotFoundException;
import com.b2s.service.product.client.exception.RequestValidationException;
import com.b2s.shop.common.User;
import com.b2s.shop.common.constant.Constant;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.json.JSONException;
import org.junit.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
  * Created by ssrinivasan on 3/25/2015.
 */
public class CartControllerTest {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private MockMvc mockMvc;

    private MockHttpSession session=new MockHttpSession();

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CartController controller;

    @Mock
    private CartService cartService;

    @Mock
    private ProductServiceV3 productServiceV3;

    @Mock
    private LocalPricingServiceV2 pricingService;

    @Mock
    private View mockView;

    @Mock
    private AppSessionInfo appSessionInfo;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).setSingleView(mockView).build();
        User user = new User();
        user.setVarId("1");
        user.setProgramId("1");
        user.setUserId("raji");
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(user.getUserId());
        shoppingCart.setVarId(user.getVarId());
        shoppingCart.setProgramId(user.getProgramId());
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testGetCart() throws Exception {
        User user = new User();
        user.setCountry(CommonConstants.COUNTRY_CODE_US);
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, new Cart());
        when(appSessionInfo.getSessionCart()).thenReturn(new Cart());
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram());

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.getCart(any(User.class),any(Cart.class), any(Program.class))).thenReturn(getCart());


        mockMvc.perform(get("/cart").session(session))
            .andExpect(status().isOk())
            .andReturn();;
    }

    @Test
    public void testGetCartWithOutPaymentOptionNull() throws Exception {
        User user = new User();
        user.setCountry(CommonConstants.COUNTRY_CODE_US);
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);

        Cart cart = getCart();

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, new Program());

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.getCart(any(User.class),any(Cart.class), any(Program.class))).thenReturn(getCart());
        when(appSessionInfo.getSessionCart()).thenReturn(cart);

        mockMvc.perform(get("/cart").session(session))
            .andExpect(status().isOk())
            .andReturn();;
    }

    @Test
    public void testGetCartWithOutPaymentOptionEmpty() throws Exception {
        User user = new User();
        user.setCountry(CommonConstants.COUNTRY_CODE_US);
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, new Cart());
        when(appSessionInfo.getSessionCart()).thenReturn(new Cart());
        Program program = new Program();
        program.setPayments(new ArrayList<>());
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.getCart(any(User.class),any(Cart.class), any(Program.class))).thenReturn(getCart());

        mockMvc.perform(get("/cart").session(session))
            .andExpect(status().isOk())
            .andReturn();;
    }
    @Test
    public void testGetCartWithOutPaymentOptionWorking() throws Exception {
        User user = new User();
        user.setCountry(CommonConstants.COUNTRY_CODE_US);
        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, new Cart());

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram());

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.getCart(any(User.class),any(Cart.class), any(Program.class))).thenReturn(getCart());

        Cart cart = getCart();
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);
        when(appSessionInfo.getSessionCart()).thenReturn(cart);

       final MvcResult result = mockMvc.perform(get("/cart").session(session))
            .andExpect(status().isOk())
            .andReturn();
        Assert.assertTrue(result.getResponse().getContentAsString().contains("{\"paymentMaxLimit\":{\"amount\":560.0," +
            "\"currencyCode\":\"CAD\",\"points\":80000},\"rewardsMinLimit\":{\"amount\":140" +
            ".0,\"currencyCode\":\"CAD\",\"points\":20000}"));
    }

    @Test
    public void testGetCartWhenSessionNotNull() throws Exception {

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());
        when(appSessionInfo.getSessionCart()).thenReturn(getCart());

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram());

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.getCart(any(User.class),any(Cart.class), any(Program.class))).thenReturn(getCart());

        MvcResult result =mockMvc.perform(get("/cart").session(session))
            .andExpect(status().isOk())
            .andReturn();

        Assert.assertNotNull(result.getResponse().getContentAsString());
    }


    @Test
    public void testGetCartFailedToLoadCart() throws Exception {

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.getCart(any(User.class), isNull(), isNull())).thenThrow(
            ServiceException.class);

        MvcResult mvcResult = mockMvc.perform(get("/cart").session(session))
            .andReturn();

        Assert.assertEquals(409, mvcResult.getResponse().getStatus());

    }

    @Test
    public void testGetCartEntityNotfound() throws Exception {

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.getCart(any(User.class), isNull(), isNull())).thenThrow(EntityNotFoundException.class);

        MvcResult mvcResult = mockMvc.perform(get("/cart").session(session))
            .andReturn();

        Assert.assertEquals(409, mvcResult.getResponse().getStatus());
        Assert.assertEquals("There are no items in the Cart ", mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testGetCartErrorLoadingCart() throws Exception {

        when(cartService.getCart(any(User.class), any(Cart.class), any(Program.class))).thenThrow(RuntimeException.class);
        MvcResult result =mockMvc.perform(get("/cart").session(session))  //Session/User modified flag will be set
            .andExpect(status().isInternalServerError())
            .andReturn();

        Assert.assertNotNull(result.getResponse().getContentAsString());
    }


    @Ignore
    @Test
    public void testAddToCart() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_DOLLAR_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(1000));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        AddToCartRequest cartRequest = new AddToCartRequest();
        cartRequest.setPsId("30001D2187Z/A");

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        when(productServiceV3.getAppleProductDetail(anyString(), anyObject(), anyBoolean(), anyObject(),
            anyBoolean(),anyBoolean(),anyBoolean(),anyBoolean()))
            .thenReturn(getProduct());

        when(cartService.addToCart(any(Cart.class), any(User.class), any(Program.class),
            any(Product.class), any(Product.class))).thenReturn(1L);

        mockMvc.perform(post("/cart/add/").session(session).contentType(MediaType.APPLICATION_JSON)
            .content(AppleUtil.asJsonString(cartRequest)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testAddToCartNoContent() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_DOLLAR_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(1000));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        AddToCartRequest cartRequest = new AddToCartRequest();
        cartRequest.setPsId("30001D2e187Z/A");

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(productServiceV3.getAppleProductDetail(anyString(), anyObject(), anyBoolean(), anyObject(),
            anyBoolean(),anyBoolean(),anyBoolean(),anyBoolean()))
            .thenReturn(null);

        MvcResult result = mockMvc.perform(post("/cart/add/").session(session).contentType(MediaType.APPLICATION_JSON)
            .content(AppleUtil.asJsonString(cartRequest)))
                .andExpect(status().is(204))
                .andReturn();

        Assert.assertEquals("No such product found for this psid/productId: 30001D2e187Z/A",
            result.getResponse().getContentAsString());

    }

    @Test
    public void testAddToCartThrowsException() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_DOLLAR_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(1000));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        AddToCartRequest cartRequest = new AddToCartRequest();
        cartRequest.setPsId("30001D2e187Z/A");

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        when(productServiceV3.getAppleProductDetail(anyString(), anyObject(), anyBoolean(), anyObject(),
            anyBoolean(),anyBoolean(),anyBoolean(),anyBoolean()))
            .thenThrow(RuntimeException.class);

        MvcResult result = mockMvc.perform(post("/cart/add/").session(session).contentType(MediaType.APPLICATION_JSON)
            .content(AppleUtil.asJsonString(cartRequest)))
            .andReturn();

        Assert.assertEquals(500, result.getResponse().getStatus());
        Assert.assertEquals("Failed to add item to cart. Please contact System Administrator ",
            result.getResponse().getContentAsString());
    }



    @Test
    public void testModifyCartQuantity() throws Exception {

        long cartItemId = 1L;
        Map<String,Integer> psidQuantities = new HashMap();
        psidQuantities.put(CartItemOption.QUANTITY.getValue(), 2);
        Gson gson = new Gson();
        String json = gson.toJson(psidQuantities);
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_DOLLAR_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(300));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), any(Program.class))).thenReturn
            (getCart());

        mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content
            (json)
            .session(session))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testModifyCartWithEmptyCart() throws Exception {

        long cartItemId = 1L;
        Map<String,Integer> psidQuantities = new HashMap();
        psidQuantities.put(CartItemOption.QUANTITY.getValue(), 2);
        Gson gson = new Gson();
        String json = gson.toJson(psidQuantities);

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), isNull())).thenReturn
            (new Cart());

        mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content
            (json)
            .session(session))
            .andExpect(status().isOk())
            .andReturn();
    }


    @Test
    public void testModifyCartWithInvalidData() throws Exception {

        long cartItemId = 1L;
        String json = "{'sdfsdfsdf'}";  // Invalid Json
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());

        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), any(Program.class))).thenReturn
            (getCart());

        mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content
            (json)
            .session(session))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void testModifyCartWithEmptyJson() throws Exception {

        long cartItemId = 1L;
        String json = "{}";  // empty Json

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());

        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), any(Program.class))).thenThrow(
            JSONException.class);

        mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content(json)
            .session(session))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    @Test
    public void testModifyCartWithJsonParseException() throws Exception {

        long cartItemId = 1L;
        String json = "{}";  // empty Json

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());

        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), any(Program.class))).thenThrow(
            JsonParseException.class);

        mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content(json)
            .session(session))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }


    @Test
    public void testModifyCartWithMessageExceedLength() throws Exception {

        long cartItemId = 1L;
        String json = "{}";  // empty Json

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), isNull())).thenThrow(
            RequestValidationException.class);

        mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content(json)
            .session(session))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void testModifyCartItemNotFound() throws Exception {

        long cartItemId = 1L;
        Map<String, Integer> psidQuantities = new HashMap();
        psidQuantities.put(CartItemOption.QUANTITY.getValue(), 2);
        Gson gson = new Gson();
        String json = gson.toJson(psidQuantities);
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), isNull())).thenThrow
            (NoSuchElementException.class);

        MvcResult result = mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .session(session))
            .andReturn();

        Assert.assertEquals(204, result.getResponse().getStatus());
        Assert.assertEquals("Item not found in Cart anymore ", result.getResponse().getContentAsString());
    }

    @Test
    public void testModifyCartFailedToLoadCartFromSession() throws Exception {

        long cartItemId = 1L;
        String json = "{}";  // empty Json
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());


        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), any(Program.class))).thenThrow(
            RuntimeException.class);

        MvcResult result=mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content(json)
                .session(session))
                .andReturn();

        Assert.assertEquals("Exception in Modifying CartItem... 1", result.getResponse().getContentAsString());
    }

    @Ignore
    @Test
    public void testModifyCartMessageContainsNaughtyWords() throws Exception {

        long cartItemId = 1L;
        String json = "{}";
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());

        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), any(Program.class))).thenThrow(
            DataException.class);

        MvcResult result=mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content(json)
            .session(session))
            .andReturn();

        Assert.assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    public void testModifyCartNoItemInCart() throws Exception {

        long cartItemId = 1L;
        String json = "{}";
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());
        when(appSessionInfo.currentUser()).thenReturn(new User());

        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(Map.class), isNull()))
            .thenThrow(EntityNotFoundException.class);

        MvcResult result = mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content(json)
            .session(session))
            .andReturn();

        Assert.assertEquals(409, result.getResponse().getStatus());
    }


    @Test
    public void testModifyCartInvalidQuantity() throws Exception {

        long cartItemId = 1L;
        String json = "{}";
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());

        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), isNull())).thenThrow(
            new ServiceException(ServiceExceptionEnums.QUANTITY_INVALID_EXCEPTION));

        MvcResult result=mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content(json)
            .session(session))
            .andReturn();

        Assert.assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    public void testModifyCartFailedToLoadCart() throws Exception {

        long cartItemId = 1L;
        String json = "{}";
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());
        when(appSessionInfo.currentUser()).thenReturn(new User());
        when(cartService.modifyCart(any(Cart.class), any(User.class), any(Long.class), any(HashMap.class), isNull())).thenThrow(
            ServiceException.class);

        MvcResult result = mockMvc.perform(post("/cart/modify/" + cartItemId).contentType(MediaType.APPLICATION_JSON).content(json)
            .session(session))
            .andReturn();

        Assert.assertEquals(409, result.getResponse().getStatus());
    }



    @Test
    public void testEmptyCart() throws Exception {

        mockMvc.perform(get("/cart/emptyCart").session(session))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void testEmptyCartEntityNotFoundException() throws Exception {

        doThrow(new EntityNotFoundException("Exception while emptying Cart ")).when(cartService).emptyCart(anyObject());

        mockMvc.perform(get("/cart/emptyCart").session(session))
            .andExpect(status().isInternalServerError())
            .andReturn();

    }

    @Ignore
    public void testGetCreditCardCostForPoints() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_POINTS_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(80));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);


        Price price=new Price(Money.of(CurrencyUnit.USD,1614.77),248427);
        CartTotal cartTotal=new CartTotal();
        cartTotal.setPrice(price);
        Cart cart=getCart();
        cart.setPointPurchaseRate(BigDecimal.valueOf(0.006500000035750000));
        cart.setCartTotal(cartTotal);

        final SupplementaryPaymentLimit supplementaryPaymentLimit = new SupplementaryPaymentLimit();
        supplementaryPaymentLimit.setPaymentMaxLimit(new Price(10000.00d, CurrencyUnit.USD.getCode(),
            Integer.valueOf(500000)));

        cart.setSupplementaryPaymentLimit(supplementaryPaymentLimit);

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock)
                throws Throwable {
                Cart cart =(Cart)invocationOnMock.getArguments()[0];
                cart.setCost(1291.81);
                //user.getProgram().getAdds().setBuyPoints(198740);
                return null;
            }
        }).when(pricingService).calculateCartPrice(any(), any(), any(), any());

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);

        MvcResult result=mockMvc.perform(post("/cart/ccDollarValue/198740").session(session))
            .andExpect(status().isOk()).andReturn();

        Assert.assertEquals("1291.81",result.getResponse().getContentAsString());
    }


    @Test
    @Ignore
    // TODO. Need to fix this test case - Ranjith
    public void testGetCreditCardCostForPointsExceedingSupplementRange() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_POINTS_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(80));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        Price price=new Price(Money.of(CurrencyUnit.USD,1314.77),248427);
        CartTotal cartTotal=new CartTotal();
        cartTotal.setPrice(price);
        Cart cart=getCart();
        cart.setPointPurchaseRate(new BigDecimal(0.006500000035750000));
        cart.setCartTotal(cartTotal);

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock)
                throws Throwable {
                Cart cart =(Cart)invocationOnMock.getArguments()[0];
                cart.setCost(1291.82);
                //cart.setAddPoints(198743);
                return null;
            }
        }).when(pricingService).calculateCartPrice(any(), any(), any());

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);

        final MvcResult result=mockMvc.perform(post("/cart/ccDollarValue/198740").session(session))
            .andExpect(status().isBadRequest()).andReturn();

        Assert.assertEquals("1291.81", result.getResponse().getContentAsString());
    }


    @Test
    public void testGetCreditCardWithSupplementPaymentInactive() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_DOLLAR_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(1000));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        Price price=new Price(Money.of(CurrencyUnit.USD,500.00),500);
        CartTotal cartTotal=new CartTotal();
        cartTotal.setPrice(price);
        Cart cart=getCart();
        cart.setCartTotal(cartTotal);

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);
        when(appSessionInfo.currentUser()).thenReturn(new User());

        mockMvc.perform(get("/cart/ccDollarValue/500").session(session))
            .andExpect(status().isOk()).andReturn();
    }

    @Test
    @Ignore
    // TODO. Need to fix this test case - Ranjith
    public void testGetCreditCardWithForDollarsExceedingSupplementRange() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_DOLLAR_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(300));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        Price price=new Price(Money.of(CurrencyUnit.USD,1614.77),248427);
        CartTotal cartTotal=new CartTotal();
        cartTotal.setPrice(price);
        Cart cart=getCart();
        cart.setPointPurchaseRate(new BigDecimal(1.00));
        cart.setCartTotal(cartTotal);
        cart.setCost(301);

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock)
                throws Throwable {
                Cart cart =(Cart)invocationOnMock.getArguments()[0];
                cart.setCost(1291.82);
                //user.getProgram().getAdds().setBuyPoints(198743);
                return null;
            }
        }).when(pricingService).calculateCartPrice(any(), any(), any());

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);

        final MvcResult result=mockMvc.perform(post("/cart/ccDollarValue/1300").session(session))
            .andExpect(status().isBadRequest()).andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString()
            .contains("CcSlider Supplemental DOLLAR verification did not satisfy. Slider amount"));
    }

    @Test
    public void testGetCreditCardWithForDollarsNotExceedingSupplementRange() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_DOLLAR_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(300));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        Price price=new Price(Money.of(CurrencyUnit.USD,1614.77),248427);
        CartTotal cartTotal=new CartTotal();
        cartTotal.setPrice(price);
        Cart cart=getCart();
        cart.setPointPurchaseRate(new BigDecimal(1.00));
        cart.setCartTotal(cartTotal);
        cart.setCost(299);

        when(appSessionInfo.currentUser()).thenReturn(new User());
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock)
                throws Throwable {
                Cart cart =(Cart)invocationOnMock.getArguments()[0];
                cart.setCost(1291.82);
                //user.getProgram().getAdds().setBuyPoints(198743);
                return null;
            }
        }).when(pricingService).calculateCartPrice(any(), any(), any());

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);

        final MvcResult result=mockMvc.perform(get("/cart/ccDollarValue/1300").session(session))
            .andExpect(status().isOk()).andReturn();

    }


    @Test
    public void testGetCreditCardCostForPointsWithoutCart () throws Exception {

        when(appSessionInfo.currentUser()).thenReturn(new User());
        mockMvc.perform(get("/cart/ccDollarValue/500").session(session))
            .andExpect(status().isBadRequest()).andReturn();
    }


    @Test
    public void testGetCreditCardCostForNegativePoint() throws Exception {

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());
        when(appSessionInfo.currentUser()).thenReturn(new User());

        mockMvc.perform(get("/cart/ccDollarValue/-500").session(session))
            .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    public void testGetCreditCardException() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_DOLLAR_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(1000));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

        Price price=new Price(Money.of(CurrencyUnit.USD,1614.77),248427);
        CartTotal cartTotal=new CartTotal();
        cartTotal.setPrice(price);
        Cart cart=getCart();
        cart.setPointPurchaseRate(new BigDecimal(1.00));
        cart.setCartTotal(cartTotal);

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);

        when(appSessionInfo.currentUser()).thenReturn(new User());
        doThrow(new EntityNotFoundException("Exception while emptying Cart")).when(cartService)
            .callPricingServiceOnCreditCardCostForPoints(anyObject(), anyObject(), anyObject(), anyObject());

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, getCart());
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram());

        mockMvc.perform(get("/cart/ccDollarValue/500").session(session))
            .andExpect(status().isInternalServerError()).andReturn();
    }

    private static Product getProduct(){
        Product product=new Product();
        product.setPsid("30001D2187Z/A");
        product.setName("Dummy product name");

        return product;
    }

    private static Cart getCart(){

        Cart cart =new Cart();
        CartTotal cartTotal=new CartTotal();
        Price price=new Price(700.0,"CAD",100000);

        cartTotal.setPrice(price);
        cart.setCartTotal(cartTotal);
        cart.setDisplayCartTotal(cartTotal);
        List cartItems=new ArrayList<CartItem>();
        CartItem cartItem = new CartItem();
        cartItem.setSupplierId(200);
        cartItems.add(cartItem);
        cart.setCartItems(cartItems);
        SupplementaryPaymentLimit supplementaryPaymentLimit = new SupplementaryPaymentLimit();
        supplementaryPaymentLimit.setRewardsMinLimit(new Price(Double.valueOf(140), "CAD", 20000));
        supplementaryPaymentLimit.setPaymentMaxLimit(new Price(Double.valueOf(560), "CAD", 80000));
        cart.setSupplementaryPaymentLimit(supplementaryPaymentLimit);

        return cart;
    }

    private static Program getProgram(){

        final Program program=new Program();
        program.setVarId("1");
        program.setProgramId("b2s_qa_only");
        final List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions =new ArrayList<>();
        final com.b2s.rewards.apple.model.PaymentOption paymentOption =new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentLimitType("P");
        paymentOption.setSupplementaryPaymentMaxLimit(80);
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);
        Map map=new HashMap<>();
        map.put("catalogId","apple");
        program.setConfig(map);
        return program;
    }

    @Ignore
    public void testGetCreditCardCostForPointsOnChange() throws Exception {

        Program program = new Program();
        com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("POINTS");
        paymentOption.setSupplementaryPaymentType("VARIABLE");
        paymentOption.setSupplementaryPaymentLimitType(Constant.SUPPLEMENTAL_POINTS_RANGE);
        paymentOption.setSupplementaryPaymentMaxLimit(Integer.valueOf(80));
        List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);


        Price price=new Price(Money.of(CurrencyUnit.USD,1614.77),248427);
        CartTotal cartTotal=new CartTotal();
        cartTotal.setPrice(price);
        Cart cart=getCart();
        cart.setPointPurchaseRate(BigDecimal.valueOf(0.006500000035750000));
        cart.setCartTotal(cartTotal);

        final SupplementaryPaymentLimit supplementaryPaymentLimit = new SupplementaryPaymentLimit();
        supplementaryPaymentLimit.setPaymentMaxLimit(new Price(10000.00d, CurrencyUnit.USD.getCode(),
            Integer.valueOf(500000)));

        cart.setSupplementaryPaymentLimit(supplementaryPaymentLimit);

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock)
                    throws Throwable {
                Cart cart =(Cart)invocationOnMock.getArguments()[0];
                cart.setCost(1291.81);
                return null;
            }
        }).when(pricingService).calculateCartPrice(any(), any(), any(), any());

        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);

        MvcResult result=mockMvc.perform(post("/cart/ccDollarValueOnChange/198740").session(session))
                .andExpect(status().isOk()).andReturn();

        Assert.assertEquals("1291.81",result.getResponse().getContentAsString());
    }


}
