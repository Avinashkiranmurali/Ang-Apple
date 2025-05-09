package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.annotation.JsonProperty;


/*** Created by rpillai on 2/5/2016.
 */
public class PurchaseSelectionInfoVitality extends PurchaseSelectionInfo {

    @JsonProperty("MemberID")
    private String memberID;
    @JsonProperty("SupplierID")
    private Integer supplierID;
    @JsonProperty("TransactionDate")
    private String transactionDate;
    @JsonProperty("PurchaseReference")
    private String purchaseReference;
    @JsonProperty("PurchaseReferenceType")
    private String purchaseReferenceType;
    @JsonProperty("ProductID")
    private String productID;
    @JsonProperty("CurrencyType")
    private String currencyType;
    private String pretaxtotal;
    private String posttaxtotal;
    @JsonProperty("TaxAmount")
    private String taxAmount;
    @JsonProperty("ActivationFee")
    private String activationFee;
    @JsonProperty("UpgradeFee")
    private String upgradeFee;
    @JsonProperty("SubsidyAmount")
    private String subsidyAmount;
    @JsonProperty("SubsidyType")
    private String subsidyType;
    @JsonProperty("MaxRepay")
    private String maxRepay;
    @JsonProperty("MaximumMonthlyRepay")
    private String maximumMonthlyRepay;
    private Integer duration;
    @JsonProperty("DurationType")
    private String durationType;
    @JsonProperty("DeliveryFullName")
    private String deliveryFullName;
    @JsonProperty("DeliveryAddressLine1")
    private String deliveryAddressLine1;
    @JsonProperty("DeliveryAddressLine2")
    private String deliveryAddressLine2;
    @JsonProperty("DeliveryCity")
    private String deliveryCity;
    @JsonProperty("DeliveryState")
    private String deliveryState;
    @JsonProperty("DeliveryCountry")
    private String deliveryCountry;
    @JsonProperty("DeliveryZipcode")
    private String deliveryZipCode;
    private String email;
    @JsonProperty("PhoneNumber")
    private String phoneNumber;
    private String itemDescription;
    private String purchaseRef;
    private String purchaseRefType;
    @JsonProperty("TotalFinanceAmount")
    private String totalFinanceAmount;
    @JsonProperty("EmployerId")
    private String employerId;
    @JsonProperty("TenantId")
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(final String tenantId) {
        this.tenantId = tenantId;
    }

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(final String employerId) {
        this.employerId = employerId;
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public Integer getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(Integer supplierID) {
        this.supplierID = supplierID;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getPurchaseReference() {
        return purchaseReference;
    }

    public void setPurchaseReference(String purchaseReference) {
        this.purchaseReference = purchaseReference;
    }

    public String getPurchaseReferenceType() {
        return purchaseReferenceType;
    }

    public void setPurchaseReferenceType(String purchaseReferenceType) {
        this.purchaseReferenceType = purchaseReferenceType;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public String getPretaxtotal() {
        return pretaxtotal;
    }

    public void setPretaxtotal(String pretaxtotal) {
        this.pretaxtotal = pretaxtotal;
    }

    public String getPosttaxtotal() {
        return posttaxtotal;
    }

    public void setPosttaxtotal(String posttaxtotal) {
        this.posttaxtotal = posttaxtotal;
    }

    public String getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getActivationFee() {
        return activationFee;
    }

    public void setActivationFee(String activationFee) {
        this.activationFee = activationFee;
    }

    public String getUpgradeFee() {
        return upgradeFee;
    }

    public void setUpgradeFee(String upgradeFee) {
        this.upgradeFee = upgradeFee;
    }

    public String getSubsidyAmount() {
        return subsidyAmount;
    }

    public void setSubsidyAmount(String subsidyAmount) {
        this.subsidyAmount = subsidyAmount;
    }

    public String getSubsidyType() {
        return subsidyType;
    }

    public void setSubsidyType(String subsidyType) {
        this.subsidyType = subsidyType;
    }

    public String getMaxRepay() {
        return maxRepay;
    }

    public void setMaxRepay(String maxRepay) {
        this.maxRepay = maxRepay;
    }

    public String getMaximumMonthlyRepay() {
        return maximumMonthlyRepay;
    }

    public void setMaximumMonthlyRepay(String maximumMonthlyRepay) {
        this.maximumMonthlyRepay = maximumMonthlyRepay;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public String getDeliveryFullName() {
        return deliveryFullName;
    }

    public void setDeliveryFullName(String deliveryFullName) {
        this.deliveryFullName = deliveryFullName;
    }

    public String getDeliveryAddressLine1() {
        return deliveryAddressLine1;
    }

    public void setDeliveryAddressLine1(String deliveryAddressLine1) {
        this.deliveryAddressLine1 = deliveryAddressLine1;
    }

    public String getDeliveryAddressLine2() {
        return deliveryAddressLine2;
    }

    public void setDeliveryAddressLine2(String deliveryAddressLine2) {
        this.deliveryAddressLine2 = deliveryAddressLine2;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(String deliveryState) {
        this.deliveryState = deliveryState;
    }

    public String getDeliveryCountry() {
        return deliveryCountry;
    }

    public void setDeliveryCountry(String deliveryCountry) {
        this.deliveryCountry = deliveryCountry;
    }

    public String getDeliveryZipCode() {
        return deliveryZipCode;
    }

    public void setDeliveryZipCode(String deliveryZipCode) {
        this.deliveryZipCode = deliveryZipCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getPurchaseRef() {
        return purchaseRef;
    }

    public void setPurchaseRef(String purchaseRef) {
        this.purchaseRef = purchaseRef;
    }

    public String getPurchaseRefType() {
        return purchaseRefType;
    }

    public void setPurchaseRefType(String purchaseRefType) {
        this.purchaseRefType = purchaseRefType;
    }

    public String getTotalFinanceAmount() {
        return totalFinanceAmount;
    }

    public void setTotalFinanceAmount(String totalFinanceAmount) {
        this.totalFinanceAmount = totalFinanceAmount;
    }

}
