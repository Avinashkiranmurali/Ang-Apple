package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.VarProgramCreditAddsFilterEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import static com.b2s.rewards.common.util.CommonConstants.*;

@Transactional
@Repository
public class VarProgramCreditAddsFilterDaoImpl extends BaseDaoWrapper<VarProgramCreditAddsFilterEntity, VarProgramCreditAddsFilterEntity.VarProgramFilterId> implements VarProgramCreditAddsFilterDao {

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(VarProgramCreditAddsFilterDaoImpl.class);

    @Override
    public List<VarProgramCreditAddsFilterEntity> findByVarProgram(final String varId, final String programId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<VarProgramCreditAddsFilterEntity> cq = cb.createQuery(VarProgramCreditAddsFilterEntity.class);
        Root<VarProgramCreditAddsFilterEntity> root = cq.from(VarProgramCreditAddsFilterEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(VAR_PROGRAM_FILTER_ID).get(VAR_ID_CAMEL_CASE), varId));
        predicates.add(cb.equal(root.get(VAR_PROGRAM_FILTER_ID).get(PROGRAM_ID), programId));

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        cq.select(root).where(predicateArray);

        try {
            Query query = session.createQuery(cq);
            query.setLockMode(LockModeType.NONE);

            @SuppressWarnings("unchecked")
            List<VarProgramCreditAddsFilterEntity> returnList = query.getResultList();
            return returnList;
        } catch (Exception e) {
            LOGGER.error("Error occurred while retrieving info from var_program_credit_adds_filter table ", e);
            return null;
        }
    }
}
