package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.OrderLineShipmentNotification;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.collections.CollectionUtils;
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
 * Created by rpillai on 6/24/2016.
 */
@Repository("shipmentNotificationDao")
@Transactional
public class ShipmentNotificationDaoImpl extends BaseDaoWrapper<OrderLineShipmentNotification, Long> implements ShipmentNotificationDao {

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    /**
     * Get shipment notification for a given order id and line num
     *
     * @param orderId,
     * @param linNum
     * @return
     */
    @Override
    public OrderLineShipmentNotification getShipmentNotification(Long orderId, Integer linNum) throws
        DataAccessException {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<OrderLineShipmentNotification> cq = cb.createQuery(OrderLineShipmentNotification.class);
        Root<OrderLineShipmentNotification> root = cq.from(OrderLineShipmentNotification.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(CommonConstants.ORDER_ID), orderId));
        predicates.add(cb.equal(root.get(CommonConstants.LINE_NUM), linNum));

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        cq.select(root)
            .where(predicateArray)
            .orderBy(cb.desc(root.get(CommonConstants.SHIPMENT_DATE)));

        Query query = session.createQuery(cq);
        query.setLockMode(LockModeType.NONE);
        query.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<OrderLineShipmentNotification> shipments = query.getResultList();
        return CollectionUtils.isNotEmpty(shipments) ? shipments.get(0) : null;
    }
}
