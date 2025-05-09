package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.QuickLinkEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static com.b2s.rewards.common.util.CommonConstants.*;

@Repository
@Transactional
public class QuickLinkDaoImpl extends BaseDaoWrapper<QuickLinkEntity, Integer> implements QuickLinkDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<QuickLinkEntity> getByVarIdProgramIdLocaleLinkCode(final String varId, final String programId,
                                                                   final String locale, final boolean isAnon) throws DataAccessException {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<QuickLinkEntity> cq = cb.createQuery(QuickLinkEntity.class);
        Root<QuickLinkEntity> root = cq.from(QuickLinkEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(QUICK_LINK_ID).get(LOCALE), locale));
        predicates.add(cb.equal(root.get(QUICK_LINK_ID).get(VAR_ID_CAMEL_CASE), varId));
        predicates.add(cb.equal(root.get(QUICK_LINK_ID).get(PROGRAM_ID), programId));
        if(isAnon){
            predicates.add(cb.equal(root.get(SHOW_UNAUTHENTICATED), true));
        }

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        cq.select(root).where(predicateArray);

        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);

        @SuppressWarnings("unchecked")
        List<QuickLinkEntity> returnList = query.getResultList();
        return returnList;
    }
}
