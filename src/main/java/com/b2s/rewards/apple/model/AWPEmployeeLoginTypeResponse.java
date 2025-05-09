package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by kselvamani on 10/26/2018.
 */
@JsonDeserialize(builder = AWPEmployeeLoginTypeResponse.Builder.class)
public class AWPEmployeeLoginTypeResponse {

    private final String emailId;
    private final String loginType;
    private final String url;
    private final String locale;
    private final String employerId;
    private final String country;
    private Set<String> errors;

    private AWPEmployeeLoginTypeResponse(final Builder builder){
        this.emailId =  builder.emailId;
        this.loginType = builder.loginType;
        this.url = builder.url;
        this.locale = builder.locale;
        this.employerId = builder.employerId;
        this.country = builder.country;
        this.errors = builder.errors;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getLocale() {
        return locale;
    }

    public String getUrl() {
        return url;
    }

    public String getLoginType() {
        return loginType;
    }

    public String getEmployerId() {
        return employerId;
    }

    public String getCountry() {
        return country;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder {
        public  String emailId;
        public  String loginType;
        public  String url;
        public  String locale;
        public  String employerId;
        public  String country;
        private Set<String> errors = new HashSet<>();

        private Builder()
        {

        }

        public Builder withEmailId(final String emailId){
            this.emailId = emailId;
            return this;
        }
        public Builder withLoginType(final String loginType){
            this.loginType = loginType;
            return this;
        }
        public Builder withUrl(final String url){
            this.url = url;
            return this;
        }
        public Builder withLocale(final String locale){
            this.locale = locale;
            return this;
        }

        public Builder withEmployerId(final String employerId){
            this.employerId = employerId;
            return this;
        }
        public Builder withCountry(final String country){
            this.country = country;
            return this;
        }

        public Builder withErrors(final Set<String> errors) {
            if(Objects.nonNull(errors)) {
                this.errors.addAll(errors);
            }
            return this;
        }

        public AWPEmployeeLoginTypeResponse build(){
            return new AWPEmployeeLoginTypeResponse(this);
        }
    }

}
