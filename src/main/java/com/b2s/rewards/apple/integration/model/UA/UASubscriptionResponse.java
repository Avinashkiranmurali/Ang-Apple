package com.b2s.rewards.apple.integration.model.UA;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by srukmagathan on 28-08-2018.
 */
@JsonDeserialize(builder = UASubscriptionResponse.Builder.class)
public class UASubscriptionResponse {

    private String callDuration;
    private String transactionID;
    private List<SubscriptionObject> subscriptionData;
    private Exception exception;

    private UASubscriptionResponse(final Builder builder){
        this.callDuration=builder.callDuration;
        this.transactionID=builder.transactionID;
        this.subscriptionData=builder.subscriptionData;
        this.exception=builder.exception;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public List<SubscriptionObject> getSubscriptionData() {
        return subscriptionData;
    }

    public Exception getException() {
        return exception;
    }

    public static class Builder{
        private String callDuration;
        private String transactionID;
        private List<SubscriptionObject> subscriptionData=new ArrayList<>();
        private Exception exception;


        public Builder withCallDuration(final String callDuration){
            this.callDuration=callDuration;
            return this;
        }

        public Builder withTransactionID(final String transactionID){
            this.transactionID=transactionID;
            return this;
        }

        public Builder withSubscriptionData(final List<SubscriptionObject> subscriptionData){
            this.subscriptionData.addAll(subscriptionData);
            return this;
        }

        public Builder withException(final Exception exception){
            this.exception=exception;
            return this;
        }

        public UASubscriptionResponse build(){
            return new UASubscriptionResponse(this);
        }
    }

}
