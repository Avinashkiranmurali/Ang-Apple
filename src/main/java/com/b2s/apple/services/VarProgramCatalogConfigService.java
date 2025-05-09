package com.b2s.apple.services;

import com.b2s.apple.entity.VarProgramCatalogConfigEntity;
import com.b2s.rewards.apple.dao.VarProgramCatalogConfigDao;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_CATALOG_ID;
import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_PROGRAM_KEY;
import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_VAR_PROGRAM;

@Service
public class VarProgramCatalogConfigService {

    @Autowired
    private VarProgramCatalogConfigDao varProgramCatalogConfigDao;

    /**
     * The DaoImpl returns a map of name=value pairs. This method extract a name from the map and returns it.
     * @param catalogId String
     * @param varId String
     * @param programId String
     * @param name String
     * @return String
     */
    public String getValue(String catalogId, String varId, String programId, String name) {
        final List<String> catalogIds = new ArrayList<>();
        catalogIds.add(catalogId);
        catalogIds.add(DEFAULT_CATALOG_ID);

        final List<String> varIds = new ArrayList<>();
        varIds.add(varId);
        varIds.add(DEFAULT_VAR_PROGRAM);

        final List<String> programIds = new ArrayList<>();
        programIds.add(programId);
        programIds.add(DEFAULT_PROGRAM_KEY);

        List<VarProgramCatalogConfigEntity> varProgramCatalogConfigEntities =
            varProgramCatalogConfigDao.getVarProgramCatalogConfigNameValue(catalogIds, varIds, programIds, name);

        if (CollectionUtils.isEmpty(varProgramCatalogConfigEntities)) {
            return StringUtils.EMPTY;
        }

        final VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = getVarProgramCatalogConfigEntity(catalogId,
            varId, programId, varProgramCatalogConfigEntities);

        return Objects.nonNull(varProgramCatalogConfigEntity) ? varProgramCatalogConfigEntity.getValue() :
            StringUtils.EMPTY;
    }

    /**
     * If the value is comma separated string, this method splits the string into a list and returns it.
     * @param catalogId String
     * @param varId String
     * @param programId String
     * @param name String
     * @return List of Strings
     */
    public List<String> getListOfValue(String catalogId, String varId, String programId, String name) {
        List<String> returnValue = new ArrayList<>();

        final String commaSeparatedCategories = getValue(catalogId, varId, programId, name);
        if(StringUtils.isNotEmpty(commaSeparatedCategories)) {
            String[] stringArray =  commaSeparatedCategories.split(CommonConstants.COMMA);
            returnValue = Arrays.asList(stringArray);
        }
        return returnValue;
    }

    /**
     * Retrieves relatively best match @VarProgramCatalogConfigEntity object from the List of varProgramCatalogConfigEntities based on override precedence
     * override precedence ==> higher overrideValue overrides lower overrideValue
     * overrideValue = 3 ==> Specific Catalog, Var and Program Ids
     * overrideValue = 2 ==> Specific Catalog and Var Ids, Generic Program Id
     * overrideValue = 1 ==> Specific Catalog Id, Generic Var and Program Ids
     * overrideValue = 0 ==> Generic Catalog, Var and Program Ids
     *
     * @param catalogId
     * @param varId
     * @param programId
     * @param varProgramCatalogConfigEntities
     * @return varProgramCatalogConfigEntity object or null
     */
    public VarProgramCatalogConfigEntity getVarProgramCatalogConfigEntity(final String catalogId, final String varId,
        final String programId, final List<VarProgramCatalogConfigEntity> varProgramCatalogConfigEntities) {

        VarProgramCatalogConfigEntity varProgramCatalogConfigEntity = null;
        int overrideValue = -1;

        for (VarProgramCatalogConfigEntity tempVarProgramCatalogConfigEntity : varProgramCatalogConfigEntities) {
            if (tempVarProgramCatalogConfigEntity.getCatalogId().equalsIgnoreCase(catalogId)) {
                if (tempVarProgramCatalogConfigEntity.getVarId().equalsIgnoreCase(varId)) {
                    if (tempVarProgramCatalogConfigEntity.getProgramId().equalsIgnoreCase(programId)) {
                        //overrideValue = 3;    //highest precedence as Catalog, Var and Program Ids matches
                        return tempVarProgramCatalogConfigEntity;
                    } else {
                        overrideValue = 2;  //higher precedence as matching Specific Catalog and Var Ids
                        varProgramCatalogConfigEntity = tempVarProgramCatalogConfigEntity;
                    }
                } else if (overrideValue < 1) {
                    overrideValue = 1;  //average precedence as only Catalog Id matches
                    varProgramCatalogConfigEntity = tempVarProgramCatalogConfigEntity;
                }
            } else if (overrideValue < 0) {
                overrideValue = 0;      //should be default configuration
                varProgramCatalogConfigEntity = tempVarProgramCatalogConfigEntity;
            }
        }

        return varProgramCatalogConfigEntity;
    }
}
