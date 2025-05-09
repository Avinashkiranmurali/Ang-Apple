import { Price } from '@app/models/price';

export interface Tax {
  taxId: string;
  amount: Price;
}
