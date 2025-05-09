package com.b2s.common.services.pricing.transformers;

import com.b2s.rewards.model.*;

/**
* @author jkattookaren Created on 9/23/2014.
*/
class ShoppingCartItemBuilder {

    public static final ShoppingCartItem GENERIC_SHOPPING_CART_ITEM = builder().build();

    public static final ShoppingCartItem GENERIC_SHOPPING_CART_ITEM_WITH_IN_STORE_SHIPPING = builder()
            .withShippingMethod(ShippingMethod.INSTOREPICKUP)
            .withProduct(ProductBuilder.GENERIC_PRODUCT)
            .build();

    public static final ShoppingCartItem GENERIC_SHOP_CART_ITEM_WITH_RESERVE_PICKUP_SHIP = builder()
            .withShippingMethod(ShippingMethod.RESERVE_AND_PICKUP)
            .withProduct(ProductBuilder.GENERIC_PRODUCT)
            .build();

    public static final ShoppingCartItem GENERIC_SHOPPING_CART_ITEM_WITH_STANDARD_SHIPPING = builder()
            .withShippingMethod(ShippingMethod.STANDARD)
            .withProduct(ProductBuilder.GENERIC_PRODUCT)
            .build();

    private ShippingMethod shippingMethod;
    private Address address;
    private Store selectedStore;
    private Product product;
    private Integer quantity;

    ShoppingCartItemBuilder() {
        quantity = Integer.valueOf(1);
    }

    public static ShoppingCartItemBuilder builder() {
        return new ShoppingCartItemBuilder()
                .withShippingMethod(ShippingMethod.STANDARD)
                .withGenericAddressInStateOrProvince("S1")
                .withGenericStoreInStateOrProvince("S2")
                .withProduct(ProductBuilder.merchandise().withPsid("psid").build());
    }

    public ShoppingCartItem build() {
        final ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        shoppingCartItem.setShippingMethod(shippingMethod);
        shoppingCartItem.setAddress(address);
        shoppingCartItem.setSelectedStore(selectedStore);
        shoppingCartItem.setProduct(product);
        shoppingCartItem.setQuantity(quantity);
        return shoppingCartItem;
    }

    public ShoppingCartItemBuilder withShippingMethod(final ShippingMethod shippingMethodFrom) {
        this.shippingMethod = shippingMethodFrom;
        return this;
    }

    public ShoppingCartItemBuilder withGenericAddressInStateOrProvince(final String stateCode) {
        this.address = createGenericAddressInStateOrProvince(stateCode);
        return this;
    }

    public ShoppingCartItemBuilder withGenericStoreInStateOrProvince(final String stateCode) {
        this.selectedStore = createGenericStoreInStateOrProvince(stateCode);
        return this;
    }

    public ShoppingCartItemBuilder withProduct(final Product productFrom) {
        this.product = productFrom;
        return this;
    }

    public ShoppingCartItemBuilder withQuantity(final Integer quantityFrom) {
        this.quantity = quantityFrom;
        return this;
    }

    private Address createGenericAddressInStateOrProvince(final String stateCode) {
        final Address addressFrom = new Address();
        addressFrom.setState(stateCode);
        return addressFrom;
    }

    private Store createGenericStoreInStateOrProvince(final String stateCode) {
        final Store store = new Store();
        store.setState(stateCode);
        return store;
    }

}
