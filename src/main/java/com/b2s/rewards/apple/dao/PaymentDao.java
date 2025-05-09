package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.PaymentEntity;
import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface PaymentDao extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByOrderIdAndTransactionType(final Long OrderId, final String transactionType);

    default List<PaymentEntity> getSaleDetails(final Long OrderId) {
        return findByOrderIdAndTransactionType(OrderId, CommonConstants.TRANSACTION_TYPE_SALE);
    }
}
