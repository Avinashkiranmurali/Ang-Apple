package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.PricingLogEntity;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author rkumar 2019-12-06
 */
@Repository
@Transactional
public class PricingLogImpl extends BaseDaoWrapper<PricingLogEntity, Long> implements PricingLogDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void insert(final PricingLogEntity pricingLogEntity) throws HibernateException {
        final Session session = sessionFactory.getCurrentSession();
        session.save(pricingLogEntity);
    }
}
