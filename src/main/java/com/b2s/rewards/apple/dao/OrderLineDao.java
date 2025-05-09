package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderLineEntity;
import com.b2s.apple.entity.OrderLineId;
import com.b2s.rewards.dao.BaseDao;

import java.util.Collection;

/**
 * @author rjesuraj Date : 8/28/2019 Time : 12:56 PM
 */
public interface OrderLineDao extends BaseDao<OrderLineEntity, OrderLineId> {

    int updateNotificationByOrderId(Long notificationId, Long orderId, final boolean isAmp, final Integer lineNum);
    void saveAll(final Collection<OrderLineEntity> toUpdateAll);
}
