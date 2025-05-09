import { ImageURLs } from './image-urls';
import { Option } from './option';
import { Offer } from './offer';

export interface ProductDataConfig {
  name: string;
  images: ImageURLs;
  psid: string;
  options: Option[];
  offers: Offer[];
}
