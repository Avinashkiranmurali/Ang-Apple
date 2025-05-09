package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.OrderCommitStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author rjesuraj Date : 8/28/2019 Time : 12:13 PM
 */
public interface OrderCommitStatusDao extends JpaRepository<OrderCommitStatusEntity,Long> {

    List<OrderCommitStatusEntity> findByVarIdAndProgramIdAndUserId(final String varId, final String programId, final String userId);

    int deleteByVarIdAndProgramIdAndUserIdAndOrderHashCode(final String varId, final String programId, final String userId, final  int orderHashCode);
}
