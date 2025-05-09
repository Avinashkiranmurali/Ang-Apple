package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.VarProgramConfigEntity;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Created by rpillai on 6/24/2016.
 */
@Transactional
public interface VarProgramConfigDao extends JpaRepository<VarProgramConfigEntity, Long> {

    List<VarProgramConfigEntity> findByVarIdAndProgramIdAndActiveInd(String varId, String programId, String activeInd);
    default List<VarProgramConfigEntity> getVarProgramConfig(String varId, String programId) {
        return findByVarIdAndProgramIdAndActiveInd(varId, programId, CommonConstants.YES_VALUE);
    }

    List<VarProgramConfigEntity> findByVarIdAndNameAndActiveInd(String varId, String name, String activeInd);
    default List<VarProgramConfigEntity> getVarProgramConfigByVarAndName(String varId, String name) {
        return findByVarIdAndNameAndActiveInd(varId, name, CommonConstants.YES_VALUE);
    }

    VarProgramConfigEntity findByVarIdAndProgramIdAndNameAndActiveInd(final String varId, final String programId,
        final String name, final String activeInd);
    default VarProgramConfigEntity getVarProgramConfigByVarProgramName(final String varId, final String programId,
        final String name) {
        return findByVarIdAndProgramIdAndNameAndActiveInd(varId, programId, name, CommonConstants.YES_VALUE);
    }

    default String getVarProgramConfigValue(
        final String varId, final String programId,
        final String name, final String defaultValue) {
        return Optional.ofNullable(findByVarIdAndProgramIdAndNameAndActiveInd(
                varId,
                programId,
                name,
                CommonConstants.YES_VALUE))
            .stream()
            .findFirst()
            .map(varProgramConfig -> varProgramConfig.getValue())
            .orElse(defaultValue);
    }

}
