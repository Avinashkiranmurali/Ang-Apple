package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderLineStatusEntity;
import com.b2s.rewards.dao.BaseDao;

/*** Created by srukmagathan on 9/17/2016.
 */
public interface OrderStatusDescDao extends BaseDao<OrderLineStatusEntity,Integer>  {

    String getDescByStatusId(final int statusId);
}
