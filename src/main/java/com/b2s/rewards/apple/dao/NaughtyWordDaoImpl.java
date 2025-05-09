package com.b2s.rewards.apple.dao;


import com.b2s.rewards.apple.model.NaughtyWord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by rperumal on 10/05/2015.
 */
@Repository("naughtyWordDao")
@Transactional
public class NaughtyWordDaoImpl extends BaseDaoWrapper<NaughtyWord, Long> implements NaughtyWordDao {
    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    public List<NaughtyWord> getByLocaleOrLanguage(Locale userLocale, String language)
        throws DataAccessException {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<NaughtyWord> cq = cb.createQuery(NaughtyWord.class);
        Root<NaughtyWord> rootClass = cq.from(NaughtyWord.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(
            cb.or(
                cb.or(
                    cb.equal(rootClass.get("language"), "-1"),
                    cb.equal(rootClass.get("locale"), userLocale.toString())
                ),
                cb.and(
                    cb.equal(rootClass.get("language"), language),
                    cb.equal(rootClass.get("locale"), "-1")
                )
            ));
        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);

        cq.select(rootClass).where(predicateArray);

        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);
        @SuppressWarnings("unchecked")
        List<NaughtyWord> returnList = query.getResultList();
        return returnList;
    }
}
