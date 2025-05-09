package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.OrderLineShipmentNotification;
import com.b2s.rewards.dao.BaseDao;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rpillai on 6/24/2016.
 */
@Transactional
public interface ShipmentNotificationDao extends BaseDao<OrderLineShipmentNotification, Long> {

    OrderLineShipmentNotification getShipmentNotification(Long orderId, Integer linNum);

}
