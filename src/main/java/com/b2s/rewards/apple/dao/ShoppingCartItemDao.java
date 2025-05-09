package com.b2s.rewards.apple.dao;


import com.b2s.rewards.apple.model.ShoppingCart;
import com.b2s.rewards.apple.model.ShoppingCartItem;
import com.b2s.rewards.dao.BaseDao;

import java.util.List;

/**
 * Created by rperumal on 9/9/2015.
 */

public interface ShoppingCartItemDao extends BaseDao<ShoppingCartItem, Long>{

    /**
     * get all shopping cart items for the user
     *
     * @return list of shopping cart items
     */
    public List<ShoppingCartItem> getShoppingCartItems(ShoppingCart cart);
    public void update(ShoppingCartItem cartItem);
    void deleteByProductId(List<String> productIdList, String varId, String programId, String userId);
}

