package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.ServicePlanInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by srajendran on 11/1/2022.
 */

@Repository
@Transactional
public interface ServicePlanInfoDao extends JpaRepository<ServicePlanInfoEntity, Long> {

    ServicePlanInfoEntity findByOrderIdAndLineNum(final long orderId, final int lineNum);

}
