package com.b2s.shop.common.order.var;

import com.b2s.rewards.common.exception.B2RException;

import java.util.Arrays;
import static com.b2s.rewards.common.util.CommonConstants.VAR_INTEGRATION_SERVICE_LOCAL_IMPL;

/**
 * Created by hranganathan on 5/18/2017.
 */
public enum VISLookupStrategy {

    WF_LOCAL_NOMOCK(VAR_INTEGRATION_SERVICE_LOCAL_IMPL),
    WF_LOCAL_MOCK(VAR_INTEGRATION_SERVICE_LOCAL_IMPL),
    WF_REMOTE_NOMOCK("varIntegrationServiceRemoteImpl"),
    WF_REMOTE_MOCK("VIS_WF_MOCK"),
    DEMO_LOCAL_NOMOCK(VAR_INTEGRATION_SERVICE_LOCAL_IMPL);

    String strategyKey;

    VISLookupStrategy(final String strategyKey) {
        this.strategyKey = strategyKey;
    }

    public static VISLookupStrategy lookup(final String varId, final Boolean isLocalProgram, final Boolean isMockProgram) throws B2RException {
        final String strategy = String.join("_", varId, (isLocalProgram != null && !isLocalProgram ? "REMOTE" : "LOCAL"), (isMockProgram != null && isMockProgram ? "MOCK" : "NOMOCK"));
        return Arrays.stream(values())
                .filter(visLookupStrategy -> visLookupStrategy.name().equalsIgnoreCase(strategy))
                .findFirst()
                .orElseThrow(() -> new B2RException("Invalid Program type configuration in lookup"));
    }

    public String getStrategyKey() {
        return this.strategyKey;
    }
}
