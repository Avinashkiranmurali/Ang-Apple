package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.Orders;
import com.b2s.shop.common.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Created by vmurugesan on 10/3/2016.
 */

@Repository("orderHistoryDao")
@Transactional
public interface OrderHistoryDao  {
    List<Orders> getOrderHistory(final User user, final Integer days, final boolean allPrograms);
    Orders getOrderHistoryDetails(final User user, final Integer orderId, final boolean allPrograms);
    Orders getOrderHistoryDetails(final User user, final String varOrderId, final boolean allPrograms);
    Orders getOrderHistoryDetails(final String orderId, final String email, final String varId, final String programId, final Locale locale);
}
