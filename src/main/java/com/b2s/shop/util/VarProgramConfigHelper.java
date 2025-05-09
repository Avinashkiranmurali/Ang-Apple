/**
 *
 */
package com.b2s.shop.util;

import com.b2s.rewards.apple.dao.VarProgramConfigDao;
import com.b2s.apple.entity.VarProgramConfigEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * No sense in duplicating code, make it a simple call...
 *
 * @author jbryant
 *
 */
@Component
public class VarProgramConfigHelper {

    @Autowired
    private VarProgramConfigDao varProgramConfigDao;

    /**
     * Pulls the value for a Name'd entry in the VAR Program Config Table
     * @param varId
     * @param programId
     * @param key
     * @return
     */
    public final String getValue(final String varId, final String programId, final String key) {
        final VarProgramConfigEntity entry = varProgramConfigDao.getVarProgramConfigByVarProgramName(varId, programId, key);
        if (entry != null ) {
            return entry.getValue();
        }
        return null;
    }

    public final boolean getBoolean(final String varId, final String programId, final String key) {
        return Boolean.valueOf(getValue(varId, programId, key));
    }

}
