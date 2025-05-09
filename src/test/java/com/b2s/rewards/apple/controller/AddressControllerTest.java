package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.apple.services.CartAddressService;
import com.b2s.rewards.model.ShoppingCart;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.View;

import java.lang.invoke.MethodHandles;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Created by rperumal on 8/20/2015.
 */

public class AddressControllerTest {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private MockHttpSession session=new MockHttpSession();

    private MockMvc mockMvc;

    @InjectMocks
    private AddressController addressController;

    @Mock
    private CartAddressService addressService;

    @Mock
    private AppSessionInfo appSessionInfo;

    @Mock
    private View mockView;



    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        mockMvc = standaloneSetup(addressController)
            .setSingleView(mockView)
            .build();

        User user = new User();
        user.setVarId("1");
        user.setProgramId("bkndtest");
        user.setUserId("rperumal");

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(user.getUserId());
        shoppingCart.setVarId(user.getVarId());
        shoppingCart.setProgramId(user.getProgramId());

        Program programConfig = new Program();
        Map<String,Object> mapConfig = new HashMap<>();
        mapConfig.put("disableCartTotalModifiedPopUp",true);
        programConfig.setConfig(mapConfig);

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, user);
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, programConfig);
        final Cart cart = new Cart();
        session.setAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT, cart);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetStates() throws Exception {
        when(appSessionInfo.currentUser()).thenReturn(new User());
        mockMvc.perform(get("/address/getStates").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").exists())
            .andReturn();
    }

    /**
     * When cart is empty, address will be taken from user object
     * @throws Exception
     */
    @Test
    public void testGetAddressWhenCartIsEmpty() throws Exception {
        when(appSessionInfo.currentUser()).thenReturn(new User());
        mockMvc.perform(get("/address/getCartAddress").session(session))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testAddressFromCart() throws Exception {

        when(addressService.getAddress(any(Cart.class), any(User.class), any(Program.class))).thenReturn(new Address());
        when(appSessionInfo.currentUser()).thenReturn(new User());
        //get cart address
        mockMvc.perform(get("/address/getCartAddress").session(session))
            .andExpect(status().isOk())
            .andReturn();

    }


    @Test
    public void testModifyCartAddressWithValidAddress() throws Exception {

        Cart cart=new Cart();
        cart.setIgnoreSuggestedAddress("true");
        cart.getNewShippingAddress().setIgnoreSuggestedAddress("true");
        cart.getShippingAddress().setErrorMessage(new HashMap<>());
        cart.getShippingAddress().setWarningMessage(new HashMap<>());
        when(addressService.updateShippingInformation(any(Cart.class), any(Map.class), any(User.class),
            any(Program.class)))
            .thenReturn(cart);

        Address newAddress = populateAddress();
        newAddress.setAddress1("Modified");
        final Map<String,Address> newAddressInfo= new HashMap<>();
        newAddressInfo.put("shippingAddress", newAddress);
        when(appSessionInfo.currentUser()).thenReturn(new User());
        Gson gson = new Gson();
        mockMvc.perform(post("/address/modifyCartAddress/").contentType(MediaType.APPLICATION_JSON).content(
            gson.toJson(newAddressInfo)).session(session))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testModifyCartAddressIgnoreAddressFalse() throws Exception {

        Cart cart=new Cart();
        when(addressService.updateShippingInformation(any(Cart.class), any(Map
                .class), any(User.class), any(Program.class)))
                .thenReturn(cart);
        cart.getNewShippingAddress().setIgnoreSuggestedAddress("false");
        cart.getShippingAddress().setErrorMessage(new HashMap<>());
        cart.getShippingAddress().setWarningMessage(new HashMap<>());


        Address newAddress = populateAddress();
        newAddress.setAddress1("Modified");
        final Map<String,Address> newAddressInfo= new HashMap<>();
        newAddressInfo.put("shippingAddress", newAddress);
        when(appSessionInfo.currentUser()).thenReturn(new User());

        Gson gson = new Gson();
        mockMvc.perform(post("/address/modifyCartAddress/").contentType(MediaType.APPLICATION_JSON).content(
                gson.toJson(newAddressInfo)).session(session))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testModifyCartAddressIgnoreAddressTrue() throws Exception {

        Cart cart=new Cart();
        when(addressService.updateShippingInformation(any(Cart.class), any(Map
                .class), any(User.class), any(Program.class)))
                .thenReturn(cart);
        cart.setIgnoreSuggestedAddress("true");
        cart.getNewShippingAddress().setIgnoreSuggestedAddress("true");
        cart.getShippingAddress().setErrorMessage(new HashMap<>());
        cart.getShippingAddress().setWarningMessage(new HashMap<>());


        Address newAddress = populateAddress();
        newAddress.setAddress1("Modified");
        final Map<String,Address> newAddressInfo= new HashMap<>();
        newAddressInfo.put("shippingAddress", newAddress);
        when(appSessionInfo.currentUser()).thenReturn(new User());

        Gson gson = new Gson();
        mockMvc.perform(post("/address/modifyCartAddress/").contentType(MediaType.APPLICATION_JSON).content(
                gson.toJson(newAddressInfo)).session(session))
                .andExpect(status().isOk())
                .andReturn();
    }
    @Test
    public void testModifyCartAddressWithInValidAddress() throws Exception {

        Cart cart=new Cart();
        cart.setAddressError(true);
        Map ermap=new HashMap();
        ermap.put("error", "Invalid address");
        cart.getShippingAddress().setErrorMessage(ermap);

        when(addressService.updateShippingInformation(any(Cart.class), any(Map
            .class), any(User.class), any(Program.class)))
            .thenReturn(cart);
        when(appSessionInfo.currentUser()).thenReturn(new User());

        Address newAddress = populateAddress();
        newAddress.setAddress1("");  //invalid address
        LinkedTreeMap map = new LinkedTreeMap();
        map.put(CartItemOption.SHIPPING_ADDRESS.getValue(), newAddress);
        Gson gson = new Gson();
        mockMvc.perform(post("/address/modifyCartAddress/").contentType(MediaType.APPLICATION_JSON).content(
            gson.toJson(map)).session(this.session))
            .andExpect(status().isBadRequest())
            .andReturn();

    }


    @Test
    public void testModifyCartAddressMelissaDown() throws Exception {

        when(addressService.updateShippingInformation(any(Cart.class), any(Map
            .class), any(User.class), any(Program.class)))
            .thenThrow(UnknownHostException.class);

        Address newAddress = populateAddress();
        newAddress.setAddress1("");  //invalid address

        LinkedTreeMap map = new LinkedTreeMap();
        map.put(CartItemOption.SHIPPING_ADDRESS.getValue(), newAddress);
        when(appSessionInfo.currentUser()).thenReturn(new User());

        Gson gson = new Gson();
        mockMvc.perform(post("/address/modifyCartAddress/").contentType(MediaType.APPLICATION_JSON).content(
            gson.toJson(map)).session(session))
            .andExpect(status().isServiceUnavailable())
            .andReturn();

    }

    private Address populateAddress() {
        //Modify Address
        Address newAddress = new Address();
        newAddress.setAddress1("101 A YOUNG INTERNATIONAL BOULEVARD");
        newAddress.setCity("Atlanta");
        newAddress.setState("GA");
        newAddress.setZip5("30303");
        newAddress.setCountry("USA");
        newAddress.setPhoneNumber("1234567890");
        newAddress.setEmail("abc@b2s.com");

        return newAddress;
    }

    @Test
    public void testModifyCartAddressGetWarningFromMelissaWithIgnoreSuggesstionFalse() throws Exception {

        Cart cart=new Cart();
        when(addressService.updateShippingInformation(any(Cart.class), any(Map
                .class), any(User.class), any(Program.class)))
                .thenReturn(cart);
        cart.setIgnoreSuggestedAddress("true");
        cart.getNewShippingAddress().setIgnoreSuggestedAddress("false");
        cart.getShippingAddress().setErrorMessage(new HashMap<>());
        cart.getShippingAddress().setWarningMessage(new HashMap<>());


        Address newAddress = populateWarningAddress();
        newAddress.setAddress1("Modified");
        final Map<String,Address> newAddressInfo= new HashMap<>();
        newAddressInfo.put("shippingAddress", newAddress);

        when(appSessionInfo.currentUser()).thenReturn(new User());
        Gson gson = new Gson();
        mockMvc.perform(post("/address/modifyCartAddress/").contentType(MediaType.APPLICATION_JSON).content(
                gson.toJson(newAddressInfo)).session(session))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testModifyCartAddressGetWarningFromMelissaWithIgnoreSuggesstionTrue() throws Exception {

        Cart cart=new Cart();
        when(addressService.updateShippingInformation(any(Cart.class), any(Map
                .class), any(User.class), any(Program.class)))
                .thenReturn(cart);
        cart.setIgnoreSuggestedAddress("true");
        cart.getNewShippingAddress().setIgnoreSuggestedAddress("true");
        cart.getShippingAddress().setErrorMessage(new HashMap<>());
        cart.getShippingAddress().setWarningMessage(new HashMap<>());


        Address newAddress = populateWarningAddress();
        newAddress.setAddress1("Modified");
        final Map<String,Address> newAddressInfo= new HashMap<>();
        newAddressInfo.put("shippingAddress", newAddress);
        when(appSessionInfo.currentUser()).thenReturn(new User());

        Gson gson = new Gson();
        mockMvc.perform(post("/address/modifyCartAddress/").contentType(MediaType.APPLICATION_JSON).content(
                gson.toJson(newAddressInfo)).session(session))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testModifyCartAddressGetErrorFromMelissa() throws Exception {

        Cart cart=new Cart();
        when(addressService.updateShippingInformation(any(Cart.class), any(Map
                .class), any(User.class), any(Program.class)))
                .thenReturn(cart);
        cart.setIgnoreSuggestedAddress("true");
        cart.getNewShippingAddress().setIgnoreSuggestedAddress("false");
        cart.getShippingAddress().setErrorMessage(new HashMap<>());
        cart.getShippingAddress().setWarningMessage(new HashMap<>());


        Address newAddress = populateWarningAddress();

        newAddress.setCity("west mambalam,chennai");
        newAddress.setState("TN");
        newAddress.setZip5("6033");

        newAddress.setAddress1("Modified");
        final Map<String,Address> newAddressInfo= new HashMap<>();
        newAddressInfo.put("shippingAddress", newAddress);
        when(appSessionInfo.currentUser()).thenReturn(new User());

        Gson gson = new Gson();
        mockMvc.perform(post("/address/modifyCartAddress/").contentType(MediaType.APPLICATION_JSON).content(
                gson.toJson(newAddressInfo)).session(session))
                .andExpect(status().isOk())
                .andReturn();
    }
    private Address populateWarningAddress() {

        //Modify Address
        Address newAddress = new Address();
        newAddress.setAddress1("Elliott Ave");
        newAddress.setCity("Parkville ");
        newAddress.setState("NT");
        newAddress.setZip5("3052");
        newAddress.setCountry("AU");
        newAddress.setPhoneNumber("1234567890");
        newAddress.setEmail("abc@b2s.com");

        return newAddress;
    }
}
