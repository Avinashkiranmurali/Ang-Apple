package com.b2s.rewards.apple.integration.model;

import java.util.Optional;

/**
 * @author rpillai
 */
public class AccountInfo {

    private AccountBalance accountBalance;
    private AccountStatus accountStatus;
    private UserInformation userInformation;
    private String pricingTier;
    private Optional<String> programId = Optional.empty();


    public AccountBalance getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(AccountBalance accountBalance) {
        this.accountBalance = accountBalance;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
    public UserInformation getUserInformation() {
        return userInformation;
    }

    public void setUserInformation(UserInformation userInformation) {
        this.userInformation = userInformation;
    }

    public String getPricingTier() {
        return pricingTier;
    }

    public void setPricingTier(final String pricingTier) {
        this.pricingTier = pricingTier;
    }

    public Optional<String> getProgramId() {
        return programId;
    }

    public void setProgramId(final Optional<String> programId) {
        this.programId = programId;
    }
}
