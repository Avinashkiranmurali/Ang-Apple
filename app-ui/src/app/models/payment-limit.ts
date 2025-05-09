import { Price } from '@app/models/price';

export interface PaymentLimit {
  paymentMinLimit?: number;
  paymentMaxLimit?: number;
  minNotMet: boolean;
  maxExceed: boolean;
}

export interface RedemptionPaymentLimit {
  pointsMaxLimit: Price;
  pointsMinLimit: Price;
  cashMaxLimit: Price;
  cashMinLimit: Price;
  useMaxPoints: Price;
  useMinPoints: Price;
  cartMaxLimit?: Price;
}

export interface SupplementaryPaymentLimit {
  paymentMaxLimit: Price;
  rewardsMinLimit: Price;
}
