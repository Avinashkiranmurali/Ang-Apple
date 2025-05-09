export interface Payment {
  paymentOption: string;
  paymentProvider?: Map<string, string>;
  paymentTemplate: string;
  isActive: boolean;
  orderBy: number;
  paymentMinLimit?: number;
  paymentMaxLimit?: number;
  supplementaryPaymentType: string;
  supplementaryPaymentLimitType: string;
  supplementaryPaymentMinLimit?: number;
  supplementaryPaymentMaxLimit: number;
}

export interface PaymentInfo {
  awardsUsed: number;
  awardsPurchased: number;
  awardsPurchasedPrice: number;
  ccLast4: string;
}
