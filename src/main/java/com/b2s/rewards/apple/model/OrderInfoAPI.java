package com.b2s.rewards.apple.model;

import com.b2s.spark.api.apple.to.impl.ContactInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Created by srukmagathan on 28-07-2017.
 * Class is intent to be used only for Order API.
 * Dont reuse for any other purpose
 *
 */
public class OrderInfoAPI {

    private Long orderID;
    private String userID;
    private String entityID;
    private String programID;
    private String orderDate;
    private List<OrderLineInfo> orderItems;
    private DeliveryAddress deliveryAddress;
    private ContactInfo contactInfo;
    private OrderAttributes orderAttributes;
    private OrderTotals orderTotals;
    private PaymentInfo paymentInfo;

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public OrderTotals getOrderTotals() {
        return orderTotals;
    }

    public void setOrderTotals(OrderTotals orderTotals) {
        this.orderTotals = orderTotals;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(final ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(final DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(final String entityID) {
        this.entityID = entityID;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(final String orderDate) {
        this.orderDate = orderDate;
    }

    public Long getOrderID() {
        return orderID;
    }

    public void setOrderID(final Long orderID) {
        this.orderID = orderID;
    }

    public List<OrderLineInfo> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(final List<OrderLineInfo> orderItems) {
        this.orderItems = orderItems;
    }

    public String getProgramID() {
        return programID;
    }

    public void setProgramID(final String programID) {
        this.programID = programID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(final String userID) {
        this.userID = userID;
    }

    public OrderAttributes getOrderAttributes() {
        return orderAttributes;
    }

    public void setOrderAttributes(OrderAttributes orderAttributes) {
        this.orderAttributes = orderAttributes;
    }


    public static class DeliveryAddress {

        private String firstName;
        private String lastName;
        private String addressLine1;
        private String addressLine2;
        private Optional<String> addressLine3 = Optional.empty();
        private Optional<String> addressLine4 = Optional.empty();
        private String city;
        private String state;
        private String country;
        private String postalCode;

        public Optional<String> getAddressLine3() {
            return addressLine3;
        }

        public void setAddressLine3(String addressLine3) {
            this.addressLine3 = Optional.ofNullable(addressLine3);
        }

        public Optional<String> getAddressLine4() {
            return addressLine4;
        }

        public void setAddressLine4(String addressLine4) {
            this.addressLine4 = Optional.ofNullable(addressLine4);
        }

        public String getAddressLine1() {
            return addressLine1;
        }

        public void setAddressLine1(final String addressLine1) {
            this.addressLine1 = addressLine1;
        }

        public String getAddressLine2() {
            return addressLine2;
        }

        public void setAddressLine2(final String addressLine2) {
            this.addressLine2 = addressLine2;
        }

        public String getCity() {
            return city;
        }

        public void setCity(final String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(final String country) {
            this.country = country;
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

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(final String postalCode) {
            this.postalCode = postalCode;
        }

        public String getState() {
            return state;
        }

        public void setState(final String state) {
            this.state = state;
        }

    }

    public static class OrderLineInfo {

        private int orderLineID;
        private String sku;
        private String itemName;
        private String itemImageURL;
        private String currencyType;
        private Integer quantity;

        private String supplierItemPrice;
        private String itemPrice;
        private String itemTax;
        private String itemFee;
        private String status;
        private String category;
        private ShipmentDeliveryInfo shipmentInfo;
        private BigDecimal upgradePrice;
        private String itemPaymentPerPeriod;


        public int getOrderLineID() {
            return orderLineID;
        }

        public void setOrderLineID(final int orderLineID) {
            this.orderLineID = orderLineID;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(final String sku) {
            this.sku = sku;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(final String itemName) {
            this.itemName = itemName;
        }

        public String getItemImageURL() {
            return itemImageURL;
        }

        public void setItemImageURL(final String itemImageURL) {
            this.itemImageURL = itemImageURL;
        }

        public String getCurrencyType() {
            return currencyType;
        }

        public void setCurrencyType(final String currencyType) {
            this.currencyType = currencyType;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(final Integer quantity) {
            this.quantity = quantity;
        }

        public String getSupplierItemPrice() {
            return supplierItemPrice;
        }

        public void setSupplierItemPrice(final String supplierItemPrice) {
            this.supplierItemPrice = supplierItemPrice;
        }

        public String getItemPrice() {
            return itemPrice;
        }

        public void setItemPrice(final String itemPrice) {
            this.itemPrice = itemPrice;
        }

        public String getItemTax() {
            return itemTax;
        }

        public void setItemTax(final String itemTax) {
            this.itemTax = itemTax;
        }

        public String getItemFee() {
            return itemFee;
        }

        public void setItemFee(final String itemFee) {
            this.itemFee = itemFee;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(final String status) {
            this.status = status;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public ShipmentDeliveryInfo getShipmentInfo() {
            return shipmentInfo;
        }

        public void setShipmentInfo(final ShipmentDeliveryInfo shipmentInfo) {
            this.shipmentInfo = shipmentInfo;
        }

        public BigDecimal getUpgradePrice() {
            return upgradePrice;
        }

        public void setUpgradePrice(BigDecimal upgradePrice) {
            this.upgradePrice = upgradePrice;
        }

        public String getItemPaymentPerPeriod() {
            return itemPaymentPerPeriod;
        }

        public void setItemPaymentPerPeriod(String itemPaymentPerPeriod) {
            this.itemPaymentPerPeriod = itemPaymentPerPeriod;
        }
    }

    public static class ShipmentDeliveryInfo {

        private String shipmentDate;
        private String trackingID;
        private String carrierName;


        public String getCarrierName() {
            return carrierName;
        }

        public void setCarrierName(final String carrierName) {
            this.carrierName = carrierName;
        }

        public String getShipmentDate() {
            return shipmentDate;
        }

        public void setShipmentDate(final String shipmentDate) {
            this.shipmentDate = shipmentDate;
        }

        public String getTrackingID() {
            return trackingID;
        }

        public void setTrackingID(final String trackingID) {
            this.trackingID = trackingID;
        }

    }

    public static class OrderAttributes {

        private String locale;
        private String currencyCode;
        private String employerID;
        private String country;
        private String programType;

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public String getEmployerID() {
            return employerID;
        }

        public void setEmployerID(String employerID) {
            this.employerID = employerID;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getProgramType() {
            return programType;
        }

        public void setProgramType(String programType) {
            this.programType = programType;
        }
    }

    public static class OrderTotals {

        private String supplierItemTotal;
        private String itemTotal;
        private String taxTotal;
        private String feeTotal;
        private String orderTotal;
        private String upgradeTotal;

        public String getSupplierItemTotal() {
            return supplierItemTotal;
        }

        public void setSupplierItemTotal(String supplierItemTotal) {
            this.supplierItemTotal = supplierItemTotal;
        }

        public String getItemTotal() {
            return itemTotal;
        }

        public void setItemTotal(String itemTotal) {
            this.itemTotal = itemTotal;
        }

        public String getTaxTotal() {
            return taxTotal;
        }

        public void setTaxTotal(String taxTotal) {
            this.taxTotal = taxTotal;
        }

        public String getFeeTotal() {
            return feeTotal;
        }

        public void setFeeTotal(String feeTotal) {
            this.feeTotal = feeTotal;
        }

        public String getOrderTotal() {
            return orderTotal;
        }

        public void setOrderTotal(String orderTotal) {
            this.orderTotal = orderTotal;
        }

        public String getUpgradeTotal() {
            return upgradeTotal;
        }

        public void setUpgradeTotal(String upgradeTotal) {
            this.upgradeTotal = upgradeTotal;
        }
    }

    public static class PaymentInfo {

        private String paymentTotal;
        private List<PaymentTender> paymentTenders;

        public String getPaymentTotal() {
            return paymentTotal;
        }

        public void setPaymentTotal(String paymentTotal) {
            this.paymentTotal = paymentTotal;
        }

        public List<PaymentTender> getPaymentTenders() {
            return paymentTenders;
        }

        public void setPaymentTenders(List<PaymentTender> paymentTenders) {
            this.paymentTenders = paymentTenders;
        }

        public static class PaymentTender {

            private String paymentType;
            private String paymentTotal;
            private Optional<String> paymentPerPeriod = Optional.empty();
            private Optional<String> payFrequency = Optional.empty();
            private Optional<Integer> numberOfPayments;
            private String currencyCode;

            public String getPaymentType() {
                return paymentType;
            }

            public void setPaymentType(String paymentType) {
                this.paymentType = paymentType;
            }

            public String getPaymentTotal() {
                return paymentTotal;
            }

            public void setPaymentTotal(String paymentTotal) {
                this.paymentTotal = paymentTotal;
            }

            public Optional<String> getPaymentPerPeriod() {
                return paymentPerPeriod;
            }

            public void setPaymentPerPeriod(String paymentPerPeriod) {
                this.paymentPerPeriod = Optional.ofNullable(paymentPerPeriod);
            }

            public Optional<String> getPayFrequency() {
                return payFrequency;
            }

            public void setPayFrequency(String payFrequency) {
                this.payFrequency = Optional.ofNullable(payFrequency);
            }

            public Optional<Integer> getNumberOfPayments() {
                return numberOfPayments;
            }

            public void setNumberOfPayments(Integer numberOfPayments) {
                this.numberOfPayments = Optional.ofNullable(numberOfPayments);
            }

            public String getCurrencyCode() {
                return currencyCode;
            }

            public void setCurrencyCode(String currencyCode) {
                this.currencyCode = currencyCode;
            }
        }


    }





}
