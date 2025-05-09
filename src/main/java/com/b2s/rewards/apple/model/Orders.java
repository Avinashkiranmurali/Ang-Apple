package com.b2s.rewards.apple.model;

import com.b2s.db.model.BundledPricingOption;
import com.b2s.rewards.common.util.CommonConstants;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by rperumal on 4/28/16
 */


@Entity
@Table(name="orders")
public class Orders {

    @Id
    @Column(name = "order_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @JoinColumn(name="order_id", insertable=false, updatable =false)
    @OneToMany(fetch = FetchType.EAGER)
    private List<OrderLineItem> lineItemList;

    @JoinColumn(name="order_id", insertable=false, updatable =false)
    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<OrderAttributeValue> orderAttributeValueList;





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

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "user_points")
    private Integer userPoints;

    @Column(name = "var_order_id")
    private String varOrderId;

    @Column(name = "order_source")
    private String orderSource;

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

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "currency_code")
    private String currencyCode;

    @Column(name = "gst_amount")
    private Double gstAmount;

    @Column(name = "earned_points")
    private Integer earnedPoints;

    public List<OrderLineItem> getLineItemList() {
        return lineItemList;
    }

    public void setLineItemList(final List<OrderLineItem> lineItemList) {
        this.lineItemList = lineItemList;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(final Integer orderId) {
        this.orderId = orderId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
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

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Integer getUserPoints() {
        return userPoints;
    }

    public void setUserPoints(Integer userPoints) {
        this.userPoints = userPoints;
    }

    public String getVarOrderId() {
        return varOrderId;
    }

    public void setVarOrderId(String varOrderId) {
        this.varOrderId = varOrderId;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
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

    public List<OrderAttributeValue> getOrderAttributeValueList() {
        return orderAttributeValueList;
    }

    public void setOrderAttributeValueList(
        final List<OrderAttributeValue> orderAttributeValueList) {
        this.orderAttributeValueList = orderAttributeValueList;
    }

    public int getOrderTotalInPoints() {
        final List<OrderLineItem> orderLinesList = getLineItemList();
        int total = 0;
        for (final OrderLineItem line : orderLinesList) {
            if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S) && !line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S)) {
                if(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S.equals(line.getSupplierId())){
                    total = total - line.getOrderLinePoints() * line.getQuantity();
                }else {
                    total += line.getOrderLinePoints() * line.getQuantity();
                }
            }
        }
        if(total < 0){
            total = 0;
        }
        return total;
    }

    public Money getOrderTotalInMoney(final BigDecimal subTotal, BundledPricingOption bundledPricingOption){
        BigDecimal total = subTotal.multiply(BigDecimal.valueOf(CommonConstants.CENTS_TO_DOLLARS_DIVISOR));
        if (BundledPricingOption.BUNDLED != bundledPricingOption) {
            final List<OrderLineItem> orderLinesList = getLineItemList();
            for (final OrderLineItem line : orderLinesList) {
                total = getTotal(total, line);
            }
        }

        if (total.compareTo(BigDecimal.ZERO) < 0){
            total = BigDecimal.ZERO;
        }
        return Money.ofMinor(CurrencyUnit.of(new Locale(this.getLanguageCode(), this.getCountryCode())),
            total.longValue());
    }

    /**
     *
     * @param total
     * @param line
     * @return
     */
    private BigDecimal getTotal(BigDecimal total, OrderLineItem line) {
        final Integer supplierItemPrice = line.getSupplierItemPrice();
        final Integer supplierTaxPrice = Objects.nonNull(line.getDiscountedTaxes()) ?
            line.getDiscountedTaxes() : line.getSupplierTaxPrice();
        final BigDecimal supplierFeePrice = Objects.nonNull(line.getDiscountedFees()) ?
            BigDecimal.valueOf(line.getDiscountedFees()) : line.getTotalFeesInMoneyMinor();

        if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S) && !line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S)) {
            if(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S.equals(line.getSupplierId())){
                total = total.subtract(BigDecimal.valueOf(supplierItemPrice).multiply(BigDecimal.valueOf(line.getQuantity())));
            }else {
                total = total.add(BigDecimal.valueOf(supplierTaxPrice).multiply(BigDecimal.valueOf(line.getQuantity())));
                total = total.add(supplierFeePrice.multiply(BigDecimal.valueOf(line.getQuantity())));
            }
        }
        return total;
    }

    public Money getOrderTotalInMoney() {
        final List<OrderLineItem> orderLinesList = getLineItemList();
        BigDecimal total = BigDecimal.ZERO;
        for (final OrderLineItem line : orderLinesList) {
            final Integer supplierItemPrice = Objects.nonNull(line.getDiscountedSupplierItemPrice()) ?
                line.getDiscountedSupplierItemPrice() : line.getSupplierItemPrice();
            final Integer supplierTaxPrice = Objects.nonNull(line.getDiscountedTaxes()) ?
                line.getDiscountedTaxes() : line.getSupplierTaxPrice();
            final BigDecimal supplierFeePrice = Objects.nonNull(line.getDiscountedFees()) ?
                BigDecimal.valueOf(line.getDiscountedFees()) : line.getTotalFeesInMoneyMinor();

            if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S) && !line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S)) {
                if(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S.equals(line.getSupplierId())){
                    total = total.subtract(BigDecimal.valueOf(supplierItemPrice).multiply(BigDecimal.valueOf(line.getQuantity())));
                }else {
                    total = total.add(BigDecimal.valueOf(supplierItemPrice)).multiply(BigDecimal.valueOf(line.getQuantity()));
                    total = total.add(BigDecimal.valueOf(supplierTaxPrice).multiply(BigDecimal.valueOf(line.getQuantity())));
                    total = total.add(supplierFeePrice.multiply(BigDecimal.valueOf(line.getQuantity())));
                }
            }
        }
        if (total.compareTo(BigDecimal.ZERO) < 0){
            total = BigDecimal.ZERO;
        }
        return Money.ofMinor(CurrencyUnit.of(new Locale(this.getLanguageCode(), this.getCountryCode())),
                total.longValue());
    }

    public Money getTotalDiscountInMoney() {
        final List<OrderLineItem> orderLinesList = getLineItemList();
        final Long total = orderLinesList.stream()
            .filter(line -> CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S.equals(line.getSupplierId()))
            .mapToLong(value -> value.getSupplierItemPrice())
            .sum();
        return Money.ofMinor(CurrencyUnit.of(new Locale(this.getLanguageCode(), this.getCountryCode())), total);
    }

    public int getTotalDiscountInPoints() {
        final List<OrderLineItem> orderLinesList = getLineItemList();
        final Integer total  = orderLinesList.stream()
            .filter(line -> CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S.equals(line.getSupplierId()))
            .mapToInt(value -> value.getOrderLinePoints())
            .sum();
        return total;
    }

    public Money getOrderSubTotalInMoney() {
        final List<OrderLineItem> orderLinesList = getLineItemList();
        BigDecimal total = BigDecimal.ZERO;
        for (final OrderLineItem line : orderLinesList) {
            final Integer supplierItemPrice = Objects.nonNull(line.getDiscountedSupplierItemPrice()) ?
                line.getDiscountedSupplierItemPrice() : line.getSupplierItemPrice();
            if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S) && !line.getSupplierId().equals
                (CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S) && !CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S.equals(line
                .getSupplierId())) {
                    total = total.add(BigDecimal.valueOf(supplierItemPrice).multiply(BigDecimal.valueOf(line.getQuantity())));
            }
        }
        return Money.ofMinor(CurrencyUnit.of(new Locale(this.getLanguageCode(), this.getCountryCode())),
                total.longValue());
    }

    public int getOrderSubTotalInPoints() {
        final List<OrderLineItem> orderLinesList = getLineItemList();
        int total = 0;
        for (final OrderLineItem line : orderLinesList) {
            if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S) && !line.getSupplierId().equals
                (CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S) && !CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S.equals(line.getSupplierId())) {
                total += line.getItemPoints() * line.getQuantity();
            }
        }
        return total;
    }

    public int getOrderTotalCashBuyInPoints() {
        int total = 0;
        final List<OrderLineItem> orderLinesList = getLineItemList();
        for (final OrderLineItem line : orderLinesList) {
            if (line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                total = total + line.getOrderLinePoints();
            }
        }
        return total;
    }

    public int getRefundedPoint(){
        int total=0;
        for(OrderLineItem lineItem: getLineItemList() ) {
            for (OrderLineAdjustment orderLineAdjustment : lineItem.getOrderLineAdjustmentList()) {
                if(orderLineAdjustment.getAdjustmentType().equalsIgnoreCase(CommonConstants
                    .ORDER_ADJUSTMENT_TYPE_P)) {
                    total += orderLineAdjustment.getPointAmount();
                }
            }
        }
        return total;
    }

    public double getRefundedPrice(){
        double total=0;
        for(OrderLineItem lineItem: getLineItemList() ) {
            for (OrderLineAdjustment orderLineAdjustment : lineItem.getOrderLineAdjustmentList()) {
                if(orderLineAdjustment.getAdjustmentType().equalsIgnoreCase(CommonConstants
                    .ORDER_ADJUSTMENT_TYPE_P)){
                    total += orderLineAdjustment.getPriceAmount();
                }
            }
        }
        return total;
    }

    public int getOrderTotalFeesInPoints() {
        int total = 0;
        for(final OrderLineItem lineItem : getLineItemList()) {
                if (!lineItem.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                    total += lineItem.getTotalFeesInPoints() * lineItem.getQuantity();
                }
        }
        return total;
    }
    public Money getOrderTotalFeesInMoney() {
        BigDecimal total = BigDecimal.ZERO;
        for (final OrderLineItem line : getLineItemList()) {
            if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                total = total.add(line.getTotalFeesInMoneyMinor().multiply(BigDecimal.valueOf(line.getQuantity())));
            }
        }
        return Money.ofMinor(CurrencyUnit.of(new Locale(this.getLanguageCode(), this.getCountryCode())),
            total.longValue());
    }

    public Money getOrderTotalShippingInMoney() {
        BigDecimal total = BigDecimal.ZERO;
        for (final OrderLineItem line : getLineItemList()) {
            if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                total = total.add(BigDecimal.valueOf(line.getSupplierShippingPrice()).multiply(BigDecimal.valueOf(line.getQuantity())));
            }
        }
        return Money.ofMinor(CurrencyUnit.of(new Locale(this.getLanguageCode(), this.getCountryCode())), total.longValue());
    }

    public int getOrderTotalShippingInPoints() {
        int total = 0;
        for (final OrderLineItem line : getLineItemList()) {
            if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                total += line.getShippingPoints() * line.getQuantity();
            }
        }
        return total;
    }

    public Money getOrderTotalTaxesInMoney() {
        final List<OrderLineItem> orderLinesList = getLineItemList();
        BigDecimal total = BigDecimal.ZERO;
        for (final OrderLineItem line : orderLinesList) {
            if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                BigDecimal totalTax = Objects.nonNull(line.getDiscountedTaxes()) ?
                    BigDecimal.valueOf(line.getDiscountedTaxes()) : line.getTotalTaxesInMoneyMinor();
                total = total.add(totalTax.multiply(BigDecimal.valueOf(line.getQuantity())));
            }
        }
        return Money.ofMinor(CurrencyUnit.of(new Locale(this.getLanguageCode(), this.getCountryCode())),
            total.longValue());
    }


    public int getOrderTotalTaxesInPoints() {
        final List<OrderLineItem> orderLinesList = getLineItemList();
        int total = 0;
        for (final OrderLineItem line : orderLinesList) {
            if (!line.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                total += line.getTaxPoints() * line.getQuantity();
            }
        }
        return total;
    }



}