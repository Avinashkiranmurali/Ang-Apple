package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.AMPProductConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.b2s.rewards.common.util.CommonConstants.SELECT_FROM_AMP_PRODUCT_CONFIGURATION;

/**
 * @author ssundaramoorthy Date : 7/7/2021 Time : 08:55 PM
 */
@Transactional
public interface AMPProductConfigurationDao extends JpaRepository<AMPProductConfigEntity, Long> {

    @Query(value = SELECT_FROM_AMP_PRODUCT_CONFIGURATION, nativeQuery = true)
    List<AMPProductConfigEntity> queryByVarProgramDate(final String varId, final String programId);

    List<AMPProductConfigEntity> findByVarIdAndProgramIdAndIsActiveTrue(String varId, String programId);

    default List<AMPProductConfigEntity> getActiveAMPConfig(final String varId, final String programId) {
        return findByVarIdAndProgramIdAndIsActiveTrue(varId,programId);
    }
}