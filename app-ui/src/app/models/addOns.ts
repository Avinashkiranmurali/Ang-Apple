import { CartItem } from '@app/models/cart';
import { Product } from '@app/models/product';

export interface AddOns {
  availableGiftItems: Product[];
  servicePlans: Product[];
}

export interface SelectedAddOns {
  giftItem: CartItem;
  servicePlan: CartItem;
}
