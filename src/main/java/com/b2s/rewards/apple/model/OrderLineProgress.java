package com.b2s.rewards.apple.model;

import java.util.Date;
import java.util.List;

public class OrderLineProgress {

    private String status;
    private int progressValue;
    private List<String> progressBarText;
    private Date modifiedDate;

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public int getProgressValue() {
        return progressValue;
    }

    public void setProgressValue(final int progressValue) {
        this.progressValue = progressValue;
    }

    public List<String> getProgressBarText() {
        return progressBarText;
    }

    public void setProgressBarText(final List<String> progressBarText) {
        this.progressBarText = progressBarText;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(final Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        return "OrderLineProgress{" +
            "status='" + status + '\'' +
            ", progressValue=" + progressValue +
            ", progressBarText=" + progressBarText +
            ", modifiedDate=" + modifiedDate +
            '}';
    }
}
