package com.b2s.apple.services;

import com.b2s.rewards.apple.dao.VarProgramDomainUserRestrictionDao;
import com.b2s.rewards.apple.model.DomainVarMapping;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.order.var.UserRestrictionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @author rjesuraj Date : 3/3/2017 Time : 1:00 PM
 */
@Service
public class VarProgramDomainUserRestrictionService {
    private static final Logger LOG= LoggerFactory.getLogger(VarProgramDomainUserRestrictionService.class);

    @Autowired
    private DomainVarMappingService domainVarMappingService;

    @Autowired
    private VarProgramConfigCustomService varProgramConfigCustomService;

    @Autowired
    private VarProgramDomainUserRestrictionDao varProgramDomainUserRestrictionDao;

    // Checking user Permission for OTP User
    public boolean isOtpUserPermitted(final String email, final String loginFunction) {
        boolean isUserPermitted = false;
        // AWP does not have Black/white list concept
        if(CommonConstants.AWP.equalsIgnoreCase(loginFunction)){
            return true;
        }

        final DomainVarMapping domainMapping = domainVarMappingService.getDomainMapping(email, loginFunction);
        if (Objects.nonNull(domainMapping)) {

            isUserPermitted =
                isUserPermitted(email, domainMapping.getVarId(), domainMapping.getProgramId(),
                    loginFunction);
        }
        return isUserPermitted;
    }

    //Checking user Permission for ADFS User
    public boolean isAdfsUserPermitted(final String userId, final String varId, final String programId, final String
        loginType) {

        return isUserPermitted(userId, varId, programId, loginType);
    }


    //Common logic to validate user permission
    private boolean isUserPermitted(final String userId, final String varId, final String programId, final String
        loginType) {
        boolean isUserPermitted = true;
        final Map<String, Boolean> varProgramConfigMap = varProgramConfigCustomService.getVarProgramConfigMap(varId,
            programId);
        final Boolean blackObj = varProgramConfigMap.get
            (UserRestrictionEnum.BLACK_LIST.getValue());
        final Boolean whiteObj = varProgramConfigMap.get
            (UserRestrictionEnum.WHITE_LIST.getValue());

        if (blackObj != null && blackObj) {
            //If blackObj is true check user is blacklisted
            if (varProgramDomainUserRestrictionDao
                .isUserOfAuthType(userId, varId, programId, loginType, UserRestrictionEnum.BLACK_LIST.getValue())) {
                LOG.error("User Restricted. {} - present in blacklist", userId);
                isUserPermitted = false;
            }

        }
        //If whitObj is true check user is whitelisted
        else if (whiteObj != null && whiteObj && !varProgramDomainUserRestrictionDao
            .isUserOfAuthType(userId, varId, programId, loginType, UserRestrictionEnum.WHITE_LIST.getValue())) {
            LOG.error("User Restricted. {} - not present in whitelist",userId);
                isUserPermitted = false;
        }
        return isUserPermitted;
    }

}
