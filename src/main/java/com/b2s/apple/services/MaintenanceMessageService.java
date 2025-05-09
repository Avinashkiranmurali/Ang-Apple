package com.b2s.apple.services;

import com.b2s.apple.entity.MaintenanceMessageEntity;
import com.b2s.rewards.apple.dao.MaintenanceMessageDao;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MaintenanceMessageService {

    @Autowired
    private MaintenanceMessageDao maintenanceMessageDao;

    /**
     * The DaoImpl returns a map of name=value pairs. This method extract a name from the map and returns it.
     *
     * @param varId
     * @param programId
     * @return
     */
    public String getMaintenanceMessage(final String varId, final String programId) {

        final List<String> varIds = new ArrayList<>();
        varIds.add(varId);
        varIds.add(CommonConstants.DEFAULT_VAR_PROGRAM);

        final List<String> programIds = new ArrayList<>();
        programIds.add(programId);
        programIds.add(CommonConstants.DEFAULT_PROGRAM_KEY);

        final List<MaintenanceMessageEntity> maintenanceMessageEntities =
            maintenanceMessageDao.getVarProgramMessageValue(varIds, programIds);

        if (CollectionUtils.isEmpty(maintenanceMessageEntities)) {
            return StringUtils.EMPTY;
        }

        final MaintenanceMessageEntity maintenanceMessageEntity = getVarProgramMaintenanceEntity(varId, programId,
            maintenanceMessageEntities);

        return Objects.nonNull(maintenanceMessageEntity) ? maintenanceMessageEntity.getMessage() :
            StringUtils.EMPTY;
    }

    /**
     * Retrieves relatively best match @MaintenanceMessageEntity object from the List of maintenanceMessageEntities based on override precedence
     * override precedence ==> higher overrideValue overrides lower overrideValue
     * overrideValue = 2 ==> Specific Var and Program Ids
     * overrideValue = 1 ==> Specific Var Id, Generic Program Id
     * overrideValue = 0 ==> Generic Var and Program Ids
     *
     * @param varId
     * @param programId
     * @param maintenanceMessageEntities
     * @return
     */
    private MaintenanceMessageEntity getVarProgramMaintenanceEntity(final String varId, final String programId,
        final List<MaintenanceMessageEntity> maintenanceMessageEntities) {

        MaintenanceMessageEntity maintenanceMessageEntity = null;
        int overrideValue = -1;

        for (MaintenanceMessageEntity tempVarProgramMaintenanceEntity : maintenanceMessageEntities) {
            if (tempVarProgramMaintenanceEntity.getVarId().equalsIgnoreCase(varId)) {
                if (tempVarProgramMaintenanceEntity.getProgramId().equalsIgnoreCase(programId)) {
                    //overrideValue = 2;    //highest precedence as Var and Program Ids matches
                    return tempVarProgramMaintenanceEntity;
                } else {
                    overrideValue = 1;  //higher precedence as matching Var Ids
                    maintenanceMessageEntity = tempVarProgramMaintenanceEntity;
                }
            } else if (overrideValue < 0) {
                overrideValue = 0;  //should be default configuration
                maintenanceMessageEntity = tempVarProgramMaintenanceEntity;
            }
        }

        return maintenanceMessageEntity;
    }
}