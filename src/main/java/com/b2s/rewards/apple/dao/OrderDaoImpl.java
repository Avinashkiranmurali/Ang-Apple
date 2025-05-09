package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderEntity;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class OrderDaoImpl extends BaseDaoWrapper<OrderEntity,Long> implements OrderDao {

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }
}
