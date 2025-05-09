export interface PricingModel {
  market: string;
  priceType: string;
  priceKey: string;
  paymentValue: number;
  paymentValuePoints: number;
  repaymentTerm: number;
  monthsSubsidized: number;
  upgradeCost: number;
  activationFee: number;
  totalDueTodayBeforeTax: number;
  totalDueTodayAfterTax: number;
  delta: number;
  discountTier1: number;
  discountTier2: number;
  discountTier3: number;
}

export interface PurchasePointsResponse {
  cashAmount: number;
  earnPoints: number;
}
