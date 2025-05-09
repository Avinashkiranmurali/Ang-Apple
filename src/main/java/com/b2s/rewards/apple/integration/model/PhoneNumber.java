package com.b2s.rewards.apple.integration.model;

/**
 * Created by rperumal on 8/28/2015.
 */
public class PhoneNumber {
    private String extension;
    private String number;
    private String type;  //TODO: enum

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
