import { Price } from '@app/models/price';
import { Fees } from '@app/models/fees';
import { Tax } from '@app/models/tax';

export interface Offer {
  basePrice?: Price;
  payPerPeriodPrice?: number;
  payPeriods: number;
  payDuration?: string;
  upgradeCost?: number;
  earnPoints: number;
  convRate?: number;
  orgSupplierTaxPrice?: number;
  msrpPrice?: number;
  sku: string;
  appleSku?: string;
  payPerPeriodTotalPrice?: number;
  displayPrice: Price;
  b2sItemPrice: Price;
  b2sShippingPrice: Price;
  varPrice: Price;
  unpromotedVarPrice: Price;
  supplierTaxPrice: Price;
  totalPrice: Price;
  unitTotalPrice: Price;
  unpromotedDisplayPrice: Price;
  fees: Map<string, Fees>;
  tax: Map<string, Tax>;
  discountApplied: number;
  isEligibleForPayrollDeduction: boolean;
  points: number;
  amount: number;
}
