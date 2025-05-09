package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderAdditionalInfoEntity;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * @author rjesuraj Date : 8/28/2019 Time : 7:59 PM
 */
@Repository
@Transactional
public class OrderAdditionalInfoDaoImpl extends BaseDaoWrapper<OrderAdditionalInfoEntity, Long> implements OrderAdditionalInfoDao {
    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }
}
