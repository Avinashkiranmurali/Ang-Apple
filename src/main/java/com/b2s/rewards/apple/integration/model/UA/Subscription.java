package com.b2s.rewards.apple.integration.model.UA;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by srukmagathan on 28-08-2018.
 */
@JsonDeserialize(builder = Subscription.Builder.class)
public class Subscription {

    private String channelTypeCode;
    private String communicationTypeDescription;
    private String displayTypeCode;
    private String channelTypeSequenceNumber;
    private String communicationProgramCode;
    private String optInOrOutCode;
    private String communicationTypeCode;
    private String channelCode;

    private Subscription(Builder builder){
        this.channelTypeCode=builder.channelTypeCode;
        this.communicationTypeDescription=builder.communicationTypeDescription;
        this.displayTypeCode=builder.displayTypeCode;
        this.channelTypeSequenceNumber=builder.channelTypeSequenceNumber;
        this.communicationProgramCode=builder.communicationProgramCode;
        this.optInOrOutCode=builder.optInOrOutCode;
        this.communicationTypeCode=builder.communicationTypeCode;
        this.channelCode=builder.channelCode;
    }

    public String getChannelTypeCode() {
        return channelTypeCode;
    }

    public String getCommunicationTypeDescription() {
        return communicationTypeDescription;
    }

    public String getDisplayTypeCode() {
        return displayTypeCode;
    }

    public String getChannelTypeSequenceNumber() {
        return channelTypeSequenceNumber;
    }

    public String getCommunicationProgramCode() {
        return communicationProgramCode;
    }

    public String getOptInOrOutCode() {
        return optInOrOutCode;
    }

    public String getCommunicationTypeCode() {
        return communicationTypeCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public static class Builder{
        private String channelTypeCode;
        private String communicationTypeDescription;
        private String displayTypeCode;
        private String channelTypeSequenceNumber;
        private String communicationProgramCode;
        private String optInOrOutCode;
        private String communicationTypeCode;
        private String channelCode;


        public Builder withChannelTypeCode(final String channelTypeCode){
            this.channelTypeCode=channelTypeCode;
            return this;
        }

        public Builder withCommunicationTypeDescription (final String communicationTypeDescription){
            this.communicationTypeDescription=communicationTypeDescription;
            return this;
        }

        public Builder withDisplayTypeCode (final String displayTypeCode){
            this.displayTypeCode=displayTypeCode;
            return this;
        }

        public Builder withChannelTypeSequenceNumber (final String channelTypeSequenceNumber){
            this.channelTypeSequenceNumber=channelTypeSequenceNumber;
            return this;
        }

        public Builder withCommunicationProgramCode (final String communicationProgramCode){
            this.communicationProgramCode=communicationProgramCode;
            return this;
        }

        public Builder withOptInOrOutCode (final String optInOrOutCode ){
            this.optInOrOutCode=optInOrOutCode;
            return this;
        }

        public Builder withCommunicationTypeCode (final String communicationTypeCode){
            this.communicationTypeCode=communicationTypeCode;
            return this;
        }

        public Builder withChannelCode (final String channelCode){
            this.channelCode=channelCode;
            return this;
        }


        public Subscription build(){
            return new Subscription(this);
        }


    }

}
