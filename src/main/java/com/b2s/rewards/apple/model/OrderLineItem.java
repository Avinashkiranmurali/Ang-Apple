package com.b2s.rewards.apple.model;

import com.b2s.rewards.common.util.CommonConstants;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

/**
 * Created by rperumal on 4/28/2016
 */

@Entity
@Table(name = "order_line")
public class OrderLineItem implements Serializable{


    @EmbeddedId
    private OrderLineItemId id;

    @ManyToOne
    @JoinColumn(name = "order_status", insertable = false, updatable = false)
    private OrderLineStatus status;

    @OneToMany
    @JoinColumns({
            @JoinColumn(name = "line_num", insertable = false, updatable = false),
            @JoinColumn(name = "order_id",insertable = false, updatable = false),
    })
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<OrderLineItemAttribute> orderLineAttributes;


    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumns({
            @JoinColumn(name = "line_number",  referencedColumnName = "line_num",insertable = false, updatable = false),
            @JoinColumn(name = "b2s_order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
    })
    private List<OrderLineShipmentNotification> orderLineShipmentNotifications;

    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumns({
        @JoinColumn(name = "line_num",  referencedColumnName = "line_num",insertable = false, updatable = false),
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
    })
    private List<OrderLineAdjustment> orderLineAdjustmentList;

    @OneToMany(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)

    @JoinColumns({
        @JoinColumn(name = "order_line",  referencedColumnName = "line_num",insertable = false, updatable = false),
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
    })
    private List<OrderLineItemFee> orderLineItemFeeList;


    @OneToMany(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumns({

        @JoinColumn(name = "order_line",  referencedColumnName = "line_num",insertable = false, updatable = false),
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
    })
    private List<OrderLineItemTax> orderLineItemTaxList;




    @Column(name = "supplier_id", nullable = false)
    private String supplierId;

    @Column(name = "var_id", nullable = false)
    private String varId;

    @Column(name = "program_id", nullable = false)
    private String programId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "supplier_order_id")
    private String supplierOrderId;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "name")
    private String name;

    @Column(name = "image_url")
    private String imageURL;

    @Column(name = "color")
    private String color;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "size")
    private String size;

    @Column(name = "attr1")
    private String attr1;

    @Column(name = "attr2")
    private String attr2;

    @Column(name = "attr3")
    private String attr3;

    @Column(name = "is_eligible_for_super_saver_shipping")
    private String isEligibleForSuperSaverShipping;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "supplier_item_price", nullable = false)
    private Integer supplierItemPrice;

    @Column(name = "discounted_supplier_item_price")
    private Integer discountedSupplierItemPrice;

    @Column(name = "discounted_var_order_line_price")
    private Integer discountedVarOrderLinePrice;

    @Column(name = "discounted_taxes")
    private Integer discountedTaxes;

    @Column(name = "discounted_fees")
    private Integer discountedFees;

    @Column(name = "supplier_tax_price", nullable = false)
    private Integer supplierTaxPrice;

    @Column(name = "supplier_per_shipment_price", nullable = false)
    private Integer supplierPerShipmentPrice;

    @Column(name = "supplier_shipping_unit", nullable = false)
    private String supplierShippingUnit;

    @Column(name = "supplier_shipping_unit_price", nullable = false)
    private Integer supplierShippingUnitPrice;

    @Column(name = "supplier_single_item_shipping_price", nullable = false)
    private Integer supplierSingleItemShippingPrice;

    @Column(name = "supplier_shipping_price", nullable = false)
    private Integer supplierShippingPrice;

    @Column(name = "conv_rate", nullable = false)
    private Double convRate;

    @Column(name = "tax_rate", nullable = false)
    private Integer taxRate;

    @Column(name = "b2s_item_margin", nullable = false)
    private Double b2sItemMargin;

    @Column(name = "var_item_margin", nullable = false)
    private Double varItemMargin;

    @Column(name = "b2s_shipping_margin", nullable = false)
    private Float b2sShippingMargin;

    @Column(name = "var_shipping_margin", nullable = false)
    private Double varShippingMargin;

    @Column(name = "item_points", nullable = false)
    private Double itemPoints;

    @Column(name = "tax_points", nullable = false)
    private Double taxPoints;

    @Column(name = "shipping_points", nullable = false)
    private Double shippingPoints;

    @Column(name = "order_line_points", nullable = false)
    private Integer orderLinePoints;

    @Column(name = "b2s_item_profit_price", nullable = false)
    private Integer b2sItemProfitPrice;

    @Column(name = "b2s_tax_profit_price", nullable = false)
    private Integer b2sTaxProfitPrice;

    @Column(name = "b2s_shipping_profit_price", nullable = false)
    private Integer b2sShippingProfitPrice;

    @Column(name = "var_item_profit_price", nullable = false)
    private Integer varItemProfitPrice;

    @Column(name = "var_tax_profit_price", nullable = false)
    private Integer varTaxProfitPrice;

    @Column(name = "var_shipping_profit_price", nullable = false)
    private Integer varShippingProfitPrice;

    @Column(name = "var_order_line_price", nullable = true)
    private Integer varOrderLinePrice;

    @Column(name = "comment")
    private String comment;

    @Column(name = "create_date", nullable = false)
    private Timestamp createDate;

    @Column(name = "order_status", nullable = false)
    private String orderStatus;

    @Column(name = "order_line_type")
    private String orderLineType;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "brand")
    private String brand;

    @Column(name = "category_path")
    private String categoryPath;

    @Column(name = "sku")
    private String sku;

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "seller_id")
    private String sellerId;

    @Column(name = "listing_id")
    private String listingId;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "order_source")
    private String orderSource;

    @Column(name = "b2s_tax_price")
    private Integer b2sTaxPrice;

    @Column(name = "b2s_tax_rate")
    private Integer b2sTaxRate;

    @Column(name = "b2s_tax_points")
    private Float b2sTaxPoints;

    @Column(name = "original_partner_order_number")
    private String originalPartnerOrderNumber;

    @Column(name = "resend_partner_order_number")
    private String resendPartnerOrderNumber;

    @Column(name = "gateway_order_number")
    private String gatewayOrderNumber;

    @Column(name = "bundle_id")
    private String bundleId;

    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "policy_id")
    private String policyId;

    @Column(name = "travel_start_date")
    private Timestamp travelStartDate;

    @Column(name = "order_line_num")
    private Integer orderLineNum;

    @Column(name = "order_line_undiscounted_unit_fee")
    private Integer orderLineUndiscountedUnitFee;

    @Column(name = "b2s_offline_fee")
    private Integer b2sOfflineFee;

    @Column(name = "b2s_online_fee")
    private Integer b2sOnlineFee;

    @Column(name = "booking_quantity")
    private Integer bookingQuantity;

    @Column(name = "travel_end_date")
    private Timestamp travelEndDate;

    @Column(name = "fx_rate")
    private Float fxRate;

    @Column(name = "fx_file_id")
    private String fxFileId;

    @Column(name = "global_carrier_fee_id")
    private String globalCarrierFeeId;

    @Column(name = "cash_buy_in_points")
    private BigDecimal cashBuyInPoints;

    @Column(name = "cash_buy_in_price")
    private BigDecimal cashBuyInPrice;

    @Column(name = "global_carrier_tracking_url")
    private String globalCarrierTrackingURL;

    public OrderLineItemId getId() {
        return id;
    }

    public void setId(final OrderLineItemId id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getSupplierOrderId() {
        return supplierOrderId;
    }

    public void setSupplierOrderId(final String supplierOrderId) {
        this.supplierOrderId = supplierOrderId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(final String imageURL) {
        this.imageURL = imageURL;
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(final Integer weight) {
        this.weight = weight;
    }

    public String getSize() {
        return size;
    }

    public void setSize(final String size) {
        this.size = size;
    }

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(final String attr1) {
        this.attr1 = attr1;
    }

    public String getAttr2() {
        return attr2;
    }

    public void setAttr2(final String attr2) {
        this.attr2 = attr2;
    }

    public String getAttr3() {
        return attr3;
    }

    public void setAttr3(final String attr3) {
        this.attr3 = attr3;
    }

    public String getIsEligibleForSuperSaverShipping() {
        return isEligibleForSuperSaverShipping;
    }

    public void setIsEligibleForSuperSaverShipping(final String isEligibleForSuperSaverShipping) {
        this.isEligibleForSuperSaverShipping = isEligibleForSuperSaverShipping;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSupplierItemPrice() {
        return Math.abs(supplierItemPrice);
    }

    public void setSupplierItemPrice(final Integer supplierItemPrice) {
        this.supplierItemPrice = supplierItemPrice;
    }

    public Integer getDiscountedSupplierItemPrice() {
        return discountedSupplierItemPrice;
    }

    public void setDiscountedSupplierItemPrice(final Integer discountedSupplierItemPrice) {
        this.discountedSupplierItemPrice = discountedSupplierItemPrice;
    }

    public Integer getDiscountedVarOrderLinePrice() {
        return discountedVarOrderLinePrice;
    }

    public void setDiscountedVarOrderLinePrice(final Integer discountedVarOrderLinePrice) {
        this.discountedVarOrderLinePrice = discountedVarOrderLinePrice;
    }

    public Integer getDiscountedTaxes() {
        return discountedTaxes;
    }

    public void setDiscountedTaxes(final Integer discountedTaxes) {
        this.discountedTaxes = discountedTaxes;
    }

    public Integer getDiscountedFees() {
        return discountedFees;
    }

    public void setDiscountedFees(final Integer discountedFees) {
        this.discountedFees = discountedFees;
    }

    public Integer getSupplierTaxPrice() {
        return supplierTaxPrice;
    }

    public void setSupplierTaxPrice(final Integer supplierTaxPrice) {
        this.supplierTaxPrice = supplierTaxPrice;
    }

    public Integer getSupplierPerShipmentPrice() {
        return supplierPerShipmentPrice;
    }

    public void setSupplierPerShipmentPrice(final Integer supplierPerShipmentPrice) {
        this.supplierPerShipmentPrice = supplierPerShipmentPrice;
    }

    public String getSupplierShippingUnit() {
        return supplierShippingUnit;
    }

    public void setSupplierShippingUnit(final String supplierShippingUnit) {
        this.supplierShippingUnit = supplierShippingUnit;
    }

    public Integer getSupplierShippingUnitPrice() {
        return supplierShippingUnitPrice;
    }

    public void setSupplierShippingUnitPrice(final Integer supplierShippingUnitPrice) {
        this.supplierShippingUnitPrice = supplierShippingUnitPrice;
    }

    public Integer getSupplierSingleItemShippingPrice() {
        return supplierSingleItemShippingPrice;
    }

    public void setSupplierSingleItemShippingPrice(final Integer supplierSingleItemShippingPrice) {
        this.supplierSingleItemShippingPrice = supplierSingleItemShippingPrice;
    }

    public Integer getSupplierShippingPrice() {
        return supplierShippingPrice;
    }

    public void setSupplierShippingPrice(final Integer supplierShippingPrice) {
        this.supplierShippingPrice = supplierShippingPrice;
    }

    public Double getConvRate() {
        return convRate;
    }

    public void setConvRate(final Double convRate) {
        this.convRate = convRate;
    }

    public Integer getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(final Integer taxRate) {
        this.taxRate = taxRate;
    }

    public Double getB2sItemMargin() {
        return b2sItemMargin;
    }

    public void setB2sItemMargin(final Double b2sItemMargin) {
        this.b2sItemMargin = b2sItemMargin;
    }

    public Double getVarItemMargin() {
        return varItemMargin;
    }

    public void setVarItemMargin(final Double varItemMargin) {
        this.varItemMargin = varItemMargin;
    }

    public Float getB2sShippingMargin() {
        return b2sShippingMargin;
    }

    public void setB2sShippingMargin(final Float b2sShippingMargin) {
        this.b2sShippingMargin = b2sShippingMargin;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(final String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getVarShippingMargin() {
        return varShippingMargin;
    }

    public void setVarShippingMargin(final Double varShippingMargin) {
        this.varShippingMargin = varShippingMargin;
    }

    public Double getItemPoints() {
        return itemPoints;
    }

    public void setItemPoints(final Double itemPoints) {
        this.itemPoints = itemPoints;
    }

    public Double getTaxPoints() {
        return taxPoints;
    }

    public void setTaxPoints(final Double taxPoints) {
        this.taxPoints = taxPoints;
    }

    public Double getShippingPoints() {
        return shippingPoints;
    }

    public void setShippingPoints(final Double shippingPoints) {
        this.shippingPoints = shippingPoints;
    }

    public Integer getOrderLinePoints() {
        return Math.abs(orderLinePoints);
    }

    public void setOrderLinePoints(final Integer orderLinePoints) {
        this.orderLinePoints = orderLinePoints;
    }

    public Integer getB2sItemProfitPrice() {
        return b2sItemProfitPrice;
    }

    public void setB2sItemProfitPrice(final Integer b2sItemProfitPrice) {
        this.b2sItemProfitPrice = b2sItemProfitPrice;
    }

    public Integer getB2sTaxProfitPrice() {
        return b2sTaxProfitPrice;
    }

    public void setB2sTaxProfitPrice(final Integer b2sTaxProfitPrice) {
        this.b2sTaxProfitPrice = b2sTaxProfitPrice;
    }

    public Integer getB2sShippingProfitPrice() {
        return b2sShippingProfitPrice;
    }

    public void setB2sShippingProfitPrice(final Integer b2sShippingProfitPrice) {
        this.b2sShippingProfitPrice = b2sShippingProfitPrice;
    }

    public Integer getVarItemProfitPrice() {
        return varItemProfitPrice;
    }

    public OrderLineStatus getStatus() {
        return status;
    }

    public void setStatus(final OrderLineStatus status) {
        this.status = status;
    }

    public void setVarItemProfitPrice(final Integer varItemProfitPrice) {
        this.varItemProfitPrice = varItemProfitPrice;
    }

    public Integer getVarTaxProfitPrice() {
        return varTaxProfitPrice;
    }

    public void setVarTaxProfitPrice(final Integer varTaxProfitPrice) {
        this.varTaxProfitPrice = varTaxProfitPrice;
    }

    public Integer getVarShippingProfitPrice() {
        return varShippingProfitPrice;
    }

    public void setVarShippingProfitPrice(final Integer varShippingProfitPrice) {
        this.varShippingProfitPrice = varShippingProfitPrice;
    }

    public Integer getVarOrderLinePrice() {
        return varOrderLinePrice;
    }

    public void setVarOrderLinePrice(final Integer varOrderLinePrice) {
        this.varOrderLinePrice = varOrderLinePrice;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(final Timestamp createDate) {
        this.createDate = createDate;
    }


    public String getOrderLineType() {
        return orderLineType;
    }

    public void setOrderLineType(final String orderLineType) {
        this.orderLineType = orderLineType;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(final String merchantId) {
        this.merchantId = merchantId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(final String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(final String categoryPath) {
        this.categoryPath = categoryPath;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(final String sku) {
        this.sku = sku;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(final String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(final String sellerId) {
        this.sellerId = sellerId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(final String listingId) {
        this.listingId = listingId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(final String storeId) {
        this.storeId = storeId;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(final String orderSource) {
        this.orderSource = orderSource;
    }

    public Integer getB2sTaxPrice() {
        return b2sTaxPrice;
    }

    public void setB2sTaxPrice(final Integer b2sTaxPrice) {
        this.b2sTaxPrice = b2sTaxPrice;
    }

    public Integer getB2sTaxRate() {
        return b2sTaxRate;
    }

    public void setB2sTaxRate(final Integer b2sTaxRate) {
        this.b2sTaxRate = b2sTaxRate;
    }

    public Float getB2sTaxPoints() {
        return b2sTaxPoints;
    }

    public void setB2sTaxPoints(final Float b2sTaxPoints) {
        this.b2sTaxPoints = b2sTaxPoints;
    }

    public String getOriginalPartnerOrderNumber() {
        return originalPartnerOrderNumber;
    }

    public void setOriginalPartnerOrderNumber(final String originalPartnerOrderNumber) {
        this.originalPartnerOrderNumber = originalPartnerOrderNumber;
    }

    public String getResendPartnerOrderNumber() {
        return resendPartnerOrderNumber;
    }

    public void setResendPartnerOrderNumber(final String resendPartnerOrderNumber) {
        this.resendPartnerOrderNumber = resendPartnerOrderNumber;
    }

    public String getGatewayOrderNumber() {
        return gatewayOrderNumber;
    }

    public void setGatewayOrderNumber(final String gatewayOrderNumber) {
        this.gatewayOrderNumber = gatewayOrderNumber;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(final String bundleId) {
        this.bundleId = bundleId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(final Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(final String policyId) {
        this.policyId = policyId;
    }

    public Timestamp getTravelStartDate() {
        return travelStartDate;
    }

    public void setTravelStartDate(final Timestamp travelStartDate) {
        this.travelStartDate = travelStartDate;
    }

    public Integer getOrderLineNum() {
        return orderLineNum;
    }

    public void setOrderLineNum(final Integer orderLineNum) {
        this.orderLineNum = orderLineNum;
    }

    public Integer getOrderLineUndiscountedUnitFee() {
        return orderLineUndiscountedUnitFee;
    }

    public void setOrderLineUndiscountedUnitFee(final Integer orderLineUndiscountedUnitFee) {
        this.orderLineUndiscountedUnitFee = orderLineUndiscountedUnitFee;
    }

    public Integer getB2sOfflineFee() {
        return b2sOfflineFee;
    }

    public void setB2sOfflineFee(final Integer b2sOfflineFee) {
        this.b2sOfflineFee = b2sOfflineFee;
    }

    public Integer getB2sOnlineFee() {
        return b2sOnlineFee;
    }

    public void setB2sOnlineFee(final Integer b2sOnlineFee) {
        this.b2sOnlineFee = b2sOnlineFee;
    }

    public Integer getBookingQuantity() {
        return bookingQuantity;
    }

    public void setBookingQuantity(final Integer bookingQuantity) {
        this.bookingQuantity = bookingQuantity;
    }

    public Timestamp getTravelEndDate() {
        return travelEndDate;
    }

    public void setTravelEndDate(final Timestamp travelEndDate) {
        this.travelEndDate = travelEndDate;
    }

    public Float getFxRate() {
        return fxRate;
    }

    public void setFxRate(final Float fxRate) {
        this.fxRate = fxRate;
    }

    public String getFxFileId() {
        return fxFileId;
    }

    public void setFxFileId(final String fxFileId) {
        this.fxFileId = fxFileId;
    }

    public String getGlobalCarrierFeeId() {
        return globalCarrierFeeId;
    }

    public void setGlobalCarrierFeeId(final String globalCarrierFeeId) {
        this.globalCarrierFeeId = globalCarrierFeeId;
    }

    public BigDecimal getCashBuyInPoints() {
        return cashBuyInPoints;
    }

    public void setCashBuyInPoints(final BigDecimal cashBuyInPoints) {
        this.cashBuyInPoints = cashBuyInPoints;
    }

    public BigDecimal getCashBuyInPrice() {
        return cashBuyInPrice;
    }

    public void setCashBuyInPrice(final BigDecimal cashBuyInPrice) {
        this.cashBuyInPrice = cashBuyInPrice;
    }

    public String getGlobalCarrierTrackingURL() {
        return globalCarrierTrackingURL;
    }

    public void setGlobalCarrierTrackingURL(final String globalCarrierTrackingURL) {
        this.globalCarrierTrackingURL = globalCarrierTrackingURL;
    }

    public List<OrderLineItemAttribute> getOrderLineAttributes() {
        return orderLineAttributes;
    }

    public void setOrderLineAttributes(List<OrderLineItemAttribute> orderLineAttributes) {
        this.orderLineAttributes = orderLineAttributes;
    }

    public List<OrderLineShipmentNotification> getOrderLineShipmentNotifications() {
        return orderLineShipmentNotifications;
    }

    public void setOrderLineShipmentNotifications(
        final List<OrderLineShipmentNotification> orderLineShipmentNotifications) {
        this.orderLineShipmentNotifications = orderLineShipmentNotifications;
    }

    public List<OrderLineAdjustment> getOrderLineAdjustmentList() {
        return orderLineAdjustmentList;
    }

    public void setOrderLineAdjustmentList(
        final List<OrderLineAdjustment> orderLineAdjustmentList) {
        this.orderLineAdjustmentList = orderLineAdjustmentList;
    }

    public List<OrderLineItemFee> getOrderLineItemFeeList() {
        return orderLineItemFeeList;
    }

    public void setOrderLineItemFeeList(final List<OrderLineItemFee> orderLineItemFeeList) {
        this.orderLineItemFeeList = orderLineItemFeeList;
    }

    public List<OrderLineItemTax> getOrderLineItemTaxList() {
        return orderLineItemTaxList;
    }

    public void setOrderLineItemTaxList(final List<OrderLineItemTax> orderLineItemTaxList) {
        this.orderLineItemTaxList = orderLineItemTaxList;
    }

    public Integer getTotalFeesInPoints() {
        Integer total = 0;
        if (orderLineItemFeeList!=null) {
            for (final OrderLineItemFee lineFee : orderLineItemFeeList) {
                total += lineFee.getPoints();
            }
        }
        return total;
    }
    public BigDecimal getTotalFeesInMoneyMinor() {
        BigDecimal total = BigDecimal.ZERO;
        if (orderLineItemFeeList != null) {
            for(final OrderLineItemFee lineFee : orderLineItemFeeList) {
                total = total.add(lineFee.getAmount());
            }
        }
        return total;
    }
    public Integer getTotalTaxesInPoints() {
        Integer total = 0;
        if (getOrderLineItemTaxList()!=null) {
            for (final OrderLineItemTax lineTax : getOrderLineItemTaxList()) {
                total += lineTax.getPoints();
            }
        }
        return total;
    }

    /*
     *  This method calculates the total tax price in pennies for this orderline for 1 quantity of this item.  This
     *  value is not persisted in the database, but calculated based on other values stored in the database.  The
     *  formula for the calculation is the following:
     *
     *  Total Tax Money = sum of all itemized taxes in money for this order line in the ORDER_LINE_TAX table
     */
    public BigDecimal getTotalTaxesInMoneyMinor() {
        BigDecimal total = BigDecimal.ZERO;
        if (getOrderLineItemTaxList() != null) {
            for (final OrderLineItemTax lineTax : getOrderLineItemTaxList()) {
                total = total.add(lineTax.getAmount());
            }
        }
        return total;
    }

    public Money getPriceForQuantity(final Locale locale) {
        BigDecimal total = BigDecimal.ZERO;

        total = total.add(BigDecimal.valueOf(getSupplierItemPrice()).multiply(BigDecimal.valueOf(getQuantity())));

        return Money.ofMinor(CurrencyUnit.of(locale), total.longValue());
    }

    public Integer getPointsForQuantity() {
        BigDecimal total = BigDecimal.ZERO;

        total = total.add(BigDecimal.valueOf(getItemPoints()).multiply(BigDecimal.valueOf(getQuantity())));

        return total.intValue();
    }

    public Money getUnitPrice(final Locale locale) {
        BigDecimal total = BigDecimal.ZERO;

        total = total.add(BigDecimal.valueOf(getSupplierItemPrice()));

        return Money.ofMinor(CurrencyUnit.of(locale), total.longValue());
    }
    public Money getOrderLineTotalPrice(final Locale locale){
        BigDecimal total = BigDecimal.ZERO;
        if(CommonConstants.BLUE_TIER_PRICING.equalsIgnoreCase(getAttr1())){
           total = total.add((Money.ofMinor(CurrencyUnit.of(locale),
                getVarOrderLinePrice().longValue()+ getVarItemProfitPrice().longValue())).multipliedBy(100).getAmount());
        }else {
            total = total.add(BigDecimal.valueOf(getVarOrderLinePrice()));
        }
        return Money.ofMinor(CurrencyUnit.of(locale),total.longValue());
    }

}