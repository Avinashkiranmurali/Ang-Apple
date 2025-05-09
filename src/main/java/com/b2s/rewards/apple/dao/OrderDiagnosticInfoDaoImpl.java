package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderDiagnosticInfoEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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

/**
 * @author rjesuraj Date : 8/28/2019 Time : 8:04 PM
 */
@Repository("orderDiagnosticInfoDao")
@Transactional
public class OrderDiagnosticInfoDaoImpl extends BaseDaoWrapper<OrderDiagnosticInfoEntity, Long> implements OrderDiagnosticInfoDao {
    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    @Override
    public OrderDiagnosticInfoEntity getHostNameByOrderId(final String orderId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<OrderDiagnosticInfoEntity> cq = cb.createQuery(OrderDiagnosticInfoEntity.class);
        Root<OrderDiagnosticInfoEntity> root = cq.from(OrderDiagnosticInfoEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("orderId"), orderId));

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        cq.select(root).where(predicateArray);

        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);

        return (OrderDiagnosticInfoEntity) query.getSingleResult();
    }
}
