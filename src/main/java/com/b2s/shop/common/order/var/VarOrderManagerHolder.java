package com.b2s.shop.common.order.var;

import org.apache.commons.collections.MapUtils;

import java.util.Map;

/**
 * Created by rpillai on 3/28/2016.
 */
public class VarOrderManagerHolder {

    private Map<String, VAROrderManagerIF> varOrderManagerMap;

    public VAROrderManagerIF getVarOrderManager(final String varId) {
        VAROrderManagerIF varOrderManager = null;
        if(MapUtils.isNotEmpty(varOrderManagerMap)) {
            varOrderManager = varOrderManagerMap.get(varId);
        }
        return varOrderManager;
    }

    public Map<String, VAROrderManagerIF> getVarOrderManagerMap() {
        return varOrderManagerMap;
    }

    public void setVarOrderManagerMap(Map<String, VAROrderManagerIF> varOrderManagerMap) {
        this.varOrderManagerMap = varOrderManagerMap;
    }
}
