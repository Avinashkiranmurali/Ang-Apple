package com.b2s.apple.services;

import com.b2s.apple.mapper.AMPMapper;
import com.b2s.rewards.apple.dao.*;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.b2s.rewards.apple.util.AppleUtil.getProgramConfigValueAsBoolean;
import static com.b2s.rewards.common.util.CommonConstants.DISABLE;
import static com.b2s.rewards.common.util.CommonConstants.DISABLE_AMP;

/**
 * Created by ssundaramoorthy on 7/7/2021.
 */
@Service
public class AMPProductConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AMPProductConfigService.class);

    @Autowired
    private AMPProductConfigurationDao ampProductConfigurationDao;

    @Autowired
    private AMPMapper ampMapper;

    /**
     * Get AMP details based on given var id and program id
     *
     * @param program
     * @return
     */

    public Set<AMPConfig> getAmpConfigurationByProgram(final Program program) {
        final boolean ampDisabled = getProgramConfigValueAsBoolean(program, DISABLE_AMP);
        final String varId = program.getVarId();
        final String programId = program.getProgramId();
        Set<AMPConfig> configs = null;
        if (!ampDisabled) {
            configs = new HashSet<>();
            LOGGER.info("Retrieval of AMP product configuration has started... ");

            final List<AMPConfig> defaultAmpConfigs = ampMapper.getProgramConfigs(ampProductConfigurationDao.
                getActiveAMPConfig(CommonConstants.DEFAULT_VAR_PROGRAM,
                    CommonConstants.DEFAULT_VAR_PROGRAM));
            final List<AMPConfig> varConfigs = ampMapper.getProgramConfigs(ampProductConfigurationDao.
                getActiveAMPConfig(varId, CommonConstants.DEFAULT_VAR_PROGRAM));

            final List<AMPConfig> programConfigs = ampMapper.getProgramConfigs(ampProductConfigurationDao.
                getActiveAMPConfig(varId, programId));


            if (CollectionUtils.isNotEmpty(programConfigs)) {
                configs.addAll(programConfigs);
            }
            if (CollectionUtils.isNotEmpty(varConfigs)) {
                configs.addAll(varConfigs);
            }
            if (CollectionUtils.isNotEmpty(defaultAmpConfigs)) {
                configs.addAll(defaultAmpConfigs);
            }


           /* Validate if there is any var specific disabling on AMP Products retrieved */
            if (CollectionUtils.isNotEmpty(configs)) {
                configs.removeIf(ampConfig ->
                    getProgramConfigValueAsBoolean(program, DISABLE.concat(ampConfig.getItemId())));
            }

            LOGGER.info("Retrieval of AMP product configuration has ended... ");

        } else {
            LOGGER.info("AMP Program has been disabled for  VAR : {} and PROGRAM : {}", varId, programId);
        }
        return configs;
    }
}