package com.b2s.apple.services;

import com.b2s.apple.util.JsonUtil;
import com.b2s.rewards.apple.dao.VarProgramTemplateDao;
import com.b2s.rewards.apple.model.VarProgramTemplate;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_PROGRAM_KEY;

/**
 * @author rkumar 2020-02-12
 */
@Service
public class VarProgramTemplateService {

    private static final String MOBILE = "N";

    @Autowired
    private VarProgramTemplateDao varProgramTemplateDao;

    public VarProgramTemplate getConfigDataByVarIdProgramId(final String varId, final String programId) {

        VarProgramTemplate varProgramTemplate;

        varProgramTemplate = varProgramTemplateDao.getByVarIdProgramId(varId, programId);
        if (Objects.isNull(varProgramTemplate)) {
            varProgramTemplate = varProgramTemplateDao.getByVarIdProgramId(varId, DEFAULT_PROGRAM_KEY);
        }
        return varProgramTemplate;
    }

    public VarProgramTemplate getConfigDataByUser(final User user) {

        VarProgramTemplate varProgramTemplate = this.getConfigDataByVarIdProgramId(user.getVarId(),
                user.getProgramId());
        if (Objects.nonNull(user) && MapUtils.isNotEmpty(user.getAdditionalInfo()) && user.getAdditionalInfo()
                .containsKey(
                        CommonConstants.DEVICE_EXPERIENCE)) {
            final String deviceExperience = user.getAdditionalInfo().get(CommonConstants.DEVICE_EXPERIENCE);
            if (StringUtils.isNotBlank(deviceExperience) && StringUtils.equalsIgnoreCase(deviceExperience, MOBILE)) {
                StringBuilder pathToInsert = new StringBuilder(JsonUtil.CONFIG_DATA + JsonUtil.PATH_DELIMITER + JsonUtil.TEMPLATES + JsonUtil.PATH_DELIMITER + JsonUtil.HEADER);
                varProgramTemplate.setConfigData(JsonUtil.addElementTo(varProgramTemplate.getConfigData(),pathToInsert.toString(),JsonUtil.SUPPESS_LOGO_TEMPLATE,
                        CommonConstants.YES_VALUE));
            }
        }
        return varProgramTemplate;
    }
}