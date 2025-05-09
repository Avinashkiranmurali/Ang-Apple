export interface DelayedShippingInfo {
  shippingAvailability: string;
  asOfDate: string;
}

export interface ShipmentDeliveryInfo {
  shipmentDate?: Date;
  trackingID?: string;
  carrierName?: string;
  deliveryDate?: Date;
}


