package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

/*** Created by srukmagathan on 8/30/2016.
 */
@Table(name = "Status_Change_Queue")
@Entity
public class StatusChangeQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QueueID")
    private int queueId;

    @JoinColumns({
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
        @JoinColumn(name = "line_num", referencedColumnName = "line_num", insertable = false, updatable = false)
    })
    @ManyToOne
    private OrderLineEntity orderLine;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "line_num")
    private Integer lineNum;

    @Column(name = "QueueDateTime")
    private Date queueDateTime;

    @Column(name = "Order_Status")
    private int orderStatus;

    @Column(name = "Attempts")
    private int attempts;

    @Column(name = "Update_User_Name")
    private String updateUserName;

    @Column(name = "Update_Machine_Name")
    private String updateMachineName;

    @Column(name = "process_status")
    private String processStatus;

    @Column(name = "process_description")
    private String processDescription;

    @Column(name = "process_date")
    private Date processDate;


    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(final int attempts) {
        this.attempts = attempts;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(final int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(final Date processDate) {
        this.processDate = processDate;
    }

    public String getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(final String processDescription) {
        this.processDescription = processDescription;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(final String processStatus) {
        this.processStatus = processStatus;
    }

    public Date getQueueDateTime() {
        return queueDateTime;
    }

    public void setQueueDateTime(final Date queueDateTime) {
        this.queueDateTime = queueDateTime;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(final int queueId) {
        this.queueId = queueId;
    }

    public String getUpdateMachineName() {
        return updateMachineName;
    }

    public void setUpdateMachineName(final String updateMachineName) {
        this.updateMachineName = updateMachineName;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(final String updateUserName) {
        this.updateUserName = updateUserName;
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

    public OrderLineEntity getOrderLine() {
        return orderLine;
    }

    public void setOrderLine(final OrderLineEntity orderLine) {
        this.orderLine = orderLine;
    }
}
