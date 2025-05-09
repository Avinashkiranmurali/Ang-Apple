package com.b2s.common.services.pricing.transformers;

import com.b2s.rewards.model.Address;
import com.b2s.rewards.model.ShoppingCart;
import com.b2s.rewards.model.ShoppingCartItem;

import java.util.ArrayList;
import java.util.List;

/**
* @author jkattookaren Created on 9/23/2014.
*/
public class ShoppingCartBuilder {

    public static final ShoppingCart EMPTY_SHOPPING_CART = ShoppingCartBuilder.builder().build();

    public static final ShoppingCart GENERIC_SHOPPING_CART = ShoppingCartBuilder.builder()
            .addShoppingCartItem(ShoppingCartItemBuilder.GENERIC_SHOPPING_CART_ITEM)
            .build();

    public static final ShoppingCart GENERIC_SHOPPING_CART_WITH_STANDARD_SHIPPING = ShoppingCartBuilder.builder()
            .addShoppingCartItem(ShoppingCartItemBuilder.GENERIC_SHOPPING_CART_ITEM_WITH_STANDARD_SHIPPING)
            .build();

    public static final ShoppingCart GENERIC_SHOPPING_CART_WITH_IN_STORE_SHIPPING = builder()
            .addShoppingCartItem(ShoppingCartItemBuilder.GENERIC_SHOPPING_CART_ITEM_WITH_IN_STORE_SHIPPING)
            .build();

    public static final ShoppingCart GENERIC_SHOP_CART_WITH_RESERVE_AND_PICKUP_SHIP = builder()
            .addShoppingCartItem(ShoppingCartItemBuilder.GENERIC_SHOP_CART_ITEM_WITH_RESERVE_PICKUP_SHIP)
            .build();

    private final List<ShoppingCartItem> shoppingCartItems = new ArrayList<>();
    private Address shippingAddress;

    ShoppingCartBuilder() {
    }

    public static ShoppingCartBuilder builder() {
        return new ShoppingCartBuilder()
                .withShippingAddressInState("S0");
    }

    public ShoppingCart build() {
        final ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setShoppingCartItems(shoppingCartItems);
        shoppingCart.setShippingAddress(shippingAddress);
        return shoppingCart;
    }

    public ShoppingCartBuilder addShoppingCartItem(final ShoppingCartItem shoppingCartItem) {
        this.shoppingCartItems.add(shoppingCartItem);
        return this;
    }

    public ShoppingCartBuilder withShippingAddressInState(final String stateCode) {
        this.shippingAddress = createGenericAddressInStateOrProvince(stateCode);
        return this;
    }

    private Address createGenericAddressInStateOrProvince(final String stateCode) {
        final Address address = new Address();
        address.setState(stateCode);
        return address;
    }

}
