package com.b2s.rewards.apple.integration.model;

/**
 * @author rpillai
 */
public class Shipment {

    private String carrier;
    private String trackingId;

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }
}
