import { Address } from '@app/models/address';
import { Fees } from '@app/models/fees'
import { Engrave } from '@app/models/engrave';
import { SelectedAddOns } from '@app/models/addOns';
import { Price } from '@app/models/price';
import { SmartPrice } from '@app/models/smart-price';
import { MediaProduct } from '@app/models/media-product';
import { PaymentLimit, RedemptionPaymentLimit, SupplementaryPaymentLimit  } from '@app/models/payment-limit';
import { Product } from '@app/models/product';
import { CreditItem } from '@app/models/credit-item';
import { Gift, GiftCardDenomination } from '@app/models/gift';
import { Tax } from '@app/models/tax';
import { DiscountCode } from '@app/models/discount-code';
import { PromotionalSubscription } from '@app/models/promotion';

export interface Cart {
  cartItems: CartItem[];
  shippingAddress: Address;
  cartTotal: CartTotal;
  displayCartTotal: CartTotal;
  cartModifiedBySystem: boolean;
  cartTotalModified: boolean;
  discounts: DiscountCode[];
  installment?: number;
  paymentType?: string;
  addPoints: number;
  pointsPayment: number;
  pointsBalance: number;
  pointPurchaseRate: number;
  cost: number;
  ccPayment: number;
  creditItem: CreditItem;
  id: number;
  userId: string;
  creditLineItem?: CartItem;
  selectedPaymentOption?: string;
  isEligibleForPayrollDeduction: boolean;
  isPayrollOnly: boolean;
  addressError: boolean;
  supplementaryPaymentLimit: SupplementaryPaymentLimit;
  redemptionPaymentLimit: RedemptionPaymentLimit;
  paymentLimit?: PaymentLimit;
  timeZoneId?: string;
  convRate: number;
  ignoreSuggestedAddress?: string;
  promotionalSubscription?: PromotionalSubscription;
  isAddressChanged?: string;
  isEmailChanged?: string;
  gstAmount: number;
  earnPoints: number;
  selectedRedemptionOption?: string;
  physicalGiftcardMaxValue: number;
  totalDiscountAmount: number;
  maxCartTotalExceeded: boolean;
  subscriptions: MediaProduct[];
  cartItemsTotalCount?: number;
  smartPrice?: SmartPrice;
}

export interface AddToCartResponse {
  cartItemId: number;
  cartTotalModified: boolean;
  quantityLimitExceed: boolean;
  giftcardMaxQuantity: boolean;
  physicalGiftcardTotalValueFull: boolean;
  pricingFull: boolean;
  physicalGiftcardMaxValue: number;
}

export interface CartTotal {
  price: Price;
  discountedPrice?: Price;
  discountAmount: number;
  shippingPrice: Price;
  taxes: Map<string, Tax>;
  fees: Map<string, Fees>;
  expeditedShippingPrice?: Price;
  itemsSubtotalPrice: Price;
  discountedItemsSubtotalPrice?: Price;
  totalTaxes: Price;
  totalFees: Price;
  currency: Price;
  establishmentFees: Price;
  payPerPeriod: number;
  payPeriods: number;
  discountCodePerOrder: number;
  discountApplied: boolean;
  actual: boolean;
}

export interface CartItem {
  id: number;
  addedDate: number;
  supplierId: number;
  productId: string;
  productName: string;
  imageURL: string;
  parentProductId: string;
  merchantId: string;
  quantity: number;
  prevQuantity?: number;
  engrave: Engrave;
  gift: Gift;
  productDetail: Product;
  productGroupId?: number;
  productGroupName?: string;
  productGroupDisplayName?: string;
  maxQuantity?: number;
  payPeriodPrice?: number;
  shippingMethod?: string;
  giftCardMaxQuantity?: number;
  giftCardDenomination?: GiftCardDenomination;
  installment?: number;
  giftItem?: CartItem;
  discount?: number;
  quantityDropdown?: number[];
  disableQty?: boolean;
  hasEngraving?: boolean;
  currentQty?: number;
  physicalGiftcardMaxValue?: number;
  quantityLimitExceed?: boolean;
  totalQty?: number;
  selectedAddOns: SelectedAddOns;
  isGift: boolean
}

