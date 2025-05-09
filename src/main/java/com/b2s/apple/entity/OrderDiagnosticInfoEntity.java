package com.b2s.apple.entity;

import javax.persistence.*;

@Entity
@Table(name = "order_diagnostic_info")
public class OrderDiagnosticInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "hostname")
    private String hostname;
    @Column(name = "IPADDRESS")
    private String ipAddress;
    @Column(name = "fraud_order")
    private Boolean fraudOrder;
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_time")
    private String updatedTime;


    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Boolean getFraudOrder() {
        return fraudOrder;
    }

    public void setFraudOrder(final Boolean fraudOrder) {
        this.fraudOrder = fraudOrder;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(final String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }
}
