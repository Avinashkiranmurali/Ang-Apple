package com.b2s.rewards.apple.integration.model;

import java.util.Map;
import java.util.Optional;

/**
 * @author rperumal
 */
public class AccountIdentifier {

    private String varId;
    private String programId;
    private String accountId;
    private String agentId;
    private Optional<String> sessionId;
    private Map<String, String> alternateIds;

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Map<String, String> getAlternateIds() {
        return alternateIds;
    }

    public void setAlternateIds(Map<String, String> alternateIds) {
        this.alternateIds = alternateIds;
    }

    public Optional<String> getSessionId() {
        return sessionId;
    }

    public void setSessionId(final Optional<String> sessionId) {
        this.sessionId = sessionId;
    }
}
