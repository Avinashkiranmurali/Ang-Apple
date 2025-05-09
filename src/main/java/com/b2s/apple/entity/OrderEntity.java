package com.b2s.apple.entity;

import com.b2s.rewards.apple.model.OrderAttributeValue;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "orders")
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = -3668015537849453057L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "supplier_id")
    private String supplierId;
    @Column(name = "var_id")
    private String varId;
    @Column(name = "program_id")
    private String programId;
    @Column(name = "order_date")
    private Date orderDate;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "firstname")
    private String firstName;
    @Column(name = "lastname")
    private String lastName;
    @Column(name = "addr1")
    private String addr1;
    @Column(name = "addr2")
    private String addr2;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Column(name = "zip")
    private String zip;
    @Column(name = "country")
    private String country;
    @Column(name = "phone")
    private String phone;
    @Column(name = "email")
    private String email;
    @Column(name = "last_update")
    private Date lastUpdate;
    @Column(name = "user_points")
    private Integer userPoints;
    @Column(name = "is_apply_super_saver_shipping")
    private String isApplySuperSaverShipping;
    @Column(name = "gift_message")
    private String giftMessage;
    @Column(name = "ship_desc")
    private String shipDesc;
    @Column(name = "var_order_id")
    private String varOrderId;
    @Column(name = "order_source")
    private String orderSource;
    @Column(name = "notification_type")
    private String notificationType;
    @Column(name = "app_version")
    private Integer appVersion;
    @Column(name = "PROXY_USER")
    private String proxyUserId;
    @Column(name = "language_code")
    private String languageCode;
    @Column(name = "country_code")
    private String countryCode;
    @Column(name = "currency_code")
    private String currencyCode;
    @Column(name = "business_name")
    private String businessName;
    @Column(name = "phone_alternate")
    private String phoneAlternate;
    @Column(name = "addr3")
    private String addr3;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "email_changed")
    private String isEmailChanged;
    @Column(name = "address_changed")
    private String isAddressChanged;
    @Column(name = "gst_amount")
    private Double gstAmount;
    @Column(name = "earned_points")
    private Integer earnedPoints;
    @Column(name = "establishment_fees_points")
    private Integer establishmentFeesPoints;
    @Column(name = "establishment_fees_price")
    private Double establishmentFeesPrice;


    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id" , insertable = false, updatable = false)
    private List<OrderAttributeValue> orderAttributeValues;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id" , insertable = false, updatable = false)
    private Set<OrderLineEntity> orderLines;


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final String supplierId) {
        this.supplierId = supplierId;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(final String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(final String programId) {
        this.programId = programId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(final Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(final String addr1) {
        this.addr1 = addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(final String addr2) {
        this.addr2 = addr2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(final String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Integer getUserPoints() {
        return userPoints;
    }

    public void setUserPoints(final Integer userPoints) {
        this.userPoints = userPoints;
    }

    public String getIsApplySuperSaverShipping() {
        return isApplySuperSaverShipping;
    }

    public void setIsApplySuperSaverShipping(final String isApplySuperSaverShipping) {
        this.isApplySuperSaverShipping = isApplySuperSaverShipping;
    }

    public String getGiftMessage() {
        return giftMessage;
    }

    public void setGiftMessage(final String giftMessage) {
        this.giftMessage = giftMessage;
    }

    public String getShipDesc() {
        return shipDesc;
    }

    public void setShipDesc(final String shipDesc) {
        this.shipDesc = shipDesc;
    }

    public String getVarOrderId() {
        return varOrderId;
    }

    public void setVarOrderId(final String varOrderId) {
        this.varOrderId = varOrderId;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(final String orderSource) {
        this.orderSource = orderSource;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(final String notificationType) {
        this.notificationType = notificationType;
    }

    public Integer getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(final Integer appVersion) {
        this.appVersion = appVersion;
    }

    public String getProxyUserId() {
        return proxyUserId;
    }

    public void setProxyUserId(final String proxyUserId) {
        this.proxyUserId = proxyUserId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(final String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(final String businessName) {
        this.businessName = businessName;
    }

    public String getPhoneAlternate() {
        return phoneAlternate;
    }

    public void setPhoneAlternate(final String phoneAlternate) {
        this.phoneAlternate = phoneAlternate;
    }

    public String getAddr3() {
        return addr3;
    }

    public void setAddr3(final String addr3) {
        this.addr3 = addr3;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIsEmailChanged() {
        return isEmailChanged;
    }

    public void setIsEmailChanged(final String isEmailChanged) {
        this.isEmailChanged = isEmailChanged;
    }

    public String getIsAddressChanged() {
        return isAddressChanged;
    }

    public void setIsAddressChanged(final String isAddressChanged) {
        this.isAddressChanged = isAddressChanged;
    }


    public Set<OrderLineEntity> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(final Set<OrderLineEntity> orderLines) {
        this.orderLines = orderLines;
    }

    public Double getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(final Double gstAmount) {
        this.gstAmount = gstAmount;
    }

    public Integer getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(final Integer earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    public List<OrderAttributeValue> getOrderAttributeValues() {
        return orderAttributeValues;
    }

    public void setOrderAttributeValues(final List<OrderAttributeValue> orderAttributeValues) {
        this.orderAttributeValues = orderAttributeValues;
    }

    public Integer getEstablishmentFeesPoints() {
        return establishmentFeesPoints;
    }

    public void setEstablishmentFeesPoints(final Integer establishmentFeesPoints) {
        this.establishmentFeesPoints = establishmentFeesPoints;
    }

    public Double getEstablishmentFeesPrice() {
        return establishmentFeesPrice;
    }

    public void setEstablishmentFeesPrice(final Double establishmentFeesPrice) {
        this.establishmentFeesPrice = establishmentFeesPrice;
    }
}
