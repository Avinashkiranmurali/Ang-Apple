package com.b2s.rewards.apple.dao;


import com.b2s.rewards.apple.model.WhiteListWord;
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

import static com.b2s.rewards.common.util.CommonConstants.*;


@Repository("whiteListWordDao")
@Transactional
public class WhiteListWordDaoImpl extends BaseDaoWrapper<WhiteListWord, Long> implements WhiteListWordDao {

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    public List<WhiteListWord> getWhitelistWords(Locale userLocale, String language) throws DataAccessException {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<WhiteListWord> cq = cb.createQuery(WhiteListWord.class);
        Root<WhiteListWord> root = cq.from(WhiteListWord.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.or(
            cb.or(
                cb.equal(root.get(LANGUAGE), DEFAULT_CATALOG_ID),
                cb.equal(root.get(LOCALE), userLocale.toString())
            ),
            cb.and(
                cb.equal(root.get(LANGUAGE), language),
                cb.equal(root.get(LOCALE), DEFAULT_CATALOG_ID)
            )
        ));

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        cq.select(root).where(predicateArray);

        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);

        @SuppressWarnings("unchecked")
        List<WhiteListWord> returnList = query.getResultList();
        return returnList;
    }


}
