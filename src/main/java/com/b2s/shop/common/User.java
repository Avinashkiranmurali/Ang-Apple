package com.b2s.shop.common;

import com.b2s.db.model.DemoUser;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.model.SessionUserInfo;
import com.b2s.rewards.apple.model.BillTo;
import com.b2s.rewards.apple.model.DiscountCode;
import com.b2s.apple.entity.DemoUserEntity;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Craig McLaughlin Date: Sep 12, 2006 To change this template use File | Settings |
 * File Templates.
 */
public class User extends DemoUser {

    private static final long serialVersionUID = 1L;

    private int supplierid = 0;
    private String category = null;
    private int process_step = 0;
    private boolean isAdmin = false;
    private boolean browseOnly = false;
    private String businessName = null;
    private String clientIpAdress = "";
    private String sessionId = "";
    private String sid = null;  // Specific for USM, cause USM identify user by sid
    private String csid = null;
    private transient BillTo billTo = null;
    private com.b2s.rewards.apple.model.Address shipTo;
    private boolean isAddressPresent;
    private List<DiscountCode> discounts;
    private boolean isEligibleForPayrollDeduction;
    private boolean isEligibleForDiscount;
    private List<com.b2s.common.services.model.Address> addresses;

    private String hostName;

    private String employerId;
    private String employerName;
    private String loginType;
    private String pricingTier;
    private boolean anonymous = false;
    private String matomoTrackerURL;
    private String matomoSiteId;

    private List<String> analytics;
    private int initialUserBalance;
    private boolean deceased;
    private boolean agentBrowse = false;

    private String hashedUserId;

    private SessionUserInfo sessionUserInfo;

    public String getHashedUserId() {
        return hashedUserId;
    }

    public void setHashedUserId(final String hashedUserId) {
        this.hashedUserId = hashedUserId;
    }

    public String getMatomoSiteId() {
        return matomoSiteId;
    }

    public void setMatomoSiteId(String matomoSiteId) {
        this.matomoSiteId = matomoSiteId;
    }

    public String getMatomoTrackerURL() {
        return matomoTrackerURL;
    }

    public void setMatomoTrackerURL(String matomoTrackerURL) {
        this.matomoTrackerURL = matomoTrackerURL;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(final String loginType) {
        this.loginType = loginType;
    }
    public String getPricingTier() {
        return pricingTier;
    }

    public void setPricingTier(final String pricingTier) {
        this.pricingTier = pricingTier;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public boolean isEligibleForDiscount() {
        return isEligibleForDiscount;
    }

    public void setIsEligibleForDiscount(boolean isEligibleForDiscount) {
        this.isEligibleForDiscount = isEligibleForDiscount;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    public String getHostName() { return this.hostName; }

    public boolean getIsEligibleForPayrollDeduction() {
        return isEligibleForPayrollDeduction;
    }

    public void setIsEligibleForPayrollDeduction(boolean isEligibleForPayrollDeduction) {
        this.isEligibleForPayrollDeduction = isEligibleForPayrollDeduction;
    }

    public List<DiscountCode> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<DiscountCode> discounts) {
        this.discounts = discounts;
    }

    public BillTo getBillTo() {
        return billTo;
    }

    public void setBillTo(BillTo billTo) {
        this.billTo = billTo;
    }

    public Address getShipTo() {
        return shipTo;
    }

    public void setShipTo(Address shipTo) {
        this.shipTo = shipTo;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(final String sid) {
        this.sid = sid;
    }

    public String getCsid() {
        return csid;
    }

    public void setCsid(String csid) {
        this.csid = csid;
    }

    private Map<String, String> additionalInfo = new HashMap<>();

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    private String imageURL = null;

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(final String imageURL) {
        this.imageURL = imageURL;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public int getInitialUserBalance() {
        return initialUserBalance;
    }

    public void setInitialUserBalance(int initialUserBalance) {
        this.initialUserBalance = initialUserBalance;
    }

    public List<com.b2s.common.services.model.Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(final List<com.b2s.common.services.model.Address> addresses) {
        this.addresses = addresses;
    }

    /**
     * Indicates if the user logged in a CSR or agent on behalf of the user.
     */
    private boolean proxyUser;
    /**
     * Id or username of the proxy user. Used when agents or CSRs log in on behalf of the user.
     */
    private String proxyUserId;

    private Locale locale = Locale.US;

    /* value will be configurable after saml update */
    private boolean isAmexInstallment = false;

    private boolean suppressTimeoutAndKeepAliveEnabled;

    public boolean getSuppressTimeoutAndKeepAliveEnabled() {
        return suppressTimeoutAndKeepAliveEnabled;
    }

    public void setSuppressTimeoutAndKeepAliveEnabled(
        final boolean suppressTimeoutAndKeepAliveEnabled) {
        this.suppressTimeoutAndKeepAliveEnabled = suppressTimeoutAndKeepAliveEnabled;
    }

    public boolean isAmexInstallment() {
        return isAmexInstallment;
    }

    public void setAmexInstallment(final boolean amexInstallment) {
        isAmexInstallment = amexInstallment;
    }

    public String getClientIpAdress() {
        return clientIpAdress;
    }

    public void setClientIpAdress(final String clientIpAdress) {
        this.clientIpAdress = clientIpAdress;
    }

    /**
     * @deprecated this is no longer valid in core
     */
    @Deprecated
    public void setAdmin(final boolean b) {
        isAdmin = b;
    }

    /**
     * @deprecated this is no longer valid in core
     */
    @Deprecated
    public boolean getIsAdmin() {
        return isAdmin;
    }

    public int getSupplierId() {
        return supplierid;
    }

    public void setSupplierId(final int o) {
        supplierid = o;
    }

    public int getProcessStep() {
        return process_step;
    }

    public void setProcessStep(final int o) {
        process_step = o;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String o) {
        category = o;
    }

    public void setUserId(final String userid) {
        this.setUserid(userid);
    }

    public String getUserId() {
        return this.getUserid();
    }

    public String getVarId() {
        return this.getVarid();
    }

    public void setVarId(final String s) {
        this.setVarid(s);
    }

    public String getProgramId() {
        return this.getProgramid();
    }

    public String getFullName() {
        return (StringUtils.isEmpty(this.getFirstName()) ? "" : this.getFirstName().trim()) + " " +
                (StringUtils.isEmpty(this.getLastName()) ? "" : this.getLastName().trim());
    }

    public int getBalance() {
        if (this.getPoints() != null) {
            return this.getPoints();
        } else {
            return 0;
        }

    }

    public void setBalance(final int s) {
        this.setPoints(s);
    }

    public void setProgramId(final String s) {
        this.setProgramid(s);
    }

    public User select(final DemoUserEntity demoUserEntity) {

        if (populateUserFromEntity(demoUserEntity)) {
            return null;
        }

        return this;

    }

    private boolean populateUserFromEntity(final DemoUserEntity demoUserEntity) {
        if (demoUserEntity == null) {
            return true;
        }

        this.setVarId(demoUserEntity.getDemoUserId().getVarId());
        this.setProgramid(demoUserEntity.getDemoUserId().getProgramId());
        this.setUserid(demoUserEntity.getDemoUserId().getUserId());
        this.setPoints(demoUserEntity.getPoints());
        this.setFirstName(demoUserEntity.getFirstname());
        this.setLastName(demoUserEntity.getLastname());
        this.setAddr1(demoUserEntity.getAddr1());
        this.setAddr2(demoUserEntity.getAddr2());
        this.setCity(demoUserEntity.getCity());
        this.setState(demoUserEntity.getState());
        this.setZip(demoUserEntity.getZip());
        this.setCountry(demoUserEntity.getCountry());
        this.setPhone(demoUserEntity.getPhone());
        this.setEmail(demoUserEntity.getEmail());
        if(this.getUserid().equalsIgnoreCase("demo")){
            populatMultipleAddresses();
        }
        return false;
    }

    /**
     *  Hardcoded test data for populating multiple address by just changing the address line 1.
     *  It will be done only to the userId - Demo
     */
    private void populatMultipleAddresses(){
        List<com.b2s.common.services.model.Address> addresses=new ArrayList<>();

        for(int i=1; i<=2;i++){
                com.b2s.common.services.model.Address.AddressBuilder  builder =
                    com.b2s.common.services.model.Address.builder();

                builder.withAddressId(i);
                if(i==2){
                    builder.withAddress1("100 Main Street");
                }else{
                    builder.withAddress1(this.getAddr1());
                }
                builder.withAddress2(this.getAddr2())
                .withCity(this.getCity())
                .withState(this.getState())
                .withCountry(this.getCountry())
                .withPhoneNumber(this.getPhone())
                .withActive(true);

            final String[] postalCode = this.getZip().split("-");
            if (postalCode.length == 2) {
                builder.withZip5(postalCode[0]);
                builder.withZip4(postalCode[1]);
            } else {
                builder.withZip5(postalCode[0]);
            }
                addresses.add(builder.build());
        }
        this.setAddresses(addresses);

    }

    public User selectUserOnly(final DemoUserEntity demoUserEntity) {

        if (populateUserFromEntity(demoUserEntity)) {
            return null;
        }

        return this;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(final String businessName) {
        this.businessName = businessName;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    // User's IP address
    private String IPAddress;

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(final String IPAddress) {
        this.IPAddress = IPAddress;
    }

    // Payment Auth random id
    private String randomId;

    public String getRandomId() {
        return randomId;
    }

    public void setRandomId(final String randomId) {
        this.randomId = randomId;
    }

    public boolean isProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(final boolean proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyUserId() {
        return proxyUserId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    public void setProxyUserId(final String proxyUserId) {
        this.proxyUserId = proxyUserId;
    }

    public boolean isAddressPresent() {
        return isAddressPresent;
    }

    public void setAddressPresent(final boolean addressPresent) {
        isAddressPresent = addressPresent;
    }

    public boolean isBrowseOnly() { return browseOnly; }

    public void setBrowseOnly(boolean browseOnly) { this.browseOnly = browseOnly; }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public List<String> getAnalytics() {
        return analytics;
    }

    public void setAnalytics(final List<String> analytics) {
        this.analytics = analytics;
    }

    public boolean isDeceased() {
        return deceased;
    }

    public void setDeceased(boolean deceased) {
        this.deceased = deceased;
    }

    public boolean isAgentBrowse() { return agentBrowse; }

    public void setAgentBrowse(final boolean agentBrowse) { this.agentBrowse = agentBrowse; }

    public SessionUserInfo getSessionUserInfo() {
        return sessionUserInfo;
    }

    public void setSessionUserInfo(SessionUserInfo sessionUserInfo) {
        this.sessionUserInfo = sessionUserInfo;
    }
}