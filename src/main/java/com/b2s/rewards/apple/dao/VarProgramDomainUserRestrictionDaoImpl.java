package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgramDomainUserRestriction;
import com.b2s.rewards.common.util.CommonConstants;
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

/**
 * @author rjesuraj Date : 3/3/2017 Time : 12:53 PM
 */
@Repository(value = "VarProgramDomainUserRestrictionDao")
@Transactional
public class VarProgramDomainUserRestrictionDaoImpl extends BaseDaoWrapper<VarProgramDomainUserRestriction, Integer>
    implements VarProgramDomainUserRestrictionDao {

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    @Override
    public boolean isUserOfAuthType(
        final String userId, final String varId, final String programId, final String
        loginType, final String authType) throws DataAccessException {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<VarProgramDomainUserRestriction> cq = cb.createQuery(VarProgramDomainUserRestriction.class);
        Root<VarProgramDomainUserRestriction> root = cq.from(VarProgramDomainUserRestriction.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(CommonConstants.USER_ID_CAMEL_CASE), userId));
        predicates.add(cb.equal(root.get(CommonConstants.VAR_ID_CAMEL_CASE), varId));
        predicates.add(cb.or(
            cb.equal(root.get(CommonConstants.PROGRAM_ID), programId),
            cb.isNull(root.get(CommonConstants.PROGRAM_ID))
        ));
        predicates.add(cb.equal(root.get(CommonConstants.LOGIN_TYPE), loginType));
        predicates.add(cb.equal(root.get(CommonConstants.IS_ACTIVE), CommonConstants.YES_VALUE));
        predicates.add(cb.equal(root.get(CommonConstants.AUTH_TYPE), authType));

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);

        cq.select(root).where(predicateArray);
        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);
        return (query.getResultList().size() == 1);
    }
}
