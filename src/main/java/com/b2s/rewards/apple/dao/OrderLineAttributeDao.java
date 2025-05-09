package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderLineAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author rjesuraj Date : 8/28/2019 Time : 12:48 PM
 */
@Repository
@Transactional
public interface OrderLineAttributeDao extends JpaRepository<OrderLineAttributeEntity, Long> {

    List<OrderLineAttributeEntity> findByOrderIdAndLineNumAndName(final long orderId, final int lineNum,
        final String name);
}
