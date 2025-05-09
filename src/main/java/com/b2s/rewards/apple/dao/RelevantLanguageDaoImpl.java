package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.RelevantLanguageEntity;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * Created by rperumal on 9/9/2015.
 */
@Repository("relevantLanguageDao")
@Transactional
public class RelevantLanguageDaoImpl extends BaseDaoWrapper<RelevantLanguageEntity, Long> implements
    RelevantLanguageDao {

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    @Override
    public RelevantLanguageEntity getByLocale(final String locale) throws HibernateException {
        return (RelevantLanguageEntity) sessionFactory.getCurrentSession()
            .createQuery("from RelevantLanguageEntity lre where lre.locale=:locale", RelevantLanguageEntity.class)
            .setParameter("locale", locale)
            .uniqueResult();
    }

}