package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

@Table(name = "order_line_shipment_notification")
@Entity
public class OrderLineShipmentNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @MapsId("orderLineId")
    @JoinColumns({
        @JoinColumn(name = "b2s_order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
        @JoinColumn(name = "line_number", referencedColumnName = "line_num", insertable = false, updatable = false)
    })
    @ManyToOne
    private OrderLineEntity orderLine;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "shipment_date")
    private Date shipmentDate;

    @Column(name = "shipping_carrier")
    private String shippingCarrier;

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "sku")
    private String sku;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Column(name = " delivery_date")
    private Date deliveryDate;


    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public Date getShipmentDate() {
        return shipmentDate;
    }

    public void setShipmentDate(final Date shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    public String getShippingCarrier() {
        return shippingCarrier;
    }

    public void setShippingCarrier(final String shippingCarrier) {
        this.shippingCarrier = shippingCarrier;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(final String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(final String sku) {
        this.sku = sku;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(final String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(final String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(final Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public OrderLineEntity getOrderLine() {
        return orderLine;
    }

    public void setOrderLine(final OrderLineEntity orderLine) {
        this.orderLine = orderLine;
    }


}
