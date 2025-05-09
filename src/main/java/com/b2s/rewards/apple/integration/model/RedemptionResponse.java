package com.b2s.rewards.apple.integration.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rperumal
 */
public class RedemptionResponse {

    private String orderId;
    private String varOrderId;
    private String orderLineId;
    private String varOrderLineId;
    private List<RedemptionOrderLine> orderLines;
    private Map<String, Object> additionalInfo = new HashMap<String, Object>();

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getVarOrderId() {
        return varOrderId;
    }

    public void setVarOrderId(String varOrderId) {
        this.varOrderId = varOrderId;
    }

    public String getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(String orderLineId) {
        this.orderLineId = orderLineId;
    }

    public String getVarOrderLineId() {
        return varOrderLineId;
    }

    public void setVarOrderLineId(String varOrderLineId) {
        this.varOrderLineId = varOrderLineId;
    }

    public List<RedemptionOrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<RedemptionOrderLine> orderLines) {
        this.orderLines = orderLines;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
