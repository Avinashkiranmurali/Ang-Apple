package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.OrderLineItemAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rperumal on 7/24/2015.
 */
@Repository
@Transactional
public interface OrderLineItemAttributeDao extends JpaRepository<OrderLineItemAttribute,Long> {

}
