package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.VarProgramConfigDao;
import com.b2s.apple.entity.VarProgramConfigEntity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author rjesuraj Date : 3/3/2017 Time : 1:04 PM
 */
@Service
public class VarProgramConfigCustomService {

    @Autowired
    private VarProgramConfigDao varProgramConfigDao;

    public Map<String, Boolean> getVarProgramConfigMap(final String varId, final String programId) {
        final List<VarProgramConfigEntity> varProgramConfigs = varProgramConfigDao.getVarProgramConfig(varId,
            programId);
        Map<String, Boolean> varProgramConfigMap = null;
        if (CollectionUtils.isNotEmpty(varProgramConfigs)) {
            varProgramConfigMap = varProgramConfigs.stream().filter(varProgramConfig -> "Y".equals(varProgramConfig.getIsActive()))
                .collect(
                    Collectors.toMap(VarProgramConfigEntity::getName, v -> {
                        return BooleanUtils.toBooleanObject(v.getValue()) != null ? new Boolean(v.getValue()) : false;
                    }));
        }
        return varProgramConfigMap;
    }


}
