package com.b2s.rewards.apple.model;

/**
 * Created by rpillai on 6/11/2018.
 */
public class Failure {

    private String messageId;

    public Failure(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
