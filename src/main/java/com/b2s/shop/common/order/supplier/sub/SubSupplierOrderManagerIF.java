package com.b2s.shop.common.order.supplier.sub;

import com.b2s.db.model.Order;
import com.b2s.shop.common.User;
import com.b2s.rewards.model.Product;
import com.b2s.rewards.model.ShoppingCart;

import javax.servlet.http.HttpServletRequest;

public interface SubSupplierOrderManagerIF {

    boolean placeOrder(ShoppingCart cart, Order order, User user);

    Product getProduct(String productId, User user);

    Product getProduct(String productId, User user, HttpServletRequest request);

    Product getProduct(ShoppingCart cart, User user);

    // convert order status to b2s status id
    int getOrderStatus(int status);

    String setSessionCart(HttpServletRequest request);

    boolean postOrderUpdate(ShoppingCart cart, Order order, User user);

}
