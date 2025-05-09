package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderDiagnosticInfoEntity;
import com.b2s.rewards.dao.BaseDao;

/**
 * @author rjesuraj Date : 8/28/2019 Time : 8:02 PM
 */
public interface OrderDiagnosticInfoDao extends BaseDao<OrderDiagnosticInfoEntity, Long> {
    OrderDiagnosticInfoEntity getHostNameByOrderId(final String orderId);
}
