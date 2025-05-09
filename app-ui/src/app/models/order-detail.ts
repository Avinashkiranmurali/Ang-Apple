import { CurrencyUnit } from '@app/models/currency-unit';
import { DelayedShippingInfo, ShipmentDeliveryInfo } from '@app/models/order-history';
import { Engrave } from '@app/models/engrave';
import { Offer } from '@app/models/offer';
import { Address, BillTo, ShippingAddress } from '@app/models/address';
import { CartItem, CartTotal } from '@app/models/cart';
import { DiscountCode } from '@app/models/discount-code';
import { MediaProduct } from '@app/models/media-product';
import { ContactInfo } from '@app/models/contact-info';
import { PaymentInfo } from '@app/models/payment';
import { PayrollDeduction } from './payroll-deduction';
import { Price } from '@app/models/price';

export interface OrderStatus {
  b2sOrderId: number;
  varOrderId: string;
  b2rMessage: string;
  varMessage: string;
  ccType: string;
  last4: string;
  emptyCart: boolean;
  discountCodes: Array<DiscountCode>;
  discountCodeError: boolean;
  orderHoldDurationInDays: number;
  orderDate: number;
  payrollDeduction: PayrollDeduction;
  promotionUseExceeded: boolean;
  timedOut: boolean;
  showVarOrderId: boolean;
  errorCode?: string;
  payrollAgreementId: number;
  payrollAgreementUrl: string;
  earnedPoints: number;
  cartTotal: CartTotal;
}

export interface DeliveryAddress {
  firstName: string;
  lastName: string;
  businessName: string;
  addressLine1: string;
  addressLine2: string;
  addressLine3?: string;
  city: string;
  state: string;
  postalCode: number;
  country: string;
}

export interface OrderLineProgress {
  status?: string;
  progressValue?: number;
  progressBarText?: Array<string>;
  modifiedDate?: Date;
}

export interface LineItem {
  orderLineID: number;
  sku: string;
  itemName: string;
  itemImageURL: string;
  currencyType: string;
  quantity: number;
  unitPrice: Price;
  price: Price;
  unpromotedUnitPrice?: Price;
  unpromotedPrice?: Price;
  status: string;
  shipmentInfo?: ShipmentDeliveryInfo;
  delayedShippingInfo?: DelayedShippingInfo;
  engrave: Engrave;
  shippingMethod: string;
  refund?: Price;
  refundStatus: boolean;
  shippingAvailability: string;
  productOptions?: Array<string>;
  orderLineProgress?: OrderLineProgress;
  offer?: Offer;
  isGift?: boolean;
}

export interface OrderConfirm {
  items: CartItem[];
  usedPoints: number;
  purchasedPoints?: number;
  totalPayment?: number;
  orderID?: string | number;
  remainingBalance: number;
  points: number;
  paymentInfo: string;
  paymentTemp: string;
  paymentType: string;
  purchasePaymentOption: string;
  promotionalSubscription: { [key: string]: boolean };
  ccLast4?: string;
  shipAddress?: Address;
  billAddress?: BillTo;
  contactInfo?: { [key: string]: string };
  subscribedMediaProducts?: Array<MediaProduct>;
}

export interface OrderDetail {
  orderId: number;
  displayOrderId: string;
  showVarOrderId: boolean;
  orderDate: number;
  shipments: number;
  orderTotal: Price;
  orderSubTotal: Price;
  totalTax: Price;
  shippingCost: Price;
  totalFee: Price;
  items: number;
  remainingBalance: number;
  lineItems: LineItem[];
  deliveryAddress: DeliveryAddress;
  contactInfo: ContactInfo;
  purchasedPriceCurrency: string;
  refundTotal: Price;
  totalDiscount: Price;
  gstAmount?: number;
  earnedPoints: number;
  imageURL: string;
  billTo: BillTo;
  ShippingAddress: ShippingAddress;
  paymentInfo: PaymentInfo;
  refundSummary: RefundSummary;
}

export interface RefundSummary {
  lineItems: RefundLineItem[];
  quantity?: number;
  total: Price;
  subTotal: Price;
  taxesAndFees: Price;
  refunds: Price;
}
export interface RefundLineItem {
  productName: string;
  taxPrice: Price;
  itemPrice: Price;
  feesPrice: Price;
}




