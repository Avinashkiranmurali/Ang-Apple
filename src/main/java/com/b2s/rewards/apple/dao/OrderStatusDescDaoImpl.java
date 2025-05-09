package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderLineStatusEntity;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/*** Created by srukmagathan on 9/17/2016.
 */
@Repository(value = "orderStatusDescDao")
@Transactional
public class OrderStatusDescDaoImpl extends BaseDaoWrapper<OrderLineStatusEntity, Integer> implements OrderStatusDescDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusDescDaoImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    @Override
    public String getDescByStatusId(final int statusId) {
        final Query query = sessionFactory.getCurrentSession().createSQLQuery("select [desc] from order_status where status_Id=?");
        query.setParameter(1, statusId);
        try{
            return query.uniqueResult().toString();
        } catch (Exception e){
            LOGGER.error("Error occurred while retrieving information from Order_status table ", e);
        }
        return null;
    }
}
