package com.b2s.rewards.apple.integration.model;

/**
 * @author rperumal
 */
public class CancelRedemptionResponse {

    private String cancellationId;
    private String varCancellationId;


    public String getCancellationId() {
        return cancellationId;
    }

    public void setCancellationId(String cancellationId) {
        this.cancellationId = cancellationId;
    }

    public String getVarCancellationId() {
        return varCancellationId;
    }

    public void setVarCancellationId(String varCancellationId) {
        this.varCancellationId = varCancellationId;
    }
}
