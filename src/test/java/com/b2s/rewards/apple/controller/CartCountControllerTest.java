package com.b2s.rewards.apple.controller;


import com.b2s.rewards.apple.model.ShoppingCart;
import com.b2s.rewards.apple.model.ShoppingCartItem;
import com.b2s.shop.common.User;
import com.b2s.apple.services.CartService;
import com.b2s.apple.services.ProgramService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CartCountControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    private CartCountController controller;

    @Mock
    private CartService cartService;

    @Mock
    private View mockView;

    @Mock
    private ProgramService programService;

    @Before
    public void setup()
        throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).setSingleView(mockView).build();
    }

    @Test
    public void testNonEmptyCartCount()
        throws Exception {
        final ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId("alex");
        shoppingCart.setVarId("FDR");
        shoppingCart.setProgramId("b2s_qa_only");
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        shoppingCartItem.setId(new Long(1));
        final List<ShoppingCartItem> shopListIteam = new ArrayList<ShoppingCartItem>();
        shopListIteam.add(shoppingCartItem);
        shoppingCart.setShoppingCartItems(shopListIteam);
        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class)))
            .thenReturn(Optional.of("true"));
        when(cartService.getShoppingCart(any(User.class))).thenReturn(shoppingCart);
        mockMvc
            .perform(get("/{varId}/{programId}/{userId}/cart/count?callback=jsfunction", "FDR", "b2s_qa_only", "alex"))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("jsfunction(1)"));
    }

    // To test http status as 404 if var not available
    @Test
    public void testForbiddenStatus()
        throws Exception {
        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class)))
            .thenReturn(Optional.of("false"));

        Mockito.when(cartService.getShoppingCart(any(User.class))).thenReturn(new ShoppingCart());
        mockMvc
            .perform(get("/{varId}/{programId}/{userId}/cart/count?callback=jsfunction", "test", "b2s_qa_only", "alex"))
            .andExpect(status().isForbidden());

    }

    @Test
    public void testEmptyCartCount()
        throws Exception {
        final ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId("alex");
        shoppingCart.setVarId("FDR");
        shoppingCart.setProgramId("b2s_qa_only");
        final ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        shoppingCartItem.setId(new Long(1));
        final List<ShoppingCartItem> shopListIteam = new ArrayList<ShoppingCartItem>();
        shopListIteam.add(shoppingCartItem);
        shoppingCart.setShoppingCartItems(shopListIteam);
        when(programService.getProgramConfigValue(any(String.class), any(String.class), any(String.class)))
            .thenReturn(Optional.of("true"));
        when(cartService.getShoppingCart(any(User.class))).thenReturn(new ShoppingCart());
        mockMvc
            .perform(get("/{varId}/{programId}/{userId}/cart/count?callback=jsfunction", "FDR", "b2s_qa_only", "alex"))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("jsfunction(0)"));
    }

}
