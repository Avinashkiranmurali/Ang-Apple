package com.b2s.apple.entity;


import javax.persistence.*;
import java.util.Date;

/**
 * Created by srajendran on 11/1/2022.
 */

@Entity
@Table(name = "service_plan_info")
public class ServicePlanInfoEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "line_num")
    private Integer lineNum;

    @Column(name = "last_update_date")
    private Date lastUpdateDate;

    @Column(name = "plan_id")
    private String planId;

    @Column(name = "plan_end_date")
    private Date planEndDate;

    @Column(name = "plan_url")
    private String planUrl;

    @Column(name = "hardware_serial_number")
    private String hardwareSerialNumber;

    @Column(name = "hardware_description")
    private String hardwareDescription;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
        @JoinColumn(name = "line_num", referencedColumnName = "line_num", insertable = false, updatable = false)
    })
    private OrderLineEntity orderLine;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public void setLineNum(final Integer lineNum) {
        this.lineNum = lineNum;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(final String planId) {
        this.planId = planId;
    }

    public Date getPlanEndDate() {
        return planEndDate;
    }

    public void setPlanEndDate(final Date planEndDate) {
        this.planEndDate = planEndDate;
    }

    public String getPlanUrl() {
        return planUrl;
    }

    public void setPlanUrl(final String planUrl) {
        this.planUrl = planUrl;
    }

    public String getHardwareSerialNumber() {
        return hardwareSerialNumber;
    }

    public void setHardwareSerialNumber(final String hardwareSerialNumber) {
        this.hardwareSerialNumber = hardwareSerialNumber;
    }

    public String getHardwareDescription() {
        return hardwareDescription;
    }

    public void setHardwareDescription(final String hardwareDescription) {
        this.hardwareDescription = hardwareDescription;
    }

    public OrderLineEntity getOrderLine() {
        return orderLine;
    }

    public void setOrderLine(final OrderLineEntity orderLine) {
        this.orderLine = orderLine;
    }
}
