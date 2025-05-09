package com.b2s.shop.common;

import com.b2s.rewards.apple.integration.model.DelayedShippingInfo;
import com.b2s.rewards.apple.model.*;
import com.b2s.spark.api.apple.to.impl.Address;
import com.b2s.spark.api.apple.to.impl.ContactInfo;

import java.util.Date;
import java.util.List;

/**
 * Created by vmurugesan on 10/3/2016.
 */
public class OrderHistory {

    private int orderId;
    private String displayOrderId;
    private boolean showVarOrderId;
    private Date orderDate;
    private int shipments;
    private Price orderTotal;
    private Price orderSubTotal;
    private Price totalTax;
    private Price shippingCost;
    private Price totalFee;
    private int items;
    private int remainingBalance;
    private List<OrderLineInfo> lineItems;
    private Address deliveryAddress;
    private BillTo billTo;
    private ContactInfo contactInfo;
    private String purchasedPriceCurrency;
    private Price refundTotal;
    private Price totalDiscount;
    private String gstAmount;
    private int earnedPoints;
    private String imageURL;
    private PaymentInfo paymentInfo = new PaymentInfo();
    private RefundSummary refundSummary;

    public RefundSummary getRefundSummary() {
        return refundSummary;
    }

    public void setRefundSummary(final RefundSummary refundSummary) {
        this.refundSummary = refundSummary;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(final String imageURL) {
        this.imageURL = imageURL;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(final ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(final Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public BillTo getBillTo() {
        return billTo;
    }

    public void setBillTo(final BillTo billTo) {
        this.billTo = billTo;
    }

    public String getPurchasedPriceCurrency() {
        return purchasedPriceCurrency;
    }

    public void setPurchasedPriceCurrency(final String purchasedPriceCurrency) {
        this.purchasedPriceCurrency = purchasedPriceCurrency;
    }

    public Integer getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(final Integer remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public void setItems(final Integer items) {
        this.items = items;
    }

    public Price getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Price totalTax) {
        this.totalTax = totalTax;
    }

    public Price getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Price totalFee) {
        this.totalFee = totalFee;
    }

    public Price getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(Price orderTotal) {
        this.orderTotal = orderTotal;
    }

    public Price getOrderSubTotal() {
        return orderSubTotal;
    }

    public Price getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(final Price shippingCost) {
        this.shippingCost = shippingCost;
    }

    public void setOrderSubTotal(Price orderSubTotal) {
        this.orderSubTotal = orderSubTotal;
    }

    public Integer getItems() {
        return items;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(final Integer orderId) {
        this.orderId = orderId;
    }

    public String getDisplayOrderId() {
        return displayOrderId;
    }

    public void setDisplayOrderId(String displayOrderId) {
        this.displayOrderId = displayOrderId;
    }

    public boolean isShowVarOrderId() {
        return showVarOrderId;
    }

    public void setShowVarOrderId(boolean showVarOrderId) {
        this.showVarOrderId = showVarOrderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(final Date orderDate) {
        this.orderDate = orderDate;
    }


    public Integer getShipments() {
        return shipments;
    }

    public void setShipments(final Integer shipments) {
        this.shipments = shipments;
    }

    public List<OrderLineInfo> getLineItems() {
        return lineItems;
    }

    public void setLineItems(final List<OrderLineInfo> lineItems) {
        this.lineItems = lineItems;
    }

    public Price getRefundTotal() {
        return refundTotal;
    }

    public void setRefundTotal(final Price refundTotal) {
        this.refundTotal = refundTotal;
    }

    public void setTotalDiscount(final Price totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Price getTotalDiscount() {
        return totalDiscount;
    }

    public String getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(final String gstAmount) {
        this.gstAmount = gstAmount;
    }

    public int getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(final int earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(final PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public class OrderLineInfo {

        private int orderLineID;
        private String sku;
        private String itemName;
        private String itemImageURL;
        private String currencyType;
        private Integer quantity;

        private Price unitPrice;
        private Price price;
        private Price unpromotedUnitPrice;
        private Price unpromotedPrice;
        private String status;
        private ShipmentDeliveryInfo shipmentInfo;
        private DelayedShippingInfo delayedShippingInfo;
        private Engrave engrave;
        private String shippingMethod;
        private Price refund;
        private boolean refundStatus;
        private String shippingAvailability;
        private Date shippingAvailabilityDate;
        private List<String> productOptions;
        private OrderLineProgress orderLineProgress;
        private boolean isGift;

        public boolean getIsGift() {
            return isGift;
        }

        public void setIsGift(final boolean isGift) {
            this.isGift = isGift;
        }

        public List<String> getProductOptions() {
            return productOptions;
        }

        public void setProductOptions(final List<String> productOptions) {
            this.productOptions = productOptions;
        }

        public Date getShippingAvailabilityDate() {
            return shippingAvailabilityDate;
        }

        public void setShippingAvailabilityDate(final Date shippingAvailabilityDate) {
            this.shippingAvailabilityDate = shippingAvailabilityDate;
        }

        public Price getPrice() {
            return price;
        }

        public void setPrice(final Price price) {
            this.price = price;
        }

        public Price getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(final Price unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Price getUnpromotedUnitPrice() {
            return unpromotedUnitPrice;
        }

        public void setUnpromotedUnitPrice(Price unpromotedUnitPrice) {
            this.unpromotedUnitPrice = unpromotedUnitPrice;
        }

        public Price getUnpromotedPrice() {
            return unpromotedPrice;
        }

        public void setUnpromotedPrice(Price unpromotedPrice) {
            this.unpromotedPrice = unpromotedPrice;
        }

        public String getCurrencyType() {
            return currencyType;
        }

        public void setCurrencyType(final String currencyType) {
            this.currencyType = currencyType;
        }

        public String getItemImageURL() {
            return itemImageURL;
        }

        public void setItemImageURL(final String itemImageURL) {
            this.itemImageURL = itemImageURL;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(final String itemName) {
            this.itemName = itemName;
        }

        public int getOrderLineID() {
            return orderLineID;
        }

        public void setOrderLineID(final int orderLineID) {
            this.orderLineID = orderLineID;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(final Integer quantity) {
            this.quantity = quantity;
        }

        public ShipmentDeliveryInfo getShipmentInfo() {
            return shipmentInfo;
        }

        public void setShipmentInfo(final ShipmentDeliveryInfo shipmentInfo) {
            this.shipmentInfo = shipmentInfo;
        }

        public DelayedShippingInfo getDelayedShippingInfo() {
            return delayedShippingInfo;
        }

        public void setDelayedShippingInfo(final DelayedShippingInfo delayedShippingInfo) {
            this.delayedShippingInfo = delayedShippingInfo;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(final String sku) {
            this.sku = sku;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(final String status) {
            this.status = status;
        }

        public Engrave getEngrave() {
            return engrave;
        }

        public void setEngrave(Engrave engrave) {
            this.engrave = engrave;
        }

        public String getShippingMethod() {
            return shippingMethod;
        }

        public void setShippingMethod(String shippingMethod) {
            this.shippingMethod = shippingMethod;
        }


        public Price getRefund() {
            return refund;
        }

        public void setRefund(final Price refund) {
            this.refund = refund;
        }

        public boolean isRefundStatus() {
            return refundStatus;
        }

        public void setRefundStatus(final boolean refundStatus) {
            this.refundStatus = refundStatus;
        }

        public String getShippingAvailability() {
            return shippingAvailability;
        }

        public void setShippingAvailability(final String shippingAvailability) {
            this.shippingAvailability = shippingAvailability;
        }

        public OrderLineProgress getOrderLineProgress() {
            return orderLineProgress;
        }

        public void setOrderLineProgress(final OrderLineProgress orderLineProgress) {
            this.orderLineProgress = orderLineProgress;
        }
    }

    public class ShipmentDeliveryInfo {

        private Date shipmentDate;
        private String trackingID;
        private String carrierName;
        private Date deliveryDate;

        public String getCarrierName() {
            return carrierName;
        }

        public void setCarrierName(final String carrierName) {
            this.carrierName = carrierName;
        }

        public Date getShipmentDate() {
            return shipmentDate;
        }

        public void setShipmentDate(final Date shipmentDate) {
            this.shipmentDate = shipmentDate;
        }

        public String getTrackingID() {
            return trackingID;
        }

        public void setTrackingID(final String trackingID) {
            this.trackingID = trackingID;
        }

        public Date getDeliveryDate() {
            return deliveryDate;
        }

        public void setDeliveryDate(Date deliveryDate) {
            this.deliveryDate = deliveryDate;
        }
    }
}
