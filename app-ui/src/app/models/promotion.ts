import { Money } from '@app/models/money';

export interface PromotionalSubscription {
  displayCheckbox: boolean;
  isChecked: boolean;
}

export interface Promotion {
  discountPercentage?: number;
  fixedPointPrice?: Money;
  costPerPoint: number;
}
