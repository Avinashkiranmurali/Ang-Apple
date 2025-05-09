package com.b2s.rewards.apple.model;

import java.io.Serializable;

/**
 * Created by sjayaraman on 12/13/2018.
 */
public class InstallmentOption implements Serializable {

    private static final long serialVersionUID = -7805264954313846591L;
    private Double payPerPeriod;
    private int payPeriods;

    public int getPayPeriods() {
        return payPeriods;
    }

    public void setPayPeriods(final int payPeriods) {
        this.payPeriods = payPeriods;
    }

    public Double getPayPerPeriod() {
        return payPerPeriod;
    }

    public void setPayPerPeriod(final Double payPerPeriod) {
        this.payPerPeriod = payPerPeriod;
    }
}
