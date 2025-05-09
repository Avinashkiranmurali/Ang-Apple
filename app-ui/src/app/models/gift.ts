import { CurrencyUnit } from '@app/models/currency-unit';
import {ImageURLs} from '@app/models/image-urls';
import {Offer} from '@app/models/offer';


export interface Gift {
  giftWrap: boolean;
  freeGiftWrap: boolean;
  giftWrapPoints: number;
  message1: string;
  message2: string;
  message3: string;
  message4: string;
  message5: string;
}

export interface GiftCardDenomination {
  negative: boolean;
  zero: boolean;
  positive: boolean;
  amountMinorInt: number;
  amountMajorInt: number;
  negativeOrZero: boolean;
  scale: number;
  amount: number;
  currencyUnit: CurrencyUnit;
  amountMajor: number;
  amountMajorLong: number;
  amountMinor: number;
  amountMinorLong: number;
  minorPart: number;
  positiveOrZero: boolean;
}

export interface GiftItemEngraveInfo {
  psid: string;
  isEngravable: boolean;
  name: string;
  images: ImageURLs;
  offers: Offer[];
  quantity: number;
}
