import { CurrencyUnit } from '@app/models/currency-unit';

export interface BigMoney {
  serialVersionUID: number;
  PARSE_REGEX: any; // change to pattern
  currency: CurrencyUnit;
  amount: number;
}

export interface Money {
  serialVersionUID: number;
  money: BigMoney;
}
