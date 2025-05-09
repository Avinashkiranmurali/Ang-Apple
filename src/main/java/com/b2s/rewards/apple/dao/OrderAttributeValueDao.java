package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.OrderAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by rperumal on 3/15/16
 */
@Repository("orderAttributeValueDao")
@Transactional
public interface OrderAttributeValueDao extends JpaRepository<OrderAttributeValue,Long> {

    default void insert(OrderAttributeValue orderAttributeValue){
        save(orderAttributeValue);
    }

    List<OrderAttributeValue> findByOrderId(Long orderId);
    default List<OrderAttributeValue> getByOrder(Long orderId) {
        return findByOrderId(orderId);
    }

}
