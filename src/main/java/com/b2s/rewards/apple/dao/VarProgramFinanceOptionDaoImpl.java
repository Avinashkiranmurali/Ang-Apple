package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgramFinanceOption;
import org.hibernate.SessionFactory;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository("varProgramFinanceOptionDao")
@Transactional(readOnly = true)
public class VarProgramFinanceOptionDaoImpl extends BaseDaoWrapper<VarProgramFinanceOption, Long> implements VarProgramFinanceOptionDao {

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    /**
     * Get var program payment option by var id and program id
     *
     * @param varId,
     * @param programId
     * @return
     */
    @Override
    public List<VarProgramFinanceOption> getVarProgramFinanceOption(final String varId, final String programId) throws
        ServiceException {

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<VarProgramFinanceOption> cq = cb.createQuery(VarProgramFinanceOption.class);
        Root<VarProgramFinanceOption> varProgramFinanceOption = cq.from(VarProgramFinanceOption.class);

        //Constructing list of parameters
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(varProgramFinanceOption.get("varId"), varId));
        predicates.add(cb.equal(varProgramFinanceOption.get("programId"), programId));

        //Criteria Query
        cq.select(varProgramFinanceOption).where(predicates.toArray(new Predicate[]{}));

        return sessionFactory.getCurrentSession().createQuery(cq).setCacheable(true).getResultList();
    }

}
