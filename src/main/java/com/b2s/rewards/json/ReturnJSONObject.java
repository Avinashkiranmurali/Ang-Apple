package com.b2s.rewards.json;

/**
 * Created by preddy on 8/24/2014.
 */
public class ReturnJSONObject {

    public String url = null;

    public boolean emailSent = false;

    public boolean printOrder = false;

    public String printContent = null;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public void setEmailSent(final boolean emailSent) {
        this.emailSent = emailSent;
    }

    public boolean isPrintOrder() {
        return printOrder;
    }

    public void setPrintOrder(final boolean printOrder) {
        this.printOrder = printOrder;
    }


    public String getPrintContent() {
        return printContent;
    }

    public void setPrintContent(final String printContent) {
        this.printContent = printContent;
    }
}
