package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.OrderLineStatusHistory;
import com.b2s.rewards.apple.model.OrderLineStatusHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 */
@Repository("orderLineStatusHistoryDao")
@Transactional
public interface OrderLineStatusHistoryDao extends JpaRepository<OrderLineStatusHistory, OrderLineStatusHistoryId> {

    default List<OrderLineStatusHistory> loadStatusHistoryByOrderId(final Long orderId) {
        return findOrderLineStatusHistoryByIdOrderId(orderId);
    }

    default List<OrderLineStatusHistory> loadStatusHistoryLatestFirstByOrderId(final Long orderId) {
        return findOrderLineStatusHistoryByIdOrderIdOrderByIdModifiedDateDesc(orderId);
    }
    List<OrderLineStatusHistory> findOrderLineStatusHistoryByIdOrderIdOrderByIdModifiedDateDesc(Long orderId);
    List<OrderLineStatusHistory> findOrderLineStatusHistoryByIdOrderId(Long orderId);

    default List<OrderLineStatusHistory> loadStatusHistoryLatestFirstByOrderIdLineNum(final Long orderId, final Integer lineNum) {
        return findOrderLineStatusHistoryByIdOrderIdAndIdLineNumOrderByIdModifiedDateDesc(orderId, lineNum);
    }
    List<OrderLineStatusHistory> findOrderLineStatusHistoryByIdOrderIdAndIdLineNumOrderByIdModifiedDateDesc(Long orderId, Integer lineNum);

    default List<OrderLineStatusHistory> loadStatusHistoryLatestFirstByOrderIdLineNumStatusId(final Long orderId, final Integer lineNum, Integer statusId) {
        return findOrderLineStatusHistoryByIdOrderIdAndIdLineNumAndIdStatusStatusIdOrderByIdModifiedDateDesc(orderId, lineNum, statusId);
    }
    List<OrderLineStatusHistory> findOrderLineStatusHistoryByIdOrderIdAndIdLineNumAndIdStatusStatusIdOrderByIdModifiedDateDesc(Long orderId, Integer lineNum, Integer statusId);
}
