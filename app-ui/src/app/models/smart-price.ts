export interface SmartPrice {
  amount: number;
  currencyCode?: string;
  isCashMaxLimitReached?: boolean;
  points: number;
}
