package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.ShoppingCart;
import com.b2s.rewards.apple.model.ShoppingCartItem;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Repository("appleShoppingCartItemDao")
@Transactional
public class ShoppingCartItemDaoImpl extends BaseDaoWrapper<ShoppingCartItem, Long> implements ShoppingCartItemDao {

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<ShoppingCartItem> getShoppingCartItems(ShoppingCart cart) throws DataAccessException {
        Query query = sessionFactory.getCurrentSession().createNativeQuery("select * from shopping_cart_items WITH (NOLOCK) where shopping_Cart_id =:cartId order by added_date desc", ShoppingCartItem.class);
        query.setParameter("cartId", cart.getId());
        return query.list();
    }

    @Override
    public void update(ShoppingCartItem cartItem) throws HibernateException {
        Session session = sessionFactory.getCurrentSession();
        cartItem.setAddedDate(new Date());
        session.saveOrUpdate(cartItem);
    }


    @Override
    public void deleteByProductId(final List<String> productIdList, final String varId, final String programId,
        final String userId) throws HibernateException {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createNativeQuery("DELETE from shopping_cart_items where product_id in (:productIds) " +
            "and shopping_cart_id in (select id from shopping_cart WITH (NOLOCK) where var_id=:varId and " +
            "program_id=:programId and user_id=:userId)");
        query.setParameter("productIds", productIdList);
        query.setParameter("varId", varId);
        query.setParameter("programId", programId);
        query.setParameter("userId", userId);
        query.executeUpdate();
    }
}

