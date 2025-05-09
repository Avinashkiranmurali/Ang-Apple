package com.b2s.rewards.apple.model;

import java.io.Serializable;

/**
 * @author rjesuraj Date : 5/25/2017 Time : 2:53 PM
 */
public class SupplementaryPaymentLimit implements Serializable {

    private static final long serialVersionUID = -7499616478998041681L;
    private Price paymentMaxLimit;
    private Price rewardsMinLimit;

    public Price getPaymentMaxLimit() {
        return paymentMaxLimit;
    }

    public void setPaymentMaxLimit(final Price paymentMaxLimit) {
        this.paymentMaxLimit = paymentMaxLimit;
    }

    public Price getRewardsMinLimit() {
        return rewardsMinLimit;
    }

    public void setRewardsMinLimit(final Price rewardsMinLimit) {
        this.rewardsMinLimit = rewardsMinLimit;
    }
}
