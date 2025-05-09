package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
* Created by srukmagathan on 7/22/2016.
 */
public class PurchaseSelectionInfoPPC extends PurchaseSelectionInfo{

    private String jwt;

    private String cartId;

    public String getJwt(){
         return jwt;
    }

    public void setJwt(final String jwt) {
        this.jwt = jwt;
    }

    private boolean isPayrollProviderRedirect;

    private String email;

    private String employeeId;

    private String employerId;

    private String employerName;

    private String clientId;

    private String programId;

    private String payFrequency;

    private String term;

    @JsonProperty("auth_token")
    private String accessToken;

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isPayrollProviderRedirect() {
        return isPayrollProviderRedirect;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getPayFrequency() {
        return payFrequency;
    }

    public void setPayFrequency(String payFrequency) {
        this.payFrequency = payFrequency;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setIsPayrollProviderRedirect(boolean isPayrollProviderRedirect) {
        this.isPayrollProviderRedirect = isPayrollProviderRedirect;
    }
}
