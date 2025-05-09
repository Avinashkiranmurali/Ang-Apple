package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderLineEntity;
import com.b2s.apple.entity.OrderLineId;
import com.b2s.rewards.common.util.CommonConstants;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * @author rjesuraj Date : 8/28/2019 Time : 12:57 PM
 */
@Repository
@Transactional
public class OrderLineDaoImpl extends BaseDaoWrapper<OrderLineEntity, OrderLineId> implements OrderLineDao{

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        this.setDaoSessionFactory(sessionFactory);
    }

    @Override
    public int updateNotificationByOrderId(final Long notificationId, final Long orderId, final boolean isAmp,
        final Integer lineNum) throws DataAccessException {

        final StringBuilder query =
            new StringBuilder("update order_line set notification_Id=:notificationId where order_Id=:orderId");

        if (isAmp) {
            // Update order lines based on Line number for AMP order lines with supplier id = 40000
            query.append(" and line_num=:lineNum and supplier_id = :supplierId");
        } else {
            // Update All order lines except AMP order lines (supplier id 40000)
            query.append(" and supplier_id != :supplierId");
        }

        final NativeQuery nativeQuery = sessionFactory.getCurrentSession()
            .createNativeQuery(query.toString())
            .setParameter("notificationId", notificationId)
            .setParameter("orderId", orderId)
            .setParameter("supplierId", CommonConstants.SUPPLIER_TYPE_AMP_S);

        if (isAmp) {
            nativeQuery.setParameter("lineNum", lineNum);
        }
        return nativeQuery.executeUpdate();

    }
}
