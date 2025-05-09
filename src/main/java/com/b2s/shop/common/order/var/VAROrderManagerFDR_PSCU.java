package com.b2s.shop.common.order.var;

import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.stereotype.Component;

@Component("varOrderManagerFDR_PSCU")
public class VAROrderManagerFDR_PSCU extends VAROrderManagerFDR {

    private static final String VAR_ID = CommonConstants.VAR_FDR_PSCU;

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

}
