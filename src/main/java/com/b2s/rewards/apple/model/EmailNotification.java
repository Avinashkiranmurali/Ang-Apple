package com.b2s.rewards.apple.model;

import javax.validation.constraints.*;
import java.util.Date;

/**
 * @author rkumar 2019-11-14
 */
public class EmailNotification {
    @NotNull
    @Min(value = 1, message = "Field orderId must be greater than 0")
    private Long orderId;

    @Min(value = 1, message = "Field lineNum must be greater than 0")
    private Integer lineNum;

    private Integer orderStatus;

    // String can be null but not blank
    @Pattern(regexp = "^(?!\\s*$).+", message = "Field processStatus must not be blank")
    @Size(max = 255)
    private String processStatus;

    // String can be null but not blank
    @Pattern(regexp = "^(?!\\s*$).+", message = "Field processDescription must not be blank")
    @Size(max = 500)
    private String processDescription;

    private Date processDate;

    // String can be null but not blank
    @Pattern(regexp = "^(?!\\s*$).+", message = "Field itemId must not be blank")
    @Size(max = 50)
    private String itemId;

    // String can be null but not blank
    @Pattern(regexp = "^(?!\\s*$).+", message = "Field message must not be blank")
    @Size(max = 500)
    private String message;

    @Email(message = "Field emailId must be be a well-formed email address")
    private String emailId;

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

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(final Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(final String processStatus) {
        this.processStatus = processStatus;
    }

    public String getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(final String processDescription) {
        this.processDescription = processDescription;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(final Date processDate) {
        this.processDate = processDate;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(final String emailId) {
        this.emailId = emailId;
    }
}
