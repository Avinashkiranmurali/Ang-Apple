import { CurrencyUnit } from '@app/models/currency-unit';

export interface CreditItem {
  baseItemPrice: number;
  b2sProfit: number;
  varProfit: number;
  varPrice: number;
  currency: CurrencyUnit;
  itemTotal: number;
  creditCardType?: string;
  ccFirstName?: string;
  ccLastName?: string;
  ccLast4?: string;
  b2sMargin: number;
  varMargin: number;
}
